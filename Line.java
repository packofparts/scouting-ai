public class Line {
    //Linear line in y = mx + b format
    private final double m; //Slope
    private final double b; //Y-intercept
    
    public Line(double m, double b){
        this.m = m;
        this.b = b;
        //TODO initialize instance variables
        //This is the slope-intercept constructor
    }

    public Line(Point p1, double m){
        this.m = m;
        this.b = p1.getY() - m * p1.getX();
        //TODO initialize instance variables
        //This is the point-slope constructor
    }

    public Line(Point p1, Point p2){
        if(p1.getX() == p2.getX()){
            this.m = (p2.getY() - p1.getY()) / ((p2.getX()+0.00000000001) - p1.getX());

        }else{this.m = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());}
        this.b = p1.getY() - m * p1.getX();
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
        return m * X + b;
    }

    public double getX(double Y){
        //TODO return the corresponding x-coord when given y-coord
        return (Y - b) / m;
    }

    public boolean isAbove(Point p){
        //TODO if point is on or above the line, return true, otherwise false
        return p.getY() >= getY(p.getX());
    }

    public Point intersection(Line l){
        //TODO returns the point of intersection between the this line and line l.
        //TODO return null if there are infinitely many points or if there is no such point.
        if (m == l.m){
            return null;
        }
        double x = (l.b - b) / (m - l.m);
        double y = getY(x);
        return new Point(x, y);
    }

}
