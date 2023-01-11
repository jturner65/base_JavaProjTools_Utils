package base_Utils_Objects.AppManager.argParse.cmdLineArgs.base;

import java.util.HashMap;
import java.util.Map;

/**
 * enum used to specify types of command line values supported by argparse4j
 * @author john turner 
 */
public enum CmdLineArgType {
	StringType(0), 
	CharType(1), 
	IntType(2), 
	FloatType(3), 
	DoubleType(4), 
	BoolType(5);
	private int value; 
	private String[] _typeExplanation = new String[] {
			"String-based command line arg",
			"Character-based command line arg",
			"Integer-based command line arg",
			"Float-based command line arg",
			"Double-based command line arg",
			"Boolean-based command line arg"};
	private static String[] _typeName = new String[] {"String Type","Char Type","Integer Type","Float Type","Double Type","Boolean Type"};
	public static String[] getListOfTypes() {return _typeName;}
	private static Map<Integer, CmdLineArgType> map = new HashMap<Integer, CmdLineArgType>(); 
	static { for (CmdLineArgType enumV : CmdLineArgType.values()) { map.put(enumV.value, enumV);}}
	private CmdLineArgType(int _val){value = _val;} 
	public int getVal(){return value;}
	public static CmdLineArgType getVal(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}						//get # of values in enum
	public String getName() {return _typeName[value];}
	@Override
    public String toString() { return ""+value + ":"+_typeExplanation[value]; }	
	

}
