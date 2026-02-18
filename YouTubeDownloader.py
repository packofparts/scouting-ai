"""
YouTube Downloader - Source from https://github.com/DevCTx/YouTubeDownloader. Edited by Anthropic's Claude Opus 4.6 to export the video to specific file inside this project.
Refactored to use yt-dlp instead of pytubefix for better bot-detection bypass.
"""
import argparse
import os
import webbrowser
from traceback import format_exc as traceback_format_exc
from typing import NamedTuple, Optional
from validators import url as valid_url
from enum import Enum

import yt_dlp

_SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MATCHES_DIR = os.path.join(_SCRIPT_DIR, "matches")
OUTPUT_FILENAME = "match.mp4"


class YTD_Error(Enum):
    UNKNOWN = 0,
    INVALID_URL = 1,
    ISNOT_YOUTUBE_URL = 2,
    NETWORK_ERROR = 3,
    GETINFO_ERROR = 4


class YTD_exceptions(Exception):

    def __init__(self, code, msg=None):
        if code == YTD_Error.UNKNOWN:
            print('Traceback : ', traceback_format_exc())
        elif code == YTD_Error.NETWORK_ERROR:
            print(f"Please check your internet connection! [error : {code} ]")
        else:
            print(f"{msg if msg is not None else 'Please contact your network administrator!'} [error : {code}]")
        exit(1)


class VideoInfo:
    """Wraps the raw yt-dlp info dict for better code completion."""

    def __init__(self, info_dict: dict):
        self._raw = info_dict
        self.title: str = info_dict.get('title', '<Title Unknown>')
        self.view_count = info_dict.get('view_count', '<Number unknown>')
        self.formats: list = info_dict.get('formats', [])


class StreamTypeOption(NamedTuple):
    """Represents a category of stream (combined, video-only, audio-only, personalized)."""
    label: str
    has_audio: bool
    has_video: bool
    is_personalized: bool


class StreamFormatOption(NamedTuple):
    """Represents a specific stream format with quality details."""
    label: str
    codec_info: str
    format_id: str


