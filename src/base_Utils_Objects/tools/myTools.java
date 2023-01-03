package base_Utils_Objects.tools;

/**
 * Miscellaneous constants and useful functions 
 * @author John Turner
 *
 */
public class myTools {
	
	
	//no instancing
	private myTools() {}
	
    
    /**
     * Return a string representation of an array of byte digits concatenated, representing a very long number
     * @param vals
     * @return
     */
    public synchronized static String digitAraToString(byte[] vals) {
        StringBuilder b = new StringBuilder(vals.length);
        for (int i = 0;i< vals.length; i++) {b.append(vals[i]);}  	
        return b.toString();
    }
     

}//myTools
