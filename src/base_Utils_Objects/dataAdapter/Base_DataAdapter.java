package base_Utils_Objects.dataAdapter;

import java.util.HashMap;
import java.util.Map;

import base_Utils_Objects.tools.flags.Base_BoolFlags;

/**
 * This class will provide a mechanism to share data (i.e. from a UI) within different components of an application.
 * @author John Turner
 *
 */
public abstract class Base_DataAdapter {	
	/**
	 * map to hold UI-driven int values, using the UI object idx's as keys
	 */
	protected Map<Integer, Integer> intValues;
	/**
	 * map to hold UI-driven float values, using the UI object idx's as keys
	 */
	protected Map<Integer, Float> floatValues;
	/**
	 * map to hold UI-driven boolean values, using the UI object flags' idx's as keys 
	 */
	protected Map<Integer, Boolean> boolValues;	
	public Base_DataAdapter() {initMaps();}
	public Base_DataAdapter(Map<Integer, Integer> _iVals, Map<Integer, Float> _fVals, Map<Integer, Boolean> _bVals) {
		initMaps();
		setAllVals(_iVals, _fVals, _bVals);
	}
	
	public Base_DataAdapter(Base_DataAdapter _otr) {
		initMaps();
		setAllVals(_otr);
	}
	
	protected final void initMaps() {
		intValues = new HashMap<Integer, Integer>();
		floatValues = new HashMap<Integer, Float>(); 
		boolValues = new HashMap<Integer, Boolean>();
	}
	
	public final void setAllVals(Base_DataAdapter _otr) {
		setAllVals(_otr.intValues,_otr.floatValues,_otr.boolValues);		
	}
	
	public final void setAllVals(Map<Integer, Integer> _intValues, Map<Integer, Float> _floatValues,Map<Integer, Boolean> _boolValues) {
		if(_intValues!=null) {for (Map.Entry<Integer, Integer> entry : _intValues.entrySet()) {intValues.put(entry.getKey(), entry.getValue());}}
		if(_floatValues!=null) {for (Map.Entry<Integer, Float> entry : _floatValues.entrySet()) {floatValues.put(entry.getKey(), entry.getValue());}}
		if(_boolValues!=null) {for (Map.Entry<Integer, Boolean> entry : _boolValues.entrySet()) {boolValues.put(entry.getKey(), entry.getValue());}}
	}
	
	public final boolean compareIntValue(Integer idx, Integer value) {	return (intValues.get(idx) != null) && (intValues.get(idx).equals(value));	}
	public final boolean compareFloatValue(Integer idx, Float value) {	return (floatValues.get(idx) != null) && (floatValues.get(idx).equals(value));	}
	public final boolean compareBoolValue(Integer idx, Boolean value) {	return (boolValues.get(idx) != null) && (boolValues.get(idx).equals(value));	}

	
	/**
	 * Getters
	 */
	public final boolean getBoolValue(int idx) {return boolValues.get(idx);}
	public final int getIntValue(int idx) {return intValues.get(idx);  }
	public final float getFloatValue(int idx) {return floatValues.get(idx);  }
	
	/**
	 * Shortcut to retrieve debug state.
	 * @return
	 */
	public final boolean getIsDebug() {return boolValues.get(Base_BoolFlags.debugIDX);}
	
	/**
	 * Setters
	 */	
	public final void setIntValue(Integer idx, Integer value){	intValues.put(idx,value);  }
	public final void setFloatValue(Integer idx, Float value){	floatValues.put(idx,value);}
	public final void setBoolValue(Integer idx, Boolean value){	boolValues.put(idx,value);}
	
	/**
	 * Shortcut to set debug state.
	 */
	public final void setIsDebug(Boolean value) {boolValues.put(Base_BoolFlags.debugIDX,value);}
	
