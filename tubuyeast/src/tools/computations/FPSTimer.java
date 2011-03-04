package tools.computations;

import java.text.NumberFormat;

/**
 * Simple class for monitoring the FPS of a chunk of code.
 * 
 * @author piuze
 */
public class FPSTimer {
	private static NumberFormat formatter = NumberFormat.getNumberInstance();

	private long t1;
	private long t2;
	private long frameLast;
	private double fpsAvg;
	private String fps;

	/**
	 * The number of frames to wait before updating the timer.
	 */
	private int frameSkip;

	/**
	 * The number of frames over which the timer is averaged.
	 */
	private int avgSize;

	/**
	 * Create a new FPS timer with default settings.
	 */
	public FPSTimer() {
		setDefaults();
	}

	/**
	 * Create a new FPS timer.
	 * 
	 * @param skip
	 *            The number of frames to wait before updating the timer.
	 */
	public FPSTimer(int skip) {
		setDefaults();
		frameSkip = skip;
	}

	/**
	 * Set default values for this timer.
	 */
	public void setDefaults() {
		t1 = 0;
		t2 = 0;
		frameLast = 0;
		fpsAvg = 0;
		fps = "0";
		frameSkip = 1;
		avgSize = 10;
	}

	/**
	 * Call this before the block of code to monitor.
	 */
	public void start() {
		t1 = System.nanoTime();
	}

	/**
	 * Call this after the block of code to monitor.
	 */
	public void stop() {
		t2 = System.nanoTime();
		computeFPS();
	}

	/**
	 * To monitor a repeated event, a tick should be called just before it starts.
	 */
	public void tick() {
		stop();
		start();
	}

	/**
	 * Compute the FPS.
	 */
	private void computeFPS() {
		double sfp = (t2 - t1) * 1e-9;
		long cfps = (long) (1 / sfp);

		// Use the last n values
		double rate = 1.0 / avgSize;

		fpsAvg = ((1 - rate) * fpsAvg + rate * cfps);

		// Compute the average fps
		// but wait to reduce flickering
		if (t1 - frameLast > 1e9 / (frameSkip)) {
			formatter.setMinimumIntegerDigits(0);
			formatter.setMaximumIntegerDigits(4);
			formatter.setMinimumFractionDigits(0);
			formatter.setMaximumFractionDigits(0);
			fps = formatter.format(fpsAvg);

			frameLast = t1;
		}
	}

	@Override
	public String toString() {
		return fps + " fps";
	}
}
