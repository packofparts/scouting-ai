import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FRCRobot {
    private Point pos; //Position of the robot
    private String team; //Robot's team number
    private List<String> positionHistory; //Stores position data for CSV output
    /** True if this robot began the match on the red alliance side of the field (per AIScout.RED_ON_LEFT). */
    private final boolean startedOnRedAlliance;

    public FRCRobot(Point pos, String team, boolean startedOnRedAlliance) {
        this.pos = pos;
        this.team = team;
        this.startedOnRedAlliance = startedOnRedAlliance;
        this.positionHistory = new ArrayList<>();
        updatePosition(pos, true);
    }

    public void updatePosition(Point pos, boolean isAuto){//isAuto is true for auto, false for teleop
        this.pos = pos;
        if (AIScout.RED_ON_LEFT) {
            positionHistory.add(isAuto + "," + (1-pos.getX()) + "," + (1-pos.getY()) + "," + startedOnRedAlliance);
        } else {
            positionHistory.add(isAuto + "," + pos.getX() + "," + pos.getY() + "," + startedOnRedAlliance);
        }
    }
    
    public void writeData(){ 
    // true for auto, false for teleop
    File dataDir = new File("data");
    if (!dataDir.exists()) {
        dataDir.mkdirs();
    }
    File csvFile = new File(dataDir, team + ".csv");
    try {
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile, true))) {
            writer.println("---------------------------------------");
            for (String dataLine : positionHistory) {
                writer.println(dataLine);
            }
        }
    } catch (IOException e) {
        System.err.println("Error for this team: " + team + ": " + e.getMessage());
        e.printStackTrace();
    }
}

    
    public Point getPosition(){
        return pos;
    }
}
