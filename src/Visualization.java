import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JToggleButton;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.FlowLayout;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

import java.util.Scanner;
import java.util.ArrayList;


public class Visualization extends JPanel {
    private static final Rectangle SIZE = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    protected static final double WIDTH = SIZE.getWidth();
    protected static final double HEIGHT = SIZE.getHeight() - 67.6767676767676767676767676767; // -67 to account for taskbar
    private static final long serialVersionUID = 1L;

    private static boolean auto = false;
    private static boolean tele = false;

    private static ArrayList<MatchData> allMatches = new ArrayList<>();

    private static final double MIN_DIST = 0.0089;// Minimum distance in field coordinates (0.0 - 1.0) for a point to be considered "visited" for heatmap purposes

    private static final int HEAT_W = 200;
    private static final int HEAT_H = 120;
    private static final int BLUR_RADIUS = 6;
    private BufferedImage heatmapImage;
    private boolean heatmapDirty = true;
    private boolean heatmapEnabled = false;
    private String sideFilter = "both"; // "both", "left", "right"

    static class MatchData {
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Boolean> states = new ArrayList<>();
        boolean startedLeft;
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to The P.A.C.K. (Predictive, Analytical, and Competitive Knowledge-base) Visualization! Which team to visualize?");
        String teamNumber = scanner.nextLine();

        System.out.println("Loading data for team " + teamNumber + "...");

        BufferedReader br;
        try{
            br = new BufferedReader(new FileReader("data/" + teamNumber + ".csv"));
        }   catch (IOException e) {
            scanner.close();
            throw new RuntimeException("Error loading data for team " + teamNumber + ". Likely causes: incorrect team number format or file not found.");
        }

        String line;
        MatchData currentMatch = null;

        while ((line = br.readLine()) != null) {
            if (line.startsWith("---")) {
                if (currentMatch != null && !currentMatch.points.isEmpty()) {
                    allMatches.add(currentMatch);
                }
                currentMatch = new MatchData();
                continue;
            }

            if (currentMatch == null) {
                currentMatch = new MatchData();
            }

            String[] values = line.split(",");

            if (values.length == 4) {
                try {
                    boolean isAuto = Boolean.parseBoolean(values[0]);
                    Point newPoint = new Point(Double.parseDouble(values[1]), Double.parseDouble(values[2]), Double.parseDouble(values[3]));

                    if (!currentMatch.points.isEmpty()) {
                        Point lastPoint = currentMatch.points.get(currentMatch.points.size() - 1);
                        if (newPoint.distanceTo(lastPoint) < MIN_DIST) {
                            continue; // Skip this point since it's too close to the last one
                        }
                    }

                    currentMatch.points.add(newPoint);
                    currentMatch.states.add(isAuto);

                    if (currentMatch.points.size() == 1) {
                        currentMatch.startedLeft = newPoint.getX() < 0.5;
                    }

                } catch (Exception e) {
                    System.out.println("Error parsing line: \"" + line + "\". Skipping this line.");
                }
            }
        }

        if (currentMatch != null && !currentMatch.points.isEmpty()) {
            allMatches.add(currentMatch);
        }
        br.close();

        int leftCount = 0, rightCount = 0;
        for (MatchData m : allMatches) {
            if (m.startedLeft) leftCount++;
            else rightCount++;
        }
        System.out.println("Loaded " + allMatches.size() + " match(es) (" + leftCount + " left-start, " + rightCount + " right-start).");

        String choice;
        System.out.println("Data loaded. How do you want to visualize it?");
        do {
            System.out.println("1 - Auto only");
            System.out.println("2 - Teleop only");
            System.out.println("3 - Both/All data");
            choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    auto = true;
                    break;          
                case "2":
                    tele = true;
                    break;
                case "3":
                    auto = true;
                    tele = true;
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
                    break;
            }
        } while (!auto && !tele);

        scanner.close();

        System.out.println("Starting visualization engine for team " + teamNumber + "...");

