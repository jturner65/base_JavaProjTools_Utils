package base_Utils_Objects.io;

import java.io.File;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * This class instances objects that are responsible for screen display, and potentially writing out to log files
 * @author john
 */
public class MessageObject {
	public static boolean hasGraphics;
	private static Boolean supportsANSITerm = null;
	private static myTimeMgr timeMgr = null;
	
	//delimiter for display or output to log
	private static final String dispDelim = " | ", logDelim = ", ", newLineDelim = "\\r?\\n";
	
	//int to encode what to do with output
	//0 : print to console
	//1 : save to log file - if logfilename is not set, will default to outputMethod==0
	//2 : both
	private static int outputMethod;
	//file name to use for current run
	private static String fileName =null;
	//manage file IO for log file saving
	private static FileIOManager fileIO = null;
	
	//temporary string display data being printed to console - show on screen for a bit and then decay
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
	
	//hasGraphics can also be set directly externally
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
		if(!hasGraphics) {	obj = new MessageObject(_hasGraphics,Instant.now().toEpochMilli());} 
		else obj = new MessageObject(hasGraphics,Instant.now().toEpochMilli());
		
		if(!termCondSet) {
			//this is to make sure we always save the log file - this will be executed on shutdown, similar to code in a destructor in c++
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@SuppressWarnings("unused")
				public void run() {	if(obj==null) {return;}obj.dispInfoMessage("MessageObject", "Shutdown Hook", "Execting msgObj.FinishLog() code to flush log buffer to files and close log files.");	obj.FinishLog();}
			});
			termCondSet=true;
		}
		return obj;
	}//buildMe
	
	//define how the messages from this and all other messageObj should be handled, and pass a file name if a log is to be saved
	public void setOutputMethod(String _fileName, int _logLevel) {
		fileName = _fileName;
		if((fileName == null) || (fileName.length() < 3) || (_logLevel<=0)) {outputMethod = 0;}
		else {
			fileName = _fileName;
			File directory = new File(fileName);
		    if (! directory.exists()){ directory.mkdir(); }
			outputMethod = (_logLevel >= 3 ? 2 : _logLevel);
			fileIO = new FileIOManager(this, "Logger");	
		}
		_dispMessage_base_console(timeMgr.getWallTimeAndTimeFromStart(dispDelim), "messageObject","setOutputMethod","Setting log level :  "+ outputMethod + " : " + getOutputMethod(outputMethod)+ " | File name specified for log (if used) : " + fileName +" | File IO Object created : " + (fileIO !=null), MsgCodes.info1,true);
	}//setOutputMethod
	
	public String getOutputMethod(int outputMethod) {		
		switch(outputMethod) {
		case 0 : return "Console output only.";
		case 1 : return "Log output only to file specified.";
		case 2 : return "Console output and log output to file";
		default :return "Unknown output method :"+outputMethod;
		}
	}
	
	//finish any logging and write to file - this should be done when program closes
	public void FinishLog() {
		if((fileName == null) || (fileIO == null) || (outputMethod == 0) || (logMsgQueue.size()==0)) {return;}
		_dispMessage_base_console(timeMgr.getWallTimeAndTimeFromStart(dispDelim), "messageObject","FinishLog","Saving last " + logMsgQueue.size() + " queued messages to log file before exiting program.", MsgCodes.info1,true);
		ArrayList<String> outList = new ArrayList<String>();
		for(String key : logMsgQueue.keySet()) {	outList.add(logMsgQueue.get(key));}
		fileIO.saveStrings(fileName, outList, true);		
	}//FinishLog
	/**
	 * Return current wall time and time from execution start in string form
	 * @return string representation of wall time and time from start separated by a |
	 */
	public String getCurrWallTimeAndTimeFromStart() {return timeMgr.getWallTimeAndTimeFromStart(dispDelim);}
	public String getCurrWallTime() { return timeMgr.getCurrWallTime();}
	public String getTimeStrFromProcStart() { return timeMgr.getTimeStrFromProcStart();}
	//pass an array to display
	public void dispMessageAra(String[] _sAra, String _callingClass, String _callingMethod, int _perLine) {dispMessageAra( _sAra,  _callingClass, _callingMethod, _perLine,  MsgCodes.info1, true);}
	//pass an array to display
	public void dispMessageAra(String[] _sAra, String _callingClass, String _callingMethod, int _perLine, MsgCodes useCode) {dispMessageAra( _sAra,  _callingClass, _callingMethod, _perLine,  useCode, true);}
	//show array of strings, either just to console or to applet window
	public void dispMessageAra(String[] _sAra, String _callingClass, String _callingMethod, int _perLine, MsgCodes useCode, boolean onlyConsole) {			
		switch(outputMethod) {
			case 0 :{//just console
				for(int i=0;i<_sAra.length; i+=_perLine){
					String s = "";
					for(int j=0; j<_perLine; ++j){	
						if((i+j >= _sAra.length)) {continue;}
						s+= _sAra[i+j]+ "\t";}
					_dispMessage_base_console(timeMgr.getWallTimeAndTimeFromStart(dispDelim) , _callingClass,_callingMethod,s, useCode,onlyConsole);
				}			
				break;}	
			case 1 :{//just log file
				for(int i=0;i<_sAra.length; i+=_perLine){
					String s = "";
					for(int j=0; j<_perLine; ++j){	
						if((i+j >= _sAra.length)) {continue;}
						s+= _sAra[i+j]+ "\t";}
					_dispMessage_base_log(timeMgr.getWallTimeAndTimeFromStart(dispDelim) , _callingClass,_callingMethod,s, useCode,onlyConsole);
				}			
				break;}			//just log file
			case 2 :{	//both log and console
				for(int i=0;i<_sAra.length; i+=_perLine){
					String s = "";
					for(int j=0; j<_perLine; ++j){	
						if((i+j >= _sAra.length)) {continue;}
						s+= _sAra[i+j]+ "\t";}
					_dispMessage_base_console(timeMgr.getWallTimeAndTimeFromStart(dispDelim) , _callingClass,_callingMethod,s, useCode,onlyConsole);
					_dispMessage_base_log(timeMgr.getWallTimeAndTimeFromStart(dispDelim) , _callingClass,_callingMethod,s, useCode,onlyConsole);
				}
			}
		}//switch
	}//dispMessageAra
	
	//pass single-line messages - only 1 display of timestamp and class/method prefix
	/**
	 * default info message
	 * @param srcClass
	 * @param srcMethod
	 * @param msgText
	 */
	public void dispInfoMessage(String srcClass, String srcMethod, String msgText){										_dispMessage_base(srcClass,srcMethod,msgText, MsgCodes.info1,true);	}	
	/**
	 * default warning message
	 * @param srcClass
	 * @param srcMethod
	 * @param msgText
	 */
	public void dispWarningMessage(String srcClass, String srcMethod, String msgText){										_dispMessage_base(srcClass,srcMethod,msgText, MsgCodes.warning1,true);	}	
	/**
	 * default error message
	 * @param srcClass
	 * @param srcMethod
	 * @param msgText
	 */
	public void dispErrorMessage(String srcClass, String srcMethod, String msgText){										_dispMessage_base(srcClass,srcMethod,msgText, MsgCodes.error1,true);	}	
	
	
	public void dispMessage(String srcClass, String srcMethod, String msgText, MsgCodes useCode){						_dispMessage_base(srcClass,srcMethod,msgText, useCode,true);}	
	public void dispMessage(String srcClass, String srcMethod, String msgText, MsgCodes useCode, boolean onlyConsole) {	_dispMessage_base(srcClass,srcMethod,msgText, useCode,onlyConsole);	}	
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
	
	
	private void _dispMessage_base(String srcClass, String srcMethod, String msgText, MsgCodes useCode, boolean onlyConsole) {		
		switch(outputMethod) {
		case 0 :{_dispMessage_base_console(timeMgr.getWallTimeAndTimeFromStart(dispDelim) , srcClass,srcMethod,msgText, useCode,onlyConsole);break;}	//just console
		case 1 :{_dispMessage_base_log(timeMgr.getWallTimeAndTimeFromStart(logDelim), srcClass,srcMethod,msgText, useCode,onlyConsole);break;}			//just log file
		case 2 :{	//both log and console
			_dispMessage_base_console(timeMgr.getWallTimeAndTimeFromStart(dispDelim), srcClass,srcMethod,msgText, useCode,onlyConsole);
			_dispMessage_base_log(timeMgr.getWallTimeAndTimeFromStart(logDelim), srcClass,srcMethod,msgText, useCode,onlyConsole);
			break;}		
		}		
	}//_dispMessage_base
	
	private void _dispMessage_base_console(String timeStr, String srcClass, String srcMethod, String msgText, MsgCodes useCode, boolean onlyConsole) {	
		String msg = _processMsgCode(timeStr + dispDelim + srcClass + "::" + srcMethod + ":" + msgText, useCode);
		//if((onlyConsole) || (pa == null)) {		System.out.println(msg);	} else {	outStr2Scr(msg, true);	}
		printAndBuildConsoleStrs(msg, (onlyConsole || !hasGraphics));
	}//dispMessage
	
	/**
	 * print informational string data to console, and to screen
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
	}
	
	/**
	 * Update consoleStrings decay timer - after specified # of calls, 
	 */
	public String updateConsoleStrs(){	return consoleStrings.poll();}//updateConsoleStrs
	/**
	 * return current array deque of console strings, to be printed to screen, as an array of strings
	 * @return
	 */
	public String[] getConsoleStringsAsArray() {		return consoleStrings.toArray(new String[0]);	}
	
	
	//only save every 20 message lines
	private void _dispMessage_base_log(String timeStr, String srcClass, String srcMethod, String msgText, MsgCodes useCode, boolean onlyConsole) {	
		String baseStr = timeStr + logDelim + srcClass + logDelim + srcMethod + logDelim + msgText;
		synchronized(logMsgQueue){
			logMsgQueue.put(timeStr, baseStr);
			if(logMsgQueue.size()> 20){
				ArrayList<String> outList = new ArrayList<String>();
				for(String key : logMsgQueue.keySet()) {	outList.add(logMsgQueue.get(key));}
				fileIO.saveStrings(fileName, outList, true);
				logMsgQueue.clear();
			}
		}//sync
	}//dispMessage
	
}//messageObject