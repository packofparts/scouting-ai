public class Point {
    private final double x;
    private final double y;
    private final double time;
    public Point(double x, double y, double time){
        this.x = x;
        this.y = y;
        this.time = time;
    }
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public double getTime(){
        return time;
    }
    public Point invert(){
        return new Point(y, -x, time);
    }
    public double distanceTo(Point other){
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow((this.y - other.y) / 2, 2)); //Y is divided by 2 to account for the fact that 1 units change in y corresponds to a change of 0.5 units in x, so we need to divide the difference in y-coordinates by 2 before squaring it and adding it to the squared difference in x-coordinates.
    }
    public String toString(){
        return "(" + x + ", " + y + ")";
    }
}
