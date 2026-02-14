"""
YouTube Downloader - Source from https://github.com/DevCTx/YouTubeDownloader. Edited by AI to export the video to specific file inside this project.
"""
import http
import os
import urllib

_SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MATCHES_DIR = os.path.join(_SCRIPT_DIR, "matches")
OUTPUT_FILENAME = "match.mp4"
import socket
import webbrowser
from traceback import format_exc as traceback_format_exc
from validators import url as valid_url
from enum import Enum
from pytubefix import YouTube, exceptions

# Needs to install ffmpeg-python module and ffmpeg zip in a directory on os and to add a path var to bin folder
import ffmpeg

class YTD_Error(Enum):
    UNKNOWN = 0,
    INVALID_URL = 1,
    ISNOT_YOUTUBE_URL = 2,
    NETWORK_ERROR = 3,
    GETINFO_ERROR = 4


class YTD_exceptions(Exception):

    def __init__(self, code, msg=None):
        if code == YTD_Error.UNKNOWN :
            print('Traceback : ', traceback_format_exc())  # Trace the last erroneous line and specify the exception
        elif code == YTD_Error.NETWORK_ERROR:
            print(f"Please check your internet connection! [error : {code} ]")
        else:
            print(f"{msg if msg is not None else 'Please contact your network administrator!'} [error : {code}]")
        exit()