	/**
	 * Shortcut to conditionally set debug state.
	 */
	public final boolean checkAndSetIsDebug(Boolean value) {
		return checkAndSetBoolValue(Base_BoolFlags.debugIDX,value);
	}
	/**
	 * Initialize the first num boolean values to false
	 * @param num how many boolean flags should be initialized to false
	 */
	public final void initAllBoolsToFalse(int num) {
		for(int idx = 0;idx<num;++idx) {setBoolValue(idx, false);}
	}
	
	/**
	 * Check thatMap vs thisMap to determine if they are different
	 * @param <T>
	 * @param idxsToIgnore any map keys to ignore
	 * @param thisMap map to compare
	 * @param thatMap map to compare
	 * @return whether there are differences in the two maps
	 */
	protected final <T extends Comparable<T>> boolean checkMapIsChangedExcludeIDXs(HashMap<Integer,Integer> idxsToIgnore, Map<Integer, T> thisMap, Map<Integer, T> thatMap ) {
	    if (thisMap.size() != thatMap.size()) {	        return true;    }
	    //either values match or key is in idxsToIgnore for comparison. Want inverse to show the map is changed
	    return ! (thisMap.entrySet().stream().allMatch(e -> (e.getValue().equals(thatMap.get(e.getKey())) || idxsToIgnore.containsKey(e.getKey()))));
	}//checkMapIsChangedExcludeIDXs
	
	/**
	 * Checks for changes between thatMap and thisMap at passed idxs.
	 * @param <T>
	 * @param idxsToCheck idxs to check for changes
	 * @param thisMap map to compare
	 * @param thatMap map to compare
	 * @return whether there are differences in the two maps
	 */
	protected final <T extends Comparable<T>> boolean checkMapIsChangedAtIDXs(HashMap<Integer,Integer> idxsToCheck, Map<Integer, T> thisMap, Map<Integer, T> thatMap ) {
		//if no idxs to check, nothing has changed.
	    if (idxsToCheck.size() == 0) {					return false;}
		if (thisMap.size() != thatMap.size()) {	        return true;    }
	    //check idxsToCheck contains the key and values are equal. if true for all keys then did not change
	    return ! (thisMap.entrySet().stream().allMatch(e -> ((idxsToCheck.containsKey(e.getKey()) && e.getValue().equals(thatMap.get(e.getKey()))) 
	    		|| !(idxsToCheck.containsKey(e.getKey())))));
	}//checkMapIsChangedExcludeIDXs
	
	
	/**
	 * Return whether or not the values are different in this updater and the passed updater, 
	 * excluding passed IDXs. Assumes they are both of the same type.
	 * @param _otr passed updater to verify this against
	 * @param IntIdxsToIgnore indexes/keys of integer values to ignore
	 * @param FloatIdxsToIgnore indexes/keys of float values to ignore
	 * @param BoolIdxsToIgnore indexes/keys of boolean values to ignore
	 * @return whether or not updaters' data are different
	 */
	protected final boolean haveValuesChangedExceptPassed(Base_DataAdapter _otr, 
			HashMap<Integer,Integer> IntIdxsToIgnore, 
			HashMap<Integer,Integer> FloatIdxsToIgnore, 
			HashMap<Integer,Integer> BoolIdxsToIgnore) {	
		if (checkMapIsChangedExcludeIDXs(IntIdxsToIgnore, intValues, _otr.intValues)) {		return true;}
		if (checkMapIsChangedExcludeIDXs(FloatIdxsToIgnore, floatValues, _otr.floatValues)) {	return true;}
		if (checkMapIsChangedExcludeIDXs(BoolIdxsToIgnore, boolValues, _otr.boolValues)) {		return true;}		
		return false;
	}//haveValuesChangedExceptPassed
	/**
	 * Return whether or not the values at the passed idxs have changed.
	 * @param _otr
	 * @param IntIdxsToCheck
	 * @param FloatIdxsToCheck
	 * @param BoolIdxsToCheck
	 * @return
	 */
	protected final boolean havePassedValuesChanged(Base_DataAdapter _otr, 
			HashMap<Integer,Integer> IntIdxsToCheck, 
			HashMap<Integer,Integer> FloatIdxsToCheck, 
			HashMap<Integer,Integer> BoolIdxsToCheck) {
		if (checkMapIsChangedAtIDXs(IntIdxsToCheck, intValues, _otr.intValues)) {		return true;}
		if (checkMapIsChangedAtIDXs(FloatIdxsToCheck, floatValues, _otr.floatValues)) {	return true;}
		if (checkMapIsChangedAtIDXs(BoolIdxsToCheck, boolValues, _otr.boolValues)) {		return true;}		
		return false;
	}
	
