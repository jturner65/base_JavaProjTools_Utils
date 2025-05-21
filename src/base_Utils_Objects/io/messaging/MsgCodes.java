package base_Utils_Objects.io.messaging;

import java.util.*;

/**
 * enum delineates each kind of message to be displayed - various information and error codes
 * @author John Turner
 *
 */
public enum MsgCodes{
	// values for msg codes are the console color mappings used to display the message to ANSI-capable terminals
	debug1(""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.WHITE),
	debug2(""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.CYAN),
	debug3(""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.YELLOW),
	debug4(""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.GREEN),
	debug5(""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.CYAN_BOLD),
	info1(""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLACK),
	info2(""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.GREEN),              
	info3(""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.YELLOW),		       
	info4(""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLUE),               
	info5(""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.MAGENTA),	           
	warning1(""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLACK_BOLD),      
	warning2(""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLUE_BOLD),	   
	warning3(""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLACK_UNDERLINED),
	warning4(""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLUE_UNDERLINED),	
	warning5(""+ConsoleCLR.WHITE_BACKGROUND+ConsoleCLR.BLUE_BRIGHT),     
	error1(""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.RED_UNDERLINED),	   
	error2(""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.RED_BOLD),		   
	error3(""+ConsoleCLR.RED_BACKGROUND_BRIGHT+ConsoleCLR.BLACK_BOLD),	
	error4(""+ConsoleCLR.WHITE_BACKGROUND_BRIGHT+ConsoleCLR.RED_BRIGHT),	
	error5(""+ConsoleCLR.BLACK_BACKGROUND+ConsoleCLR.RED_BOLD_BRIGHT);
	
	private static Map<Integer, MsgCodes> map = new HashMap<Integer, MsgCodes>(20); 
	static { for (MsgCodes enumV : MsgCodes.values()) { map.put(enumV.ordinal(), enumV);}}
	// code to display in every message denoting the type and level of message being displayed
	private static Map<MsgCodes, String> prefixMap = new HashMap<MsgCodes,String>(20);
	static {
		prefixMap.put(debug1,"DBG_1 ");
		prefixMap.put(debug2,"DBG_2 ");
		prefixMap.put(debug3,"DBG_3 ");
		prefixMap.put(debug4,"DBG_4 ");
		prefixMap.put(debug5,"DBG_5 ");
		prefixMap.put(info1,"INFO_1");
		prefixMap.put(info2,"INFO_2");
		prefixMap.put(info3,"INFO_3");
		prefixMap.put(info4,"INFO_4");
		prefixMap.put(info5,"INFO_5");
		prefixMap.put(warning1,"WARN_1");
		prefixMap.put(warning2,"WARN_2");
		prefixMap.put(warning3,"WARN_3");
		prefixMap.put(warning4,"WARN_4");
		prefixMap.put(warning5,"WARN_5");
		prefixMap.put(error1,"ERR_1 ");
		prefixMap.put(error2,"ERR_2 ");
		prefixMap.put(error3,"ERR_3 ");
		prefixMap.put(error4,"ERR_4 ");
		prefixMap.put(error5,"ERR_5 ");
	}

	private final String value;
	private MsgCodes(String _val){value = _val;} 
	public String getColorCode() {		return value;	}
	public static String getMsgColorCode(MsgCodes msgCode) {return msgCode.value;	}
	public String getVal(){return value;}
	public int getOrdinal() {return ordinal();}
	public String getPrefixStr() {return prefixMap.get(this);}
	public static String getMsgPrefixStr(MsgCodes msgCode) {return prefixMap.get(msgCode);}
	public static MsgCodes getEnumByIndex(int idx){return map.get(idx);}
	public static MsgCodes getEnumFromValue(int idx){return map.get(idx);}
	public static int getNumVals(){return map.size();}						//get # of values in enum
	@Override
    public String toString() { return ""+this.name(); }	
}