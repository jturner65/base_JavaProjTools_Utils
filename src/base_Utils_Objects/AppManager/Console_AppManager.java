package base_Utils_Objects.appManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import base_Utils_Objects.appManager.argParse.CmdLineArgMgr;
import base_Utils_Objects.appManager.argParse.cmdLineArgs.base.Base_CmdLineArg;
import base_Utils_Objects.appManager.argParse.cmdLineArgs.base.CmdLineArgType;

/**
 * Manage features of a console application
 * @author John Turner
 *
 */
public abstract class Console_AppManager extends Java_AppManager {
	/**
	 * Command line argument parsing/handling
	 */
	protected final CmdLineArgMgr argParseMgr;
	
	public Console_AppManager() {
		super();
		//argument parser and manager
		argParseMgr = new CmdLineArgMgr(this);
	}
	
	
	/**
	 * Build list of command line argument descriptions specific to this project
	 * @return
	 */
	protected abstract ArrayList<Base_CmdLineArg> getCommandLineParserAttributes();
	
	/**
	 * Invoke the application main function - this is called from instancing Console_AppManager class
	 * @param <T>
	 * @param _appMgr
	 * @param passedArgs
	 */
	public static <T extends Console_AppManager> void invokeMain(T _appMgr, String[] _passedArgs) {
		_appMgr.handleRuntimeArgs(_passedArgs);
	}
	
	/**
	 * Build runtime argument map, either from command-line arguments (for console applications) or from specifications in UI-based instancing AppManager
	 * @param passedArgs
	 */	
	@Override
	protected final void handleRuntimeArgs(String[] passedArgs) {
		ArrayList<Base_CmdLineArg> cmdLineDesc = getCommandLineParserAttributes();
		TreeMap<String, Object> rawArgsMap = new TreeMap<String, Object>();
		if((cmdLineDesc != null) && (cmdLineDesc.size()>0)){
	        rawArgsMap = argParseMgr.getCmndLineArgs(passedArgs, cmdLineDesc);
		}
		//possibly override arguments from arg parser within application
        argsMap = setRuntimeArgsVals(rawArgsMap);
    }//handleRuntimeArgs
	
	/**
	 * Build a command-line argument descriptor of a String type
	 * @param _cmdChar
	 * @param _cmdStr
	 * @param _destStr
	 * @param _helpStr
	 * @param _defaultValue
	 * @param _choices
	 * @return
	 */
	public Base_CmdLineArg buildStringCommandLineArgDesc(char _cmdChar, String _cmdStr, String _destStr, String _helpStr, String _defaultValue, Collection<String> _choices) {
		return argParseMgr.buildCommandLineArgDesc(String.class, CmdLineArgType.StringType, _cmdChar, _cmdStr, _destStr, _helpStr, _defaultValue, _choices);
	}
	/**
	 * Build a command-line argument descriptor of a Integer type
	 * @param _cmdChar
	 * @param _cmdStr
	 * @param _destStr
	 * @param _helpStr
	 * @param _defaultValue
	 * @param _choices
	 * @return
	 */
	public Base_CmdLineArg buildIntCommandLineArgDesc(char _cmdChar, String _cmdStr, String _destStr, String _helpStr, Integer _defaultValue, Collection<Integer> _choices, Integer[] _bounds) {
		return argParseMgr.buildCommandLineCompArgDesc(Integer.class, CmdLineArgType.IntType, _cmdChar, _cmdStr, _destStr, _helpStr, _defaultValue, _choices, _bounds);
	}
	/**
	 * Build a command-line argument descriptor of a Float type
	 * @param _cmdChar
	 * @param _cmdStr
	 * @param _destStr
	 * @param _helpStr
	 * @param _defaultValue
	 * @param _choices
	 * @return
	 */
	public Base_CmdLineArg buildFloatCommandLineArgDesc(char _cmdChar, String _cmdStr, String _destStr, String _helpStr, Float _defaultValue, Collection<Float> _choices, Float[] _bounds) {
		return argParseMgr.buildCommandLineCompArgDesc(Float.class, CmdLineArgType.FloatType, _cmdChar, _cmdStr, _destStr, _helpStr, _defaultValue, _choices, _bounds);
	}
	/**
	 * Build a command-line argument descriptor of a Double type
	 * @param _cmdChar
	 * @param _cmdStr
	 * @param _destStr
	 * @param _helpStr
	 * @param _defaultValue
	 * @param _choices
	 * @return
	 */
	public Base_CmdLineArg buildDoubleCommandLineArgDesc(char _cmdChar, String _cmdStr, String _destStr, String _helpStr, Double _defaultValue, Collection<Double> _choices, Double[] _bounds) {
		return argParseMgr.buildCommandLineCompArgDesc(Double.class, CmdLineArgType.DoubleType, _cmdChar, _cmdStr, _destStr, _helpStr, _defaultValue, _choices, _bounds);
	}
	/**
	 * Build a command-line argument descriptor of a Character type
	 * @param _cmdChar
	 * @param _cmdStr
	 * @param _destStr
	 * @param _helpStr
	 * @param _defaultValue
	 * @param _choices
	 * @return
	 */
	public Base_CmdLineArg buildCharCommandLineArgDesc(char _cmdChar, String _cmdStr, String _destStr, String _helpStr, Character _defaultValue, Collection<Character> _choices) {
		return argParseMgr.buildCommandLineArgDesc(Character.class, CmdLineArgType.CharType, _cmdChar, _cmdStr, _destStr, _helpStr, _defaultValue, _choices);
	}
	/**
	 * Build a command-line argument descriptor of a Boolean type
	 * @param _cmdChar
	 * @param _cmdStr
	 * @param _destStr
	 * @param _helpStr
	 * @param _defaultValue
	 * @param _choices
	 * @return
	 */
	public Base_CmdLineArg buildBoolCommandLineArgDesc(char _cmdChar, String _cmdStr, String _destStr, String _helpStr, Boolean _defaultValue, Collection<Boolean> _choices) {
		return argParseMgr.buildCommandLineArgDesc(Boolean.class, CmdLineArgType.BoolType, _cmdChar, _cmdStr, _destStr, _helpStr, _defaultValue, _choices);
	}

	
		
}//class Console_AppManager
