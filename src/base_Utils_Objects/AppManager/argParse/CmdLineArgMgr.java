package base_Utils_Objects.appManager.argParse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import base_Utils_Objects.appManager.Java_AppManager;
import base_Utils_Objects.appManager.argParse.cmdLineArgs.CmdLineCompTypeArg;
import base_Utils_Objects.appManager.argParse.cmdLineArgs.CmdLineTypeArg;
import base_Utils_Objects.appManager.argParse.cmdLineArgs.base.Base_CmdLineArg;
import base_Utils_Objects.appManager.argParse.cmdLineArgs.base.CmdLineArgType;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class CmdLineArgMgr {
	protected Java_AppManager appMgr;

	public CmdLineArgMgr(Java_AppManager _appMgr) {
		appMgr = _appMgr;
	}
	
	public final TreeMap<String, Object> getCmndLineArgs(String[] passedArgs, ArrayList<Base_CmdLineArg> _cmdLineDesc){
		ArgumentParser argParser = buildArgParser(_cmdLineDesc);
		Namespace args = null;
        try {
        	args = argParser.parseArgs(passedArgs);
        } catch (ArgumentParserException e) {          
        	argParser.handleError(e);   
        	System.exit(1);
        }
        return (TreeMap<String, Object>) args.getAttrs();
	}//getCmndLineArgs
	
	/**
	 * This method builds a command-line argument parser specific to the application
	 * @return an appropriately configured ArgumentParser
	 */
	@SuppressWarnings("unchecked")
	private ArgumentParser buildArgParser(ArrayList<Base_CmdLineArg> _cmdLineDesc) {
		ArgumentParser parser = ArgumentParsers.newFor(appMgr.getPrjNmShrt())
				.build().defaultHelp(true).description(appMgr.getPrjDescr());
		for(Base_CmdLineArg argObj : _cmdLineDesc) {
			var parserArg = parser.addArgument(argObj.getCmdChar(), argObj.getCmdString())
					.dest(argObj.getDestString())
					.help(argObj.getHelpString());
			switch(argObj.getCmdType()) {
				case StringType:{
					setClassAndDefault(String.class, parserArg, (CmdLineTypeArg<String>) argObj);
					break;}
				case CharType:{
					setClassAndDefault(Character.class, parserArg, (CmdLineTypeArg<Character>) argObj);
					break;}
				case IntType:{
					setClassAndDefault(Integer.class, parserArg, (CmdLineTypeArg<Integer>) argObj);
					break;}
				case FloatType:{
					setClassAndDefault(Float.class, parserArg, (CmdLineTypeArg<Float>) argObj);
					break;}
				case DoubleType:{
					setClassAndDefault(Double.class, parserArg, (CmdLineTypeArg<Double>) argObj);
					break;}
				case BoolType:{
					setClassAndDefault(Boolean.class, parserArg, (CmdLineTypeArg<Boolean>) argObj);
					break;}
			}
		}	
		return parser;
	}//buildArgParser
	
	/**
	 * Set type, default values and whether or not multiple choices are supported 
	 * @param <T>
	 * @param _class
	 * @param parserArg
	 * @param argObj
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> void setClassAndDefault(Class<T> _class, Argument parserArg, CmdLineTypeArg<T> argObj) {
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
			if((argObj.isComparable()) && (((CmdLineCompTypeArg)argObj).isRange()))  {
				setRangeForComparableArg(parserArg, (CmdLineCompTypeArg) argObj);				
			} else {
				parserArg.choices(argObj.getCmndLineChoices());
			}
		}
	}//setClassAndDefault
	
	private <T extends Comparable<T>> void setRangeForComparableArg(Argument parserArg, CmdLineCompTypeArg<T> argObj) {		
		var range = argObj.getCmndLineRange();
		parserArg.choices(Arguments.range(range.get(0), range.get(1)));
	}
	
	/**
	 * Build cmd line argument object
	 * @param <T>
	 * @param Class<T> _class class name of argument
	 * @param _cmdArgType enum describing type.
	 * @param _cmdChar
	 * @param _cmdStr
	 * @param _destStr
	 * @param _helpStr
	 * @param _defaultValue
	 * @param _choices
	 * @return
	 */
	public <T> Base_CmdLineArg buildCommandLineArgDesc(Class<T> _class, CmdLineArgType _cmdArgType, char _cmdChar, String _cmdStr, String _destStr, String _helpStr, T _defaultValue, Collection<T> _choices) {
		CmdLineTypeArg<T> arg = new CmdLineTypeArg<T>(_cmdChar, _cmdStr, _destStr, _helpStr, _cmdArgType);		
		if(_defaultValue != null) {
			arg.setDefaultVal(_defaultValue);
		}
		if((_choices != null) && (_choices.size() > 0)) {
			arg.setCmndLineChoices(_choices);			
		} 
		return arg;
	}//buildCommandLineArgDesc
	/**
	 * Build cmd line argument object for comparable type
	 * @param <T>
	 * @param Class<T> _class class name of argument
	 * @param _cmdArgType enum describing type.
	 * @param _cmdChar
	 * @param _cmdStr
	 * @param _destStr
	 * @param _helpStr
	 * @param _defaultValue
	 * @param _choices
	 * @param _bounds
	 * @return
	 */
	public <T extends Comparable<T>> Base_CmdLineArg buildCommandLineCompArgDesc(Class<T> _class, CmdLineArgType _cmdArgType, char _cmdChar, String _cmdStr, String _destStr, String _helpStr, T _defaultValue, Collection<T> _choices, T[] _bounds) {
		CmdLineCompTypeArg<T> arg = new CmdLineCompTypeArg<T>(_cmdChar, _cmdStr, _destStr, _helpStr, _cmdArgType);		
		if(_defaultValue != null) {
			arg.setDefaultVal(_defaultValue);
		}
		if((_choices != null) && (_choices.size() > 0)) {
			arg.setCmndLineChoices(_choices);			
		} else if((_bounds != null) && (_bounds.length == 2)) {
			arg.setCmndLineRange(_bounds[0],_bounds[1]);
		}
		return arg;
	}//buildCommandLineCompArgDesc

}//class CmdLineArgMgr
