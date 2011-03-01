package simulation;

import java.util.ArrayList;

import javax.vecmath.Point2d;

/**
 * @author piuze
 */
public class Constraints {

    static ArrayList<Boundary> createCircle(Point2d p, double size, double resolution) {
        
        ArrayList<Boundary> circle = new ArrayList<Boundary>();
        
        double dt = 2.0 * Math.PI / resolution;
        Point2d p1 = new Point2d(p);
        Point2d p2 = new Point2d(p);
        
        p1.x = p.x + size * Math.cos(0);
        p1.y = p.y + size * Math.sin(0);
        for (double t = dt; t < 2*Math.PI + dt; t += dt) {
            p2.x = p.x + size * Math.cos(t);
            p2.y = p.y + size * Math.sin(t);
            circle.add(new Boundary(p1, p2));
            
            p1.set(p2);
        }
        
        return circle;
    }

    static ArrayList<Boundary> createBox(Point2d p1, Point2d p2, Point2d p3, Point2d p4) {
        
        ArrayList<Boundary> wall = new ArrayList<Boundary>();
 
        // Left wall
        wall.add(new Boundary(p1, p2));

        // Top wall
        wall.add(new Boundary(p2, p3));

        // Right wall
        wall.add(new Boundary(p4, p3));

        // Bottom wall
        wall.add(new Boundary(p1, p4));
        
        return wall;
    }

    static ArrayList<Boundary> createSquare(Point2d p1, double size) {
        
        ArrayList<Boundary> wall = new ArrayList<Boundary>();
 
        Point2d p2 = new Point2d(p1.x, p1.y - size);
        Point2d p3 = new Point2d(p1.x + size, p1.y - size);
        Point2d p4 = new Point2d(p1.x + size, p1.y);

        
        // Left wall
        wall.add(new Boundary(p1, p2));

        // Top wall
        wall.add(new Boundary(p2, p3));

        // Right wall
        wall.add(new Boundary(p4, p3));

        // Bottom wall
        wall.add(new Boundary(p1, p4));
        
        return wall;
    }
    
    
}
