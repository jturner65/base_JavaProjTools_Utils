package base_Utils_Objects.AppManager;

import base_UI_Objects.GUI_AppManager;

/**
 * Manage features of a console application
 * @author John Turner
 *
 */
public abstract class Console_AppManager extends Java_AppManager {
	/**
	 * 
	 */
	public Console_AppManager() {
		super();
	}

	/**
	 * Invoke the application main function - this is called from instancing Console_AppManager class
	 * @param <T>
	 * @param _appMgr
	 * @param passedArgs
	 */
	public static <T extends GUI_AppManager> void invokeMain(T _appMgr, String[] _passedArgs) {
		Java_AppManager.processArgs(_appMgr, _passedArgs);
	}
		
}//class Console_AppManager
