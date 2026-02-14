public class Detection {
    protected double xMin;
    protected double yMin;
    protected double xMax;
    protected double yMax;
    protected String className;
    protected double confidence;
    protected String trackerId;
    protected int frameWidth;
    protected int frameHeight;
    protected int frameId;
    protected int classId;
    

    public Detection(double xMin, double yMin, double xMax, double yMax, String className, double confidence, String trackerId, int frameId, int classId, int frameWidth, int frameHeight){
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.className = className;
        this.confidence = confidence;
        this.trackerId = trackerId;
        this.frameId = frameId;
        this.classId = classId;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }
    public double getXMin(){
        return xMin;
    }
    public double getYMin(){
        return yMin;
    }
    public double getXMax(){
        return xMax;
    }
    public double getYMax(){
        return yMax;
    }
    public String getClassName(){
        return className;
    }
    public double getConfidence(){
        return confidence;
    }
    public String getTrackerId(){
        return trackerId;
    }
    public int getFrameId(){
        return frameId;
    }
    public int getClassId(){
        return classId;
    }
    public int getFrameWidth(){
        return frameWidth;
    }
    public int getFrameHeight() {
        return frameHeight;
    }

    public void setXMin(double xMin){
        this.xMin = xMin;
    }
    public void setYMin(double yMin){
        this.yMin = yMin;
    }
    public void setXMax(double xMax){
        this.xMax = xMax;
    }
    public void setYMax(double yMax){
        this.yMax = yMax;
    }
    public void setClass_name(String className){
        this.className = className;
    }
    public void setConfidence(double confidence){
        this.confidence = confidence;
    }
    public void setTrackerId(String trackerId){
        this.trackerId = trackerId;
    }
    public void setFrameId(int frameId){
        this.frameId = frameId;
    }
    public void setClassId(int classId){
        this.classId = classId;
    }
    public void setFrameHeight(int frameHeight){
        this.frameHeight = frameHeight;
    }
    public void setFrameWidth(int frameWidth){
        this.frameWidth = frameWidth;
    }

    
}
