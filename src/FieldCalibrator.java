
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;

public class FieldCalibrator extends JPanel implements MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;
    private static final String CALIBRATION_FILE = "calibration.txt";
    private static final int DOT_RADIUS = 10;

    private BufferedImage backgroundImage;
    private double panelWidth;
    private double panelHeight;

    // The four corner points in relative coordinates (0.0 - 1.0)
    private double[] xCoords = new double[4]; // TL, BL, TR, BR
    private double[] yCoords = new double[4];
    private String[] labels = {"TL", "BL", "TR", "BR"};

    private int dragIndex = -1; // which point is being dragged

    private boolean saved = false;

    public FieldCalibrator(double topLeftX, double topLeftY,
                           double bottomLeftX, double bottomLeftY,
                           double topRightX, double topRightY,
                           double bottomRightX, double bottomRightY) {
        xCoords[0] = topLeftX;     yCoords[0] = topLeftY;
        xCoords[1] = bottomLeftX;  yCoords[1] = bottomLeftY;
        xCoords[2] = topRightX;    yCoords[2] = topRightY;
        xCoords[3] = bottomRightX; yCoords[3] = bottomRightY;

        try {
            backgroundImage = ImageIO.read(new File("matches/cover.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        panelWidth = getWidth();
        panelHeight = getHeight();

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, (int) panelWidth, (int) panelHeight, null);
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw field boundary lines in green
        g2d.setStroke(new BasicStroke(3f));
        g2d.setColor(Color.GREEN);
        int tlX = toPixelX(xCoords[0]), tlY = toPixelY(yCoords[0]);
        int blX = toPixelX(xCoords[1]), blY = toPixelY(yCoords[1]);
        int trX = toPixelX(xCoords[2]), trY = toPixelY(yCoords[2]);
        int brX = toPixelX(xCoords[3]), brY = toPixelY(yCoords[3]);

        g2d.drawLine(tlX, tlY, trX, trY); // top edge
        g2d.drawLine(tlX, tlY, blX, blY); // left edge
        g2d.drawLine(blX, blY, brX, brY); // bottom edge
        g2d.drawLine(trX, trY, brX, brY); // right edge

        // Draw draggable yellow dots with labels
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        for (int i = 0; i < 4; i++) {
            int px = toPixelX(xCoords[i]);
            int py = toPixelY(yCoords[i]);

            g2d.setColor(Color.YELLOW);
            g2d.fillOval(px - DOT_RADIUS, py - DOT_RADIUS, DOT_RADIUS * 2, DOT_RADIUS * 2);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(px - DOT_RADIUS, py - DOT_RADIUS, DOT_RADIUS * 2, DOT_RADIUS * 2);
            g2d.setColor(Color.WHITE);
            g2d.drawString(labels[i], px + DOT_RADIUS + 4, py + 5);
        }
    }

    private int toPixelX(double relX) {
        return (int) (relX * panelWidth);
    }

    private int toPixelY(double relY) {
        return (int) (relY * panelHeight);
    }

    private double toRelX(int px) {
        return px / panelWidth;
    }

    private double toRelY(int py) {
        return py / panelHeight;
    }

    private int findClosestPoint(int mx, int my) {
        int closest = -1;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            int px = toPixelX(xCoords[i]);
            int py = toPixelY(yCoords[i]);
            double dist = Math.sqrt((mx - px) * (mx - px) + (my - py) * (my - py));
            if (dist < minDist && dist < DOT_RADIUS * 3) {
                minDist = dist;
                closest = i;
            }
        }
        return closest;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        dragIndex = findClosestPoint(e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragIndex = -1;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragIndex >= 0) {
            double rx = toRelX(e.getX());
            double ry = toRelY(e.getY());
            // Clamp to [0, 1]
            rx = Math.max(0, Math.min(1, rx));
            ry = Math.max(0, Math.min(1, ry));
            xCoords[dragIndex] = rx;
            yCoords[dragIndex] = ry;
            repaint();
        }
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}

    public boolean isSaved() {
        return saved;
    }

    public void markSaved() {
        saved = true;
    }

    public double getTopLeftX()     { return xCoords[0]; }
    public double getTopLeftY()     { return yCoords[0]; }
    public double getBottomLeftX()  { return xCoords[1]; }
    public double getBottomLeftY()  { return yCoords[1]; }
    public double getTopRightX()    { return xCoords[2]; }
    public double getTopRightY()    { return yCoords[2]; }
    public double getBottomRightX() { return xCoords[3]; }
    public double getBottomRightY() { return yCoords[3]; }

    /**
     * Saves calibration values to calibration.txt.
     */
    public static void saveCalibration(double tlX, double tlY, double blX, double blY,
                                       double trX, double trY, double brX, double brY) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CALIBRATION_FILE))) {
            pw.println(tlX + "," + tlY);
            pw.println(blX + "," + blY);
            pw.println(trX + "," + trY);
            pw.println(brX + "," + brY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads calibration values from calibration.txt.
     * Returns null if file doesn't exist or is invalid.
     * Returns double[8]: tlX, tlY, blX, blY, trX, trY, brX, brY
     */
    public static double[] loadCalibration() {
        File f = new File(CALIBRATION_FILE);
        if (!f.exists()) {
            return null;
        }
        try {
            java.util.List<String> lines = Files.readAllLines(f.toPath());
            if (lines.size() < 4) return null;
            double[] vals = new double[8];
            for (int i = 0; i < 4; i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length != 2) return null;
                vals[i * 2] = Double.parseDouble(parts[0].trim());
                vals[i * 2 + 1] = Double.parseDouble(parts[1].trim());
            }
            return vals;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Launches the calibrator GUI. Blocks until user clicks Save & Exit.
     * Returns the calibrated values as double[8]: tlX, tlY, blX, blY, trX, trY, brX, brY.
     * Returns null if the user closes the window without saving.
     */
    public static double[] launch(double tlX, double tlY, double blX, double blY,
                                  double trX, double trY, double brX, double brY) {
        FieldCalibrator calibrator = new FieldCalibrator(tlX, tlY, blX, blY, trX, trY, brX, brY);

        JFrame frame = new JFrame("Field Calibration - Drag yellow dots to field corners");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize((int) Visualization.WIDTH / 2, (int) Visualization.HEIGHT / 2 + 50);
        frame.setLocation(0, 0);
        frame.setLayout(new BorderLayout());

        try {
            frame.setIconImage(ImageIO.read(new File("pop.png")));
        } catch (IOException e) {
            // ignore
        }

        frame.add(calibrator, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save & Exit");
        saveButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        saveButton.setPreferredSize(new Dimension(0, 45));
        saveButton.addActionListener(e -> {
            calibrator.markSaved();
            synchronized (calibrator) {
                calibrator.notifyAll();
            }
        });
        frame.add(saveButton, BorderLayout.SOUTH);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // User closed without saving - still unblock
                synchronized (calibrator) {
                    calibrator.notifyAll();
                }
            }
        });

        frame.setVisible(true);

        // Block until save or close
        synchronized (calibrator) {
            try {
                calibrator.wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }

        frame.dispose();

        if (calibrator.isSaved()) {
            double[] result = new double[] {
                calibrator.getTopLeftX(), calibrator.getTopLeftY(),
                calibrator.getBottomLeftX(), calibrator.getBottomLeftY(),
                calibrator.getTopRightX(), calibrator.getTopRightY(),
                calibrator.getBottomRightX(), calibrator.getBottomRightY()
            };
            saveCalibration(result[0], result[1], result[2], result[3],
                            result[4], result[5], result[6], result[7]);
            return result;
        }
        return null;
    }
}
