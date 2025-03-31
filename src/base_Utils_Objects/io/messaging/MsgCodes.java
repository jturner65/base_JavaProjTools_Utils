package base_Utils_Objects.io.messaging;

import java.util.*;

/**
 * enum delineates each kind of message to be displayed - various information and error codes
 * @author John Turner
 *
 */
public enum MsgCodes{
	debug1, debug2, debug3, debug4, debug5,
	info1, info2, info3, info4, info5,
	warning1, warning2, warning3, warning4, warning5,	
	error1, error2, error3, error4, error5;	
	
	private static Map<Integer, MsgCodes> map = new HashMap<Integer, MsgCodes>(); 
	static { for (MsgCodes enumV : MsgCodes.values()) { map.put(enumV.ordinal(), enumV);}}
	public int getVal(){return ordinal();}
	public static MsgCodes getEnumByIndex(int idx){return map.get(idx);}
	public static MsgCodes getEnumFromValue(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}						//get # of values in enum
	@Override
    public String toString() { return ""+this.name(); }	
}