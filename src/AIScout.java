
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;

import javax.imageio.ImageIO;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Graphics;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import java.util.Optional;

public class AIScout extends JPanel{
    private static HashSet<Integer> autoFrameIndices = new HashSet<>();
    private static HashSet<Integer> teleFrameIndices = new HashSet<>();
    private static final long serialVersionUID = 1L; //Recommended for JPanel subclasses
    
    //Relative locations of field joints (i.e. 0.5 is half of the screen)
    //Ideally, all x & y vals must differ to avoid errors in pose estimation

    // Relative locations of field joints (i.e. 0.5 is half of the screen)
    // All x vals must differ to avoid errors in pose estimation

    // Currently calibrated for PNW District Sammamish Event 2025
    // Can be easily changed for other fields by changing these 4 points

    private static final Point TOP_LEFT = new Point(0.19620, 0.19515);
    private static final Point BOTTOM_LEFT = new Point(0.02259, 0.69198);
    private static final Point TOP_RIGHT = new Point(0.84483, 0.21941);
    private static final Point BOTTOM_RIGHT = new Point(0.98811, 0.72152);

    public AIScout() {
        //Empty constructor for JPanel subclass
    }

    public static void main(String[] args) throws IOException {
        
        if (args.length != 6) {
            throw new IllegalArgumentException("Exactly 6 team numbers must be provided as arguments, not " + args.length);
        }
        ArrayList<ArrayList<Optional<Point>>> detections = detect();

        JPanel confirm = new AIScout();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize((int) Visualization.WIDTH/2, (int) Visualization.HEIGHT/2);
        frame.setLocation(0, 0);
        frame.setName("The P.A.C.K. (Predictive, Analytical, and Competitive Knowledge-base) Field Calibration Confirmation");
        frame.setTitle("The P.A.C.K. (Predictive, Analytical, and Competitive Knowledge-base) Field Calibration Confirmation");
        frame.setIconImage(ImageIO.read(new File("pop.png")));
        frame.add(confirm);
        frame.setVisible(true);
        System.out.println("Is field properly aligned in the window that just opened? (y/n)");

        Scanner scanner = new Scanner(System.in);
        if (!scanner.nextLine().equalsIgnoreCase("y")) {
            scanner.close();
            frame.dispose();
            throw new IllegalStateException("Field not properly aligned. Please adjust TOP_LEFT, BOTTOM_LEFT, TOP_RIGHT, and BOTTOM_RIGHT so that the green lines are exactly on the field boundaries, then try again.");
        }
        frame.dispose();
        

        // Finds the first frame with 6 robots detected
        int firstFrameIndex = 0;

        int amountShows = 0;
        int redShows = 0;
        int blueShows = 0;
        for (int i = 0; i < args.length; i++) {
            if (!args[i].equals("no_show")) {
                amountShows++;
                if (i < 3) {
                    redShows++;
                } else {
                    blueShows++;
                }
            }
        }
        FRCRobot[] robots = new FRCRobot[amountShows];
        int insertionIndex = 0;
        while (firstFrameIndex < detections.size() && detections.get(firstFrameIndex).size() != amountShows) {
            firstFrameIndex++;
        }
        if (firstFrameIndex >= detections.size()) {
            scanner.close();
            throw new IllegalStateException("Cannot confirm starting point. No frame with " + amountShows + " robots detected found in the video. Please abandon this video and try another one, or check that the detector is working correctly. Exiting");
        }


        System.out.println("First frame with " + amountShows + " robots detected is at index " + firstFrameIndex + " (which is about " + Math.round(firstFrameIndex * 1000.0 / detections.size()) / 10.0 + "% of the video). Confirm as starting point? (y/n)");

        if (!scanner.nextLine().equalsIgnoreCase("y")) {
            scanner.close();
            throw new IllegalStateException("Starting point not confirmed. Exiting.");
        }
        scanner.close();

        long time = System.currentTimeMillis();

        System.out.println("Starting point confirmed. Initializing robots and writing data...");

        ArrayList<Optional<Point>> startingDetections = detections.get(firstFrameIndex);

        // Splits frame into left and right halvesby x value, then sorts each half by y
        // value, then concatenates the halves back together. This way, the robots are
        // ordered from top left to bottom right, which should be consistent with the
        // order of team numbers in args
        startingDetections.sort(Comparator.comparing(Optional::get, Comparator.comparing(Point::getX)));

        // split array in half & find out how many noshows are there.
        String[] firstHalf = Arrays.copyOfRange(args, 0, 3);
        String[] secondHalf = Arrays.copyOfRange(args, 3, args.length);

        List<Optional<Point>> leftHalf = startingDetections.subList(0, redShows);

        leftHalf.sort(Comparator.comparing(Optional::get, Comparator.comparing(Point::getY).reversed()));

        List<Optional<Point>> rightHalf = startingDetections.subList(redShows, redShows + blueShows);
        rightHalf.sort(Comparator.comparing(Optional::get, Comparator.comparing(Point::getY).reversed()));

        int pointIndex = 0;
        for (int i = 0; i < firstHalf.length; i++) {
            if (!firstHalf[i].equals("no_show")) {
                Point coord = leftHalf.get(pointIndex).get();
                robots[insertionIndex] = new FRCRobot(coord, firstHalf[i]);
                insertionIndex++;
                pointIndex++;
            }
        }
        pointIndex = 0;
        for (int i = 0; i < secondHalf.length; i++) {
            if (!secondHalf[i].equals("no_show")) {
                Point coord = rightHalf.get(pointIndex).get();
                robots[insertionIndex] = new FRCRobot(coord, secondHalf[i]);
                insertionIndex++;
                pointIndex++;
            }
        }

        // For every frame, assign robots new positions using Hungarian Algorithm and
        // writes data to robot files
        for (int i = firstFrameIndex + 1; i < detections.size(); i++) {
            System.out.print("\r");

            double percent = (double) i / (detections.size() - 1);
            int hashtags = (int) Math.round(percent * 10);
            String display = String.format("%.1f", percent * 100);

            System.out.print("Analyzing... |");

            for (int j = 0; j < hashtags; j++) {
                System.out.print("#");
            }
            for (int j = 0; j < 10 - hashtags; j++) {
                System.out.print("-");
            }
            System.out.print("| " + display + "% (" + i + "/" + (detections.size() - 1) + " frames)");

            ArrayList<Optional<Point>> frameDetections = detections.get(i);
            if (frameDetections.size() == 0) {
                continue;
            }
            // Create cost matrix for Hungarian Algorithm, where the cost is the distance
            // between the robot's current position and the detected position
            double[][] costMatrix = new double[amountShows][frameDetections.size()];
            for (int j = 0; j < amountShows; j++) {
                for (int k = 0; k < frameDetections.size(); k++) {
                    costMatrix[j][k] = robots[j].getPosition().distanceTo(frameDetections.get(k).get());
                }
            }

            int[] assignment = new HungarianAlgorithm(costMatrix).execute();

            // Update robot positions based on Hungarian Algorithm assignment
            for (int j = 0; j < assignment.length; j++) {
                if (assignment[j] != -1) {
                    robots[j].updatePosition(frameDetections.get(assignment[j]).get(), autoFrameIndices.contains(i));
                }
            }
        }

        System.out.println(" Done!");

        System.out.println("Analysis complete. Writing data to files...");
        for (FRCRobot robot : robots) {
            robot.writeData();
        }
        System.out.println("Wrote data to files. (took "
                + Math.round((System.currentTimeMillis() - time) / 100.0) / 10.0 + " seconds)");
    }

