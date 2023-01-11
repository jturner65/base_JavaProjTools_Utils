package base_Utils_Objects.AppManager.argParse.cmdLineArgs;

import base_Utils_Objects.AppManager.argParse.cmdLineArgs.base.Base_CmdLineTypeArg;
import base_Utils_Objects.AppManager.argParse.cmdLineArgs.base.CmdLineArgType;

public class DoubleCmdLineArg extends Base_CmdLineTypeArg<Double> {

	public DoubleCmdLineArg(char _cmdChar, String _cmdString, String _destString, String _helpString) {
		super(_cmdChar, _cmdString, _destString, _helpString, CmdLineArgType.DoubleType);
	}

	public DoubleCmdLineArg(char _delim, char _cmdChar, String _cmdString, String _destString, String _helpString) {
		super(_delim, _cmdChar, _cmdString, _destString, _helpString, CmdLineArgType.DoubleType);
	}

}//class DoubleCmdLineArg
