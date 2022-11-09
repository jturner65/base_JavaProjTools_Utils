package base_Utils_Objects.tools;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.format.*;
import java.util.Locale;

public class myTimeMgr {

	/**
	 * time of current process start, from initial construction of mapmgr - TODO use this to monitor specific process time elapsed.  set to 0 at beginning of a particular process, then measure time elapsed in process
	 */
	protected long curProcStartTime = 0;
	/**
	 * time prog initially run/built, in millis - used as offset for instant to provide smaller values for timestamp
	 */
	protected final long initialRunTime;
	
	/**
	 * formatter for Date output
	 */
	private DateTimeFormatter preMadeDateFormatter;
	private char curDateToken = '?';
	/**
	 * formatter for Time output
	 */
	private DateTimeFormatter preMadeTimeFormatter;
	private char curTimeToken = '?';
	
	public myTimeMgr() {
		this(Instant.now().toEpochMilli());
	}
	public myTimeMgr(long _initialRunTime) {
		initialRunTime=_initialRunTime;
		buildDateFormatter('-');
		buildTimeFormatter(':');
	}
	
	/**
	 * Use this to specify a date formatter
	 * @param dateToken
	 */
	public void buildDateFormatter(char dateToken) {
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
	
	public void buildTimeFormatter(char timeToken) {
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
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .toFormatter(Locale.getDefault(Locale.Category.FORMAT)).withZone( ZoneId.systemDefault() );		
	}
	//////////////////////////
	// time functions
	/**
	 * Returns time program was initialized/first run.
	 * @return
	 */
	public long getInitRunTime() {return initialRunTime;}
	/**
	 * set time of current process start - all future time measurements will measure from here
	 * @param _curProcStartTime
	 */
	public void setCurProcStartTime(long _curProcStartTime) {curProcStartTime=_curProcStartTime;}
	
	/**
	 * get time from "start time" (initialRunTime)
	 * @return
	 */
	public long getCurTimeFromStart() {			
		Instant now = Instant.now();
		return now.toEpochMilli() - initialRunTime;//milliseconds since 1/1/1970, subtracting when mapmgr was built to keep millis low		
	}//getCurTime() 
	
	/**
	 * returns a positive int value in millis of current world time since sim start
	 * @return
	 */
	public long getCurRunTimeForProc() {	return getCurTimeFromStart() - curProcStartTime;}
	
	public String getTimeStrFromProcStart() {return  getTimeStrFromPassedMillis(getCurRunTimeForProc());}
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
	
	public String getCurrWallTime() {
		String res = getDateTimeString();
		return res;			
	}
	
	/**
	 * Build a date-time with given delimiter token
	 * @return
	 */
	public String getDateTimeString(){
		Instant now = Instant.now();
		String result = preMadeDateFormatter.format(now);
		result += "|";
		result += preMadeTimeFormatter.format(now);
		return result;
	}
	public String getDateTimeString(char dateToken, char timeToken){
		Instant now = Instant.now();
		String result = getDateString(now, dateToken);
		result += getTimeString(now, timeToken);
		return result;
	}
	//utilities
	public String getDateString(){return getDateString(Instant.now(), '-');}
	public String getDateString(char token){ return getDateString(Instant.now(), token);}
	public String getDateString(Instant now, char token){
		buildDateFormatter(token);
		String result = preMadeDateFormatter.format(now);
		return result;
	}//getDateString
	
	public String getTimeString(){return getTimeString(Instant.now(), '-');}
	public String getTimeString(char token){ return getTimeString(Instant.now(), token);}
	public String getTimeString(Instant now, char token){
		buildTimeFormatter(token);
		String result  = preMadeTimeFormatter.format(now);
		return result;
	}//getDateString
	

	public String getWallTimeAndTimeFromStart(String delim) {	return getCurrWallTime() + delim + getTimeStrFromProcStart();}

}//myTimeMgr
