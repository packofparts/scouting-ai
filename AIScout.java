public class AIScout {
    private int autoFrameIndex = -1;
    private int teleFrameIndex = -1;
    //team numbers in memoriam
    private String red1Team = "1844";
    private String red2Team = "2907";
    private String red3Team = "3588";
    private String blue1Team = "4918";
    private String blue2Team = "5827";
    private String blue3Team = "10498";
    
    //Relative locations of field joints (i.e. 0.5 is half of the screen)
    private Point topLeft = new Point(0, 0);
    private Point bottomLeft = new Point(0, 0);
    private Point topRight = new Point(0, 0);
    private Point bottomRight = new Point(0, 0);

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

    public static double estimateYcoord(double robotYcoord, Point topLeft, Point topRight, Point bottomLeft, Point bottomRight, int iterations, double bound0, double bound1){
        //TODO Estimates the robot's y-coord with pose estimation

        //If iterations == 0 return the average of bound0 and bound1

        //Create a line through topLeft and topRight

            //If the point is above this line, then the robot is outside of bounds and return -1

        //Create a line through bottomLeft and bottomRight

            //If the point is below this line, then the robot is outside of bounds and return -1

        //Create a line through topLeft and bottomRight

        //Create a line through bottomLeft and topRight

        //Find the lines' intersect

        //Create a line with the lines' slopes averaged and passing through the intersect point

       
        //If point is above this line, then call recursively on the top half;

        //Otherwise, call recursively on the bottom half
        return 0;
    }

    public static double estimateXcoord(double robotXcoord, Point topLeft, Point topRight, Point bottomLeft, Point bottomRight, int iterations, double bound0, double bound1){
        return estimateYcoord(robotXcoord, topRight, bottomRight, topLeft, bottomLeft, iterations, bound0, bound1);
    }
    
}
