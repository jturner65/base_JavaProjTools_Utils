package base_Utils_Objects.sim;

import java.util.Map;

import base_Utils_Objects.dataAdapter.Base_DataAdapter;
import base_Utils_Objects.simExec.Base_SimExec;

/**
 * Manage UI/interface data between a sim exec and a subordinate simulator
 * @author John Turner
 */
public abstract class Base_SimDataUpdater extends Base_DataAdapter {
	/**
	 * Owning Simulator executive
	 */
	protected final Base_SimExec simExec;
	
	/**
	 * 
	 * @param _simExec
	 */
	public Base_SimDataUpdater(Base_SimExec _simExec) {super(); simExec = _simExec;}

	public Base_SimDataUpdater(Base_SimExec _simExec, Map<Integer, Integer> _iVals, Map<Integer, Float> _fVals, Map<Integer, Boolean> _bVals) {
		super(_iVals, _fVals, _bVals);
		simExec = _simExec;
	}

	public Base_SimDataUpdater(Base_SimDataUpdater _otr) {
		super(_otr);
		simExec=_otr.simExec;
	}
	
	@Override
	public String getName() {	return simExec.name+"_Updater";	}

}//class Base_SimDataUpdater
