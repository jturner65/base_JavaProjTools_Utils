package base_Utils_Objects.AppManager;

import java.io.File;
import java.util.TreeMap;

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
	private TreeMap<String, Object> argsMap;

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
	
	
	protected void setArgsMap(TreeMap<String, Object> _argsMap) {
		argsMap = _argsMap;
	}
	
	/**
	 * Inheriting classes need to call this to process command line arguments
	 * @param <T>
	 * @param _appMgr
	 * @param passedArgs
	 */
	public static <T extends Java_AppManager> void processArgs(T _appMgr, String[] passedArgs) {
		_appMgr.setRuntimeArgsVals(passedArgs);
	}
	
	/**
	 * Set various relevant runtime arguments in argsMap
	 * @param _passedArgs command-line arguments
	 */
	protected abstract void setRuntimeArgsVals(String[] _passedArgs);
	
}//class Java_AppManager
