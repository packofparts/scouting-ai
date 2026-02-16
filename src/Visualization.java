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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.Optional;


public class Visualization extends JPanel {
    private static final Rectangle SIZE = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    private static final double WIDTH = SIZE.getWidth();
    private static final double HEIGHT = SIZE.getHeight();
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

        BufferedReader br = new BufferedReader(new FileReader("data/" + teamNumber + ".csv"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");

            if (values.length == 3) {
                try {

                    boolean state = Boolean.parseBoolean(values[0]);
                    double x = Double.parseDouble(values[1]);
                    double y = Double.parseDouble(values[2]);
                    points.add(Optional.of(new Point(x, y)));
                    states.add(Optional.of(state));

                } catch (Exception e) {
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
        Optional<Boolean> prevState = Optional.empty();

        for (int i = 0; i < points.size() && i < states.size(); i++) {
            Optional<Point> point = points.get(i);
            Optional<Boolean> state = states.get(i);

            if (point.isPresent() && state.isPresent() && prevPoint.isPresent() && prevState.isPresent()) {
                if (state.get() && prevState.get()) {
                    g.setColor(Color.PINK);
                    if (auto) {
                        g.drawLine((int) (prevPoint.get().getX() * WIDTH), (int) (prevPoint.get().getY() * HEIGHT), (int) (point.get().getX() * WIDTH), (int) (point.get().getY() * HEIGHT));
                    }
                } else if (!state.get() && !prevState.get()) {
                    g.setColor(Color.CYAN);
                    if (tele) {
                        g.drawLine((int) (prevPoint.get().getX() * WIDTH), (int) (prevPoint.get().getY() * HEIGHT), (int) (point.get().getX() * WIDTH), (int) (point.get().getY() * HEIGHT));
                    }
                } else {
                    g.setColor(Color.YELLOW);
                    g.drawLine((int) (prevPoint.get().getX() * WIDTH), (int) (prevPoint.get().getY() * HEIGHT), (int) (point.get().getX() * WIDTH), (int) (point.get().getY() * HEIGHT));
                }
            }
            prevPoint = point;
            prevState = state;
        }
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
