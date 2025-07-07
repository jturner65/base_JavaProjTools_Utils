package base_Utils_Objects.io.messaging;

import java.io.File;
import java.util.ArrayList;
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
    
    //whether the terminal supports ANSI color codes for message output
    private Boolean supportsANSITerm = null;
    private TimerManager timerMgr = null;
    
    /**
     * delimiters for display to console or output to log.
     * First is between components of date/time stamp
     * 2nd is between owning class and owning method
     * 3rd is between method and message.
     */
    private static final String[] dispDelims = new String[]{" | ", "::"," : "};
    // commas for log files
    private static final String[] logDelims = new String[]{", ",", ",", "};
    // newline regex delim to split strings on
    private static final String newLineDelim = "\\r?\\n";
    
    
    
    /** 
     * enum to encode what to do with output 
     * 0 : print to console 
     * 1 : save to log file - if logfilename is not set, will default to outputMethod==0 
     * 2 : both 
     */
    private MsgOutputMethod outputMethod = MsgOutputMethod.Console;
    
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
    
    /**
     * Structure to hold log messages before writing to file
     */
    private ConcurrentSkipListMap<Long, String> logMsgQueue = new ConcurrentSkipListMap<Long, String>();    
    private int logMsgQueueSize = 0;
    //multithreading lock for flushing the log queue (writing to file)
    private Object logMsgModLock = new Object();
    
    private static final int maxLogMsgSize = 50;
    
    private static boolean termCondSet = false;
        
    /**
     * Private constructor
     */
    private MessageObject() {
        if(supportsANSITerm == null) {
            //if no console then doesn't support ANSI
//            if (System.console() == null) {            supportsANSITerm = false;} 
//            else 
            {                
                //if windows
                String osName = System.getProperty("os.name");
                if (osName.startsWith("Windows")) {
                    // Check for Windows 10 or later (crude check)
                    supportsANSITerm = (osName.startsWith("Windows 10") || osName.startsWith("Windows 11") || osName.startsWith("Windows Server"));
                } else {
                    // Assume ANSI support for non-windows
                    supportsANSITerm = true;
                }
            }            
        }
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
            msgObj.dispConsoleInfoMessage("MessageObject", "constructor", "Object created with supportsANSITerm == "+msgObj.supportsANSITerm);    
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
        MsgOutputMethod outputMethodEnum = _outputMethod >= MsgOutputMethod.ConsoleAndLogToFile.ordinal() 
                ? MsgOutputMethod.ConsoleAndLogToFile : (_outputMethod<=MsgOutputMethod.Console.ordinal()) 
                        ? MsgOutputMethod.Console : MsgOutputMethod.getEnumByIndex(_outputMethod);
        setOutputMethod(_fileName, outputMethodEnum);
    }
    
    /**
     * define how the messages from this and all other messageObj should be handled, and pass a file name if a log is to be saved
     * @param _fileName
     * @param _outputMethod
     */
    public synchronized void setOutputMethod(String _fileName, MsgOutputMethod _outputMethod) {
        fileName = _fileName;
        if((fileName == null) || (fileName.length() < 3)) {outputMethod = MsgOutputMethod.Console;fileIO = null;}
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
            outputMethod = _outputMethod;
            if (fileIO==null) {    fileIO = new FileIOManager(this, "Logger");}    
        }
        _dispMessage_base_console("MessageObject","setOutputMethod","Setting log level :  "+ outputMethod.toString() + " | File name specified for log (if used) : " + fileName +" | File IO Object created : " + (fileIO !=null), MsgCodes.info1,true);
    }//setOutputMethod
    
    
    /**
     * Returns whether or not the currently running system supports ANSI encoding for console display.
     * @return
     */
    public boolean getSupportsANSITerm() {        return supportsANSITerm;    }

    
    /**
     * finish any logging and write to file - this should be done when program closes
     */
    public void FinishLog() {
        if((outputMethod == MsgOutputMethod.Console) || (logMsgQueueSize==0)) { return;}
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
    public String getCurrWallTimeAndTimeFromStart() {return timerMgr.getWallTimeAndTimeFromStart(dispDelims[0]);}
    public String getCurrWallTime() { return timerMgr.getCurrWallTime();}
    public String getElapsedTimeStrForTimer(String timerName) { return timerMgr.getElapsedTimeStrForTimer(timerName);}
    public String getDateTimeStringForFileName() {return timerMgr.getDateTimeStringForFileName();}
    /**
     * pass an array to display
     * @param _callingClass
     * @param _callingMethod
     * @param _sAra
     * @param _perLine
     */
    public void dispMessageAra(String _callingClass, String _callingMethod, String[] _sAra, int _perLine) {
        _dispMessageAraInternal( _sAra,  _callingClass, _callingMethod, _perLine,  MsgCodes.info1, true, outputMethod);
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
        _dispMessageAraInternal( _sAra,  _callingClass, _callingMethod, _perLine,  useCode, true, outputMethod);
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
        _dispMessageAraInternal( _sAra,  _callingClass, _callingMethod, _perLine,  useCode, onlyConsole, outputMethod);
    }
    
    //pass single-line messages - only 1 display of timestamp and class/method prefix
    /**
     * default debug message
     * @param srcClass name of calling class
     * @param srcMethod name of calling method
     * @param msgText message text to print
     */
    public void dispDebugMessage(String srcClass, String srcMethod, String msgText){    _dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.debug1,true, outputMethod);    }    
    /**
     * default info message
     * @param srcClass name of calling class
     * @param srcMethod name of calling method
     * @param msgText message text to print
     */
    public void dispInfoMessage(String srcClass, String srcMethod, String msgText){        _dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.info1,true, outputMethod);    }    
    /**
     * default warning message
     * @param srcClass name of calling class
     * @param srcMethod name of calling method
     * @param msgText message text to print
     */
    public void dispWarningMessage(String srcClass, String srcMethod, String msgText){    _dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.warning1,true, outputMethod);    }    
    /**
     * default error message
     * @param srcClass name of calling class
     * @param srcMethod name of calling method
     * @param msgText message text to print
     */
    public void dispErrorMessage(String srcClass, String srcMethod, String msgText){    _dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.error1,true, outputMethod);    }    
    
    /////////////////////////
    // Console-only message displays    
    /**
     * default info message to console regardless of log level specified
     * @param srcClass name of calling class
     * @param srcMethod name of calling method
     * @param msgText message text to print
     */
    public void dispConsoleDebugMessage(String srcClass, String srcMethod, String msgText){        _dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.debug1,true, MsgOutputMethod.Console);    }    
    /**
     * default info message to console regardless of log level specified
     * @param srcClass name of calling class
     * @param srcMethod name of calling method
     * @param msgText message text to print
     */
    public void dispConsoleInfoMessage(String srcClass, String srcMethod, String msgText){        _dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.info1,true, MsgOutputMethod.Console);    }    
    /**
     * default warning message to console regardless of log level specified
     * @param srcClass name of calling class
     * @param srcMethod name of calling method
     * @param msgText message text to print
     */
    public void dispConsoleWarningMessage(String srcClass, String srcMethod, String msgText){    _dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.warning1,true, MsgOutputMethod.Console);    }    
    /**
     * default error message to console regardless of log level specified
     * @param srcClass name of calling class
     * @param srcMethod name of calling method
     * @param msgText message text to print
     */
    public void dispConsoleErrorMessage(String srcClass, String srcMethod, String msgText){    _dispMessage_base(srcClass, srcMethod,msgText, MsgCodes.error1,true, MsgOutputMethod.Console);    }    

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
    public String updateConsoleStrs(){    return consoleStrings.poll();}//updateConsoleStrs
    /**
     * return current array deque of console strings, to be printed to screen, as an array of strings
     * @return
     */
    public String[] getConsoleStringsAsArray() {        return consoleStrings.toArray(new String[0]);    }
    
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
        String clrStr = useCode.getColorCode();
        if (clrStr != null) {        return clrStr + src + ConsoleCLR.RESET.toString();}
        return src;
    }//_processMsgCode

    private void _dispMessage_base(String srcClass, String srcMethod, String msgText, MsgCodes useCode, boolean onlyConsole, MsgOutputMethod outputMethod) {        
        switch(outputMethod) {
        case Console :{
            if(useCode.getOrdinal() < this.minConsoleDispCode.getOrdinal()) {return;}
            _dispMessage_base_console(srcClass, srcMethod,msgText, useCode, onlyConsole);break;}    //just console
        case LogToFile :{
            if(useCode.getOrdinal() < this.minLogDispCode.getOrdinal()) {return;}
            _dispMessage_base_log(srcClass, srcMethod,msgText, useCode);break;}            //just log file
        case ConsoleAndLogToFile :{    //both log and console
            if(useCode.getOrdinal() >= this.minConsoleDispCode.getOrdinal()) {
                _dispMessage_base_console(srcClass, srcMethod,msgText, useCode, onlyConsole);    
            }
            if(useCode.getOrdinal() >= this.minLogDispCode.getOrdinal()) {
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
    private void _dispMessageAraInternal(String[] _sAra, String _callingClass, String _callingMethod, int _perLine, MsgCodes useCode, boolean onlyConsole, MsgOutputMethod outputMethod) {            
        switch(outputMethod) {
            case Console :{//just console
                if(useCode.getOrdinal() < this.minConsoleDispCode.getOrdinal()) {return;}
                for(int i=0;i<_sAra.length; i+=_perLine){
                    String s = buildLine(_sAra, _perLine, i);
                    _dispMessage_base_console(_callingClass, _callingMethod, s, useCode,onlyConsole);
                }            
                break;}            //just console
            case LogToFile :{//just log file
                if(useCode.getOrdinal() < this.minLogDispCode.getOrdinal()) {return;}
                for(int i=0;i<_sAra.length; i+=_perLine){
                    String s = buildLine(_sAra, _perLine, i);
                    _dispMessage_base_log(_callingClass, _callingMethod, s, useCode);
                }            
                break;}            //just log file
            case ConsoleAndLogToFile :{    //both log and console
                if(useCode.getOrdinal() < this.minConsoleDispCode.getOrdinal()) {
                    //neither mechanism should process this message
                    if(useCode.getOrdinal() < this.minLogDispCode.getOrdinal()) {return;} 
                    //use only log output for this message
                    for(int i=0;i<_sAra.length; i+=_perLine){
                        String s = buildLine(_sAra, _perLine, i);
                        _dispMessage_base_log(_callingClass, _callingMethod, s, useCode);
                    }                        
                } else if(useCode.getOrdinal() < this.minLogDispCode.getOrdinal()) {
                    //use only console output for this message
                    for(int i=0;i<_sAra.length; i+=_perLine){
                        String s = buildLine(_sAra, _perLine, i);
                        _dispMessage_base_console(_callingClass, _callingMethod, s, useCode,onlyConsole);
                    }                    
                } else {
                    for(int i=0;i<_sAra.length; i+=_perLine){
                        String s = buildLine(_sAra, _perLine, i);
                        _dispMessage_base_console(_callingClass, _callingMethod, s, useCode,onlyConsole);
                        _dispMessage_base_log(_callingClass, _callingMethod, s, useCode);
                    }
                }
                break;}            //both log and console
        }//switch
    }//dispMessageAra
    
    private String _buildDispMessage(String srcClass, String srcMethod, String msgText, MsgCodes useCode, String[] delims) {
        String timeStr = timerMgr.getWallTimeAndTimeFromStart(delims[0]);
        return timeStr + delims[0] +useCode.getPrefixStr() +delims[0] + srcClass + delims[1] + srcMethod + delims[2] + msgText;        
    }
    
    private void _dispMessage_base_console(String srcClass, String srcMethod, String msgText, MsgCodes useCode, boolean onlyConsole) {    
        String baseStr = _buildDispMessage(srcClass, srcMethod, msgText, useCode, dispDelims);
        String msg = _processMsgCode(baseStr, useCode);
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
        String baseStr = _buildDispMessage(srcClass, srcMethod, msgText, useCode, logDelims);
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
        for(Long key : logMsgQueue.keySet()) {    outList.add(logMsgQueue.get(key));}
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
                consoleStrings.add(res[i]);        //add console string output to screen display- decays over time
            }
        }
    }//printAndBuildConsoleStrs
    
}//messageObject
