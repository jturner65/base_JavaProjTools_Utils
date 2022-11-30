package base_Utils_Objects.io;

import java.io.*;
import java.util.*;

import base_Utils_Objects.io.messaging.MessageObject;
import base_Utils_Objects.io.messaging.MsgCodes;

/**
 * This class will manage file IO
 * @author john
 *
 */
public class FileIOManager{
	//msg object interface
	protected MessageObject msg;
	//name of owning class of the instance of this object, for display
	protected final String owner;
	
	public FileIOManager(MessageObject _msg, String _owner) {msg=_msg;owner=_owner; }	
	/**
	 * write data to file
	 * @param fname file name
	 * @param data array of data
	 * @return success
	 */
	public boolean saveStrings(String fname, String[] data) { return saveStrings( fname, data, false);}
	/**
	 * write data to file
	 * @param fname file name
	 * @param data array of data
	 * @param append if should be appended to existing file
	 * @return success 
	 */
	public boolean saveStrings(String fname, String[] data, boolean append) {
		boolean success = false;
		PrintWriter pw = null;
		try {
		     File file = new File(fname);
		     FileWriter fw = new FileWriter(file, append);
		     pw = new PrintWriter(fw);
		     for (int i=0;i<data.length;++i) { pw.println(data[i]);}
		     success = true;
		} catch (IOException e) {	e.printStackTrace(); success = false;}
		finally {			if (pw != null) {pw.close();}}
		return success;
	}//saveStrings
	/**
	 * write data to file
	 * @param fname file name
	 * @param data arraylist of data
	 * @return success
	 */
	public boolean saveStrings(String fname, ArrayList<String> data) { return saveStrings( fname, data, false);}
	/**
	 * write data to file
	 * @param fname file name
	 * @param data arraylist of data
	 * @param append if should be appended to existing file
	 * @return success 
	 */
	public boolean saveStrings(String fname, ArrayList<String> data, boolean append) {
		boolean success = false;
		PrintWriter pw = null;
		try {
		     File file = new File(fname);
		     FileWriter fw = new FileWriter(file, append);
		     pw = new PrintWriter(fw);
		     for (int i=0;i<data.size();++i) { pw.println(data.get(i));}
		     success = true;
		} catch (IOException e) {	
			e.printStackTrace();
			msg.dispConsoleErrorMessage("FileIOManager ("+owner +")", "saveStrings", "Saving " +data.size() + " strings to " + fname + " failed");
			success = false;
		}
		finally {			if (pw != null) {pw.close();}}
		return success;
	}//saveStrings
	
	/**
	 * Load the passed filename into an array of strings, which is returned
	 * @param fileName fully qualified name of the file
	 * @param dispYesStr message to display if file loaded successfully
	 * @param dispNoStr message to display if load fails
	 * @return array of strings from the file
	 */
	public String[] loadFileIntoStringAra(String fileName, String dispYesStr, String dispNoStr) {
		try {
			return _loadFileIntoStringAra(fileName, dispYesStr, dispNoStr);
		} catch (Exception e) {
			e.printStackTrace(); 
			msg.dispMessage("FileIOManager ("+owner +")", "_loadFileIntoStringAra","!!"+dispNoStr, MsgCodes.error3);
		} 
		return new String[0];
	}
	/**
	 * stream read the csv file and build the data objects
	 * @param fileName
	 * @param dispYesStr
	 * @param dispNoStr
	 * @return
	 * @throws IOException
	 */
	private String[] _loadFileIntoStringAra(String fileName, String dispYesStr, String dispNoStr) throws IOException {		
		FileInputStream inputStream = null;
		Scanner sc = null;
		List<String> lines = new ArrayList<String>();
		String[] res = null;
	    //int line = 1, badEntries = 0;
		try {
		    inputStream = new FileInputStream(fileName);
		    sc = new Scanner(inputStream);
		    while (sc.hasNextLine()) {lines.add(sc.nextLine()); }
		    //Scanner suppresses exceptions
		    if (sc.ioException() != null) { throw sc.ioException(); }
		    msg.dispMessage("FileIOManager ("+owner +")", "_loadFileIntoStringAra",dispYesStr+"\tLength : " + lines.size(), MsgCodes.info3);
		    res = lines.toArray(new String[0]);		    
		} catch (Exception e) {	
			e.printStackTrace();
			msg.dispMessage("FileIOManager ("+owner +")", "_loadFileIntoStringAra","!!"+dispNoStr, MsgCodes.error3);
			res= new String[0];
		} 
		finally {
		    if (inputStream != null) {inputStream.close();		    }
		    if (sc != null) { sc.close();		    }
		}
		return res;
	}//loadFileContents	
	
