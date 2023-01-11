package base_Utils_Objects.AppManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import base_Utils_Objects.AppManager.argParse.cmdLineArgs.base.Base_CmdLineArg;
import base_Utils_Objects.AppManager.argParse.cmdLineArgs.base.Base_CmdLineTypeArg;
import base_Utils_Objects.io.messaging.MessageObject;
import base_Utils_Objects.timer.TimerManager;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Build a java application which does not require UI functionality.  GUI_AppManager inherits from this class
 * @author John Turner
 *
 */

public abstract class Java_AppManager {
	////////////////////////////////////////////////////////////////////////////////////////////////////	
	//platform independent path separator
	public final String dirSep = File.separator;
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Time and date
	/**
	 * Single program-wide manager for time functions. Records beginning of execution
	 */
	public final TimerManager timeMgr;
	/**
	 * Single, program-wide manager of display and log messages TODO
	 */
	//public final MessageObject msgObj;
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// Misc Fields

	/**
	 * runtime arguments key-value pair
	 */
	private TreeMap<String, Object> argsMap;
	/**
	 * Parser to manage command line arguments
	 */
	private ArgumentParser argParser;

	public Java_AppManager() {
		//project arguments
		argsMap = new TreeMap<String,Object>();
		//single point manager for datetime tracking for application		
		timeMgr = TimerManager.getInstance();
	}

		
	/**
	 * Returns milliseconds that have passed since application began
	 * @return
	 */
	public int timeSinceStart() {return (int)(timeMgr.getMillisFromProgStart());}
	
	/**
	 * Returns a copy of the arguments used to launch the program (intended to be read-only)
	 * @return
	 */
	public TreeMap<String, Object> getArgsMap(){
		return new TreeMap<String, Object>(argsMap);
	}
	
	/**
	 * Inheriting classes need to call this to process command line arguments
	 * @param <T>
	 * @param _appMgr
	 * @param passedArgs
	 */
	public static <T extends Java_AppManager> void processArgs(T _appMgr, String[] passedArgs) {
		_appMgr.handleRuntimeArgs(passedArgs);
	}
	
	/**
	 * Build argument parser for application and handle 
	 * @param passedArgs
	 */
	
	protected void handleRuntimeArgs(String[] passedArgs) {
		ArrayList<Base_CmdLineArg> _cmdLineDesc = getCommandLineParserAttributes();
		HashMap<String, Object> rawArgsMap = new HashMap<String, Object>();
		if((_cmdLineDesc != null) && (_cmdLineDesc.size()>0)){
			argParser = buildArgParser(_cmdLineDesc);
			Namespace args = null;
	        try {
	        	args = argParser.parseArgs(passedArgs);
	        } catch (ArgumentParserException e) {          
	        	argParser.handleError(e);   
	        	System.exit(1);
	        }
	        rawArgsMap = (HashMap<String, Object>) args.getAttrs();
		}
		//possibly override arguments from arg parser within application
        argsMap = setRuntimeArgsVals(rawArgsMap);
    }//handleRuntimeArgs
	
	/**
	 * This method builds a command-line argument parser specific to the application
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ArgumentParser buildArgParser(ArrayList<Base_CmdLineArg> _cmdLineDesc) {
		ArgumentParser parser = ArgumentParsers.newFor(getPrjNmShrt())
				.build().defaultHelp(true).description(getPrjDescr());
		for(Base_CmdLineArg argObj : _cmdLineDesc) {
			var parserArg = parser.addArgument(argObj.getCmdChar(), argObj.getCmdString())
					.dest(argObj.getDestString())
					.help(argObj.getHelpString());
			switch(argObj.getCmdType()) {
				case StringType:{
					setClassAndDefault(String.class, parserArg, (Base_CmdLineTypeArg<String>) argObj);
					break;}
				case CharType:{
					setClassAndDefault(Character.class, parserArg, (Base_CmdLineTypeArg<Character>) argObj);
					break;}
				case IntType:{
					setClassAndDefault(Integer.class, parserArg, (Base_CmdLineTypeArg<Integer>) argObj);
					break;}
				case FloatType:{
					setClassAndDefault(Float.class, parserArg, (Base_CmdLineTypeArg<Float>) argObj);
					break;}
				case DoubleType:{
					setClassAndDefault(Double.class, parserArg, (Base_CmdLineTypeArg<Double>) argObj);
					break;}
				case BoolType:{
					setClassAndDefault(Boolean.class, parserArg, (Base_CmdLineTypeArg<Boolean>) argObj);
					break;}
			}
		}	
		return parser;
	}//buildArgParser
	
	private <T> void setClassAndDefault(Class<T> _class, Argument parserArg, Base_CmdLineTypeArg<T> argObj) {
		if(_class == Boolean.class) {
			//strict cmd line boolean handling
			parserArg.type(Arguments.booleanType());
		} else {
			parserArg.type(_class);
		}
		//set default if exists
		if(argObj.hasDefaultValue()) {
			parserArg.setDefault(argObj.getDefaultVal());
		}
		//set choices values if present
		if(argObj.hasChoices()) {
			parserArg.choices(argObj.getCmndLineChoices());
		}
	}//setClassAndDefault
	

	
	/**
	 * Build list of command line argument descriptions
	 * @return
	 */
	protected abstract ArrayList<Base_CmdLineArg> getCommandLineParserAttributes();
	
	/**
	 * Set various relevant runtime arguments in argsMap based on specified command line args
	 * @param _passedArgsMap command-line arguments as key-value pairs
	 */
	protected abstract TreeMap<String,Object> setRuntimeArgsVals(Map<String, Object> _passedArgsMap);
	

	
	/**
	 * Implementation-specified short name of project
	 * @return
	 */	
	protected abstract String getPrjNmShrt();
	
	/**
	 * Implementation-specified descriptive name of project
	 * @return
	 */
	protected abstract String getPrjNmLong();
	
	/**
	 * Implementation-specified description of project
	 * @return
	 */	
	protected abstract String getPrjDescr();
	
	
	
}//class Java_AppManager