class YouTubeDownloader():

    BASE_YOUTUBE_URL = "https://www.youtube.com"
    DEFAULT_URL = "https://www.youtube.com/watch?v=WRyzVaf46Ts"

    def __init__(self):
        self.url = None
        self.__youtube_video = None
        self.__with_audio_track = True
        self.__with_video_track = True
        os.makedirs(MATCHES_DIR, exist_ok=True)

    def __try(self, func):
        try:
            return func
        except exceptions.RegexMatchError as e :
            raise YTD_exceptions(YTD_Error.ISNOT_YOUTUBE_VIDEO, "Please enter a valid Video YouTube URL!") from e
        except (urllib.error.URLError, http.client.RemoteDisconnected) as e:
            raise YTD_exceptions(YTD_Error.NETWORK_ERROR) from e
        except socket.gaierror as e:
            raise YTD_exceptions(YTD_Error.GETINFO_ERROR) from e
        except :
            raise YTD_exceptions(YTD_Error.UNKNOWN, "Non identified error")

    def get_url_from_user(self, default=None):
        if default == None :
            self.url = self.check_youtube_url( input("Video to download : ") )
        else:
            self.url = self.check_youtube_url(default)
        return self.url

    def check_youtube_url(self, url):
        # Checks the url format
        if not valid_url(url):
            raise YTD_exceptions(YTD_Error.INVALID_URL, "Please enter a valid URL!")
        elif not url.lower().startswith(YouTubeDownloader.BASE_YOUTUBE_URL):
            raise YTD_exceptions(YTD_Error.ISNOT_YOUTUBE_URL, "Please enter a valid Youtube URL!")
        return url

    def get_title(self):
        title = "<Title Unknown>"
        if self.__youtube_video is not None:
            try:
                title = self.__youtube_video.title
            except (urllib.error.URLError, http.client.RemoteDisconnected) as e:
                raise YTD_exceptions(YTD_Error.NETWORK_ERROR) from e
            except socket.gaierror as e:
                raise YTD_exceptions(YTD_Error.GETINFO_ERROR) from e
            except:
                raise YTD_exceptions(YTD_Error.UNKNOWN, "Non identified error")
        return title

    def get_views(self):
        views = "<Number unknown>"
        if self.__youtube_video is not None:
            try:
                views = self.__youtube_video.views
            except (urllib.error.URLError, http.client.RemoteDisconnected) as e:
                raise YTD_exceptions(YTD_Error.NETWORK_ERROR) from e
            except socket.gaierror as e:
                raise YTD_exceptions(YTD_Error.GETINFO_ERROR) from e
            except:
                raise YTD_exceptions(YTD_Error.UNKNOWN, "Non identified error")
        return views

    def get_youtube_information(self, url):
        # Try to catch the information from YouTube and print the Title and number of views if available
        self.__youtube_video = self.__try(YouTube(self.url, on_progress_callback= self.__on_progress))
        return self.__youtube_video

    def displays_all_kind_of_available_stream(self):
        print("\nWhat kind of stream would you like to download ?")
        print("Please wait ...",end='')
        # Get the list of all available streams on YouTube for this url
        streams = self.__try(self.__youtube_video.streams.filter(file_extension='mp4'))
        print("\r",end='')

        # Displays the resolutions available to download and ask user to select a choice
        self.__stream_list = []
        audio_available = False
        video_available = False
        for stream in streams:
            if stream.includes_audio_track:
                audio_available = True
            if stream.includes_video_track:
                video_available = True
            self.__stream_list.append((stream.includes_audio_track,
                                       stream.includes_video_track,
                                       stream.mime_type))
        self.__stream_list = list(set(self.__stream_list))
        if audio_available and video_available :
            self.__stream_list.append((audio_available, video_available, "personalized"))
        for idx, elem in enumerate(self.__stream_list):
            msg = f'{elem[2] if elem[2]=="personalized" else "standard" } audio-video stream' if elem[0] and elem[1] \
              else 'video stream only' if elem[0] == False and elem[1] == True \
              else 'audio stream only' if elem[0] == True and elem[1] == False \
              else elem[2]
            print(f"{idx + 1} - {msg}")

    def set_audio_video_choice(self, choice):
        self.__with_audio_track = self.__stream_list[choice - 1][0]
        self.__with_video_track = self.__stream_list[choice - 1][1]
        self.__personalized = True if self.__stream_list[choice - 1][2] == "personalized" else False
        print(f"audio = {self.__with_audio_track}", end=' ')
        print(f"/ video = {self.__with_video_track}", end=' ')
        print(f"/ personalized" if self.__personalized else "/ standard")
        return self.__personalized

    def set_audio_only(self):
        self.__with_audio_track = True
        self.__with_video_track = False

    def set_video_only(self):
        self.__with_audio_track = False
        self.__with_video_track = True

    def displays_audio_video_stream_list(self):
        """
        Displays the list of available streams on YouTube for this url
        depending on __with_audio_track and __with_video_track

        if __with_audio_track and __with_video_track, get the progressive streams = by audio-video combined pairs
        if __with_audio_track only get the adaptive audio streams = audio streams only order by audio rate
        if __with_video_track only get the adaptive video streams = video streams only order by resolution
        :return: None
        """
        print("Please wait ...", end='')
        if self.__with_audio_track and self.__with_video_track :
            streams = self.__try(
                self.__youtube_video.streams.filter(progressive=True, file_extension='mp4')
                .order_by("resolution")
                .desc()
            )
        elif self.__with_audio_track and not self.__with_video_track:
            streams = self.__try(
                self.__youtube_video.streams.filter(adaptive=True, file_extension='mp4', type="audio")
                .order_by("abr")
                .desc()
            )
        elif not self.__with_audio_track and self.__with_video_track :
            streams = self.__try(
                self.__youtube_video.streams.filter( adaptive=True, file_extension='mp4', type="video")
                .order_by("resolution")
                .desc()
            )
        print("\r",end='')

        # Displays the resolutions available to download and ask user to select a choice
        self.__stream_list = []
        for stream in streams:
            if self.__with_audio_track and not self.__with_video_track:
                self.__stream_list.append((stream.__dict__['abr'],
                                           stream.__dict__['mime_type'],
                                           stream.__dict__['itag']))
            else:
                self.__stream_list.append((stream.__dict__['resolution'],
                                           stream.__dict__['mime_type'],
                                           stream.__dict__['itag']))

        for idx, elem in enumerate(self.__stream_list):
            print(f"{idx + 1} - {elem[0]} {elem[1]} (itag:{elem[2]})")

    def asks_user_choice(self):
        choice = None
        while choice == None:
            try:
                choice = int(input("Enter your choice : "))
            except ValueError:
                print("Enter the integer corresponding to your choice")
            else:
                if choice == 0:
                    exit()
                elif 1 <= choice <= len(self.__stream_list):
                    break
                else:
                    print("Please select a available option!")
            choice = None
        return choice

    def __on_progress(self, stream, chunk, bytes_remaining):
        percentage_done = (stream.filesize - bytes_remaining) * 100 / stream.filesize
        print(f"\rDownloading {self.resolution} {self.mime_type} (itag:{self.itag})...",
              int(percentage_done), "%", end='' if percentage_done != 100 else ' done !\n')

    def download_selected_stream(self, choice):
        # Download the selected resolution via the itag ident
        self.resolution = self.__stream_list[choice - 1][0]
        self.mime_type = self.__stream_list[choice - 1][1]
        self.itag = int(self.__stream_list[choice - 1][2])
        print("\nPlease wait ... ", end='')
        stream = self.__try(self.__youtube_video.streams.get_by_itag(self.itag))
        output_path = (
            'audio' if not self.__with_video_track and self.__with_audio_track and self.__personalized
            else 'video' if self.__with_video_track and not self.__with_audio_track and self.__personalized
            else MATCHES_DIR
        )
        self.__try(stream.download(output_path))
        print("\r", end='')
        return stream


