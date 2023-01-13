package base_Utils_Objects.appManager;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import base_Utils_Objects.io.messaging.MessageObject;
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
	//public final MessageObject msgObj;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Misc Fields

	/**
	 * runtime arguments key-value pair
	 */
	protected TreeMap<String, Object> argsMap;

	public Java_AppManager() {
		//project arguments
		argsMap = new TreeMap<String,Object>();
		//single point manager for datetime tracking for application		
		timeMgr = TimerManager.getInstance();
	}
		
	/**
	 * Returns milliseconds that have passed since application began
	 * @return
	 */
	public int timeSinceStart() {return (int)(timeMgr.getMillisFromProgStart());}
	
	/**
	 * Returns a copy of the arguments used to launch the program (intended to be read-only)
	 * @return
	 */
	public TreeMap<String, Object> getArgsMap(){
		return new TreeMap<String, Object>(argsMap);
	}

	/**
	 * Build runtime argument map, either from command-line arguments (for console applications) or from specifications in UI-based instancing AppManager
	 * @param passedArgs
	 */	
	protected abstract void handleRuntimeArgs(String[] passedArgs);
	
	/**
	 * Set various relevant runtime arguments in argsMap based on specified command line args.  May override values from command line
	 * @param _passedArgsMap current command-line arguments as key-value pairs
	 */
	protected abstract TreeMap<String,Object> setRuntimeArgsVals(Map<String, Object> _passedArgsMap);

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
	
	
}//class Java_AppManager
