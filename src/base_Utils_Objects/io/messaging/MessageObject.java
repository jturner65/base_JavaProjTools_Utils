package base_Utils_Objects.io.messaging;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;

import base_Utils_Objects.io.file.FileIOManager;
import base_Utils_Objects.timer.TimerManager;

/**
 * This class instances objects that are responsible for screen display, and potentially writing out to log files
 * @author john
 */
public class MessageObject {
	//Single instance 
	private static MessageObject msgObj;
	
	/**
	 * Whether or not the application owning this message object has graphical UI or is running headless
	 */
	private boolean hasGraphics;
	
	
	private Boolean supportsANSITerm = null;
	private TimerManager timerMgr = null;
	
	/**
	 * delimiter for display or output to log
	 */
	private static final String dispDelim = " | ", logDelim = ", ", newLineDelim = "\\r?\\n";
	
	/** 
	 * int to encode what to do with output 
	 * 0 : print to console 
	 * 1 : save to log file - if logfilename is not set, will default to outputMethod==0 
	 * 2 : both 
	 */
	private int outputMethod = 0;
	
	/**
	 * Minimum message level to display to console.
	 */
	private MsgCodes minConsoleDispCode;
	
	/**
	 * Minumum message level to save to log
	 */
	private MsgCodes minLogDispCode;
	
	
	/**
	 * File name to use for current program
	 */
	private String fileName = null;
	/**
	 * Manage file IO for log file saving
	 */
	private FileIOManager fileIO = null;
	
	/**
	 * Temporary string display data being printed to console - show on screen for a bit and then decay
	 */
	private ConcurrentLinkedDeque<String> consoleStrings;
	
	private ConcurrentSkipListMap<Long, String> logMsgQueue = new ConcurrentSkipListMap<Long, String>();	
	private int logMsgQueueSize = 0;
	private Object logMsgModLock = new Object();
	
	private static final int maxLogMsgSize = 50;
	
	private static boolean termCondSet = false;
	
	/**
	 * map to hold pre-computed console color strings for output high-lighting
	 */
	private static final HashMap<MsgCodes, String> msgClrPrefix = new HashMap<MsgCodes, String>();
	static {
		msgClrPrefix.put(MsgCodes.debug1, ""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLACK);			//basic debug printout
		msgClrPrefix.put(MsgCodes.debug2, ""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.GREEN);
		msgClrPrefix.put(MsgCodes.debug3, ""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.YELLOW);		
		msgClrPrefix.put(MsgCodes.debug4, ""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLUE);
		msgClrPrefix.put(MsgCodes.debug5, ""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.MAGENTA);	
		msgClrPrefix.put(MsgCodes.info1, ""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.WHITE);			//basic informational printout
		msgClrPrefix.put(MsgCodes.info2, ""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.CYAN);
		msgClrPrefix.put(MsgCodes.info3, ""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.YELLOW);		
		msgClrPrefix.put(MsgCodes.info4, ""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.GREEN);
		msgClrPrefix.put(MsgCodes.info5, ""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.CYAN_BOLD);	
		msgClrPrefix.put(MsgCodes.warning1, ""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLACK_BOLD);
		msgClrPrefix.put(MsgCodes.warning2, ""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLUE_BOLD);	
		msgClrPrefix.put(MsgCodes.warning3, ""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLACK_UNDERLINED);
		msgClrPrefix.put(MsgCodes.warning4, ""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLUE_UNDERLINED);	
		msgClrPrefix.put(MsgCodes.warning5, ""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLUE_BRIGHT);
		msgClrPrefix.put(MsgCodes.error1, ""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.RED_UNDERLINED);	
		msgClrPrefix.put(MsgCodes.error2, ""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.RED_BOLD);		
		msgClrPrefix.put(MsgCodes.error3, ""+ConsoleCLR.RED_BACKGROUND_BRIGHT+ConsoleCLR.BLACK_BOLD);	
		msgClrPrefix.put(MsgCodes.error4, ""+ConsoleCLR.WHITE_BACKGROUND_BRIGHT+ConsoleCLR.RED_BRIGHT);	
		msgClrPrefix.put(MsgCodes.error5, ""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.RED_BOLD_BRIGHT);
	}
	
