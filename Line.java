public class Line {
    //Linear line in y = mx + b format
    private final double m = 0; //Slope
    private final double b = 0; //Y-intercept
    
    public Line(double m, double b){
        //TODO initialize instance variables
        //This is the slope-intercept constructor
    }

    public Line(Point p1, double m){
        //TODO initialize instance variables
        //This is the point-slope constructor
    }

    public Line(Point p1, Point p2){
        //TODO initialize instance variables
        //This is the point-point constructor
    }
    
    public double getSlope(){
        return m;
    }
    public double getYIntercept(){
        return b;
    }


    public double getY(double X){
        //TODO return the corresponding y-coord when given x-coord
        return 0;
    }

    public double getX(double Y){
        //TODO return the corresponding x-coord when given y-coord
        return 0;
    }

    public boolean isAbove(Point p){
        //TODO if point is on or above the line, return true, otherwise false
        return false;
    }

    public Point intersection(Line l){
        //TODO returns the point of intersection between the this line and line l.
        //TODO return null if there are infinitely many points or if there is no such point.
        return null;
    }

}
