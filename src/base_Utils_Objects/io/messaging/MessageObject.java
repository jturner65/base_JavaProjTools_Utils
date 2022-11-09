package base_Utils_Objects.io.messaging;

import java.io.File;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;

import base_Utils_Objects.io.FileIOManager;
import base_Utils_Objects.tools.myTimeMgr;

/**
 * This class instances objects that are responsible for screen display, and potentially writing out to log files
 * @author john
 */
public class MessageObject {
	public static boolean hasGraphics;
	private static Boolean supportsANSITerm = null;
	private static myTimeMgr timeMgr = null;
	
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
	private static int outputMethod = 0;
	/**
	 * File name to use for current run/process
	 */
	private static String fileName =null;
	/**
	 * Manage file IO for log file saving
	 */
	private static FileIOManager fileIO = null;
	
	/**
	 * Temporary string display data being printed to console - show on screen for a bit and then decay
	 * Different deque for every instance
	 */
	private ArrayDeque<String> consoleStrings;
	
	private static ConcurrentSkipListMap<String, String> logMsgQueue = new ConcurrentSkipListMap<String, String>();	
	private static boolean termCondSet = false;
	
	private MessageObject(boolean _hasGraphics, long _exeBuiltTime) {
		hasGraphics=_hasGraphics; 
		if(supportsANSITerm == null) {supportsANSITerm = (System.console() != null && System.getenv().get("TERM") != null);	}
		if(timeMgr == null) {timeMgr = new myTimeMgr(_exeBuiltTime);}
		consoleStrings = new ArrayDeque<String>();	
	}	
	private MessageObject() {
		consoleStrings = new ArrayDeque<String>();
	}//in case we ever use any instance-specific data for this - copy ctor	
	
	/**
	 * hasGraphics can also be set directly externally
	 * @return
	 */
	public static MessageObject buildMe() { 
		if(!termCondSet) {		return buildMe(false);	} 
		else {					return new MessageObject();}	//returns another instance
	}
	