	/**
	 * Private constructor
	 */
	private MessageObject() {
		if(supportsANSITerm == null) {supportsANSITerm = (System.console() != null && System.getenv().get("TERM") != null);	}
		if(timerMgr == null) {timerMgr = TimerManager.getInstance();}
		consoleStrings = new ConcurrentLinkedDeque<String>();	
		minConsoleDispCode = MsgCodes.debug1;
		minLogDispCode = MsgCodes.debug1;		
	}	
	
	/**
	 * Singleton Factory
	 * @return
	 */
	public static MessageObject getInstance() {
		if (msgObj == null) {
			msgObj = new MessageObject();
			if(!termCondSet) {
				//this is to make sure we always save the log file - this will be executed on shutdown, similar to code in a destructor in c++
				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {	
						if(msgObj==null) {return;}
						msgObj.dispConsoleInfoMessage("MessageObject", "Shutdown Hook", "Executing FinishLog() code to flush log buffer to specified log file.");	
						msgObj.FinishLog();
					}					
				});
				termCondSet=true;
			}			
		}
		return msgObj;
	}//getInstance()
	
	/**
	 * define how the messages from this and all other messageObj should be handled, and pass a file name if a log is to be saved
	 * @param _fileName
	 * @param _outputMethod
	 */
	public synchronized void setOutputMethod(String _fileName, int _outputMethod) {
		fileName = _fileName;
		if((_outputMethod<=0) || (fileName == null) || (fileName.length() < 3)) {outputMethod = 0;fileIO = null;}
		else {
			File fileNameFile = new File(fileName);
			String fName = fileNameFile.getName();
					
			//make any directories that have not been built yet required to save this log file
			File directory = fileNameFile.isDirectory() ? fileNameFile : fileNameFile.getParentFile();
			String dirName = directory.toString();
		    if (!directory.exists()){
		    	boolean mkDirSuccess = false;
		    	try {mkDirSuccess = directory.mkdirs();} 
		    	catch(Exception e) {		    		
		    		_dispMessage_base_console("MessageObject","setOutputMethod","Thrown Error making logging directory :"+dirName+" for log file `"+fName +"` :\n"+e.getLocalizedMessage(), MsgCodes.error1,true);		    			    		
		    	}
		    	//no thrown error but failed none-the-less
		    	if (!mkDirSuccess) {
		    		_dispMessage_base_console("MessageObject","setOutputMethod","Error: Failed making logging directory :"+dirName+" for log file `"+fName, MsgCodes.error1,true);		    		
		    	} else {
		    		_dispMessage_base_console("MessageObject","setOutputMethod","Success making logging directory :"+dirName+" for log file `"+fName, MsgCodes.info1,true);
		    	}
		    }
			outputMethod = (_outputMethod >= 3 ? 2 : _outputMethod);
			if (fileIO==null) {	fileIO = new FileIOManager(this, "Logger");}	
		}
		_dispMessage_base_console("MessageObject","setOutputMethod","Setting log level :  "+ outputMethod + " : " + getOutputMethod(outputMethod)+ " | File name specified for log (if used) : " + fileName +" | File IO Object created : " + (fileIO !=null), MsgCodes.info1,true);
	}//setOutputMethod
	
	public String getOutputMethod(int outputMethod) {		
		switch(outputMethod) {
		case 0 : return "Console output only.";
		case 1 : return "Log output only to file specified.";
		case 2 : return "Console output and log output to file";
		default :return "Unknown output method :"+outputMethod;
		}
	}
	
	/**
	 * finish any logging and write to file - this should be done when program closes
	 */
	public void FinishLog() {
		if((outputMethod == 0) || (logMsgQueueSize==0)) { return;}
		if(fileName == null){
			_dispMessage_base_console("MessageObject","FinishLog","Unknown/unspecified file name so unable to save log of " + logMsgQueueSize + " queued messages.", MsgCodes.warning1,true);
			return;
		}
		if(fileIO == null){
			_dispMessage_base_console("MessageObject","FinishLog","Unavailable/null fileIO object so unable to save log of " + logMsgQueueSize + " queued messages.", MsgCodes.warning1,true);
			return;
		}
		_dispMessage_base_console("MessageObject","FinishLog","Saving last " + logMsgQueueSize + " queued messages to log file before exiting program.", MsgCodes.info1,true);
		synchronized(logMsgModLock){
			_flushAndSaveLogMsgQueue();
		}
	}//FinishLog
	/**
	 * Return current wall time and time from execution start in string form
	 * @return string representation of wall time and time from start separated by a |
	 */
	public String getCurrWallTimeAndTimeFromStart() {return timerMgr.getWallTimeAndTimeFromStart(dispDelim);}
	public String getCurrWallTime() { return timerMgr.getCurrWallTime();}
	public String getElapsedTimeStrForTimer(String timerName) { return timerMgr.getElapsedTimeStrForTimer(timerName);}
	
	/**
	 * pass an array to display
	 * @param _callingClass
	 * @param _callingMethod
	 * @param _sAra
	 * @param _perLine
	 */
	public void dispMessageAra(String _callingClass, String _callingMethod, String[] _sAra, int _perLine) {
		_dispMessageAra( _sAra,  _callingClass, _callingMethod, _perLine,  MsgCodes.info1, true, outputMethod);
	}
	/**
	 * pass an array to display
	 * @param _callingClass
	 * @param _callingMethod
	 * @param _sAra
	 * @param _perLine
	 * @param useCode
	 */
	public void dispMessageAra(String _callingClass, String _callingMethod, String[] _sAra, int _perLine, MsgCodes useCode) {
		_dispMessageAra( _sAra,  _callingClass, _callingMethod, _perLine,  useCode, true, outputMethod);
	}
	/**
	 * show array of strings, either just to console or to applet window
	 * @param _callingClass
	 * @param _callingMethod
	 * @param _sAra
	 * @param _perLine
	 * @param useCode
	 * @param onlyConsole whether to only print to console or also to print to screen window, if UI is available
	 */
	public void dispMessageAra(String _callingClass, String _callingMethod, String[] _sAra, int _perLine, MsgCodes useCode, boolean onlyConsole) {	
		_dispMessageAra( _sAra,  _callingClass, _callingMethod, _perLine,  useCode, onlyConsole, outputMethod);
	}
	
	//pass single-line messages - only 1 display of timestamp and class/method prefix
	/**
	 * default debug message
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispDebugMessage(String srcClass, String srcMethod, String msgText){	_dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.debug1,true, outputMethod);	}	
	/**
	 * default info message
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispInfoMessage(String srcClass, String srcMethod, String msgText){		_dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.info1,true, outputMethod);	}	
	/**
	 * default warning message
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispWarningMessage(String srcClass, String srcMethod, String msgText){	_dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.warning1,true, outputMethod);	}	
	/**
	 * default error message
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispErrorMessage(String srcClass, String srcMethod, String msgText){	_dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.error1,true, outputMethod);	}	
	
	/////////////////////////
	// Console-only message displays	
	/**
	 * default info message to console regardless of log level specified
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispConsoleDebugMessage(String srcClass, String srcMethod, String msgText){		_dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.debug1,true, 0);	}	
	/**
	 * default info message to console regardless of log level specified
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispConsoleInfoMessage(String srcClass, String srcMethod, String msgText){		_dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.info1,true, 0);	}	
	/**
	 * default warning message to console regardless of log level specified
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispConsoleWarningMessage(String srcClass, String srcMethod, String msgText){	_dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.warning1,true, 0);	}	
	/**
	 * default error message to console regardless of log level specified
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispConsoleErrorMessage(String srcClass, String srcMethod, String msgText){	_dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.error1,true, 0);	}	

	/////////////////////////
	// Multi-line message displays	
	/**
	 * Display passed message as multi-line debug message, splitting on '\n' characters
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText msgTextWithNewLines text to print, with embedded newlines where splits should occur 
	 */
	public void dispMultiLineDebugMessage(String srcClass, String srcMethod, String msgTextWithNewLines){
		String[] _sAra = msgTextWithNewLines.split(newLineDelim);dispMessageAra(srcClass, srcMethod, _sAra, 1, MsgCodes.debug1, true);
	}	

	/**
	 * Display passed message as multi-line info message, splitting on '\n' characters
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText msgTextWithNewLines text to print, with embedded newlines where splits should occur 
	 */
	public void dispMultiLineInfoMessage(String srcClass, String srcMethod, String msgTextWithNewLines){
		String[] _sAra = msgTextWithNewLines.split(newLineDelim);dispMessageAra(srcClass, srcMethod, _sAra, 1, MsgCodes.info1, true);
	}	

	/**
	 * Display passed message as multi-line warning message, splitting on '\n' characters
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText msgTextWithNewLines text to print, with embedded newlines where splits should occur 
	 */
	public void dispMultiLineWarningMessage(String srcClass, String srcMethod, String msgTextWithNewLines){
		String[] _sAra = msgTextWithNewLines.split(newLineDelim);dispMessageAra(srcClass, srcMethod, _sAra, 1, MsgCodes.warning1, true);
	}	

	/**
	 * Display passed message as multi-line error message, splitting on '\n' characters
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText msgTextWithNewLines text to print, with embedded newlines where splits should occur 
	 */
	public void dispMultiLineErrorMessage(String srcClass, String srcMethod, String msgTextWithNewLines){
		String[] _sAra = msgTextWithNewLines.split(newLineDelim);dispMessageAra(srcClass, srcMethod, _sAra, 1, MsgCodes.error1, true);
	}	
	
	/////////////////////////
	// MsgCodes-agnostic message displays
	/**
	 * Display passed message using passed msgText code for display level
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 * @param useCode MsgCodes code/level to use for display
	 */
	public void dispMessage(String srcClass, String srcMethod, String msgText, MsgCodes useCode){
		_dispMessage_base(srcClass, srcMethod, msgText, useCode, true, outputMethod);
	}	
	
	/**
	 * Display passed message using passed msgText code for display level
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 * @param useCode MsgCodes code/level to use for display
	 * @param onlyConsole Whether to display only on console or also to UI window if it exists
	 */
	public void dispMessage(String srcClass, String srcMethod, String msgText, MsgCodes useCode, boolean onlyConsole) {	
		_dispMessage_base(srcClass, srcMethod, msgText, useCode, onlyConsole, outputMethod);	
	}	
	
	/**
	 * Display passed message string as multi-line message, parsing the string on '\n' characters
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText msgTextWithNewLines text to print, with embedded newlines where splits should occur 
	 * @param useCode MsgCodes code/level to use for display
	 */
	public void dispMultiLineMessage(String srcClass, String srcMethod, String msgTextWithNewLines, MsgCodes useCode){
		String[] _sAra = msgTextWithNewLines.split(newLineDelim);dispMessageAra(srcClass, srcMethod, _sAra, 1, useCode, true);
	}	
	
	/**
	 * Display passed message string as multi-line message, parsing the string on '\n' characters
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText msgTextWithNewLines text to print, with embedded newlines where splits should occur 
	 * @param useCode message level code to use
	 * @param onlyConsole whether to only print to console or also to print to screen window, if UI is available
	 */
	public void dispMultiLineMessage(String srcClass, String srcMethod, String msgTextWithNewLines, MsgCodes useCode, boolean onlyConsole) {
		String[] _sAra = msgTextWithNewLines.split(newLineDelim);dispMessageAra(srcClass, srcMethod, _sAra, 1, useCode, onlyConsole);
	}		
	
	////////////////////////
	// getter/setter
	/**
	 * Pop and return the head of the consoleStrings deque 
	 */
	public String updateConsoleStrs(){	return consoleStrings.poll();}//updateConsoleStrs
	/**
	 * return current array deque of console strings, to be printed to screen, as an array of strings
	 * @return
	 */
	public String[] getConsoleStringsAsArray() {		return consoleStrings.toArray(new String[0]);	}
	
	/**
	 * Set whether the owning program is running headless or has a UI
	 * @param _hasGraphics
	 */
	public void setHasGraphics(boolean _hasGraphics) {hasGraphics = _hasGraphics;}
	
	/**
	 * Set min level of message to display in console
	 * @param _minCnslLevel
	 */
	public void setMinConsoleOutputLevel(MsgCodes _minCnslLevel) {
		minConsoleDispCode = _minCnslLevel;
	}
	/**
	 * Set min level of message to save to log file
	 * @param _minLogLevel
	 */
	public void setMinLogOutputLevel(MsgCodes _minLogLevel) {
		minLogDispCode = _minLogLevel;
	}
	/**
	 * Set the minimum level of messages to display to console and to logger
	 * @param _minLevel min level of message to display to console and save to log file
	 */
	public void setMinOutputLevel(MsgCodes _minLevel) {
		minConsoleDispCode = _minLevel;
		minLogDispCode = _minLevel;
	}
	/**
	 * Set the minimum level of messages to display to console and to logger
	 * @param _minCnslLevel min level of message to display to console
	 * @param _minLogLevel min level of message to save to log file
	 */
	public void setMinOutputLevel(MsgCodes _minCnslLevel, MsgCodes _minLogLevel) {
		minConsoleDispCode = _minCnslLevel;
		minLogDispCode = _minLogLevel;
	}	
	
	////////////////////////
	// private methods
	/**
	 * Augment message with color highlighting based on message type, if supported
	 * @param src
	 * @param useCode
	 * @return
	 */
	private String _processMsgCode(String src, MsgCodes useCode) {
		if (!supportsANSITerm) {return src;}
		//find appropriate color code for message
		String clrStr = msgClrPrefix.get(useCode);
		if (clrStr != null) {		return clrStr + src + ConsoleCLR.RESET.toString();}
		return src;
	}//_processMsgCode

	private void _dispMessage_base(String srcClass, String srcMethod, String msgText, MsgCodes useCode, boolean onlyConsole, int outputMethod) {		
		switch(outputMethod) {
		case 0 :{
			if(useCode.getVal() < this.minConsoleDispCode.getVal()) {return;}
			_dispMessage_base_console(srcClass, srcMethod,msgText, useCode, onlyConsole);break;}	//just console
		case 1 :{
			if(useCode.getVal() < this.minLogDispCode.getVal()) {return;}
			_dispMessage_base_log(srcClass, srcMethod,msgText, useCode);break;}			//just log file
		case 2 :{	//both log and console
			if(useCode.getVal() >= this.minConsoleDispCode.getVal()) {
				_dispMessage_base_console(srcClass, srcMethod,msgText, useCode, onlyConsole);	
			}
			if(useCode.getVal() >= this.minLogDispCode.getVal()) {
				_dispMessage_base_log(srcClass, srcMethod,msgText, useCode);
			}
			break;}		
		}		
	}//_dispMessage_base
	private String buildLine(String[] _sAra, int _perLine, int _row) {
		String s = "";
		for(int j=0; j<_perLine; ++j){	
			if((_row+j >= _sAra.length)) {continue;}
			s+= _sAra[_row+j]+ "\t";}
		return s;
	}	
	private void _dispMessageAra(String[] _sAra, String _callingClass, String _callingMethod, int _perLine, MsgCodes useCode, boolean onlyConsole, int outputMethod) {			
		switch(outputMethod) {
			case 0 :{//just console
				if(useCode.getVal() < this.minConsoleDispCode.getVal()) {return;}
				for(int i=0;i<_sAra.length; i+=_perLine){
					String s = buildLine(_sAra, _perLine, i);
					_dispMessage_base_console(_callingClass, _callingMethod, s, useCode,onlyConsole);
				}			
				break;}			//just console
			case 1 :{//just log file
				if(useCode.getVal() < this.minLogDispCode.getVal()) {return;}
				for(int i=0;i<_sAra.length; i+=_perLine){
					String s = buildLine(_sAra, _perLine, i);
					_dispMessage_base_log(_callingClass, _callingMethod, s, useCode);
				}			
				break;}			//just log file
			case 2 :{	//both log and console
				if(useCode.getVal() < this.minConsoleDispCode.getVal()) {
					//neither mechanism should process this message
					if(useCode.getVal() < this.minLogDispCode.getVal()) {return;} 
					//use only log output for this message
					_dispMessageAra(_sAra,_callingClass, _callingMethod, _perLine, useCode, onlyConsole, 1);					
				} else if(useCode.getVal() < this.minLogDispCode.getVal()) {
					//use only console output for this message
					_dispMessageAra(_sAra,_callingClass, _callingMethod, _perLine, useCode, onlyConsole, 0);					
				} else {
					for(int i=0;i<_sAra.length; i+=_perLine){
						String s = buildLine(_sAra, _perLine, i);
						_dispMessage_base_console(_callingClass, _callingMethod, s, useCode,onlyConsole);
						_dispMessage_base_log(_callingClass, _callingMethod, s, useCode);
					}
				}
				break;}			//both log and console
		}//switch
	}//dispMessageAra
	
	private void _dispMessage_base_console(String srcClass, String srcMethod, String msgText, MsgCodes useCode, boolean onlyConsole) {	
		String timeStr = timerMgr.getWallTimeAndTimeFromStart(dispDelim);
		String msg = _processMsgCode(timeStr + dispDelim + srcClass + "::" + srcMethod + ":" + msgText, useCode);
		printAndBuildConsoleStrs(msg, (onlyConsole || !hasGraphics));
	}	

	/**
	 * only save every 20 message lines
	 * @param srcClass
	 * @param srcMethod
	 * @param msgText
	 * @param useCode
	 */
	private void _dispMessage_base_log(String srcClass, String srcMethod, String msgText, MsgCodes useCode) {
		long timeKey = timerMgr.getMillisFromProgStart();
		String timeStr = timerMgr.getWallTimeAndTimeFromStart(logDelim);
		String baseStr = timeStr + logDelim + srcClass + logDelim + srcMethod + logDelim + msgText;
		synchronized(logMsgModLock){
			++logMsgQueueSize;
			logMsgQueue.put(timeKey, baseStr);		
			if(logMsgQueueSize> maxLogMsgSize){
				_flushAndSaveLogMsgQueue();
			}
		}
	}
	
	/**
	 * Save the current logMsgQueue to disk
	 */
	private void _flushAndSaveLogMsgQueue() {
		ArrayList<String> outList = new ArrayList<String>();
		for(Long key : logMsgQueue.keySet()) {	outList.add(logMsgQueue.get(key));}
		fileIO.saveStrings(fileName, outList, true);
		logMsgQueue.clear();
		logMsgQueueSize = 0;
	}
	
	/**
	 * print informational string data to console, and to screen if appropriate and has graphics support
	 * @param str
	 * @param onlyConsole whether to print only to console or to add to consoleStrings to be displayed in graphical window as well
	 */
	private void printAndBuildConsoleStrs(String str, boolean onlyConsole){
		System.out.println(str);
		if(!onlyConsole){
			String[] res = str.split("\\r?\\n");
			for(int i =0; i<res.length; ++i){
				consoleStrings.add(res[i]);		//add console string output to screen display- decays over time
			}
		}
	}//printAndBuildConsoleStrs
	
}//messageObject