if __name__ == '__main__' :
    downloader = YouTubeDownloader()

    url_user = downloader.get_url_from_user()       # (YouTubeDownloader.DEFAULT_URL) to test
    downloader.get_youtube_information(url_user)
    print("Title :", downloader.get_title(), "(", downloader.get_views(),"views )")

    downloader.displays_all_kind_of_available_stream()
    choice = downloader.asks_user_choice()

    if downloader.set_audio_video_choice(choice) == False:  # Standard choice
        print("\nSelect the video resolution ...")
        downloader.displays_audio_video_stream_list()
        choice = downloader.asks_user_choice()
        stream = downloader.download_selected_stream(choice)
        output_path = os.path.join(MATCHES_DIR, OUTPUT_FILENAME)
        if os.path.exists(output_path):
            os.remove(output_path)
        downloaded_path = os.path.join(MATCHES_DIR, stream.default_filename)
        os.rename(downloaded_path, output_path)
        file_path = output_path
    else:   # Personalized choice
        print("\nSelect a video resolution ...")
        downloader.set_video_only()
        downloader.displays_audio_video_stream_list()
        choice = downloader.asks_user_choice()
        stream = downloader.download_selected_stream(choice)

        print("\n.. then an audio rate ...")
        downloader.set_audio_only()
        downloader.displays_audio_video_stream_list()
        choice = downloader.asks_user_choice()
        audio_stream = downloader.download_selected_stream(choice)

        print("\n... and the magic appears ...") # and build the new video
        audio_filename = os.path.join("audio", audio_stream.default_filename)
        video_filename = os.path.join("video", stream.default_filename)
        output_filename = os.path.join(MATCHES_DIR, OUTPUT_FILENAME)
        if os.path.exists(output_filename):
            os.remove(output_filename)
        # vcodec et acodec = "copy" to directlystream associated the downloaded video and audio streams
        ffmpeg.output( ffmpeg.input(audio_filename),
                       ffmpeg.input(video_filename),
                       output_filename, vcodec='copy', acodec='copy', loglevel='quiet').run(overwrite_output=True)

        # delete the temporary files and folders
        os.remove(audio_filename)
        os.remove(video_filename)
        os.rmdir('audio')
        os.rmdir('video')

        file_path = output_filename

    print("\nHere it is : ", file_path)
    webbrowser.open(file_path)
