import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;


public class Visualization extends JPanel {
    private static final Rectangle SIZE = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    protected static final double WIDTH = SIZE.getWidth();
    protected static final double HEIGHT = SIZE.getHeight() - 50.0; // -50 to account for taskbar
    private static final long serialVersionUID = 1L;

    private enum StartSideFilter {
        RED, BLUE, BOTH
    }

    private boolean showAuto = false;
    private boolean showTele = false;
    private StartSideFilter startSideFilter = StartSideFilter.BOTH;

    private final ArrayList<Optional<Point>> points = new ArrayList<>();
    private final ArrayList<Optional<Boolean>> states = new ArrayList<>();
    /** Session index per point; -1 marks gaps (same as empty optionals). */
    private final ArrayList<Integer> segmentIds = new ArrayList<>();
    private final HashMap<Integer, Optional<Boolean>> segmentStartedRed = new HashMap<>();

    public void setShowModes(boolean auto, boolean tele) {
        this.showAuto = auto;
        this.showTele = tele;
        repaint();
    }

    public void setSideFilterBoth() {
        startSideFilter = StartSideFilter.BOTH;
        repaint();
    }

    public void setSideFilterRed() {
        startSideFilter = StartSideFilter.RED;
        repaint();
    }

    public void setSideFilterBlue() {
        startSideFilter = StartSideFilter.BLUE;
        repaint();
    }

    private static boolean isSessionSeparatorLine(String line) {
        String t = line.trim();
        return t.length() > 0 && t.matches("-+");
    }

    private boolean segmentPassesFilter(int segmentId) {
        if (segmentId < 0) {
            return false;
        }
        Optional<Boolean> startedRed = segmentStartedRed.get(segmentId);
        if (startedRed == null) {
            startedRed = Optional.empty();
        }
        if (startSideFilter == StartSideFilter.BOTH) {
            return true;
        }
        if (startedRed.isEmpty()) {
            return false;
        }
        if (startSideFilter == StartSideFilter.RED) {
            return startedRed.get();
        }
        return !startedRed.get();
    }

