package numerical;

import java.util.List;

import simulation.Particle;

public class BoxCollider {

    // Offset to the boundary
    public static int WDX = 10;
    public static int WDY = 10;

	public static void collide(double boxwidth, double boxheight, double rcv, List<Particle> particles) {
        for ( Particle p : particles ) {
            
            // If the particle is pinned, do not change its position/velocity
            if (!p.pinned) {
                // Check if the new position lies outside of the window
                if (p.p.x <= 0 + WDX) {
                    p.p.x = WDX;
                    p.v.x = p.v.x < 0 ? -p.v.x * rcv : p.v.x;
                }
                
                if (p.p.x >= boxwidth - WDX) {
                    p.p.x = boxwidth - WDX;
                    p.v.x = p.v.x > 0 ? -p.v.x * rcv: p.v.x;
                }
                
                if (p.p.y <= WDY) {
                    p.p.y = WDY;
                    p.v.y = p.v.y < 0 ? -p.v.y * rcv : p.v.y;
                }

                if (p.p.y >= boxheight - WDY) {
                    p.p.y = boxheight - WDY;
                    p.v.y = p.v.y > 0 ? -p.v.y * rcv : p.v.y;
                }
                

            }            
        }
	}
	
}
