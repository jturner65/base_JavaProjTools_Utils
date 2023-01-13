package base_Utils_Objects.appManager.argParse.cmdLineArgs;

import java.util.ArrayList;

import base_Utils_Objects.appManager.argParse.cmdLineArgs.base.CmdLineArgType;

public class CmdLineCompTypeArg<T extends Comparable<T>> extends CmdLineTypeArg<T> {
	
	/**
	 * Set min and max value for range
	 */
	protected ArrayList<T> range;
	
	/**
	 * Whether the choices are a range. If true, the first value and last value in choices will be only values used
	 */
	private boolean isRange;
	
	/**
	 * Set command line argument descriptive values. Uses the default delimiter character
	 * @param _cmdChar
	 * @param _cmdString
	 * @param _destString
	 * @param _helpString
	 * @param _type 
	 */
	public CmdLineCompTypeArg(char _cmdChar, String _cmdString, String _destString, String _helpString, CmdLineArgType _type) {
		super(_cmdChar, _cmdString, _destString, _helpString, _type);
		initCompCmd();
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
	public CmdLineCompTypeArg(char _delim, char _cmdChar, String _cmdString, String _destString, String _helpString, CmdLineArgType _type) {
		super(_delim, _cmdChar, _cmdString, _destString, _helpString, _type);
		initCompCmd();
	}
	
	//only call from ctor.  All initial setup for this object
	private void initCompCmd() {
		range = new ArrayList<T>(2);
		isRange = false;
		isComparable = true;
	}

	
	/**
	 * Set min and max values for range
	 * @param min
	 * @param max
	 */
	public void setCmndLineRange(T min, T max) {
		range.clear();
		range.add(0, min);
		range.add(1, max);
		isRange = range.size() == 2;
	}
	
	/**
	 * Whether or not the choices list hold a range of values
	 */
	public boolean isRange() {return isRange;}
	
	/**
	 * Retrieve a 2-element array list of min and max choices to use as range
	 */
	public ArrayList<T> getCmndLineRange(){		return range;}

}//class CmdLineCompTypeArg
