package base_Utils_Objects.io.messaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum describing the possible output targets for log/console messages
 * @return
 */
public enum MsgOutputMethod {
	Console,
	LogToFile,
	ConsoleAndLogToFile;
	private final String[] _typeExplanation = new String[]{
			"Console output only.", "Log output only to file specified.", "Console output and log output to file."
	};	
	private static Map<Integer, MsgOutputMethod> map = new HashMap<Integer, MsgOutputMethod>(); 
	static { for (MsgOutputMethod enumV : MsgOutputMethod.values()) { map.put(enumV.ordinal(), enumV);}}
	public int getOrdinal() {return ordinal();}
	public static MsgOutputMethod getEnumByIndex(int idx){return map.get(idx);}
	public static MsgOutputMethod getEnumFromValue(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}						//get # of values in enum
	@Override
    public String toString() { return ""+this.name()+":"+_typeExplanation[ordinal()]; }	
    public String toStrBrf() { return ""+_typeExplanation[ordinal()]; }
}


		
		