        Visualization app = new Visualization();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocation(0, 0);
        frame.setName("The P.A.C.K. (Predictive, Analytical, and Competitive Knowledge-base) Visualization - Team " + teamNumber);
        frame.setTitle("The P.A.C.K. (Predictive, Analytical, and Competitive Knowledge-base) Visualization - Team " + teamNumber);
        frame.setIconImage(ImageIO.read(new File("pop.png")));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JToggleButton heatmapBtn = new JToggleButton("Heatmap");
        heatmapBtn.addActionListener(e -> {
            app.heatmapEnabled = heatmapBtn.isSelected();
            app.heatmapDirty = true;
            app.repaint();
        });
        toolbar.add(heatmapBtn);

        toolbar.add(new JLabel("   |   Starting Side: "));

        JToggleButton bothBtn = new JToggleButton("Both (" + allMatches.size() + ")", true);
        JToggleButton leftBtn = new JToggleButton("Left (" + leftCount + ")");
        JToggleButton rightBtn = new JToggleButton("Right (" + rightCount + ")");

        bothBtn.addActionListener(e -> {
            bothBtn.setSelected(true);
            leftBtn.setSelected(false);
            rightBtn.setSelected(false);
            app.sideFilter = "both";
            app.heatmapDirty = true;
            app.repaint();
        });
        leftBtn.addActionListener(e -> {
            leftBtn.setSelected(true);
            bothBtn.setSelected(false);
            rightBtn.setSelected(false);
            app.sideFilter = "left";
            app.heatmapDirty = true;
            app.repaint();
        });
        rightBtn.addActionListener(e -> {
            rightBtn.setSelected(true);
            bothBtn.setSelected(false);
            leftBtn.setSelected(false);
            app.sideFilter = "right";
            app.heatmapDirty = true;
            app.repaint();
        });

        toolbar.add(bothBtn);
        toolbar.add(leftBtn);
        toolbar.add(rightBtn);

