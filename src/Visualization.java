import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JFrame;
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
import java.util.Optional;


public class Visualization extends JPanel {
    private static final Rectangle SIZE = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    protected static final double WIDTH = SIZE.getWidth();
    protected static final double HEIGHT = SIZE.getHeight() - 50.0; // -50 to account for taskbar
    private static final long serialVersionUID = 1L;

    private static boolean auto = false;
    private static boolean tele = false;

    private static ArrayList<Optional<Point>> points = new ArrayList<>();
    private static ArrayList<Optional<Boolean>> states = new ArrayList<>();

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
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");

            if (values.length == 3) {
                try {

                    boolean isAuto = Boolean.parseBoolean(values[0]);
                    double x = Double.parseDouble(values[1]);
                    double y = Double.parseDouble(values[2]);
                    points.add(Optional.of(new Point(x, y)));
                    states.add(Optional.of(isAuto));

                } catch (Exception e) {
                    System.out.println("Error parsing line: \"" + line + "\". Skipping this line.");
                    points.add(Optional.empty());
                    states.add(Optional.empty());
                }
            } else {
                points.add(Optional.empty());
                states.add(Optional.empty());
            }
        }
        br.close();

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

        JPanel app = new Visualization();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((int) WIDTH, (int) HEIGHT);
        frame.setLocation(0, 0);
        frame.setName("The P.A.C.K. (Predictive, Analytical, and Competitive Knowledge-base) Visualization - Team " + teamNumber);
        frame.setTitle("The P.A.C.K. (Predictive, Analytical, and Competitive Knowledge-base) Visualization - Team " + teamNumber);
        frame.setIconImage(ImageIO.read(new File("pop.png")));
        frame.add(app);
        frame.setVisible(true);
    }

    public Visualization() {
        
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);

        try {
            drawImage(0, 0, WIDTH, HEIGHT, 0, ImageIO.read(new File("field.png")), g);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Optional<Point> prevPoint = Optional.empty();
        Optional<Boolean> prevIsAuto = Optional.empty();

        Graphics2D g2d = (Graphics2D) g;

        for (int i = 0; i < points.size() && i < states.size(); i+=2) {
            Optional<Point> point = points.get(i);
            Optional<Boolean> isAuto = states.get(i);

            if (point.isPresent() && isAuto.isPresent() && prevPoint.isPresent() && prevIsAuto.isPresent()) {
                if (isAuto.get() && prevIsAuto.get()) {
                    g.setColor(Color.PINK);
                    if (auto) {
                        int x1 = (int) ((1-prevPoint.get().getX()) * WIDTH);
                        int y1 = (int) ((1-prevPoint.get().getY()) * HEIGHT);
                        int x2 = (int) ((1-point.get().getX()) * WIDTH);
                        int y2 = (int) ((1-point.get().getY()) * HEIGHT);
                        g2d.setPaint(new GradientPaint(x1, y1, Color.PINK, x2, y2, Color.RED, false));
                        g2d.drawLine(x1, y1, x2, y2);
                    }
                } else if (!isAuto.get() && !prevIsAuto.get()) {
                    g.setColor(Color.CYAN);
                    if (tele) {
                        int x1 = (int) ((1-prevPoint.get().getX()) * WIDTH);
                        int y1 = (int) ((1-prevPoint.get().getY()) * HEIGHT);
                        int x2 = (int) ((1-point.get().getX()) * WIDTH);
                        int y2 = (int) ((1-point.get().getY()) * HEIGHT);
                        g2d.setPaint(new GradientPaint(x1, y1, Color.CYAN, x2, y2, Color.BLUE, false));
                        g2d.drawLine(x1, y1, x2, y2);
                    }
                } 
            }
            prevPoint = point;
            prevIsAuto = isAuto;
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