	/**
	 * Factory to build the message objects. Note the consoleStrings is independent of instance
	 * @param _hasGraphics
	 * @return
	 */
	public static MessageObject buildMe(boolean _hasGraphics) {
		MessageObject obj;
		//ignore _pa==null if pa is already set
		//can turn on graphics but cannot turn it off
		if(!hasGraphics) {	obj = new MessageObject(_hasGraphics,Instant.now().toEpochMilli());} 
		else obj = new MessageObject(hasGraphics,Instant.now().toEpochMilli());
		
		if(!termCondSet) {
			//this is to make sure we always save the log file - this will be executed on shutdown, similar to code in a destructor in c++
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@SuppressWarnings("unused")
				public void run() {	
					if(obj==null) {return;}
					obj.dispConsoleInfoMessage("MessageObject", "Shutdown Hook", "Executing FinishLog() code to flush log buffer to specified log file.");	
					obj.FinishLog();
				}					
			});
			termCondSet=true;
		}
		return obj;
	}//buildMe
	
	/**
	 * define how the messages from this and all other messageObj should be handled, and pass a file name if a log is to be saved
	 * @param _fileName
	 * @param _logLevel
	 */
	public synchronized void setOutputMethod(String _fileName, int _logLevel) {
		fileName = _fileName;
		if((_logLevel<=0) || (fileName == null) || (fileName.length() < 3)) {outputMethod = 0;fileIO = null;}
		else {
			File fileNameFile = new File(fileName);
			//make any directories that have not been built yet required to save this log file
			File directory = fileNameFile.isDirectory() ? fileNameFile : fileNameFile.getParentFile();
		    if (!directory.exists()){ 
		    	boolean mkDirSuccess = directory.mkdirs();
				_dispMessage_base_console("MessageObject","setOutputMethod","Making logging directory :"+directory.toString()+" to contain file `"+fileNameFile.getName() +"` : "+ (mkDirSuccess ? "Success" : "FAILED!!") , MsgCodes.info1,true);
		    }
			outputMethod = (_logLevel >= 3 ? 2 : _logLevel);
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
		if((fileName == null) || (fileIO == null) || (outputMethod == 0) || (logMsgQueue.size()==0)) {return;}
		_dispMessage_base_console("MessageObject","FinishLog","Saving last " + logMsgQueue.size() + " queued messages to log file before exiting program.", MsgCodes.info1,true);
		_flushAndSaveLogMsgQueue();
	}//FinishLog
	/**
	 * Return current wall time and time from execution start in string form
	 * @return string representation of wall time and time from start separated by a |
	 */
	public String getCurrWallTimeAndTimeFromStart() {return timeMgr.getWallTimeAndTimeFromStart(dispDelim);}
	public String getCurrWallTime() { return timeMgr.getCurrWallTime();}
	public String getTimeStrFromProcStart() { return timeMgr.getTimeStrFromProcStart();}
	//pass an array to display
	public void dispMessageAra(String[] _sAra, String _callingClass, String _callingMethod, int _perLine) {
		_dispMessageAra( _sAra,  _callingClass, _callingMethod, _perLine,  MsgCodes.info1, true, outputMethod);
	}
	//pass an array to display
	public void dispMessageAra(String[] _sAra, String _callingClass, String _callingMethod, int _perLine, MsgCodes useCode) {
		_dispMessageAra( _sAra,  _callingClass, _callingMethod, _perLine,  useCode, true, outputMethod);
	}
	//show array of strings, either just to console or to applet window
	public void dispMessageAra(String[] _sAra, String _callingClass, String _callingMethod, int _perLine, MsgCodes useCode, boolean onlyConsole) {	
		_dispMessageAra( _sAra,  _callingClass, _callingMethod, _perLine,  useCode, onlyConsole, outputMethod);
	}
	
	//pass single-line messages - only 1 display of timestamp and class/method prefix
	/**
	 * default info message
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispInfoMessage(String srcClass, String srcMethod, String msgText){		_dispMessage_base(srcClass,srcMethod,msgText, MsgCodes.info1,true, outputMethod);	}	
	/**
	 * default warning message
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispWarningMessage(String srcClass, String srcMethod, String msgText){	_dispMessage_base(srcClass,srcMethod,msgText, MsgCodes.warning1,true, outputMethod);	}	
	/**
	 * default error message
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispErrorMessage(String srcClass, String srcMethod, String msgText){	_dispMessage_base(srcClass,srcMethod,msgText, MsgCodes.error1,true, outputMethod);	}	
	
	
	/**
	 * default info message to console regardless of log level specified
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispConsoleInfoMessage(String srcClass, String srcMethod, String msgText){		_dispMessage_base(srcClass,srcMethod,msgText, MsgCodes.info1,true, 0);	}	
	/**
	 * default warning message to console regardless of log level specified
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispConsoleWarningMessage(String srcClass, String srcMethod, String msgText){	_dispMessage_base(srcClass,srcMethod,msgText, MsgCodes.warning1,true, 0);	}	
	/**
	 * default error message to console regardless of log level specified
	 * @param srcClass name of calling class
	 * @param srcMethod name of calling method
	 * @param msgText message text to print
	 */
	public void dispConsoleErrorMessage(String srcClass, String srcMethod, String msgText){	_dispMessage_base(srcClass,srcMethod,msgText, MsgCodes.error1,true, 0);	}	
	
	public void dispMessage(String srcClass, String srcMethod, String msgText, MsgCodes useCode){						_dispMessage_base(srcClass,srcMethod,msgText, useCode,true, outputMethod);}	
	public void dispMessage(String srcClass, String srcMethod, String msgText, MsgCodes useCode, boolean onlyConsole) {	_dispMessage_base(srcClass,srcMethod,msgText, useCode,onlyConsole, outputMethod);	}	
	//parse string on \n characters
	public void dispMultiLineInfoMessage(String srcClass, String srcMethod, String msgTextWithNewLines){String[] _sAra = msgTextWithNewLines.split(newLineDelim);dispMessageAra(_sAra, srcClass,srcMethod,1, MsgCodes.info1,true);}	
	public void dispMultiLineMessage(String srcClass, String srcMethod, String msgTextWithNewLines, MsgCodes useCode){String[] _sAra = msgTextWithNewLines.split(newLineDelim);dispMessageAra(_sAra, srcClass,srcMethod,1, useCode,true);}	
	public void dispMultiLineMessage(String srcClass, String srcMethod, String msgTextWithNewLines, MsgCodes useCode, boolean onlyConsole) {String[] _sAra = msgTextWithNewLines.split(newLineDelim);dispMessageAra(_sAra, srcClass,srcMethod,1, useCode,onlyConsole);}	
	
	
	////////////////////////
	// private methods
	private String buildClrStr(ConsoleCLR bk, ConsoleCLR clr, String str) {return bk.toString() + clr.toString() + str + ConsoleCLR.RESET.toString();	}
	private String _processMsgCode(String src, MsgCodes useCode) {
		if (!supportsANSITerm) {return src;}
		switch(useCode) {//add background + letter color for messages
			//info messages
			case info1 : {		return  buildClrStr(ConsoleCLR.BLACK_BACKGROUND, ConsoleCLR.WHITE, src);}		//basic informational printout
			case info2 : {		return  buildClrStr(ConsoleCLR.BLACK_BACKGROUND, ConsoleCLR.CYAN, src);}
			case info3 : {		return  buildClrStr(ConsoleCLR.BLACK_BACKGROUND, ConsoleCLR.YELLOW, src);}		//informational output from som EXE
			case info4 : {		return  buildClrStr(ConsoleCLR.BLACK_BACKGROUND, ConsoleCLR.GREEN, src);}
			case info5 : {		return  buildClrStr(ConsoleCLR.BLACK_BACKGROUND, ConsoleCLR.CYAN_BOLD, src);}	//beginning or ending of processing chuck/function
			//warning messages                                                 , 
			case warning1 : {	return  buildClrStr(ConsoleCLR.WHITE_BACKGROUND, ConsoleCLR.BLACK_BOLD, src);}
			case warning2 : {	return  buildClrStr(ConsoleCLR.WHITE_BACKGROUND, ConsoleCLR.BLUE_BOLD, src);}	//warning info re: ui does not exist
			case warning3 : {	return  buildClrStr(ConsoleCLR.WHITE_BACKGROUND, ConsoleCLR.BLACK_UNDERLINED, src);}
			case warning4 : {	return  buildClrStr(ConsoleCLR.WHITE_BACKGROUND, ConsoleCLR.BLUE_UNDERLINED, src);}	//info message about unexpected behavior
			case warning5 : {	return  buildClrStr(ConsoleCLR.WHITE_BACKGROUND, ConsoleCLR.BLUE_BRIGHT, src);}
			//error messages                                                   , 
			case error1 : {		return  buildClrStr(ConsoleCLR.BLACK_BACKGROUND, ConsoleCLR.RED_UNDERLINED, src);}//try/catch error
			case error2 : {		return  buildClrStr(ConsoleCLR.BLACK_BACKGROUND, ConsoleCLR.RED_BOLD, src);}		//code-based error
			case error3 : {		return  buildClrStr(ConsoleCLR.RED_BACKGROUND_BRIGHT, ConsoleCLR.BLACK_BOLD, src);}	//file load error
			case error4 : {		return  buildClrStr(ConsoleCLR.WHITE_BACKGROUND_BRIGHT, ConsoleCLR.RED_BRIGHT, src);}	//error message thrown by som executable
			case error5 : {		return  buildClrStr(ConsoleCLR.BLACK_BACKGROUND, ConsoleCLR.RED_BOLD_BRIGHT, src);}
		}
		return src;
	}//_processMsgCode

	private void _dispMessage_base(String srcClass, String srcMethod, String msgText, MsgCodes useCode, boolean onlyConsole, int outputMethod) {		
		switch(outputMethod) {
		case 0 :{_dispMessage_base_console(srcClass,srcMethod,msgText, useCode,onlyConsole);break;}	//just console
		case 1 :{_dispMessage_base_log(srcClass,srcMethod,msgText, useCode,onlyConsole);break;}			//just log file
		case 2 :{	//both log and console
			_dispMessage_base_console(srcClass,srcMethod,msgText, useCode,onlyConsole);
			_dispMessage_base_log(srcClass,srcMethod,msgText, useCode,onlyConsole);
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
				for(int i=0;i<_sAra.length; i+=_perLine){
					String s = buildLine(_sAra, _perLine, i);
					_dispMessage_base_console(_callingClass,_callingMethod,s, useCode,onlyConsole);
				}			
				break;}	
			case 1 :{//just log file
				for(int i=0;i<_sAra.length; i+=_perLine){
					String s = buildLine(_sAra, _perLine, i);
					_dispMessage_base_log(_callingClass,_callingMethod,s, useCode,onlyConsole);
				}			
				break;}			//just log file
			case 2 :{	//both log and console
				for(int i=0;i<_sAra.length; i+=_perLine){
					String s = buildLine(_sAra, _perLine, i);
					_dispMessage_base_console(_callingClass,_callingMethod,s, useCode,onlyConsole);
					_dispMessage_base_log(_callingClass,_callingMethod,s, useCode,onlyConsole);
				}
			}
		}//switch
	}//dispMessageAra
	private void _dispMessage_base_console(String srcClass, String srcMethod, String msgText, MsgCodes useCode, boolean onlyConsole) {	
		String timeStr = timeMgr.getWallTimeAndTimeFromStart(dispDelim);
		String msg = _processMsgCode(timeStr + dispDelim + srcClass + "::" + srcMethod + ":" + msgText, useCode);
		printAndBuildConsoleStrs(msg, (onlyConsole || !hasGraphics));
	}
	

	/**
	 * only save every 20 message lines
	 * @param srcClass
	 * @param srcMethod
	 * @param msgText
	 * @param useCode
	 * @param onlyConsole
	 */
	private void _dispMessage_base_log(String srcClass, String srcMethod, String msgText, MsgCodes useCode, boolean onlyConsole) {
		String timeStr = timeMgr.getWallTimeAndTimeFromStart(logDelim);
		String baseStr = timeStr + logDelim + srcClass + logDelim + srcMethod + logDelim + msgText;
		synchronized(logMsgQueue){
			logMsgQueue.put(timeStr, baseStr);
			if(logMsgQueue.size()> 20){
				_flushAndSaveLogMsgQueue();
			}
		}//sync
	}
	
	/**
	 * Save the current logMsgQueue to disk
	 */
	private synchronized void _flushAndSaveLogMsgQueue() {
		ArrayList<String> outList = new ArrayList<String>();
		for(String key : logMsgQueue.keySet()) {	outList.add(logMsgQueue.get(key));}
		fileIO.saveStrings(fileName, outList, true);
		logMsgQueue.clear();
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
	
	/**
	 * Pop the head of the consoleStrings deque 
	 */
	public String updateConsoleStrs(){	return consoleStrings.poll();}//updateConsoleStrs
	/**
	 * return current array deque of console strings, to be printed to screen, as an array of strings
	 * @return
	 */
	public String[] getConsoleStringsAsArray() {		return consoleStrings.toArray(new String[0]);	}

	
}//messageObject
