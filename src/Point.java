public class Point {
    private final double x;
    private final double y;
    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public Point invert(){
        return new Point(y, -x);
    }
    public double distanceTo(Point other){
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }
    public String toString(){
        return "(" + x + ", " + y + ")";
    }
}