class YouTubeDownloader():

    BASE_YOUTUBE_URL = "https://www.youtube.com"
    DEFAULT_BROWSER = "chrome"

    def __init__(self, browser: str = DEFAULT_BROWSER):
        self.url = None
        self.__info: Optional[VideoInfo] = None
        self.__stream_list = []
        self.__with_audio_track = True
        self.__with_video_track = True
        self.__personalized = False
        self.browser = browser
        os.makedirs(MATCHES_DIR, exist_ok=True)

    def get_url_from_user(self, default=None):
        if default is None:
            self.url = self.check_youtube_url(input("Video to download : "))
        else:
            self.url = self.check_youtube_url(default)
        return self.url

    def check_youtube_url(self, url):
        if not valid_url(url):
            raise YTD_exceptions(YTD_Error.INVALID_URL, "Please enter a valid URL!")
        elif not url.lower().startswith(YouTubeDownloader.BASE_YOUTUBE_URL):
            raise YTD_exceptions(YTD_Error.ISNOT_YOUTUBE_URL, "Please enter a valid Youtube URL!")
        return url

    def get_youtube_information(self, url):
        """Extract video metadata using yt-dlp without downloading."""
        try:
            ydl_opts = {
                'quiet': True,
                'no_warnings': True,
                'skip_download': True,
                'cookiesfrombrowser': (self.browser,),
                'js_runtimes': {'node': {}},
                'remote_components': {'ejs': 'github'},
            }
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                info_dict = ydl.extract_info(url, download=False)
                self.__info = VideoInfo(info_dict)
        except yt_dlp.utils.DownloadError as e:
            if 'urlopen' in str(e).lower() or 'network' in str(e).lower():
                raise YTD_exceptions(YTD_Error.NETWORK_ERROR) from e
            raise YTD_exceptions(YTD_Error.GETINFO_ERROR, str(e)) from e
        except Exception as e:
            raise YTD_exceptions(YTD_Error.UNKNOWN, str(e)) from e
        return self.__info

    def get_title(self):
        if self.__info is not None:
            return self.__info.title
        return '<Title Unknown>'

    def get_views(self):
        if self.__info is not None:
            return self.__info.view_count
        return '<Number unknown>'

    def _get_compatible_formats(self):
        """Return formats compatible with mp4 output (ffmpeg can convert most formats)."""
        if self.__info is None:
            return []
        compatible_formats = []
        for f in self.__info.formats:
            if f.get('ext') in ('mp4', 'm4a', 'webm', 'mp3', 'opus'):
                compatible_formats.append(f)
        return compatible_formats

    def displays_all_kind_of_available_stream(self):
        print("\nWhat kind of stream would you like to download ?")
        print("Please wait ...", end='')

        compat_formats = self._get_compatible_formats()

        # Check what types of streams are available
        audio_available = False
        video_available = False
        combined_available = False

        for f in compat_formats:
            has_audio = f.get('acodec', 'none') != 'none'
            has_video = f.get('vcodec', 'none') != 'none'

            if has_audio and not has_video:
                audio_available = True
            elif has_video and not has_audio:
                video_available = True
            elif has_audio and has_video:
                combined_available = True

        print("\r", end='')

        self.__stream_list: list = []
        idx = 0
        if combined_available:
            self.__stream_list.append(StreamTypeOption('audio+video', True, True, False))
            idx += 1
            print(f"{idx} - standard audio-video stream")
        if video_available:
            self.__stream_list.append(StreamTypeOption('video_only', False, True, False))
            idx += 1
            print(f"{idx} - video stream only")
        if audio_available:
            self.__stream_list.append(StreamTypeOption('audio_only', True, False, False))
            idx += 1
            print(f"{idx} - audio stream only")
        if audio_available and video_available:
            self.__stream_list.append(StreamTypeOption('personalized', True, True, True))
            idx += 1
            print(f"{idx} - personalized audio-video stream")

    def set_audio_video_choice(self, choice):
        # Input sanitization and bounds checking
        if choice < 1 or choice > len(self.__stream_list):
            raise ValueError(f"Choice {choice} is out of range. Valid range: 1-{len(self.__stream_list)}")

        entry: StreamTypeOption = self.__stream_list[choice - 1]
        self.__with_audio_track = entry.has_audio
        self.__with_video_track = entry.has_video
        self.__personalized = entry.is_personalized
        print(f"audio = {self.__with_audio_track}", end=' ')
        print(f"/ video = {self.__with_video_track}", end=' ')
        print("/ personalized" if self.__personalized else "/ standard")
        return self.__personalized

    def set_audio_only(self):
        self.__with_audio_track = True
        self.__with_video_track = False

    def set_video_only(self):
        self.__with_audio_track = False
        self.__with_video_track = True

    def displays_audio_video_stream_list(self):
        """Display available streams filtered by current audio/video selection."""
        print("Please wait ...", end='')

        compat_formats = self._get_compatible_formats()

        filtered = []
        if self.__with_audio_track and self.__with_video_track and not self.__personalized:
            # Progressive (muxed) streams
            for f in compat_formats:
                if (f.get('vcodec', 'none') != 'none' and
                        f.get('acodec', 'none') != 'none'):
                    filtered.append(f)
            filtered.sort(key=lambda f: f.get('height') or 0, reverse=True)
        elif self.__with_audio_track and not self.__with_video_track:
            # Audio-only adaptive (includes m4a)
            for f in compat_formats:
                if (f.get('acodec', 'none') != 'none' and
                        f.get('vcodec', 'none') == 'none'):
                    filtered.append(f)
            filtered.sort(key=lambda f: f.get('abr') or 0, reverse=True)
        elif self.__with_video_track and not self.__with_audio_track:
            # Video-only adaptive
            for f in compat_formats:
                if (f.get('vcodec', 'none') != 'none' and
                        f.get('acodec', 'none') == 'none'):
                    filtered.append(f)
            filtered.sort(key=lambda f: f.get('height') or 0, reverse=True)

        print("\r", end='')

        self.__stream_list: list = []
        for f in filtered:
            format_id = f.get('format_id', '?')
            if self.__with_audio_track and not self.__with_video_track:
                label = f"{f.get('abr', '?')}kbps"
            else:
                label = f"{f.get('height', '?')}p"
            codec = f.get('vcodec', f.get('acodec', '?'))
            self.__stream_list.append(StreamFormatOption(label, f"mp4 ({codec})", format_id))

        for idx, entry in enumerate(self.__stream_list):
            print(f"{idx + 1} - {entry.label} {entry.codec_info} (format_id:{entry.format_id})")

    def asks_user_choice(self):
        choice = None
        while choice is None:
            try:
                choice = int(input("Enter your choice : "))
            except ValueError:
                print("Enter the integer corresponding to your choice")
            else:
                if choice == 0:
                    exit(1)
                elif 1 <= choice <= len(self.__stream_list):
                    break
                else:
                    print("Please select an available option!")
            choice = None
        return choice

    def download_selected_stream(self, choice):
        """Download the selected format using yt-dlp."""
        # Input sanitization and bounds checking
        if choice < 1 or choice > len(self.__stream_list):
            raise ValueError(f"Choice {choice} is out of range. Valid range: 1-{len(self.__stream_list)}")

        entry: StreamFormatOption = self.__stream_list[choice - 1]

        if not self.__with_video_track and self.__with_audio_track and self.__personalized:
            output_dir = 'audio'
        elif self.__with_video_track and not self.__with_audio_track and self.__personalized:
            output_dir = 'video'
        else:
            output_dir = MATCHES_DIR

        os.makedirs(output_dir, exist_ok=True)

        ydl_opts = {
            'format': entry.format_id,
            'outtmpl': os.path.join(output_dir, '%(title)s.%(ext)s'),
            'quiet': False,
            'no_warnings': True,
            'cookiesfrombrowser': (self.browser,),
            'js_runtimes': {'node': {}},
            'remote_components': {'ejs': 'github'},
        }

        print(f"\nDownloading {entry.label} (format_id:{entry.format_id})...")
        try:
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                info = ydl.extract_info(self.url, download=True)
                filename = ydl.prepare_filename(info)
        except Exception as e:
            raise YTD_exceptions(YTD_Error.UNKNOWN, str(e)) from e

        print("Download complete!")
        return filename

    def download_best_auto(self):
        """
        Convenience method: let yt-dlp pick the best audio+video and merge
        them automatically (requires ffmpeg on PATH).
        """
        output_path = os.path.join(MATCHES_DIR, OUTPUT_FILENAME)
        if os.path.exists(output_path):
            os.remove(output_path)

        ydl_opts = {
            'format': 'bestvideo+bestaudio/best',
            'outtmpl': output_path,
            'merge_output_format': 'mp4',
            'quiet': False,
            'no_warnings': True,
            'cookiesfrombrowser': (self.browser,),
            'js_runtimes': {'node': {}},
            'remote_components': {'ejs': 'github'},
        }

        print("\nDownloading best quality...")
        try:
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                ydl.download([self.url])
        except Exception as e:
            raise YTD_exceptions(YTD_Error.UNKNOWN, str(e)) from e

        print("Download complete!")
        return output_path


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="YouTube video downloader")
    parser.add_argument(
        '--browser',
        default=YouTubeDownloader.DEFAULT_BROWSER,
        help="Browser to use for cookie extraction (e.g. chrome, firefox, safari, edge, brave). Default: chrome"
    )
    args = parser.parse_args()

    downloader = YouTubeDownloader(browser=args.browser)

    url_user = downloader.get_url_from_user()
    downloader.get_youtube_information(url_user)
    print("Title :", downloader.get_title(), "(", downloader.get_views(), "views )")

    downloader.displays_all_kind_of_available_stream()
    choice = downloader.asks_user_choice()

    if downloader.set_audio_video_choice(choice) == False:  # Standard choice
        print("\nSelect the video resolution ...")
        downloader.displays_audio_video_stream_list()
        choice = downloader.asks_user_choice()
        downloaded_file = downloader.download_selected_stream(choice)
        output_path = os.path.join(MATCHES_DIR, OUTPUT_FILENAME)
        if os.path.exists(output_path):
            os.remove(output_path)
        os.rename(downloaded_file, output_path)
        file_path = output_path
    else:  # Personalized choice
        print("\nSelect a video resolution ...")
        downloader.set_video_only()
        downloader.displays_audio_video_stream_list()
        choice = downloader.asks_user_choice()
        video_file = downloader.download_selected_stream(choice)

        print("\n.. then an audio rate ...")
        downloader.set_audio_only()
        downloader.displays_audio_video_stream_list()
        choice = downloader.asks_user_choice()
        audio_file = downloader.download_selected_stream(choice)

        print("\n... and the magic appears ...")
        import ffmpeg as ffmpeg_lib

        output_path = os.path.join(MATCHES_DIR, OUTPUT_FILENAME)
        if os.path.exists(output_path):
            os.remove(output_path)

        ffmpeg_lib.output(
            ffmpeg_lib.input(audio_file),
            ffmpeg_lib.input(video_file),
            output_path, vcodec='copy', acodec='copy', loglevel='quiet'
        ).run(overwrite_output=True)

        # Clean up temporary files and folders
        os.remove(audio_file)
        os.remove(video_file)
        if not os.listdir('audio'):
            os.rmdir('audio')
        if not os.listdir('video'):
            os.rmdir('video')

        file_path = output_path

    print("\nHere it is : ", file_path)
    webbrowser.open(file_path)
