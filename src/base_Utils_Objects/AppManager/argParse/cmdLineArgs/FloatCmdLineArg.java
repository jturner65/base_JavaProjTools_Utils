package base_Utils_Objects.AppManager.argParse.cmdLineArgs;

import base_Utils_Objects.AppManager.argParse.cmdLineArgs.base.Base_CmdLineTypeArg;
import base_Utils_Objects.AppManager.argParse.cmdLineArgs.base.CmdLineArgType;

/**
 * @author John Turner
 *
 */
public class FloatCmdLineArg extends Base_CmdLineTypeArg<Float> {

	public FloatCmdLineArg(char _delim, char _cmdChar, String _cmdString, String _destString, String _helpString) {
		super(_delim, _cmdChar, _cmdString, _destString, _helpString, CmdLineArgType.FloatType);
	}
	
	public FloatCmdLineArg(char _cmdChar, String _cmdString, String _destString, String _helpString) {
		super(_cmdChar, _cmdString, _destString, _helpString, CmdLineArgType.FloatType);
	}
	
}//class FloatCmdLineArg
