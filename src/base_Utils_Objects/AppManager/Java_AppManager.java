package base_Utils_Objects.appManager;

import java.io.File;
import java.util.HashMap;

import base_Utils_Objects.io.messaging.MessageObject;
import base_Utils_Objects.io.messaging.MsgCodes;
import base_Utils_Objects.timer.TimerManager;

/**
 * Build a java application which does not require UI functionality.  GUI_AppManager inherits from this class
 * @author John Turner
 *
 */

public abstract class Java_AppManager {
	////////////////////////////////////////////////////////////////////////////////////////////////////	
	//platform independent path separator
	public final String dirSep = File.separator;
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Time and date
	/**
	 * Single program-wide manager for time functions. Records beginning of execution
	 */
	public final TimerManager timeMgr;

	/**
	 * Single, program-wide manager of display and log messages TODO
	 */
	public final MessageObject msgObj;
	
	/**
	 * Whether this application has a graphical UI or is strictly a console application
	 */
	public final boolean hasGraphicalUI;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Misc Fields

	/**
	 * runtime arguments key-value pair
	 */
	protected HashMap<String, Object> argsMap;
	
	public Java_AppManager(boolean _hasGraphicalUI) {
		hasGraphicalUI = _hasGraphicalUI;
		//project arguments
		argsMap = new HashMap<String,Object>();
		//single point manager for datetime tracking for application		
		timeMgr = TimerManager.getInstance();
		//build this application's message object and specify whether or not it should support graphical UI
		msgObj = MessageObject.getInstance();
		msgObj.setHasGraphics(hasGraphicalUI);
		MsgCodes minConsole = getMinConsoleMsgCodes();
		if (minConsole != null) {msgObj.setMinConsoleOutputLevel(minConsole);}
		MsgCodes minLog = getMinLogMsgCodes(); 
		if (minLog != null) {msgObj.setMinLogOutputLevel(minLog);}
	}//ctor
	
	/**
	 * Set minimum level of message object console messages to display for this application. If null then all messages displayed
	 * @return
	 */
	protected abstract MsgCodes getMinConsoleMsgCodes(); 
	/**
	 * Set minimum level of message object log messages to save to log for this application. If null then all messages saved to log.
	 * @return
	 */
	protected abstract MsgCodes getMinLogMsgCodes(); 
		
	/**
	 * Returns milliseconds that have passed since application began+
	 * @return
	 */
	public int timeSinceStart() {return (int)(timeMgr.getMillisFromProgStart());}
	
	/**
	 * Returns a copy of the arguments used to launch the program (intended to be read-only)
	 * @return
	 */
	public HashMap<String, Object> getArgsMap(){
		return new HashMap<String, Object>(argsMap);
	}
	
	/**
	 * Build runtime argument map from command-line arguments provided at launch
	 * @param passedArgs
	 */	
	protected abstract HashMap<String, Object> buildCmdLineArgs(String[] passedArgs);
	
	/**
	 * Set various relevant runtime arguments in argsMap programmatically.  May override values from command line
	 * @param _passedArgsMap current command-line arguments as key-value pairs
	 */
	protected abstract HashMap<String,Object> setRuntimeArgsVals(HashMap<String, Object> _passedArgsMap);

	/**
	 * Implementation-specified short name of project
	 * @return
	 */	
	public abstract String getPrjNmShrt();
	
	/**
	 * Implementation-specified descriptive name of project
	 * @return
	 */
	public abstract String getPrjNmLong();
	
	/**
	 * Implementation-specified description of project
	 * @return
	 */	
	public abstract String getPrjDescr();
	
	
	protected void setArgsMap(String[] _passedArgs) {
		var tmpArgsMap = buildCmdLineArgs(_passedArgs);
		//programmatic overrides to console args - specified in instancing AppManager
		argsMap = setRuntimeArgsVals(tmpArgsMap);
	}
	
	/**
	 * Invoke the application main function - this is called from instancing Console_AppManager class
	 * This will set the argsMap Hashmap of runtime arguments
	 * @param <T>
	 * @param _appMgr
	 * @param passedArgs
	 */
	protected static <T extends Java_AppManager> void invokeMain(T _appMgr, String[] _passedArgs) {
		_appMgr.setArgsMap(_passedArgs);
	}
	
}//class Java_AppManager
