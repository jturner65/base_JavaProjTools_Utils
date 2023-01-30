package base_Utils_Objects.io.messaging;

import java.util.*;

/**
 * enum delineates each kind of message to be displayed - various information and error codes
 * @author John Turner
 *
 */
public enum MsgCodes{
	debug1(0),debug2(1),debug3(2), debug4(3), debug5(4),
	info1(5),info2(6),info3(7), info4(8), info5(9),
	warning1(10),warning2(11),warning3(12),warning4(13),warning5(14),	
	error1(15),error2(16),error3(17),error4(18),error5(19);	
	private int value; 
	
	private static Map<Integer, MsgCodes> map = new HashMap<Integer, MsgCodes>(); 
	static { for (MsgCodes enumV : MsgCodes.values()) { map.put(enumV.value, enumV);}}
	private MsgCodes(int _val){value = _val;} 
	public int getVal(){return value;}
	public static MsgCodes getVal(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}						//get # of values in enum
	@Override
    public String toString() { return ""+this.name(); }	
}