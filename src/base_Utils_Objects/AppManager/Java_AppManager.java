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
	 * Whether or not a physical display is connected to the system this is running on.
	 */
	protected final boolean _isHeadless;
	/**
	 * Whether this application has a graphical UI or is strictly a console application
	 */
	public final boolean hasGraphicalUI;
	
	/**
	 * Maximum memory the JVM can expand to if necessary.
	 */
	public final Long maxJVMMem;
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Misc Fields

	/**
	 * runtime arguments key-value pair
	 */
	protected HashMap<String, Object> argsMap;
	
	public Java_AppManager(boolean _hasGraphicalUI) {
		var ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		_isHeadless = ge.isHeadlessInstance();
		//get primary monitor size if not headless
		if(!_isHeadless) {
			var dispMode = ge.getDefaultScreenDevice().getDisplayMode();
			_displayWidth = dispMode.getWidth();
			_displayHeight = dispMode.getHeight();
		} else {
			//Currently do not support headless execution. TODO
			_displayWidth = 0;
			_displayHeight = 0;
		}
		maxJVMMem = Runtime.getRuntime().maxMemory();
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
	 * Display the current machine data and memory layout on launch, if specified
	 */
	public final void viewMachineData() {
		if (!showMachineData()){return;}
		showDebugMachineData();
	}
	
	/**
	 * Returns the number of processors available to the JVM.
	 * 
	 * Applications built off Java_AppManager often use this value as a gauge of
	 * how many threads to instantiate. 
	 * 
	 * This value may change during a particular invocation of the virtual machine.
	 * Applications that are sensitive to the number of available processors should
	 * therefore occasionally poll this property and adjust their resource usage 
	 * appropriately.
	 *  
	 * @return
	 */
	public static final int getNumThreadsAvailable() {return Runtime.getRuntime().availableProcessors();}
	
	//Used by memory status array
	public static final int maxMemIDX = 0;
	public static final int allocMemIDX = 1;
	public static final int growthMemIDX = 2;
	public static final int freeMemIDX = 3;
	public static final int usedMemIDX = 4;

	
	public static final String[] memDispText = new String[] {
		"Max Memory Available to JVM:",           
		"Total Allocated Memory to JVM:",       
		"Available Memory for the JVM to grow:",
		"Current Available Free Memory:",         
		"Current Used Memory:"                
	};
	
	public static final String[] memDispTextAbbrev = new String[] {
			"Max JVM","TTL JVM","Space JVM","Free","Used"
	};
	
	/**
	 * Return a map of the JVM memory status, in bytes
	 * 	'maxMemIDX' :  maximum amount of memory that the JVM will attempt to use
	 * 	'allocMemIDX' : total amount of memory currently allocated to the JVM
	 *  'growthMemIDX' : max - alloc - amount of memory growth available to JVM
	 * 	'freeMemIDX' : amount of free memory in the JVM
	 * 	'usedMemIDX' : alloc - free - amount of memory used by program
	 * @return 
	 */
	public final Long[] getMemoryStatusMap() {
		Runtime runtime = Runtime.getRuntime(); 
		Long allocMem = runtime.totalMemory(), freeMem = runtime.freeMemory();
		Long[] res = new Long[] {
				// JVM/Heap stats
				maxJVMMem,
				allocMem,
				(maxJVMMem - allocMem),
				// Program-related stats
				freeMem,
				(allocMem-freeMem)
		};
		return res;
	}//getMemoryStatusMap
		
	/**
	 * Display the current machine data and memory layout either on launch, or on debug
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
					msgObj.dispInfoMessage("Java_AppManager", className,"\t"+propKeyVals.getKey()+": `"+System.getProperty(propKeyVals.getValue())+"`");
				}
				msgObj.dispInfoMessage("Java_AppManager", className,"");
			}
		}
		
		msgObj.dispInfoMessage("Java_AppManager", className,"\tNumber of Available Processors: "+runtime.availableProcessors());		
		if (_isHeadless) {
			msgObj.dispInfoMessage("Java_AppManager", className,"-----No Display Connected-(Headless Execution)------------------------");
		} else {
			msgObj.dispInfoMessage("Java_AppManager", className,"-----Display----------------------------------------------------------");
			msgObj.dispInfoMessage("Java_AppManager", className,"\tWidth:"+_displayWidth+" | Height:"+_displayHeight);			
		}
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
		var memMap = getMemoryStatusMap();
		float div = 1024.0f * 1024.0f;
		for(int i=0;i< memMap.length; ++i) { 
			msgObj.dispInfoMessage("Java_AppManager", className,"\t"+ String.format("%-40s", memDispText[i]) +String.format("%.3f", memMap[i]/div)+" MB");
		}
	}//checkMemorySetup
	

}//class Java_AppManager