	/**
	 * load into multiple arrays for multi-threaded processing
	 * @param fileName
	 * @param dispYesStr
	 * @param dispNoStr
	 * @param numHdrLines
	 * @param numThds
	 * @return
	 */
	public String[][] loadFileIntoStringAra_MT(String fileName, String dispYesStr, String dispNoStr, int numHdrLines, int numThds) {
		try {return _loadFileIntoStringAra_MT(fileName, dispYesStr, dispNoStr, numHdrLines, numThds);} 
		catch (Exception e) {
			e.printStackTrace();
			msg.dispMessage("FileIOManager ("+owner +")", "_loadFileIntoStringAra","!!"+dispNoStr, MsgCodes.error3);
		} 
		return new String[0][];
	}
	/**
	 * load files into multiple arrays for multi-threaded processing
	 * @param fileName
	 * @param dispYesStr
	 * @param dispNoStr
	 * @param numHdrLines
	 * @param numThds
	 * @return
	 * @throws IOException
	 */
	private String[][] _loadFileIntoStringAra_MT(String fileName, String dispYesStr, String dispNoStr, int numHdrLines, int numThds) throws IOException {		
		FileInputStream inputStream = null;
		Scanner sc = null;
		boolean saveHeader = (numHdrLines > 0);
		@SuppressWarnings("unchecked")
		List<String>[] lines = new ArrayList[numThds];
		for (int i=0;i<numThds;++i) {lines[i]=new ArrayList<String>();	}
		//add extra spot for header(s) if present
		
		String[][] res = new String[numThds+(saveHeader ? 1 : 0)][];
		String[] hdrRes = new String[numHdrLines];
		int idx = 0, count = 0;
		try {
		    inputStream = new FileInputStream(fileName);
		    sc = new Scanner(inputStream);
		    if (saveHeader) {for(int i=0;i<numHdrLines;++i) {    	hdrRes[i]=sc.nextLine();   }}
		    while (sc.hasNextLine()) {
		    	lines[idx].add(sc.nextLine()); 
		    	idx = (idx + 1)%numThds;
		    	++count;
		    }
		    //Scanner suppresses exceptions
		    if (sc.ioException() != null) { throw sc.ioException(); }
		    msg.dispMultiLineInfoMessage("FileIOManager ("+owner +")", "_loadFileIntoStringAra_MT",
		    		dispYesStr+"\n\tLength : " + count + " lines distributed into "+numThds+" arrays" + (saveHeader ? "\n\twith "+numHdrLines+" header lines saved as last idx in array" : "") + ".");
		    for (int i=0;i<lines.length;++i) {res[i] = lines[i].toArray(new String[0]);	 }
		    res[res.length-1]=hdrRes;
		} catch (Exception e) {	
			e.printStackTrace();
			msg.dispMessage("FileIOManager ("+owner +")", "_loadFileIntoStringAra_MT","!!"+dispNoStr, MsgCodes.error2);
			res= new String[0][];
		} 
		finally {
		    if (inputStream != null) {inputStream.close();		    }
		    if (sc != null) { sc.close();		    }
		}
		return res;
	}//_loadFileIntoStringAra_MT

}//class fileIOManager