    public void loadData(String teamNumber) throws IOException {
        points.clear();
        states.clear();
        segmentIds.clear();
        segmentStartedRed.clear();

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("data/" + teamNumber + ".csv"));
        } catch (IOException e) {
            throw new RuntimeException("Error loading data for team " + teamNumber + ". Likely causes: incorrect team number format or file not found.");
        }
        String line;
        int lineCount = 0;
        // Each written block begins with "---------------------------------------"; first data lines belong to segment 0.
        int segment = -1;
        try {
            while ((line = br.readLine()) != null) {
                if (isSessionSeparatorLine(line)) {
                    points.add(Optional.empty());
                    states.add(Optional.empty());
                    segmentIds.add(-1);
                    segment++;
                    lineCount = 0;
                    continue;
                }

                String[] values = line.split(",");
                if (values.length == 3 || values.length == 4) {
                    try {
                        if (segment < 0) {
                            segment = 0;
                        }
                        boolean isAuto = Boolean.parseBoolean(values[0]);
                        double x = Double.parseDouble(values[1]);
                        double y = Double.parseDouble(values[2]);
                        if (values.length == 4) {
                            boolean startRed = Boolean.parseBoolean(values[3]);
                            segmentStartedRed.putIfAbsent(segment, Optional.of(startRed));
                        } else {
                            // Legacy 3-column CSV: infer starting alliance from field X (visualization coords:
                            // blue alliance left / red right on field.png → red starts x >= 0.5).
                            segmentStartedRed.putIfAbsent(segment, Optional.of(x >= 0.5));
                        }
                        if (lineCount % 2 == 0) {
                            points.add(Optional.of(new Point(x, y)));
                            states.add(Optional.of(isAuto));
                            segmentIds.add(segment);
                        }
                        lineCount++;
                    } catch (Exception e) {
                        System.out.println("Error parsing line: \"" + line + "\". Skipping this line.");
                        points.add(Optional.empty());
                        states.add(Optional.empty());
                        segmentIds.add(-1);
                    }
                } else {
                    points.add(Optional.empty());
                    states.add(Optional.empty());
                    segmentIds.add(-1);
                    lineCount = 0;
                }
            }
        } finally {
            br.close();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to The P.A.C.K. (Predictive, Analytical, and Competitive Knowledge-base) Visualization! Which team to visualize?");
        String teamNumber = scanner.nextLine();

        System.out.println("Loading data for team " + teamNumber + "...");

        Visualization app = new Visualization();
        app.loadData(teamNumber);

        String choice;
        boolean auto = false;
        boolean tele = false;
        System.out.println("Data loaded. How do you want to visualize it?");
        do {
            System.out.println("1 - Auto only");
            System.out.println("2 - Teleop only");
            System.out.println("3 - Both/All data");
            choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    auto = true;
                    tele = false;
                    break;
                case "2":
                    auto = false;
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
        app.setShowModes(auto, tele);

        scanner.close();

        System.out.println("Starting visualization engine for team " + teamNumber + "...");

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((int) WIDTH, (int) HEIGHT);
        frame.setLocation(0, 0);
        frame.setName("The P.A.C.K. (Predictive, Analytical, and Competitive Knowledge-base) Visualization - Team " + teamNumber);
        frame.setTitle("The P.A.C.K. (Predictive, Analytical, and Competitive Knowledge-base) Visualization - Team " + teamNumber);
        frame.setIconImage(ImageIO.read(new File("pop.png")));

        JPanel root = new JPanel(new BorderLayout());
        root.add(app, BorderLayout.CENTER);

        JPanel sideBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        sideBar.setBorder(BorderFactory.createTitledBorder("Starting alliance (per match block in CSV)"));
        ButtonGroup sideGroup = new ButtonGroup();
        JToggleButton bothSides = new JToggleButton("Both sides");
        JToggleButton redStart = new JToggleButton("Red side start");
        JToggleButton blueStart = new JToggleButton("Blue side start");
        sideGroup.add(bothSides);
        sideGroup.add(redStart);
        sideGroup.add(blueStart);
        bothSides.setSelected(true);
        bothSides.addActionListener(e -> app.setSideFilterBoth());
        redStart.addActionListener(e -> app.setSideFilterRed());
        blueStart.addActionListener(e -> app.setSideFilterBlue());
        sideBar.add(bothSides);
        sideBar.add(redStart);
        sideBar.add(blueStart);

        root.add(sideBar, BorderLayout.SOUTH);
        frame.add(root);
        frame.setVisible(true);
    }

    public Visualization() {
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        try {
            drawImage(0, 0, WIDTH, HEIGHT, 0, ImageIO.read(new File("field.png")), g);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Optional<Point> prevPoint = Optional.empty();
        Optional<Boolean> prevIsAuto = Optional.empty();
        int prevSeg = -1;

        Graphics2D g2d = (Graphics2D) g;

        for (int i = 0; i < points.size() && i < states.size() && i < segmentIds.size(); i++) {
            Optional<Point> point = points.get(i);
            Optional<Boolean> isAuto = states.get(i);
            int seg = segmentIds.get(i);

            if (!point.isPresent() || !isAuto.isPresent()) {
                prevPoint = Optional.empty();
                prevIsAuto = Optional.empty();
                prevSeg = -1;
                continue;
            }

            boolean sideOk = segmentPassesFilter(seg);
            if (!sideOk) {
                prevPoint = Optional.empty();
                prevIsAuto = Optional.empty();
                prevSeg = -1;
                continue;
            }

            if (prevPoint.isPresent() && prevIsAuto.isPresent() && prevSeg == seg) {
                int x1 = (int) (prevPoint.get().getX() * WIDTH);
                int y1 = (int) (prevPoint.get().getY() * HEIGHT);
                int x2 = (int) (point.get().getX() * WIDTH);
                int y2 = (int) (point.get().getY() * HEIGHT);
                if (isAuto.get() && prevIsAuto.get()) {
                    if (showAuto) {
                        g2d.setPaint(new GradientPaint(x1, y1, Color.PINK, x2, y2, Color.RED, false));
                        g2d.drawLine(x1, y1, x2, y2);
                    }
                } else if (!isAuto.get() && !prevIsAuto.get()) {
                    if (showTele) {
                        g2d.setPaint(new GradientPaint(x1, y1, Color.CYAN, x2, y2, Color.BLUE, false));
                        g2d.drawLine(x1, y1, x2, y2);
                    }
                } else {
                    g2d.setPaint(new GradientPaint(x1, y1, Color.YELLOW, x2, y2, Color.ORANGE, false));
                    g2d.drawLine(x1, y1, x2, y2);
                }
            } else if (point.isPresent()) {
                int x = (int) (point.get().getX() * WIDTH);
                int y = (int) (point.get().getY() * HEIGHT);
                g.setColor(Color.YELLOW);
                g2d.fillOval(x - 15, y - 15, 30, 30);
            }
            prevPoint = point;
            prevIsAuto = isAuto;
            prevSeg = seg;
        }
        g.dispose();
    }

    public static void drawImage(double x, double y, double width, double height, float direction, BufferedImage image, Graphics g) {
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        at.rotate(Math.toRadians(-direction));
        at.scale(width / image.getWidth(), height / image.getHeight());
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(image, at, null);
    }

}
