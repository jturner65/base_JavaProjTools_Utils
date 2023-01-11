package base_Utils_Objects.AppManager.argParse.cmdLineArgs;

import base_Utils_Objects.AppManager.argParse.cmdLineArgs.base.Base_CmdLineTypeArg;
import base_Utils_Objects.AppManager.argParse.cmdLineArgs.base.CmdLineArgType;

/**
 * Integer command line argument
 * @author John Turner
 *
 */
public class IntCmdLineArg extends Base_CmdLineTypeArg<Integer> {		
	public IntCmdLineArg(char _delim, char _cmdChar, String _cmdString, String _destString, String _helpString) {
		super( _delim, _cmdChar, _cmdString, _destString, _helpString,CmdLineArgType.IntType);
	}
	
	public IntCmdLineArg(char _cmdChar, String _cmdString, String _destString, String _helpString) {
		super(_cmdChar, _cmdString, _destString, _helpString,CmdLineArgType.IntType);
	}


}//class intCmdLineArg
