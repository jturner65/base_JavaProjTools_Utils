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
    public static String digitAraToString(byte[] vals) {
        StringBuilder b = new StringBuilder(vals.length);
        for (int i = 0;i< vals.length; i++) {b.append(vals[i]);}  	
        return b.toString();
    }
    /**
     * Return a string holding the double values in the vals array of the form [(val0),(val1),...], using
     * the passed format string 
     * @param vals
     * @param fmt
     * @return
     */
    public static String arrayToString(double[] vals, String fmt) {
    	StringBuilder b = new StringBuilder(vals.length );
    	b.append("[");
    	b.append(String.format(fmt, vals[0]));
    	for(int i=1;i<vals.length;++i) { b.append(",");b.append(String.format(fmt, vals[i]));}
    	b.append("]");
    	return b.toString();
    }
 
    /**
     * Return a string holding the float values in the vals array of the form [(val0),(val1),...], using
     * the passed format string 
     * @param vals
     * @param fmt
     * @return
     */
    public static String arrayToString(float[] vals, String fmt) {
    	StringBuilder b = new StringBuilder(vals.length );
    	b.append("[");
    	b.append(String.format(fmt, vals[0]));
    	for(int i=1;i<vals.length;++i) { b.append(",");b.append(String.format(fmt, vals[i]));}
    	b.append("]");
    	return b.toString();
    }
 
    /**
     * Return a string holding the long values in the vals array of the form [(val0),(val1),...], using
     * the passed format string 
     * @param vals
     * @param fmt
     * @return
     */
    public static String arrayToString(long[] vals, String fmt) {
    	StringBuilder b = new StringBuilder(vals.length );
    	b.append("[");
    	b.append(String.format(fmt, vals[0]));
    	for(int i=1;i<vals.length;++i) { b.append(",");b.append(String.format(fmt, vals[i]));}
    	b.append("]");
    	return b.toString();
    }

    /**
     * Return a string holding the int values in the vals array of the form [(val0),(val1),...], using
     * the passed format string 
     * @param vals
     * @param fmt
     * @return
     */
    public static String arrayToString(int[] vals, String fmt) {
    	StringBuilder b = new StringBuilder(vals.length );
    	b.append("[");
    	b.append(String.format(fmt, vals[0]));
    	for(int i=1;i<vals.length;++i) { b.append(",");b.append(String.format(fmt, vals[i]));}
    	b.append("]");
    	return b.toString();
    }

}//myTools
