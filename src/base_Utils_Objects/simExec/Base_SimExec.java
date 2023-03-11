package base_Utils_Objects.simExec;

import java.nio.file.Path;
import java.nio.file.Paths;

import base_Utils_Objects.io.messaging.MessageObject;
import base_Utils_Objects.io.messaging.MsgCodes;
import base_Utils_Objects.sim.Base_SimDataAdapter;
import base_Utils_Objects.sim.Base_Simulator;
import base_Utils_Objects.timer.TimerManager;

public abstract class Base_SimExec {
	/**
	 * Name of this simulation executive
	 */
	public final String name;
	/**
	 * Manager for time functions. Records beginning of program execution. Singleton
	 */
	protected final TimerManager timeMgr;
	
	/**
	 * MessageObject for output to console or log
	 */
	protected final MessageObject msgObj;
	
	/**
	 * Flags object managing sim exec functionality
	 */
	public SimExecPrivStateFlags execFlags;
	
	/**
	 * Number of simulators this sim exec will manage
	 */
	public final int maxSimLayouts;
	
	/**
	 * All simulations this sim exec manages
	 */
	protected Base_Simulator[] sims;
	
	/**
	 * Currently executing simulation
	 */
	protected Base_Simulator currSim;
	
	/**
	 * Name of sim exec timer
	 */
	protected final String timerName;
	
	/**
	 * current simulation time in milliseconds from simStartTime - will be scaled by calling window to manage sim speed; set at start of every simMe call
	 */
	protected double nowTime;
	/**
	 * Now time from last sim step
	 */
	protected double lastTime;
	
	/**
	 * Data updater to keep UI/configuration data in synch between interface and individual sims. Follows sim data format!
	 */
	protected Base_SimDataAdapter masterDataUpdate;
	
	/**
	 * scaling time to speed up simulation == amount to multiply modAmtMillis by
	 */
	protected float frameTimeScale = 1000.0f;	
	
	/**
	 * Create a simulation executive that will manage one or more simulations
	 * @param _win the owning window, or null if console
	 * @param _name the name of this simulation executive
	 */
	public Base_SimExec(String _name, int _maxSimLayouts) {
		name = _name;
		maxSimLayouts = _maxSimLayouts;
		msgObj = MessageObject.getInstance();
		timeMgr = TimerManager.getInstance();	
		timerName = this.name+"_simExecStartTime";
		execFlags = new SimExecPrivStateFlags(this);
		masterDataUpdate = buildSimDataUpdater();
	}
	
	/**
	 * Initialize all sim exec values one time. Call from concrete class constructor
	 */
	protected final void initSimExec() {		
		initSimExec_Indiv();
		execFlags.setIsInitialized(true);
	}
	
	/**
	 * Implementation-specific initialization
	 */
	protected abstract void initSimExec_Indiv();
	
	
	/**
	 * Create the simulations this sim exec will manage
	 */
	public final void createAllSims() {
		sims = new Base_Simulator[maxSimLayouts];
		for(int i=0;i<maxSimLayouts;++i) {sims[i] = buildSimOfType(name, i);}
		//TODO build Base_SimDataUpdater for specific sim type to be owned by this sim exec
		//to be populated by owning interface and broadcast to current sim, or to be read from current sim and 
		//then broadcast to interface as appropriate.
		for(int i=0;i<maxSimLayouts;++i) {sims[i].setSimDataUpdate(masterDataUpdate);}
	}
		
	/**
	 * Build an instance of a simulator that this Sim exec will manage
	 * @param name
	 * @param type
	 * @return
	 */
	protected abstract Base_Simulator buildSimOfType(String name, int type);
	
	/**
	 * Build appropriate simulation updater for simulation types that this sim exec manages
	 * @return
	 */
	public abstract Base_SimDataAdapter buildSimDataUpdater();
	
	/**
	 * Update current sim with changes from UI/interactive input, if any
	 */
	public final void updateSimFromExecData() {
		currSim.setSimDataUpdate(masterDataUpdate);
	}
		
	/**
	 * Update the simulation executive's Base_SimDataUpdater values to match those passed
	 * from simulation and propagate to implementation.  This should be called when currSim changes.
	 * 
	 * @param _simData 
	 */
	public final void updateExecFromSimData(Base_SimDataAdapter _simDataUpdate) {
		masterDataUpdate.setAllVals(_simDataUpdate);
		updateOwnerWithSimVals();
	}

	/**
	 * Update any appropriate owning UI or interface components owning this simulation executive with values
	 * from # masterDataUpdate.
	 */
	protected abstract void updateOwnerWithSimVals();
	
