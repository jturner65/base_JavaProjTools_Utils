package base_Utils_Objects.simExec;

/**
 * This class holds the fundamentals and interface for experimental process data.
 * @author John Turner
 *
 */
public abstract class Base_SimExpLayout {
	/**
	 * Owning sim exec
	 */
	protected final Base_SimExec simExec;
	
	/**
	 * Number of experimental trials to perform
	 */
	protected int numTrials;
	
	/**
	 * Experimental duration in milliseconds
	 */
	protected float expDurMSec;
	
	
	
	public Base_SimExpLayout(Base_SimExec _simExec, int _numTrials, float _expDurMSec) {
		simExec = _simExec;
		numTrials = _numTrials;
		expDurMSec = _expDurMSec;
	}
	
	/**
	 * Number of experimental trials to perform
	 * @return
	 */
	
	public final int getNumTrials() {return numTrials;}
	/**
	 * Duration of experimental trial in milliseconds
	 * @return
	 */
	public final float getExpDurMSec() {return expDurMSec;}
	
	//TODO support random exp duration 
	
	
	
}//class Base_SimExpLayout
