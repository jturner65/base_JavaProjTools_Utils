package base_Utils_Objects.sim;

import base_Utils_Objects.tools.flags.Base_BoolFlags;

/**
 * Flags internal to simulator that govern simulator behavior
 * @author John Turner
 *
 */
public class SimPrivStateFlags extends Base_BoolFlags {
	/**
	 * Owning simulator
	 */
	private final Base_Simulator owner;
	public SimPrivStateFlags(Base_Simulator _owner, int _numFlags) {
		super(_numFlags);
		owner=_owner;
	}

	public SimPrivStateFlags(SimPrivStateFlags _otr) {
		super(_otr);
		owner = _otr.owner;
	}

	@Override
	protected void handleSettingDebug(boolean val) {
		owner.handlePrivFlagsDebugMode(val);
	}

	@Override
	protected void handleFlagSet_Indiv(int idx, boolean val, boolean oldVal) {
		//if flags are being set to same value, ignore
		if(val == oldVal) {return;}
		// update owning simuator
		owner.handlePrivFlags_Indiv(idx, val, oldVal);
	}

}//class SimPrivStateFlags
