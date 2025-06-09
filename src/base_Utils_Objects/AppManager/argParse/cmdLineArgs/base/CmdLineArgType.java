package base_Utils_Objects.appManager.argParse.cmdLineArgs.base;

import java.util.HashMap;
import java.util.Map;

/**
 * enum used to specify types of command line values supported by argparse4j
 * @author john turner 
 */
public enum CmdLineArgType {
	StringType, 
	CharType, 
	IntType, 
	FloatType, 
	DoubleType, 
	BoolType;
	private final String[] _typeExplanation = new String[]{
			"String-based command line arg",
			"Character-based command line arg",
			"Integer-based command line arg",
			"Float-based command line arg",
			"Double-based command line arg",
			"Boolean-based command line arg"};
	private static final String[] _typeName = new String[]{"String Type","Char Type","Integer Type","Float Type","Double Type","Boolean Type"};
	public static String[] getListOfTypes() {return _typeName;}
	private static Map<Integer, CmdLineArgType> map = new HashMap<Integer, CmdLineArgType>(); 
	static { for (CmdLineArgType enumV : CmdLineArgType.values()) { map.put(enumV.ordinal(), enumV);}}
	public int getOrdinal() {return ordinal();}
	public static CmdLineArgType getEnumByIndex(int idx){return map.get(idx);}
	public static CmdLineArgType getEnumFromValue(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}						//get # of values in enum
	public String getName() {return _typeName[ordinal()];}
	@Override
    public String toString() { return ""+_typeExplanation[ordinal()] + "("+ordinal()+")"; }	
    public String toStrBrf() { return ""+_typeExplanation[ordinal()]; }	

}
