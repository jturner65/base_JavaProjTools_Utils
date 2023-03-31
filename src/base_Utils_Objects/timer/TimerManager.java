package base_Utils_Objects.timer;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.format.*;
import java.util.HashMap;
import java.util.Locale;

/**
 * Singleton that manages timers and date-time processes.
 * @author John Turner
 *
 */
public class TimerManager {
	
	private static TimerManager timeMgr;
	
	/**
	 * timer measuring from program start
	 */
	private Timer progStartTimer;
	
	/**
	 * Key denoting current timer - use as a shortcut to avoid having to name and track names of multiple timers
	 */
	private final String curTimerKey = "curTimer";

	/**
	 * This map holds timers that each hold the start time in millis for a particular named process
	 */
	private HashMap<String, Timer> processTimers;
	private Object procTimerLock = new Object();
	
	/**
	 * formatter for Date output
	 */
	private DateTimeFormatter preMadeDateFormatter;
	private Object dateFmtLock = new Object();
	private char curDateToken = '?';
	
	/**
	 * formatter for Time output
	 */
	private DateTimeFormatter preMadeTimeFormatter;
	private Object timeFmtLock = new Object();
	private char curTimeToken = '?';
	
	private TimerManager(long _initialRunTime) {
		progStartTimer = new Timer(_initialRunTime);
		processTimers = new HashMap<String,Timer>();
		setTimerStart(curTimerKey, _initialRunTime);
		buildDateFormatter('-');
		buildTimeFormatter(':');
	}
	
	public static TimerManager getInstance() {
		if (timeMgr == null) {
			timeMgr = new TimerManager(Instant.now().toEpochMilli());
		}
		return timeMgr;
	}//singleton factory
	
	///////////////////////////
	// timer functions
	
	/**
	 * set now as start time for current process timer. Overwrites previously specified current process
	 */
	public void setCurTimerStart() {setTimerStart(curTimerKey, Instant.now().toEpochMilli());}
	
	/**
	 * set start time for current process timer. Overwrites previously specified current process
	 * @param startTime time in millis to use for current process
	 */
	public void setCurTimerStart(long startTime) {setTimerStart(curTimerKey, startTime);}
	
	/**
	 * Build and save a named timer, using now as the start time.
	 * @param processName the name to use for this timer
	 */
	public void setTimerStart(String timerName) {setTimerStart(timerName, Instant.now().toEpochMilli());}
	
	/**
	 * Build and save a named timer.  If a timer exists with this name it will be overwritten
	 * @param processName the name to use to reference the process
	 * @param startTime time in millis from epoch to use as start of this timer
	 */
	public void setTimerStart(String timerName, long startTime) {
		synchronized(procTimerLock) {processTimers.put(timerName, new Timer(startTime));}
	}
	
	private String _getErrorMessagePrefix(String funcName) {
		return "!!!!!"+getDateTimeString()+ " | " + getTimeStrFromProgStart() + " | myTimeMgr::" + funcName +" :";
	}
	
	/**
	 * Get time elapsed in millis for current active timer
	 * @return
	 */
	public long getElapsedTimeInMillisForCurTimer() {return getElapsedTimeInMillisForTimer(curTimerKey);}
	
	/**
	 * Get time in millis for named timer
	 * @param timerName
	 * @return
	 */
	public long getElapsedTimeInMillisForTimer(String timerName) {
		synchronized(procTimerLock) {
			Timer timer = processTimers.get(timerName);
			if(timer != null) {		return timer.getTimeElapsedInMillis();} 
		}
		System.out.println(_getErrorMessagePrefix("getElapsedTimeInMillisForTimer") +
				"Error!! No Timer found with passed name " + timerName);
		return Long.MAX_VALUE;	
			
	}//getTimeElapsedInMillisForTimer
	
	/**
	 * Return a formatted string of the time elapsed for the current active timer 
	 * @return
	 */
	public String getElapsedTimeStrForCurTimer() {return getElapsedTimeStrForTimer(curTimerKey);}
	
	/**
	 * Return a formatted string of the time elapsed for the named timer
	 * @param timerName
	 * @return
	 */
	public String getElapsedTimeStrForTimer(String timerName) {
		synchronized(procTimerLock) {
			Timer timer = processTimers.get(timerName);
			if(timer != null) {		return timer.getTimeElapsedString();} 
		}
		System.out.println(_getErrorMessagePrefix("getElapsedTimeStrForTimer") +
				"Error!! No Timer found with passed name " + timerName+".");
		return getTimeStrFromPassedMillis(Long.MAX_VALUE);
	}//getElapsedTimeStrForTimer
	
	
	//////////////////////////
	// time functions
	