	/**
	 * Specify current sim to use and reinitialize sim world
	 * @param _simToUseIDX
	 * @param _showMsg
	 */
	public final void setSimAndInit(int _simToUseIDX, boolean _showMsg) {
		currSim = sims[_simToUseIDX];
		initSimWorld(_showMsg);
	}
	
	/**
	 * Set the current sim to execute, and initialize it if necessary
	 * @param _sim sim to set as current
	 * @param _showMsg whether or not to show debug messages pertaining to initialization
	 */
	public final void initSimWorld(boolean _showMsg) {		
		//Instance sim exec implementation
		initSimWorld_Indiv();
		resetSimExec(_showMsg);
	}
	
	/**
	 * Whether this sim executive has a render interface (whether it belongs to a 
	 * console application or a UI-enabled one.)  Override in Base_UISimExecutive
	 * @return
	 */
	public boolean hasRenderInterface() {return false;}
	
	/**
	 * Concrete class's specific sim world init
	 */
	protected abstract void initSimWorld_Indiv();
	
	/**
	 * start or restart current simulation
	 * @param showMsg whether to show any messages relating to reset
	 */
	public final void resetSimExec(boolean showMsg) {
		//Set named timer
		setNamedTimerStartNow(timerName);
		
		//reset Now to be 0
		nowTime = 0;	
		lastTime = 0;
		resetSimExec_Indiv(showMsg);
	}//resetSimExec
	
	protected abstract void resetSimExec_Indiv(boolean showMsg);
	
	/**
	 * returns a positive int value in millis of current wall time since sim start
	 * @return
	 */
	protected long getCurSimTime() {	return getNamedTimerEllapsed(timerName);}
	
	
	/**
	 * Give a timer the passed name and set it to start right now
	 * @param _timerName
	 */
	public final void setNamedTimerStartNow(String _timerName) {
		timeMgr.setTimerStart(_timerName);
	}
	
	/**
	 * Get ellapsed time in millis for named timer
	 * @param timerName
	 * @return
	 */
	public final long getNamedTimerEllapsed(String _timerName) {
		return timeMgr.getElapsedTimeInMillisForTimer(_timerName);
	}

	/**
	 * Display message and time elapsed with named timer
	 * @param callingClass
	 * @param callingMethod
	 * @param _str
	 * @param stTime
	 */
	public final void showTimeMsgNow(String _simName, String _callingMethod, String _function, String _timerName) {
		msgObj.dispMessage(_simName, _callingMethod,_function+" Time Elapsed: "+getNamedTimerEllapsed(_timerName), MsgCodes.info2);
	}
	
	/**
	 * Advance current sim time by modAmtMillis * frameTimeScale (for speed of simulation increase or decrease relative to realtime)
	 * @param modAmtMillis is milliseconds elapsed since last frame
	 * @return whether sim is complete or not
	 */
	public final boolean stepSimulation(float modAmtMillis) {
		//Record last time and set nowTime
		float scaledMillisSinceLastFrame = modAmtMillis * frameTimeScale;		
		lastTime = nowTime;
		nowTime += scaledMillisSinceLastFrame;
		//sim implementation advancement
		boolean isDone = stepSimulation_Indiv(modAmtMillis, scaledMillisSinceLastFrame);		
		
		return isDone;
	}//stepSimulation	
	
	/**
	 * Advance current sim using nowTime as the time to simulate up to
	 * @param modAmtMillis is milliseconds elapsed since last frame
	 * @param scaledMillisSinceLastFrame is milliseconds since last frame scaled to speed up simulation
	 * @return whether sim is complete or not
	 */
	protected abstract boolean stepSimulation_Indiv(float modAmtMillis, float scaledMillisSinceLastFrame);
	
	
	///////////////////////////
	// accessors
	
	/**
	 * Initialize the master data adapter to useful initial values
	 * @param idx
	 * @param val
	 */
	public final void initMasterDataAdapter(int idx, boolean val) {
		@SuppressWarnings("unused")
		boolean hasChanged = masterDataUpdate.checkAndSetBoolValue(idx, val);
	}
	
	/**
	 * Get number of simulation flags defined for the sims managed by this sim exec
	 * @return
	 */
	public abstract int getNumSimFlags();
	
	/**
	 * Get current sim's specified flag's value
	 * @param idx
	 * @return
	 */
	public final boolean getSimFlag(int idx) {return currSim.getSimFlag(idx);}
	
	/**
	 * Set current simulation to be drawing visualizations
	 * TODO move to UI sim exec?
	 * @param val
	 */
	public final void setSimDrawVis(boolean val) {
		@SuppressWarnings("unused")
		boolean hasChanged = masterDataUpdate.checkAndSetSimDrawVis(val);
		currSim.setSimDrawVis(val);
	}
	