	/**
	 * this will check if bool value is different than previous value, and if so will change it
	 * @param idx
	 * @param val
	 * @return whether new value was set
	 */	
	public final boolean checkAndSetBoolValue(int idx, boolean value) {if(!compareBoolValue(idx, value)) {boolValues.put(idx,value); return true;}return false;}
	/**
	 * this will check if int value is different than previous value, and if so will change it
	 * @param idx
	 * @param val
	 * @return whether new value was set
	 */	
	public final boolean checkAndSetIntVal(int idx, int value) {if(!compareIntValue(idx, value)) {intValues.put(idx,value); return true;}return false;}
	/**
	 * this will check if float value is different than previous value, and if so will change it
	 * @param idx
	 * @param val
	 * @return whether new value was set
	 */	
	public final boolean checkAndSetFloatVal(int idx, float value) {if(!compareFloatValue(idx, value)) {floatValues.put(idx,value);return true;}return false;}
	
	/**
	 * Boolean value updater, as well as call instancing class's updater
	 */
	public final void updateBoolValue(int idx, boolean value) {
		setBoolValue(idx, value);
		updateBoolValue_Indiv(idx, value);
	}	
	/**
	 * Boolean value updater - this will update the owning window's corresponding data values as well
	 */
	protected abstract void updateBoolValue_Indiv(int idx, boolean value);
	
	/**
	 * Integer value updater, as well as call instancing class's updater
	 */	
	public final void updateIntValue(int idx, Integer value) {
		setIntValue(idx,value);
		updateIntValue_Indiv(idx, value);
	}
	/**
	 * Integer value updater - this will update the owning window's corresponding data values as well
	 */	
	protected abstract void updateIntValue_Indiv(int idx, Integer value);
	/**
	 * Float value updater, as well as call instancing class's updater
	 */	
	public final void updateFloatValue(int idx, Float value) {
		setFloatValue(idx,value);
		updateFloatValue_Indiv(idx, value);
	}
	/**
	 * Float value updater - this will update the owning window's corresponding data values as well
	 */	
	protected abstract void updateFloatValue_Indiv(int idx, Float value);
	
	/**
	 * Return this updater's owner's name
	 * @return
	 */
	public abstract String getName();
	
	@Override
	public String toString() {
		String res = "Owning Window Name: "+getName()+" | Tracked values : "+intValues.size() +" Integers, " +floatValues.size() +" Floats, " +boolValues.size() + " Booleans\n";
		if (intValues.size() > 0) {
			res +="Int Values: (" +intValues.size() +")\n";
			for (Map.Entry<Integer, Integer> entry : intValues.entrySet()) {
				res += "\tKey : "+entry.getKey()+" | Value : "+entry.getValue()+"\n";
			}
		} else {		res+="No Integer values present/being tracked";	}
		if (floatValues.size() > 0) {
			res+="Float Values: (" +floatValues.size() +")\n";
			for (Map.Entry<Integer, Float> entry : floatValues.entrySet()) {
				res += "\tKey : "+entry.getKey()+" | Value : "+entry.getValue()+"\n";
			}
		} else {		res+="No Float values present/being tracked";	}
		if (boolValues.size() > 0) {	
			res+="Boolean Values: (" +boolValues.size() +")\n";
			for (Map.Entry<Integer, Boolean> entry : boolValues.entrySet()) {
				res += "\tKey : "+entry.getKey()+" | Value : "+entry.getValue()+"\n";
			}	
		} else {		res+="No Boolean values present/being tracked";	}
		
		return res;
	}
}
