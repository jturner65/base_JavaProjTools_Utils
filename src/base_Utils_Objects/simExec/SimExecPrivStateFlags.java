package base_Utils_Objects.simExec;

import base_Utils_Objects.tools.flags.Base_BoolFlags;

/**
 * Class to monitor and manage sim executive state
 * @author John Turner
 *
 */
public class SimExecPrivStateFlags extends Base_BoolFlags {
	/**
	 * Owning sim exec
	 */
	private final Base_SimExec owner;
	
	/**
	 * IDXs of flags - 0 is always debug
	 */
	public static final int
		initializedIDX 	= 1,		//owning sim executive has been initialized 
		conductExpIDX	= 2,		//conduct an experiment
		condSweepExpIDX	= 3,		//conduct a set of experiments where values within the simulation world will change each iteration
		drawVisIDX		= 4;		//draw visualization - if false should ignore all render interface stuff
	
	private static final int numExecFlags = 4;
	
	public SimExecPrivStateFlags(Base_SimExec _owner) {
		super(numExecFlags);
		owner=_owner;
		
	}

	public SimExecPrivStateFlags(SimExecPrivStateFlags _otr) {
		super(_otr);
		owner=_otr.owner;
	}

	@Override
	protected void handleSettingDebug(boolean val) {
		owner.handlePrivFlagsDebugMode(val);
	}
	
	
	
	/**
	 * Get whether or not the owning exec has been initialized
	 * @return
	 */
	public final boolean getIsInitialized() {return getFlag(initializedIDX);}

	/**
	 * Set whether or not the owning exec has been initialized
	 * @param val
	 */
	public final void setIsInitialized(boolean val) {setFlag(initializedIDX, val);}	
	
	/**
	 * Get whether or not the owning exec will conduct an experiment
	 * @return
	 */
	public final boolean getConductExp() {return getFlag(conductExpIDX);}

	/**
	 * Set whether or not the owning exec will conduct an experiment
	 * @param val
	 */
	public final void setConductExp(boolean val) {setFlag(conductExpIDX, val);}	
	
	
	/**
	 * Get whether or not the owning exec will conduct a set of experiments 
	 * where values within the simulation world will change each iteration
	 * @return
	 */
	public final boolean getConductSweepExps() {return getFlag(condSweepExpIDX);}

	/**
	 * Set whether or not the owning exec will conduct a set of experiments 
	 * where values within the simulation world will change each iteration
	 * @param val
	 */
	public final void setConductSweepExps(boolean val) {setFlag(condSweepExpIDX, val);}
		
	/**
	 * Get whether or not the owning exec will draw visualizations
	 * @return
	 */
	public final boolean getDrawVis() {return getFlag(drawVisIDX);}

	/**
	 * Set whether or not the owning exec will draw visualizations
	 * @param val
	 */
	public final void setDrawVis(boolean val) {setFlag(drawVisIDX, val);}

	@Override
	protected void handleFlagSet_Indiv(int idx, boolean val, boolean oldVal) {
		//if flags are being set to same value, ignore
		if(val == oldVal) {return;}
		// update owning simuator
		switch(idx){
			case initializedIDX 		: {//if true then the owning simulator exec has been initialized
				break;}
			case conductExpIDX			: {//if true then conducting an experiment.  reset simulation to beginning with current settings and then run until # of minutes have passed	
				break;}			
			case condSweepExpIDX		: {
				break;}
			case drawVisIDX				: {//draw visualization - if false should ignore all processing/papplet stuff
				owner.setSimDrawVis(val);				
				break;}
			}			
	}
}//class SimExecPrivStateFlags
