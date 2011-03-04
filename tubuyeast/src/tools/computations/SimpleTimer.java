package tools.computations;

import java.text.NumberFormat;

public class SimpleTimer {
    private long t0 = 0;

    private long t1 = 0;

    private double delta;

    /**
     * Formatter, in seconds
     */
    private final NumberFormat sformatter = NumberFormat.getNumberInstance();

    {
        sformatter.setMinimumIntegerDigits(0);
        sformatter.setMaximumIntegerDigits(6);
        sformatter.setMinimumFractionDigits(2);
        sformatter.setMaximumFractionDigits(0);
    }

    /**
     * Formatter, in milliseconds
     */
    private final NumberFormat msformatter = NumberFormat.getNumberInstance();

    {
    	msformatter.setMinimumIntegerDigits(0);
    	msformatter.setMaximumIntegerDigits(6);
    	msformatter.setMinimumFractionDigits(4);
    	msformatter.setMaximumFractionDigits(4);
    }
    
    /**
     * @return a string containing the number of seconds since the last tick  and reset the clock.
     */
    public String tick_s() {
        return sformatter.format(tick()) + "s";
    }

    /**
     * @return a string containing the number of milliseconds since the last tick  and reset the clock.
     */
    public String tick_ms() {
        return msformatter.format(tick()*1000) + "ms";
    }

    /**
     * @return the number of seconds since the last tick and reset the clock.
     */
    public double tick() {
        t1 = System.nanoTime();
        delta = (t1 - t0) * 1e-9;

        t0 = t1;

        return delta;
    }

    /**
     * @return observe the number of seconds since the last tick but don't reset the clock.
     */
    public double observeTick() {
        t1 = System.nanoTime();
        delta = (t1 - t0) * 1e-9;
        return delta;
    }
}
