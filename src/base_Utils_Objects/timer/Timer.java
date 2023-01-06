package base_Utils_Objects.timer;

import java.time.Instant;

/**
 * This class will provide timer/stop-watch functionality, by tracking an initial time and providing passed time data.
 * It is in turn owned and managed by a timeMgr
 * @author John Turner
 *
 */
public class Timer {
	/**
	 * Absolute start time in millis marking this timer's start (milliseconds since 1/1/1970)
	 */
	private final long startTime;
	
	public Timer(long _startTime) {	startTime = _startTime;}
	
	/**
	 * returns a positive int value in millis of current time elapsed since timer started
	 * @return
	 */
	public long getTimeElapsedInMillis() {	return getTimeElapsedInMillis(Instant.now());}
	
	/**
	 * Get string representation of time elapsed in this timer
	 * @return
	 */
	public String getTimeElapsedString() { return getTimeStrFromPassedMillis(getTimeElapsedInMillis());}
	
	/**
	 * returns a positive int value in millis of passed now's time elapsed since timer started.
	 * @param now value to check elapsed time against
	 * @return
	 */
	public long getTimeElapsedInMillis(Instant now) {	return now.toEpochMilli() - startTime;}
	
	/**
	 * Get string representation of time elapsed in this timer
	 * @return
	 */
	public String getTimeElapsedString(Instant now) { return getTimeStrFromPassedMillis(getTimeElapsedInMillis(now));}

	/**
	 * get a decent display of milliseconds elapsed
	 * @param msElapsed millis to convert to time string format
	 * @return
	 */
	private String getTimeStrFromPassedMillis(long msElapsed) {
		long ms = msElapsed % 1000, sec = (msElapsed / 1000) % 60, min = (msElapsed / 60000) % 60, hr = (msElapsed / 3600000) % 24;	
		String res = String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
		return res;
	}//getTimeStrFromPassedMillis
	
	/**
	 * Return this timer's start time in millis (milliseconds since 1/1/1970)
	 * @return
	 */
	public long getStartTime() {return startTime;}
	
}//class myTimer 
