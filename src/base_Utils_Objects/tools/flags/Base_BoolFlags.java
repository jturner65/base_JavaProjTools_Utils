package base_Utils_Objects.tools.flags;

import java.util.TreeMap;

/**
 * This class will manage a boolean flag state machine consisting of bit strings
 * @author John Turner
 *
 */
public abstract class Base_BoolFlags {
	/**
	 * Arrays of ints, each index holds 32 bit strings
	 */
	private int[] flags;
	
	/**
	 * idxs in all flags
	 */
	public static final int
		debugIDX 						= 0;			//first idx is always for debug
	
	/**
	 * Number of flags defined in base class, available for all inheriting structures. 
	 * Subsequent flags need to increment from this value
	 */
	public static final int _numBaseFlags = 1;
	
	/**
	 * Number of flags this structure tracks
	 */
	public final int numFlags;
	
	public Base_BoolFlags(int _numFlags) {
		numFlags =_numFlags;
		initFlags();		
	}//ctor
	
	public Base_BoolFlags(Base_BoolFlags _otr) {
		numFlags = _otr.numFlags;
		initFlags();
		for(int i=0;i<flags.length;++i) {flags[i]=_otr.flags[i];}
	}//copy ctor
	
	/**
	 * Return the number of predefined flags.  All extending flags should start at this value
	 * @return
	 */
	public static int getNumBaseFlags() {return _numBaseFlags;}

	/**
	 * initialize flag structure
	 */
	private final void initFlags(){flags = new int[1 + numFlags/32];}			
	
	/**
	 * Set all flags to false, calling setFlag handler
	 */
	public final void clearAllFlags() {
		for(int i =0; i<numFlags;++i){setFlag(i,false);}
	}
	
	/**
	 * Get whether the owner of this flags is in debug mode
	 * @return
	 */
	public final boolean getIsDebug() {return getFlag(debugIDX);}
	
	/**
	 * Set whether the owner of this flags is in debug mode
	 * @param val
	 */
	public final void setIsDebug(boolean val) {setFlag(debugIDX,val);}
	
	/**
	 * Handle flag owner being put into, or taken from, debug mode
	 * @param val
	 */
	protected abstract void handleSettingDebug(boolean val);
	
	/**
	 * get flag value at passed idx
	 * @param idx
	 * @return
	 */
	public final boolean getFlag(int idx){int bitLoc = 1<<(idx%32);return (flags[idx/32] & bitLoc) == bitLoc;}	
	
	/**
	 * Return flag array entry as integer (x'th 32 flag values).  Returns first entry in flags int array if idx is too big.
	 * @param idx index in flags integer array
	 * @return
	 */
	public final int getFlagsAsInt(int idx) {
		if(idx > flags.length) {return flags[0];}
		return flags[idx];
	}
	
	/**
	 * Check if all passed flags are true
	 * @param idxs
	 * @return
	 */
	public final boolean getAllFlagsAreTrue(int [] idxs){
		int bitLoc; 
		for(int idx : idxs){
			bitLoc = 1<<(idx%32);
			if ((flags[idx/32] & bitLoc) != bitLoc){return false;}
		} 
		return true;
	}//getAllFlags
	
	/**
	 * Check if any passed flags are true
	 * @param idxs
	 * @return
	 */
	public final boolean getAnyFlagsAreTrue(int [] idxs){
		int bitLoc; 
		for(int idx : idxs){
			bitLoc = 1<<(idx%32);
			if ((flags[idx/32] & bitLoc) == bitLoc){return true;}
		} 
		return false;
	}//getAnyFlags
	
	/**
	 * set a list of indexes in flags array to be a specific value
	 * @param idxs all idxs to set
	 * @param val value to be shared among all idxs passed
	 */	
	public final void setAllFlags(int[] idxs, boolean val) { 
		if (val) {	setAllFlagsToTrue(idxs); } 
		else {		setAllFlagsToFalse(idxs);}
	}//setAllFlags
	
	/**
	 * sets flag values to true without calling instancing flag handler
	 * @param idxs
	 * @param val
	 */
	public void setAllFlagsToTrue(int[] idxs) { 
		for(int idx : idxs){
			int flIDX = idx / 32, mask = 1 << (idx % 32);
			flags[flIDX] = flags[flIDX] | mask;
		}
	}
	
	/**
	 * sets flag values to false without calling instancing flag handler
	 * @param idxs
	 * @param val
	 */
	public void setAllFlagsToFalse(int[] idxs) { 
		for(int idx : idxs){
			int flIDX = idx / 32, mask = 1 << (idx % 32);
			flags[flIDX] = flags[flIDX] & ~mask;
		}
	}
	
	/**
	 * Force the passed index to have the passed value without calling instancing class's setFlag, to avoid loops
	 * @param idx idx to set
	 * @param val value to set
	 */
	public final void forceVisFlag(int idx, boolean val) {
		int flIDX = idx/32, mask = 1<<(idx%32);
		flags[flIDX] = (val ?  flags[flIDX] | mask : flags[flIDX] & ~mask);
	}	
	
	
	/**
	 * Toggle the button represented by passed idx
	 * @param idx
	 */
	public final void toggleFlag(int idx) {
		setFlag(idx, !getFlag(idx));
	}
	
	/**
	 * set flag value @ idx
	 * @param idx idx to set
	 * @param val value to set
	 */
	public final void setFlag(int idx, boolean val) {
		boolean oldVal = getFlag(idx);
		forceVisFlag(idx, val);
		//Handle debug mode
		if (idx == debugIDX) {
			handleSettingDebug(val);
			return;
		}
		handleFlagSet_Indiv(idx, val, oldVal);
	}//setFlag
	
	/**
	 * Custom handling for each individual flag being set/cleared
	 * @param idx flag idx to set
	 * @param val value being set to
	 * @param oldval value being changed from
	 */
	protected abstract void handleFlagSet_Indiv(int idx, boolean val, boolean oldval);
	
	/**
	 * This function syncs flag state and flag handling dependent on state.
	 */
	public final void refreshAllFlags() {
		for(int i = 0; i<numFlags; ++i){setFlag(i,getFlag(i));}
	}
	
	/**
	 * Get every flag state and put in a treemap, with key in map being idx in flag state
	 * @return map of states
	 */
	public final TreeMap<Integer, Boolean> getFlagsState() {
		TreeMap<Integer, Boolean> flagValues = new TreeMap<Integer, Boolean>();
		for(Integer i=0;i<numFlags;++i) {		flagValues.put(i, getFlag(i));}
		return flagValues;
	}//getFlagsState
	
	/**
	 * Builds a string of concatenated values of this flags' state
	 * @return
	 */
	public final String getStrOfFlags() {
		StringBuilder sb = new StringBuilder(numFlags * 10);
		for(int i =0;i<numFlags; ++i){
			sb.append(getFlag(i) ? " true" : " false");
		}
		return sb.toString().trim();	
	}//getStrForState
	
	/**
	 * Set this flags state from passed string. Expected to be separated by space
	 * @param token a string of space-separated "true"/"false" strings
	 */
	public final void setFlagsFromString(String token) {
		for(int i =0;i<numFlags; ++i) {
			setFlag(i, Boolean.parseBoolean(token.split("\\s")[i].trim()));
		}
	}//setValFromStrTokens
	
	

}//class Base_BoolFlags
