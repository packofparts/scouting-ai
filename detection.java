public class Detection {
    protected double x_min;
    protected double y_min;
    protected double x_max;
    protected double y_max;
    protected String class_name;
    protected double confidence;
    protected String tracker_id;
    protected int frame_id;
    protected int class_id;

    public Detection(){

    }
    public double getX_min(){
        return x_min;
    }
    public double getY_min(){
        return y_min;
    }
    public double getX_max(){
        return x_max;
    }
    public double getY_max(){
        return y_max;
    }
    public String getClass_name(){
        return class_name;
    }
    public double getConfidence(){
        return confidence;
    }
    public String getTracker_id(){
        return tracker_id;
    }
    public int getFrame_id(){
        return frame_id;
    }
    public int getClass_id(){
        return class_id;
    }

    public void setX_min(double x_min){
        this.x_min = x_min;
    }
    public void setY_min(double y_min){
        this.y_min = y_min;
    }
    public void setX_max(double x_max){
        this.x_max = x_max;
    }
    public void setY_max(double y_max){
        this.y_max = y_max;
    }
    public void setClass_name(String class_name){
        this.class_name = class_name;
    }
    public void setConfidence(double confidence){
        this.confidence = confidence;
    }
    public void setTracker_id(String tracker_id){
        this.tracker_id = tracker_id;
    }
    public void setFrame_id(int frame_id){
        this.frame_id = frame_id;
    }
    public void setClass_id(int class_id){
        this.class_id = class_id;
    }

    
}
