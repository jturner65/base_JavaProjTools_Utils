package base_Utils_Objects.AppManager.argParse.cmdLineArgs;

import base_Utils_Objects.AppManager.argParse.cmdLineArgs.base.Base_CmdLineTypeArg;
import base_Utils_Objects.AppManager.argParse.cmdLineArgs.base.CmdLineArgType;

public class StringCmdLineArg extends Base_CmdLineTypeArg<String> {

	public StringCmdLineArg(char _delim, char _cmdChar, String _cmdString, String _destString, String _helpString) {
		super( _delim, _cmdChar, _cmdString, _destString, _helpString,CmdLineArgType.StringType);
	}
	
	public StringCmdLineArg(char _cmdChar, String _cmdString, String _destString, String _helpString) {
		super(_cmdChar, _cmdString, _destString, _helpString,CmdLineArgType.StringType);
	}

}//class stringCmdLineArg
