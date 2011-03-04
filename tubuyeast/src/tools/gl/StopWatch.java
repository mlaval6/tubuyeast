/*
 * Created on Jan 14, 2005
 */
package tools.gl;

import java.text.NumberFormat;

/**
 * A StopWatch object for simple profiling, based on System.nanoTime()
 * @author Shinjiro Sueda
 */
public class StopWatch
{
    private String name;
    private String comment;
    private long t0;
    private int n;
    private long total;
    private long avg;
    double alpha = 0.1;
    
    private static int maxNameLen = 0;
    private static int num = 0;
    private static NumberFormat formatter;
    
    static
    {
        formatter = NumberFormat.getIntegerInstance();

        // Call itself once to get it into memory.
        StopWatch tmp = new StopWatch();
        tmp.start();
        tmp.stop();
    }
    
    /**
     * Creates a new stopwatch with the given name
     * @param name
     */
    public StopWatch(String name)
    {
        this.name = name;
        comment = "";
        maxNameLen = Math.max(maxNameLen, name.length());
    }
    
    /**
     * Creates a stopwatch with an automatically generated name
     */
    public StopWatch()
    {
        this("SW" + num);
        ++num;
    }
    
    /**
     * Starts the StopWatch
     */
    public void start()
    {
        t0 = System.nanoTime() / 1000;
    }
    
    /**
     * Stops the StopWatch
     */
    public void stop()
    {
        long t1 = System.nanoTime() / 1000;
        long diff = t1 - t0;
        total += diff;
        avg = (long)((1 - alpha) * avg + alpha * diff);
        ++n;
    }
    
    /** 
     * @return the name of this StopWatch
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Sets a comment for this StopWatch
     * @param comment
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }
    
    @Override
    public String toString()
    {
        StringBuilder nameStr = new StringBuilder(name);
        for(int i = name.length(); i < maxNameLen; ++i)
        {
            char ch = (i % 2 == 0 ? '-' : ' ');
            nameStr.append(ch);
        }
        formatter.setMinimumIntegerDigits(6);
        formatter.setMaximumIntegerDigits(6);
        String nStr = formatter.format(n);
        formatter.setMinimumIntegerDigits(11);
        formatter.setMaximumIntegerDigits(11);
        String totalStr = formatter.format(total);
        formatter.setMinimumIntegerDigits(7);
        formatter.setMaximumIntegerDigits(7);
        String avgStr = formatter.format(avg);
        return
            nameStr + " | " + nStr + "  " +
            totalStr + "  " + avgStr + " | " + comment;
    }

    public String toShortString()
    {
        StringBuilder nameStr = new StringBuilder(name);
        nameStr.append(" -");
        formatter.setMinimumIntegerDigits(7);
        formatter.setMaximumIntegerDigits(10);
        String avgStr = formatter.format(avg*1000);
        return
            nameStr + " |" + "  " +
            avgStr + " ms |" + comment;
    }

    /**
     * Tests the StopWatch
     * @param args
     */
    public static void main(String[] args)
    {
        StopWatch sw = new StopWatch();
        sw.start();
        try
        {
            Thread.sleep(10);
        }
        catch(InterruptedException x)
        {
            // do nothing
        }
        sw.stop();
        System.out.println(sw);
    }
}
