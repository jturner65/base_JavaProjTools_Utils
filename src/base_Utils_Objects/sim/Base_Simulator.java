package base_Utils_Objects.sim;

import base_Utils_Objects.io.messaging.MessageObject;
import base_Utils_Objects.simExec.Base_SimExec;

/**
 * A simulation world, owned and managed by a simulation executive
 * @author John Turner
 */
public abstract class Base_Simulator {
	/**
	 * Owning simulator executive
	 */
	protected final Base_SimExec exec;
	/**
	 * Name of this simulation world
	 */
	protected final String name;
	/**
	 * msg object for output to console or log
	 */
	protected MessageObject msgObj;
	
	/**
	 * Boolean flags that govern simulator behavior
	 */	
	public SimPrivStateFlags simFlags;
	
	/**
	 * Data updater to keep UI/configuration data in synch
	 */
	protected Base_SimDataAdapter dataUpdate;
	
	/**
	 * 
	 * @param _exec
	 * @param _name
	 */
	public Base_Simulator(Base_SimExec _exec, String _name) {
		exec = _exec;
		name = _name;
		msgObj = _exec.getMsgObj();
		//build initial data updater
		dataUpdate = exec.buildSimDataUpdater();
	}
		
	/**
	 * Set this simulation's data update values to match those passed
	 * @param _dataUpdate
	 */
	public final void setSimDataUpdate(Base_SimDataAdapter _dataUpdate) {
		dataUpdate.setAllVals(_dataUpdate);
		//update all sim flags
		simFlags.updateAllFlagsFromAdapter(dataUpdate);
		useDataUpdateVals_Indiv();
	}

	/**
	 * Consume the newly set data values from sim exec
	 */
	protected abstract void useDataUpdateVals_Indiv();
	
	/**
	 * Initialize this simulation environment. Should be called by concrete child class constructor
	 */
	protected final void initSim() {
		simFlags = new SimPrivStateFlags(this, exec.getNumSimFlags());
		
		initSim_Indiv();
	}
	/**
	 * Sim-specific initialization
	 */
	protected abstract void initSim_Indiv();
	
	/**
	 * Get sim flag value for passed idx
	 * @param idx
	 * @return
	 */
	public final boolean getSimFlag(int idx){return simFlags.getFlag(idx);}
	
	/**
	 * Returns whether or not this sim is in debug mode
	 * @return
	 */
	public final boolean getSimDebug() {return simFlags.getIsDebug();}
	
	/**
	 * Set sim flag value for passed idx
	 * @param idx idx to set
	 * @param val value to set
	 */
	public final void setSimFlag(int idx, boolean val) { simFlags.setFlag(idx, val);}
	
	/**
	 * Set simulator's debug state
	 * @param val
	 */
	public final void setSimDebug(boolean val) { simFlags.setIsDebug(val); }
	
	/**
	 * Set this simulator to draw or not draw visualization.
	 * @param val
	 */
	public abstract void setSimDrawVis(boolean val);
	
	/**
	 * Set all passed flags to passed value
	 * @param idxs
	 * @param val
	 */
	public final void setAllSimFlags(int[] idxs, boolean val) { simFlags.setAllFlags(idxs, val);}	
	
	/**
	 * Application-specific Debug mode functionality (application-specific). Called only from privflags structure
	 * @param val
	 */
	public final void handlePrivFlagsDebugMode(boolean val) {
		msgObj.dispDebugMessage("Base_Simulator", "handlePrivFlagsDebugMode", "Start App-specific Debug, called from App-specific Debug flags with value "+ val +".");
		handlePrivFlagsDebugMode_Indiv(val);
		msgObj.dispDebugMessage("Base_Simulator",  "handlePrivFlagsDebugMode", "End App-specific Debug, called from App-specific Debug flags with value "+ val +".");
	}
	
	/**
	 * Application-specific Debug mode functionality (application-specific). Called only from privflags structure
	 * @param val
	 */
	protected abstract void handlePrivFlagsDebugMode_Indiv(boolean val);
	
	
	/**
	 * Switch structure only that handles priv flags being set or cleared. Called from WinAppPrivStateFlags structure
	 * @param idx
	 * @param val new value for this index
	 * @param oldVal previous value for this index
	 */
	protected abstract void handlePrivFlags_Indiv(int idx, boolean val, boolean oldVal);
	

}//class Base_Simulator
