public class FRCRobot {
    private Point pos; //Position of the robot
    private String team; //Robot's team number
    public FRCRobot(Point pos, String team){
        this.pos = pos;
        this.team = team;
    }
    public void updatePosition(Point pos, boolean gameState){//gameState is true for auto, false for teleop
        this.pos = pos;
    }
    public void writeData(){ 
        //TODO write robot position data
    }
    public Point getPosition(){
        return pos;
    }
}
