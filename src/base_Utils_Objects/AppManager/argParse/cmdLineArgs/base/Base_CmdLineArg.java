package base_Utils_Objects.AppManager.argParse.cmdLineArgs.base;

public abstract class Base_CmdLineArg {
	/**
	 * Type of this command line arg
	 */
	private CmdLineArgType cmdType;
	
	/**
	 * Delimiter
	 */
	private static char delimChar = '-';

	/**
	 * Argument character delimiter (i.e. "-x")
	 */
	private char cmdChar;
	/**
	 * Argument string delimiter (i.e. "--test_val")
	 */
	private String cmdString;
	
	/**
	 * String describing the destination of the value
	 */
	private String destString;
	
	/**
	 * Help string
	 */
	private String helpString;

	/**
	 * Set command line argument descriptive values
	 * @param _delim
	 * @param _cmdChar use char only, do not include delimiter
	 * @param _cmdString use string only, do not include delimiter
	 * @param _destString
	 * @param _helpString
	 * @param _type 
	 */	
	public Base_CmdLineArg(char _delim, char _cmdChar, String _cmdString, String _destString, String _helpString, CmdLineArgType _type) {
		delimChar = _delim;
		cmdChar = _cmdChar;
		cmdString = _cmdString;
		destString = _destString;
		helpString = _helpString;
		cmdType = _type;
	}

	/**
	 * Set command line argument descriptive values. Uses the default delimiter character
	 * @param _cmdChar use char only, do not include delimiter
	 * @param _cmdString use string only, do not include delimiter
	 * @param _destString
	 * @param _helpString
	 * @param _type 
	 */
	public Base_CmdLineArg(char _cmdChar, String _cmdString, String _destString, String _helpString, CmdLineArgType _type) {
		this(delimChar, _cmdChar, _cmdString, _destString, _helpString, _type);
	}
	
	/**
	 * Get the type of this command line argument
	 * @return
	 */
	public CmdLineArgType getCmdType() {return cmdType;}
	
	/**
	 * @return the cmdChar
	 */
	public String getCmdChar() {return ""+delimChar+""+cmdChar;}

	/**
	 * @return the cmdString
	 */
	public String getCmdString() {return ""+delimChar+""+""+delimChar+""+cmdString;}

	/**
	 * @return the destString
	 */
	public String getDestString() {return destString;}

	/**
	 * @return the helpString
	 */
	public String getHelpString() {return helpString;}
	
}//class Base_CmdLineArg
