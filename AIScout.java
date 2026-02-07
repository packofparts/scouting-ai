import java.util.Optional;

public class AIScout {
    private static int autoFrameIndex = -1;
    private static int teleFrameIndex = -1;
    //team numbers in memoriam
    private static final String RED1 = "1844";
    private static final String RED2 = "2907";
    private static final String RED3 = "3588";
    private static final String BLUE1 = "4918";
    private static final String BLUE2 = "5827";
    private static final String BLUE3 = "10498";
    
    //Relative locations of field joints (i.e. 0.5 is half of the screen)
    //All x vals must differ to avoid errors in pose estimation
    private static final Point TOP_LEFT = new Point(0.1, 0.1);
    private static final Point BOTTOM_LEFT = new Point(0, 0.85);
    private static final Point TOP_RIGHT = new Point(0.67, 0.1);
    private static final Point BOTTOM_RIGHT = new Point(0.9, 0.89);

    public static void main(String[] args) {
        Point[][] detections = detect();
        FRCRobot[] robots = {};
        //find the first frame with 6 robots detected
        //Insert 6 FRCRobot objects into robot with corresponding team numbers and positions

        //For every frame, assign robots new positions using Hungarian Algorithm and writes data to robot files

    }

    public static Point[][] detect(){
        //TODO run detector, read the output, and save robot detections as a list of list of position-estimated points.  

        //TODO set autoFrameIndex to the first frame where auto appears
        //TODO set teleFrameIndex to the first frame where teleop appears AFTER autoFrameIndex
        return null;
    }

    public static Optional<Double> estimateYcoord(Point robot, Point topLeft, Point topRight, Point bottomLeft, Point bottomRight, int iterations, double bound0, double bound1){
        //TODO Estimates the robot's y-coord with pose estimation


        if (iterations == 0) {
            return Optional.of((bound0 + bound1) / 2);
        }

        //Create a line through topLeft and topRight
        Line lineTop = new Line(topLeft, topRight);
        if (!lineTop.isAbove(robot)) {
            return Optional.empty();
        }

        //Create a line through bottomLeft and bottomRight
        Line lineBottom = new Line(bottomLeft, bottomRight);
        if (lineBottom.isAbove(robot)) {
            return Optional.empty();
        }

        //Create a line through topLeft and bottomRight
        Line lineLeft = new Line(topLeft, bottomRight);

        //Create a line through bottomLeft and topRight
        Line lineRight = new Line(bottomLeft, topRight);

        //Find the lines' intersect
        Point intersect = lineLeft.intersection(lineRight);

        //Create a line with the lines' slopes averaged and passing through the intersect point
        Line lineMid = new Line(intersect, (lineLeft.getSlope() + lineRight.getSlope()) / 2);

        Line sideLeft = new Line(topLeft,bottomLeft);
        Line sideRight = new Line(topRight, bottomRight);

        Point leftIntersect = lineMid.intersection(sideLeft);
        Point rightIntersect = lineMid.intersection(sideRight);
       
        //If point is above this line, then call recursively on the top half;
        if (!lineMid.isAbove(robot)) {
            return estimateYcoord(robot, topLeft, topRight, leftIntersect, rightIntersect, iterations - 1, bound0, (bound0 + bound1) / 2);
        }

        //Otherwise, call recursively on the bottom half
        return estimateYcoord(robot, leftIntersect, rightIntersect, bottomLeft, bottomRight, iterations - 1, (bound0 + bound1) / 2, bound1);
    }

    public static Optional<Double> estimateXcoord(Point robot, Point topLeft, Point topRight, Point bottomLeft, Point bottomRight, int iterations, double bound0, double bound1){
        Optional<Double> result = estimateYcoord(robot.invert(), topRight.invert(), bottomRight.invert(), topLeft.invert(), bottomLeft.invert(), iterations, bound0, bound1);
        if (result.isPresent()) {
            return Optional.of(1 - result.get().doubleValue());
        }
        return Optional.empty();
    }
    
}