    public static ArrayList<ArrayList<Optional<Point>>> detect() {
        // Run detector, then read the output
        ArrayList<ArrayList<Optional<Point>>> allDetections = new ArrayList<>();

        ArrayList<Detection> detections = new ArrayList<>();
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get("temp/output.json")));
            JSONArray detectionsArray = new JSONArray(jsonContent);

            for (int i = 0; i < detectionsArray.length(); i++) {
                JSONObject detectionObject = detectionsArray.getJSONObject(i);
                Detection detection = new Detection(detectionObject.getDouble("x_min"),
                        detectionObject.getDouble("y_min"), detectionObject.getDouble("x_max"),
                        detectionObject.getDouble("y_max"), detectionObject.getString("class_name"),
                        detectionObject.getDouble("confidence"), detectionObject.getString("tracker_id"),
                        detectionObject.getInt("frame_id"), detectionObject.getInt("class_id"),
                        detectionObject.getInt("frame_width"), detectionObject.getInt("frame_height"));
                detections.add(detection);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        int prevFrame = 0;
        for (Detection det : detections) {
            if (det.getClassName().equals("Robot") || det.getClassName().equals("Auto")) {
                double centerX = (det.getXMin() + det.getXMax()) / 2.0;
                double centerY = (det.getYMin() + det.getYMax()) / 2.0;
                centerX = centerX / det.getFrameWidth();
                centerY = centerY / det.getFrameHeight();

                if (prevFrame != det.getFrameId()) {
                    ArrayList<Optional<Point>> frameDetections = new ArrayList<>();
                    if (det.getClassName().equals("Auto")) {
                        frameDetections.add(Optional.empty());

                    } else if (det.getClassName().equals("Robot")) {
                        Optional<Double> xCoord = estimateXcoord(new Point(centerX, centerY), TOP_LEFT, TOP_RIGHT,
                                BOTTOM_LEFT, BOTTOM_RIGHT, 10, 0, 1);
                        Optional<Double> yCoord = estimateYcoord(new Point(centerX, centerY), TOP_LEFT, TOP_RIGHT,
                                BOTTOM_LEFT, BOTTOM_RIGHT, 10, 0, 1);
                        if (xCoord.isPresent() && yCoord.isPresent()) {
                            frameDetections.add(Optional.of(new Point(xCoord.get(), yCoord.get())));
                        } else {
                            // Do nothing, cuz if the robot is outside the field, then we don't want to add
                            // it to the detections
                            // However, we still need to add an empty frame to allDetections to keep the
                            // indices correct, which is done below
                        }
                    }
                    allDetections.add(frameDetections);
                    prevFrame = det.getFrameId();
                } else {
                    ArrayList<Optional<Point>> frameDetections = allDetections.get(allDetections.size() - 1);
                    if (det.getClassName().equals("Auto")) {
                        frameDetections.add(Optional.empty());

                    } else if (det.getClassName().equals("Robot")) {
                        Optional<Double> xCoord = estimateXcoord(new Point(centerX, centerY), TOP_LEFT, TOP_RIGHT,
                                BOTTOM_LEFT, BOTTOM_RIGHT, 10, 0, 1);
                        Optional<Double> yCoord = estimateYcoord(new Point(centerX, centerY), TOP_LEFT, TOP_RIGHT,
                                BOTTOM_LEFT, BOTTOM_RIGHT, 10, 0, 1);
                        if (xCoord.isPresent() && yCoord.isPresent()) {
                            frameDetections.add(Optional.of(new Point(xCoord.get(), yCoord.get())));
                        } else {
                            // Do nothing, cuz if the robot is outside the field, then we don't want to add
                            // it to the detections
                        }
                    }
                }
            }

        }
        for (int i = 0; i < allDetections.size(); i++) {
            int nullIndex = allDetections.get(i).indexOf(Optional.empty());
            boolean autoDetected = false;
            while (nullIndex != -1) {
                allDetections.get(i).remove(nullIndex);
                nullIndex = allDetections.get(i).indexOf(Optional.empty());
                autoDetected = true;
            }
            if (autoDetected) {
                autoFrameIndices.add(i);
            } else {
                teleFrameIndices.add(i);
            }
        }
        return allDetections;
    }

    public static Optional<Double> estimateYcoord(Point robot, Point topLeft, Point topRight, Point bottomLeft,
            Point bottomRight, int iterations, double bound0, double bound1) {
        // TODO Estimates the robot's y-coord with pose estimation

        if (iterations == 0) {
            return Optional.of((bound0 + bound1) / 2);
        }

        // Create a line through topLeft and topRight
        Line lineTop = new Line(topLeft, topRight);
        if (!lineTop.isAbove(robot)) {
            return Optional.empty();
        }

        // Create a line through bottomLeft and bottomRight
        Line lineBottom = new Line(bottomLeft, bottomRight);
        if (lineBottom.isAbove(robot)) {
            return Optional.empty();
        }

        // Create a line through topLeft and bottomRight
        Line lineLeft = new Line(topLeft, bottomRight);

        // Create a line through bottomLeft and topRight
        Line lineRight = new Line(bottomLeft, topRight);

        // Find the lines' intersect
        Point intersect = lineLeft.intersection(lineRight);

        // Create a line with the lines' slopes averaged and passing through the
        // intersect point
        Line lineMid = new Line(intersect, (lineLeft.getSlope() + lineRight.getSlope()) / 2);

        Line sideLeft = new Line(topLeft, bottomLeft);
        Line sideRight = new Line(topRight, bottomRight);

        Point leftIntersect = lineMid.intersection(sideLeft);
        Point rightIntersect = lineMid.intersection(sideRight);

        // If point is above this line, then call recursively on the top half;
        if (!lineMid.isAbove(robot)) {
            return estimateYcoord(robot, topLeft, topRight, leftIntersect, rightIntersect, iterations - 1, bound0,
                    (bound0 + bound1) / 2);
        }

        // Otherwise, call recursively on the bottom half
        return estimateYcoord(robot, leftIntersect, rightIntersect, bottomLeft, bottomRight, iterations - 1,
                (bound0 + bound1) / 2, bound1);
    }

    public static Optional<Double> estimateXcoord(Point robot, Point topLeft, Point topRight, Point bottomLeft,
            Point bottomRight, int iterations, double bound0, double bound1) {
        Optional<Double> result = estimateYcoord(robot.invert(), topRight.invert(), bottomRight.invert(),
                topLeft.invert(), bottomLeft.invert(), iterations, bound0, bound1);
        if (result.isPresent()) {
            return Optional.of(1 - result.get().doubleValue());
        }
        return Optional.empty();
    }
    
    public void paint(Graphics g){
        super.paint(g);

        try {
            Visualization.drawImage(0, 0, Visualization.WIDTH/2, Visualization.HEIGHT/2, 0, ImageIO.read(new File("matches/cover.png")), g);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(5f));
        g2d.setColor(Color.GREEN);
        g2d.drawLine((int) (TOP_LEFT.getX() * Visualization.WIDTH/2), (int) (TOP_LEFT.getY() * Visualization.HEIGHT/2), (int) (TOP_RIGHT.getX() * Visualization.WIDTH/2), (int) (TOP_RIGHT.getY() * Visualization.HEIGHT/2));
        g2d.drawLine((int) (TOP_LEFT.getX() * Visualization.WIDTH/2), (int) (TOP_LEFT.getY() * Visualization.HEIGHT/2), (int) (BOTTOM_LEFT.getX() * Visualization.WIDTH/2), (int) (BOTTOM_LEFT.getY() * Visualization.HEIGHT/2));
        g2d.drawLine((int) (BOTTOM_LEFT.getX() * Visualization.WIDTH/2), (int) (BOTTOM_LEFT.getY() * Visualization.HEIGHT/2), (int) (BOTTOM_RIGHT.getX() * Visualization.WIDTH/2), (int) (BOTTOM_RIGHT.getY() * Visualization.HEIGHT/2));
        g2d.drawLine((int) (TOP_RIGHT.getX() * Visualization.WIDTH/2), (int) (TOP_RIGHT.getY() * Visualization.HEIGHT/2), (int) (BOTTOM_RIGHT.getX() * Visualization.WIDTH/2), (int) (BOTTOM_RIGHT.getY() * Visualization.HEIGHT/2));
        
        g.dispose();
        g2d.dispose();
    }
    
}