	/**
	 * Set current simulation to be in debug mode
	 * @param val
	 */
	public final void setSimDebug(boolean val) {
		@SuppressWarnings("unused")
		boolean hasChanged = masterDataUpdate.checkAndSetIsDebug(val);
		currSim.setSimDebug(val);
	}
	
	/**
	 * Set current sim's specified flag to passed value
	 * @param idx
	 * @param val
	 */
	public final void setSimFlag(int idx, boolean val) {
		@SuppressWarnings("unused")
		boolean hasChanged = masterDataUpdate.checkAndSetBoolValue(idx, val);
		currSim.setSimFlag(idx, val);
	}
	
	/**
	 * Set passed flag idx for all managed sims
	 * @param idx
	 * @param val
	 */
	public final void setAllSimsFlag(int idx, boolean val) {
		@SuppressWarnings("unused")
		boolean hasChanged = masterDataUpdate.checkAndSetBoolValue(idx, val);
		for(Base_Simulator sim : sims) {sim.setSimFlag(idx, val);}
		
	}
	
	/**
	 * Set all managed sims to be drawing visualizations
	 * @param val
	 */
	public final void setAllSimsDrawVis(boolean val) {
		@SuppressWarnings("unused")
		boolean hasChanged = masterDataUpdate.checkAndSetSimDrawVis(val);
		for(Base_Simulator sim : sims) {sim.setSimDrawVis(val);}
	}
	
	/**
	 * Set all managed sims to be in debug mode
	 * @param val
	 */
	public final void setAllSimsDebug(boolean val) {
		@SuppressWarnings("unused")
		boolean hasChanged = masterDataUpdate.checkAndSetIsDebug(val);
		for(Base_Simulator sim : sims) {sim.setSimDebug(val);}
	}
	
	/**
	 * Get this sim executive's passed flag idx
	 * @param idx
	 * @return
	 */
	public final boolean getExecFlag(int idx) {return execFlags.getFlag(idx);}
	
	/**
	 * Get whether or not the owning exec will draw visualizations
	 * @return
	 */
	public final boolean getDoDrawViz() {return execFlags.getDrawVis();}
	
	/**
	 * Get whether we are in debug mode or not for sim exec
	 * @param val
	 * @return
	 */
	public final boolean getExecDebug(boolean val) {return execFlags.getIsDebug();}
	
	/**
	 * Set this sim executive's passed idx to the specified value
	 * @param idx
	 * @param val
	 */
	public final void setExecFlag(int idx, boolean val) {execFlags.setFlag(idx, val);}
	
	/**
	 * Set whether or not the owning exec will draw visualizations
	 * @param val
	 */
	public final void setDoDrawViz(boolean val) {execFlags.setDrawVis(val);}
	
	/**
	 * Set whether we are in debug mode or not for sim exec
	 * @param val
	 */
	public final void setExecDebug(boolean val) {execFlags.setIsDebug(val);}	
	
	/**
	 * Application-specific Debug mode functionality (application-specific). Called only from privflags structure
	 * @param val
	 */
	public final void handlePrivFlagsDebugMode(boolean val) {
		msgObj.dispDebugMessage("Base_SimExec", "handlePrivFlagsDebugMode", "Start App-specific Debug, called from App-specific Debug flags with value "+ val +".");
		setSimDebug(val);
		handlePrivFlagsDebugMode_Indiv(val);
		msgObj.dispDebugMessage("Base_SimExec",  "handlePrivFlagsDebugMode", "End App-specific Debug, called from App-specific Debug flags with value "+ val +".");
	}
	
	/**
	 * Application-specific Debug mode functionality (application-specific). Called only from privflags structure
	 * @param val
	 */
	protected abstract void handlePrivFlagsDebugMode_Indiv(boolean val);	
	
	/**
	 * Get the current simulation time in milliseconds from simStartTime
	 * @return
	 */
	public final double getNowTime() {return nowTime;}
	
	/**
	 * Retrieve MessageObject for logging and message display
	 * @return
	 */
	public final MessageObject getMsgObj() {return msgObj;}
	
	/**
	 * Set the scaling amount to speed up simulation
	 * @param _ts
	 */
	public final void setTimeScale(float _ts) {		frameTimeScale = _ts;	}
	
	/**
	 * Retrieve the scaling amount to speed up simulation
	 * @return
	 */
	public final float getTimeScale() {		return frameTimeScale;}	
	
	/** 
	 * Returns working directory  TODO get this some other way
	 * @return
	 */
	public String getCWD() {
		Path currentRelativePath = Paths.get("");
		return currentRelativePath.toAbsolutePath().toString();	
	}// getCWD()
	
}//class Base_SimExec