	/**
	 * get time from "start time" (initialRunTime)
	 * @return
	 */
	public long getMillisFromProgStart() {	
		//milliseconds since 1/1/1970, subtracting when mapmgr was built to keep millis low		
		return progStartTimer.getTimeElapsedInMillis();
	}//getCurTime() 
	
	/**
	 * Get nicely formatted string of millis ellapsed since program start
	 * @return
	 */
	public String getTimeStrFromProgStart() {return progStartTimer.getTimeElapsedString();}
	/**
	 * get a decent display of passed milliseconds elapsed
	 * @param msElapsed millis to convert to time string format
	 * @return
	 */
	public String getTimeStrFromPassedMillis(long msElapsed) {
		long ms = msElapsed % 1000, sec = (msElapsed / 1000) % 60, min = (msElapsed / 60000) % 60, hr = (msElapsed / 3600000) % 24;	
		String res = String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
		return res;
	}//getTimeStrFromPassedMillis	
	
	public String getCurrWallTime() {	return getDateTimeString();	}
	
	/**
	 * Build a date-time string with default delimiter tokens
	 * @return
	 */
	public String getDateTimeString(){
		Instant now = Instant.now();
		String result;
		result = getDateString(now, curDateToken);
		result += "|";
		result += getTimeString(now, curTimeToken);
		return result;
	}
	
	/**
	 * Get a filename-safe date-time string version of now time
	 */
	public String getDateTimeStringForFileName() {
		Instant now = Instant.now();
		String result = getDateString(now, '-');
		result += "_";
		result += getTimeString(now, '-');
		return result;
	}//getDateTimeStringForFilename

	/**
	 * Build a date-time string with given date and time delimiter tokens
	 * @param dateToken token to delimit date fields
	 * @param timeToken token to delimit time fields
	 * @return
	 */
	public String getDateTimeString(char dateToken, char timeToken){
		Instant now = Instant.now();
		String result = getDateString(now, dateToken);
		result += getTimeString(now, timeToken);
		return result;
	}
	//utilities
	public String getDateString(){return getDateString(Instant.now(), curDateToken);}
	public String getDateString(char token){ return getDateString(Instant.now(), token);}
	public String getDateString(Instant now, char token){
		String result;
		synchronized(dateFmtLock) {
			buildDateFormatter(token);
			result = preMadeDateFormatter.format(now);
		}
		return result;
	}//getDateString
	
	public String getTimeString(){return getTimeString(Instant.now(), curTimeToken);}
	public String getTimeString(char token){ return getTimeString(Instant.now(), token);}
	public String getTimeString(Instant now, char token){
		String result;
		synchronized(timeFmtLock) {
			buildTimeFormatter(token);
			result  = preMadeTimeFormatter.format(now);
		}
		return result;
	}//getDateString
	

	public String getWallTimeAndTimeFromStart(String delim) {	return getCurrWallTime() + delim + getTimeStrFromProgStart();}

	//////////////////////////
	// Date and time formatter creation
	
	/**
	 * Use this to specify a date formatter
	 * @param dateToken delimiter between date val elements
	 */
	private void buildDateFormatter(char dateToken) {
		if(dateToken == curDateToken) {return;}
		curDateToken = dateToken;
		preMadeDateFormatter = new DateTimeFormatterBuilder()
				.appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .appendLiteral(curDateToken)
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendLiteral(curDateToken)
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .toFormatter(Locale.getDefault(Locale.Category.FORMAT)).withZone( ZoneId.systemDefault());		
	}
	
	/**
	 * Use this to specify a time formatter
	 * @param timeToken delimiter between time val elements
	 */
	private void buildTimeFormatter(char timeToken) {
		if(timeToken == curTimeToken) {return;}
		curTimeToken = timeToken;
		preMadeTimeFormatter = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendLiteral(curTimeToken)
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(curTimeToken)
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .optionalStart()
                .appendFraction(ChronoField.NANO_OF_SECOND, 7, 7, true)
                .toFormatter(Locale.getDefault(Locale.Category.FORMAT)).withZone( ZoneId.systemDefault() );		
	}
	
	
}//myTimeMgr
