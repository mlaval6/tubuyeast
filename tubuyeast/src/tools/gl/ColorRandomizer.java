package tools.gl;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;

import tools.parameters.BooleanParameter;

public class ColorRandomizer {
    private int color_buffer = 1000;
    private List<double[]> colors = new LinkedList<double[]>();
    private double color_min;
    private Random rand = new Random();
    private double[] default_color = new double[] { 0, 0, 0};
    public ColorRandomizer() {
        color_min = 0.1;
        
        update(color_buffer);
    }
    
    /**
     * Force the color list to contain at least n elements.
     * @param n
     */
    public void update(int n) {
//    	System.out.println("Updating colors from " + colors.size() + " to " + n);
        for (int i = colors.size(); i <= n; i++) {
            double[] color = new double[] { color_min + rand.nextDouble(),
                    color_min + rand.nextDouble(), color_min + rand.nextDouble() };
            colors.add(color);

        }
    }
    
    private static int COLOR_LIMIT = 100000;
    /**
     * Fetch a color for this index. If the color list has not enough elements,
     * it is expanded by 1+10%.
     * @param i
     * @return
     */
    public double[] get(int i) {
        if (i >= COLOR_LIMIT) {
        	return default_color;
        }
        else if (i >= colors.size()){
        	update(i);
        }

        return colors.get(i);
    }
    
    public void set(int i, double[] c) {
        colors.set(i, c);
    }
    
    public void clear() {
        colors.clear();
        update(color_buffer);
    }
}
