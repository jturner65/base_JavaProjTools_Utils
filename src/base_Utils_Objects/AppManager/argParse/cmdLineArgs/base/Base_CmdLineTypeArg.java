package base_Utils_Objects.AppManager.argParse.cmdLineArgs.base;

import java.util.ArrayList;

/**
 * Class provides base functionality to handle command line argument configuration, to be consumed by argparse4j
 * @author John Turner
 *
 */
public abstract class Base_CmdLineTypeArg<T> extends Base_CmdLineArg{
	/**
	 * Potential choices of a value of type T
	 */
	protected ArrayList<T> choices;
	
	private boolean hasChoices;
	
	/**
	 * A default value if none is specified
	 */
	protected T defaultVal;
	
	private boolean hasDefaultVal;
	
	
	/**
	 * Set command line argument descriptive values. Uses the default delimiter character
	 * @param _cmdChar
	 * @param _cmdString
	 * @param _destString
	 * @param _helpString
	 * @param _type 
	 */
	public Base_CmdLineTypeArg(char _cmdChar, String _cmdString, String _destString, String _helpString, CmdLineArgType _type) {
		super(_cmdChar, _cmdString, _destString, _helpString, _type);
		initCmd();
	}

	/**
	 * Set command line argument descriptive values
	 * @param _delim
	 * @param _cmdChar
	 * @param _cmdString
	 * @param _destString
	 * @param _helpString
	 * @param _type 
	 */	
	public Base_CmdLineTypeArg(char _delim, char _cmdChar, String _cmdString, String _destString, String _helpString, CmdLineArgType _type) {
		super(_delim, _cmdChar, _cmdString, _destString, _helpString, _type);
		initCmd();
	}
	
	//only call from ctor.  All initial setup for this object
	private void initCmd() {
		choices = new ArrayList<T>();
		hasDefaultVal = false;
		hasChoices = false;
	}
		
	/**
	 * Whether or not this object has a default value
	 * @return
	 */
	public boolean hasDefaultValue() {return hasDefaultVal;}
	
	/**
	 * Set the default value of this object
	 * @param _val
	 */
	public void setDefaultVal(T _val) {defaultVal = _val;hasDefaultVal = true;}
	
	/**
	 * Get the default value of this object if one exists, otherwise returns null
	 * @return
	 */
	public T getDefaultVal() {return hasDefaultVal ? defaultVal : null;}
	
	/**
	 * Set list of possible choice values for this command line argument
	 * @param _choices
	 */
	@SuppressWarnings("unchecked")
	public void setCmndLineChoices(T... _choices) {
		choices.clear();
		for(T val : _choices) {	choices.add(val);}
		hasChoices = choices.size() > 0;
	}
	
	/**
	 * Whether or not specific choices were provided for cmd line arg
	 * @return
	 */
	public boolean hasChoices() {return hasChoices;}
	
	/**
	 * Retrieve list of possible choice values. If list is size == 0 then no choices are specified.
	 * @return
	 */
	public ArrayList<T> getCmndLineChoices(){return choices;}
	

}//class Base_CmdLineTypeArg
