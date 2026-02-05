import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final Point TOP_LEFT = new Point(0, 0);
    private static final Point BOTTOM_LEFT = new Point(0, 0);
    private static final Point TOP_RIGHT = new Point(0, 0);
    private static final Point BOTTOM_RIGHT = new Point(0, 0);

    public static void main(String[] args) {
        Point[][] detections = detect();
        FRCRobot[] robots = {};

        //find the first frame with 6 robots detected
        //Insert 6 FRCRobot objects into robot with corresponding team numbers and positions

        //For every frame, assign robots new positions using Hungarian Algorithm and writes data to robot files


    }

    public static Point[][] detect(){
        ObjectMapper mapper = new ObjectMapper();
        // Run detector, then read the output
        File file = new File("temp/output.json");
        ArrayList<ArrayList<Point>> allDetections = new ArrayList<>();
        try {
            List<detection> detections = mapper.readValue(file, new TypeReference<List<detection>>(){});
            int prevFrame = -1;
            for (detection det : detections) {
                if(det.getClass_name().equals("Robot")){
                    double centerX = (det.getX_min() + det.getX_max()) / 2.0;
                    double centerY = (det.getY_min() + det.getY_max()) / 2.0;
                    centerX = centerX / 1280.0;
                    centerY = centerY / 720.0;
                    
                    if(prevFrame != det.getFrame_id()){
                        ArrayList<Point> frameDetections = new ArrayList<>();
                        frameDetections.add(new Point(centerX, centerY));
                        allDetections.add(frameDetections);
                        prevFrame = det.getFrame_id();
                    } else {
                        allDetections.get(allDetections.size() - 1).add(new Point(centerX, centerY));
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Runs the Python detector script to produce temp/output.json. */
    


    
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