        frame.add(toolbar, BorderLayout.NORTH);
        frame.add(app, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    public Visualization() {
        
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int) WIDTH, (int) HEIGHT);
    }

    private boolean matchIncluded(MatchData m) {
        if (sideFilter.equals("left")) return m.startedLeft;
        if (sideFilter.equals("right")) return !m.startedLeft;
        return true;
    }

    private void rebuildHeatmapImage() {
        float[][] density = new float[HEAT_W][HEAT_H];
        for (MatchData m : allMatches) {
            if (!matchIncluded(m)) continue;
            for (int i = 0; i < m.points.size() && i < m.states.size(); i++) {
                Point p = m.points.get(i);
                Boolean st = m.states.get(i);
                if (p == null || st == null) continue;
                boolean isAut = st;
                if ((auto && tele) || (auto && isAut) || (tele && !isAut)) {
                    int cx = Math.min(HEAT_W - 1, Math.max(0, (int) (p.getX() * HEAT_W)));
                    int cy = Math.min(HEAT_H - 1, Math.max(0, (int) (p.getY() * HEAT_H)));
                    density[cx][cy] += 1.0f;
                }
            }
        }

        float[][] blurred = gaussianBlur(density, HEAT_W, HEAT_H, BLUR_RADIUS);

        float max = 0;
        for (int x = 0; x < HEAT_W; x++)
            for (int y = 0; y < HEAT_H; y++)
                if (blurred[x][y] > max) max = blurred[x][y];

        heatmapImage = new BufferedImage(HEAT_W, HEAT_H, BufferedImage.TYPE_INT_ARGB);
        if (max > 0) {
            for (int x = 0; x < HEAT_W; x++) {
                for (int y = 0; y < HEAT_H; y++) {
                    float t = blurred[x][y] / max;
                    if (t < 0.01f) continue;
                    heatmapImage.setRGB(x, y, heatColor(t));
                }
            }
        }
        heatmapDirty = false;
    }

    /** ARGB color for a 0..1 intensity using a blue->cyan->green->yellow->red gradient. */
    private static int heatColor(float t) {
        int alpha = (int) (80 + 175 * t);
        float r, g, b;
        if (t < 0.25f) {
            float s = t / 0.25f;
            r = 0; g = s; b = 1;
        } else if (t < 0.5f) {
            float s = (t - 0.25f) / 0.25f;
            r = 0; g = 1; b = 1 - s;
        } else if (t < 0.75f) {
            float s = (t - 0.5f) / 0.25f;
            r = s; g = 1; b = 0;
        } else {
            float s = (t - 0.75f) / 0.25f;
            r = 1; g = 1 - s; b = 0;
        }
        return (alpha << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
    }

    private static float[][] gaussianBlur(float[][] src, int w, int h, int radius) {
        float[] kernel = new float[radius * 2 + 1];
        float sigma = radius / 2.5f;
        float sum = 0;
        for (int i = -radius; i <= radius; i++) {
            kernel[i + radius] = (float) Math.exp(-(i * i) / (2 * sigma * sigma));
            sum += kernel[i + radius];
        }
        for (int i = 0; i < kernel.length; i++) kernel[i] /= sum;

        float[][] temp = new float[w][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float v = 0;
                for (int k = -radius; k <= radius; k++) {
                    int sx = Math.min(w - 1, Math.max(0, x + k));
                    v += src[sx][y] * kernel[k + radius];
                }
                temp[x][y] = v;
            }
        }

        float[][] out = new float[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                float v = 0;
                for (int k = -radius; k <= radius; k++) {
                    int sy = Math.min(h - 1, Math.max(0, y + k));
                    v += temp[x][sy] * kernel[k + radius];
                }
                out[x][y] = v;
            }
        }
        return out;
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        try {
            drawImage(0, 0, WIDTH, HEIGHT, 0, ImageIO.read(new File("field.png")), g);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (heatmapEnabled) {
            if (heatmapDirty) rebuildHeatmapImage();
            if (heatmapImage != null) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(heatmapImage, 0, 0, (int) WIDTH, (int) HEIGHT, null);
            }
        }

        if (heatmapEnabled) {
            g.dispose();
            return;
        }

        for (MatchData m : allMatches) {
            if (!matchIncluded(m)) continue;

            Point prevPoint = m.points.get(0);
            boolean prevIsAuto = m.states.get(0);

            g.setColor(Color.YELLOW);
            g.fillOval((int) (WIDTH * prevPoint.getX()) - 8, (int) (HEIGHT * prevPoint.getY()) - 8, 16, 16);

            for (int i = 0; i < m.points.size() && i < m.states.size(); i++) {
                Point point = m.points.get(i);
                boolean isAuto = m.states.get(i);

                int x1 = (int) (prevPoint.getX() * WIDTH);
                int y1 = (int) (prevPoint.getY() * HEIGHT);
                int x2 = (int) (point.getX() * WIDTH);
                int y2 = (int) (point.getY() * HEIGHT);
                if (isAuto && prevIsAuto) {
                    g.setColor(Color.PINK);
                    if (auto) {
                        g2d.setPaint(new GradientPaint(x1, y1, Color.PINK, x2, y2, Color.RED, false));
                        g2d.drawLine(x1, y1, x2, y2);
                    }
                } else if (!isAuto && !prevIsAuto) {
                    g.setColor(Color.CYAN);
                    if (tele) {
                        g2d.setPaint(new GradientPaint(x1, y1, Color.CYAN, x2, y2, Color.BLUE, false));
                        g2d.drawLine(x1, y1, x2, y2);
                    }
                } else {
                    g.setColor(Color.ORANGE);
                    g2d.setPaint(new GradientPaint(x1, y1, Color.YELLOW, x2, y2, Color.ORANGE, false));
                    g2d.drawLine(x1, y1, x2, y2);
                }
                prevPoint = point;
                prevIsAuto = isAuto;
            }
        }
        g.dispose();
    }

    public static void drawImage(double x, double y, double width, double height, float direction, BufferedImage image, Graphics g) {
  	  AffineTransform at = new AffineTransform();
  	  at.translate(x, y);
  	  at.rotate(Math.toRadians(-direction));
  	  at.scale(width/image.getWidth(), height/image.getHeight());
  	  Graphics2D g2d = (Graphics2D) g;
  	  g2d.drawImage(image, at, null);
	}

}
