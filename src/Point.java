//reference: https://dev.to/realedwintorres/a-simple-2d-point-class-in-java-19p3
public class Point {

    private double x, y;

    public Point(double x1, double y1){
        x = x1;
        y = y1;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
