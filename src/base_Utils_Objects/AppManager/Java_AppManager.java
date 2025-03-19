package base_Utils_Objects.appManager;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
	 * physical display width and height this project is running on
	 */
	protected final int _displayWidth, _displayHeight;
		
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
		var ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		//get primary monitor size				
		if(!ge.isHeadlessInstance()) {
			var dispMode = ge.getDefaultScreenDevice().getDisplayMode();
			_displayWidth = dispMode.getWidth();
			_displayHeight = dispMode.getHeight();
		} else {
			//Currently do not support headless execution. TODO
			_displayWidth = 0;
			_displayHeight = 0;
		}
		//Whether the implementation is Console_AppManager or GUI_AppManager
		hasGraphicalUI = _hasGraphicalUI;
		//project arguments
		argsMap = new HashMap<String,Object>();
		//single point manager for datetime tracking for application		
		timeMgr = TimerManager.getInstance();
		//build this application's message object and specify whether or not it should support graphical UI
		msgObj = MessageObject.getInstance();
		msgObj.setHasGraphics(hasGraphicalUI);
		viewMachineData();
		MsgCodes minConsole = getMinConsoleMsgCodes();
		if (minConsole != null) {msgObj.setMinConsoleOutputLevel(minConsole);}
		MsgCodes minLog = getMinLogMsgCodes(); 
		if (minLog != null) {msgObj.setMinLogOutputLevel(minLog);}
	}//ctor

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
	
	/**
	 * Whether or not we should show the machine data on launch
	 * @return
	 */
	protected abstract boolean showMachineData();
	
	protected void setArgsMap(String[] _passedArgs) {
		var tmpArgsMap = buildCmdLineArgs(_passedArgs);
		//programmatic overrides to console args - specified in instancing AppManager
		argsMap = setRuntimeArgsVals(tmpArgsMap);
	}
	///////////////////////////////////////////////////
	// Math utilities

	
	
	///////////////////////////////////////////////////
	// Misc utilities
	
	/**
	 * display the current machine data and memory layout on launch, if specified
	 */
	public final void viewMachineData() {
		if (!showMachineData()){return;}
		showDebugMachineData();
	}
	
	/**
	 * display the current machine data and memory layout either on launch, or on debug
	 */
	public final void showDebugMachineData() {
		Runtime runtime = Runtime.getRuntime(); 
		String className = getPrjNmShrt();
		msgObj.dispInfoMessage("Java_AppManager", className,"-Host Machine Data----------------------------------------------------");
		msgObj.dispInfoMessage("Java_AppManager", className,"--System---------------------------------------------------------------");
		TreeMap<String, TreeMap<String,String>> keyMap = new TreeMap<String, TreeMap<String,String>>();
		keyMap.put("user", new TreeMap<String,String>());
		keyMap.put("java", new TreeMap<String,String>());
		keyMap.put("sun", new TreeMap<String,String>());
		keyMap.put("misc", new TreeMap<String,String>());
		for (var propertyKeyName : System.getProperties().keySet()){
			String propName = propertyKeyName.toString();
			boolean found = false;
			for(var keyVals : keyMap.entrySet()) {
				String fullKey = keyVals.getKey();
				if(propName.startsWith(fullKey)) {keyVals.getValue().put(propName.split(fullKey+".")[1],propName);found=true;break;}
			}
			if(!found && !(propName.contains("line.separator"))) {keyMap.get("misc").put(propName,propName);}
        }
		for(var keyVals : keyMap.entrySet()) {
			var vals = keyVals.getValue();
			if(vals.size() > 0) {
				String typeKey = keyVals.getKey();
				msgObj.dispInfoMessage("Java_AppManager", className,"---------"+typeKey+"------------------------------------------------------");
				for(var propKeyVals : vals.entrySet()) {
					msgObj.dispInfoMessage("Java_AppManager", className,"\t"+typeKey+"."+propKeyVals.getKey()+": "+System.getProperty(propKeyVals.getValue()));
				}
				msgObj.dispInfoMessage("Java_AppManager", className,"");
			}
		}
		
		msgObj.dispInfoMessage("Java_AppManager", className,"\tNumber of Available Processors: "+runtime.availableProcessors());		
		msgObj.dispInfoMessage("Java_AppManager", className,        "-----Display----------------------------------------------------------");
		msgObj.dispInfoMessage("Java_AppManager", className,"\tWidth:"+_displayWidth+" | Height:"+_displayHeight);		
		msgObj.dispInfoMessage("Java_AppManager", className,"-Java VM Runtime Data-------------------------------------------------");
		msgObj.dispInfoMessage("Java_AppManager", className,"-----Java Version-----------------------------------------------------");
		var vers = Runtime.version();
        StringBuilder sb = new StringBuilder(vers.version().stream().map(Object::toString).collect(Collectors.joining(".")));
        sb.append("|");
        vers.pre().ifPresent(v -> sb.append("Pre-Release: ").append(v).append("|"));

        if (vers.build().isPresent()) {
            sb.append("Build: ").append(vers.build().get());
            if (vers.optional().isPresent()) {sb.append(":").append(vers.optional().get());}
        } else {
            if (vers.optional().isPresent()) {
                sb.append(vers.pre().isPresent() ? ":" : "Info:").append(vers.optional().get());
            }
        }
		msgObj.dispInfoMessage("Java_AppManager", className,"\tVersion: "+sb.toString());
		msgObj.dispInfoMessage("Java_AppManager", className,"-----Memory Layout----------------------------------------------------");  
		long maxMem = runtime.maxMemory(), allocMem = runtime.totalMemory(), freeMem = runtime.freeMemory();
		float freeMemKB = freeMem/1024.0f, allocMemKB = allocMem / 1024.0f, maxMemKB = maxMem/1024.0f, usedMemKB = (allocMemKB - freeMemKB);
		msgObj.dispInfoMessage("Java_AppManager", className,"\tMax mem Available: \t\t" + String.format("%.3f",maxMemKB/1024.0f)+" MB");  
		msgObj.dispInfoMessage("Java_AppManager", className,"\tCurrent Available Free mem: \t" + String.format("%.3f",freeMemKB/1024.0f) +" MB");  
		msgObj.dispInfoMessage("Java_AppManager", className,"\tTotal Allocated mem: \t\t" + String.format("%.3f",allocMemKB/1024.0f)+" MB");  
		msgObj.dispInfoMessage("Java_AppManager", className,"\tUsed mem: \t\t\t" + String.format("%.3f",usedMemKB/1024.0f)+" MB");  
		msgObj.dispInfoMessage("Java_AppManager", className,"\tTotal free mem: \t\t" +  String.format("%.3f",(maxMemKB - allocMemKB) / 1024.0f)+" MB"); 
	
	}//checkMemorySetup
	

}//class Java_AppManager
