package base_Utils_Objects.sim;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

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
     * Which sim layout to build
     */
    protected int simLayoutToUse = 0;    
    
    /**
     * current simulation time in milliseconds from simStartTime - will be scaled by calling window to manage sim speed; set at start of every simMe call
     */
    protected double nowTime;
    
    /**
     * Now time from last sim step
     */
    protected double lastTime;
    
    /**
     * scaling time to speed up simulation == amount to multiply modAmtMillis by (i.e. amount of time between frames that should be simulated)
     */
    protected float frameTimeScale = 1000.0f;    
    
    /**
     * Time step for simulation integration
     */
    protected float timeStep = 0.01f;
    
    /**
     * Per step simulation value scaled by frameTimeScale
     * Used to evolve nowTime
     */
    protected float scaledMillisSinceLastFrame;
    
    
    ////////////////////////
    // reporting stuff
    /**
     * string representation of date for report data
     */
    private String rptDateNowPrfx;
    /**
     * string representation of date for report data
     */
    private String rptExpDir;
    /**
     * main directory to put experiments
     */
    private String baseDirStr;
    
    /**
     * 
     * @param _exec
     * @param _name
     */
    public Base_Simulator(Base_SimExec _exec, String _name, int _simLayoutToUse) {
        exec = _exec;
        name = _name;
        simLayoutToUse = _simLayoutToUse;
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
        rptDateNowPrfx = "ExpDate_"+msgObj.getDateTimeStringForFileName()+"_";
        rptDateNowPrfx += name+"_";
        //root directory to put experimental results = add to all derived exp res directories
        String initBaseDir = setAndCreateRptExpDir(exec.getCWD(),"experiments");        
        baseDirStr = setAndCreateRptExpDir(initBaseDir, "Base_experiments_"+name);
                
        initSim_Indiv();
    }
    /**
     * Sim-specific initialization
     */
    protected abstract void initSim_Indiv();
    
    public final void resetSim(boolean showMsg) {
        //reset Now to be 0
        nowTime = 0;    
        lastTime = 0;
    
    }//resetSim
    
    /**
     * Check if simulation NowTime has passed beyond experimental duration time.  Called at beginning of stepSimulation
     * @param modAmtMillis
     * @param conductExp
     * @param expDurMSec
     * @return whether experiments are being conducted and nowtime has evolved past the end of the experimental duration
     */
    public final boolean checkExpNowTimeIsDone(float modAmtMillis, boolean conductExp, long expDurMSec) {
        scaledMillisSinceLastFrame = modAmtMillis * frameTimeScale;    
        //Record last time and set nowtime
        lastTime = nowTime;
        nowTime += scaledMillisSinceLastFrame;
        boolean expDoneNow = false;
        if(conductExp && (nowTime >= expDurMSec)){//conducting experiments            
            //make sure to cover last run, up to expDurMSec
            nowTime = expDurMSec;
            expDoneNow = true;
        }        
        return expDoneNow;
    }//checkExpNowTimeIsDone
    
    
    /**
     * Initialize the simulator for a series of experimental trials.  Only call from Base_SimExec
     * @param numTrials
     */
    public final void initExperimentalTrials(int numTrials) {
        //implementation specific trials init
        initExperimentalTrials_Indiv(numTrials);
        
        //build experimental output directory
        buildRptExpDir();
    }//initExperimentalTrials
    
    /**
     * Build reporting base directory
     * @return
     */
    protected final String buildRptExpDir() {
        rptExpDir = setAndCreateRptExpDir(baseDirStr, rptDateNowPrfx + "dir");
        return rptExpDir;
    }

    /**
     * Implementation-specific experimental setup
     * @param numTrials
     */
    protected abstract void initExperimentalTrials_Indiv(int numTrials);
    
    /**
     * end a round of experiments and save this round's results
     * @param curTrial
     * @param numTrials
     * @param expDurMSec
     */
    public final void endExperiment(int curTrial, int numTrials, long expDurMSec) {
        //finish up individual experiment - save results at they are now, with appropriate timestamp, uav count, and other appropriate values for file name
        //record results
        //build base file name
        String bseFileName = rptExpDir + File.separatorChar + rptDateNowPrfx +"trl_"+curTrial+"_of_"+numTrials+"_dur_"+expDurMSec ;
        //implementation-specific : save reporting at indicated file name
        HashMap<String, String[]> reportRes = endExperiment_Indiv(bseFileName, curTrial);
        //Write results : key is filename; value is string array results
        for (Entry<String, String[]> keyVal : reportRes.entrySet()) {            
            exec.saveReport(keyVal.getKey(), keyVal.getValue());
        }
    }//endExperiment
    
    /**
     * Implementation-specific end to a round of experiments and save this round's results
     * @param bseFileName base file name path to save experiment
     * @param curTrial
     * @return map where key is filename and value is string array of report values to write
     */
    protected abstract HashMap<String, String[]> endExperiment_Indiv(String bseFileName, int curTrial);
    
    
    /**
     * finish entire set of trials, save last trial's data and then calculate and save aggregate/average data
     * @param curTrial
     * @param numTrials
     * @param expDurMSec
     */
    public final void endTrials(int curTrial, int numTrials, long expDurMSec) {
        endExperiment(curTrial,numTrials,expDurMSec);        
        //aggregate results and save to special files/directories
        String finalResDir = setAndCreateRptExpDir(rptExpDir,"Final_Exp_Final_Results");
        
        //process aggregate aras of aras of data and build 
        String finalResFNmeBase = finalResDir + File.separatorChar + "FinalRes_Trls_"+numTrials+"_dur_"+expDurMSec;
        //implementation-specific : save reporting at indicated file name
        HashMap<String, String[]> reportRes = endTrials_Indiv(finalResFNmeBase, curTrial);
        //Write results : key is filename; value is string array results
        for (Entry<String, String[]> keyVal : reportRes.entrySet()) {            
            exec.saveReport(keyVal.getKey(), keyVal.getValue());
        }
    }//endTrials
    
    /**
     * Implementation-specific ending set of trials functionality, to construct report map
     * @param finalResFNmeBase
     * @param numTrials
     */
    protected abstract HashMap<String, String[]> endTrials_Indiv(String finalResFNmeBase, int numTrials);
    
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
     * Retrieve the timestep for the simulation
     * @return
     */
    public final float getTimeStep() {        return timeStep;}
    
    /**
     * Set the timestep for the simulation
     * @param _timeStep
     */
    public final void setTimeStep(float _timeStep) {timeStep = _timeStep;}
    
    
    /**
     * Set the scaling amount to speed up simulation
     * @param _ts
     */
    public final void setTimeScale(float _ts) {        frameTimeScale = _ts;    }
    
    /**
     * Retrieve the scaling amount to speed up simulation
     * @return
     */
    public final float getTimeScale() {        return frameTimeScale;}    
    
    /**
     * Return a display of NowTime and FrameTimeScale for simulation output/reporting
     * @return
     */
    public final String getNowTimeAndFrameScaleStr() {
        return "Frame Time : "+getNowTimeForDisp()+" Frame Size : " +  ((int)frameTimeScale);
    }
    
    /**
     * Whether the passed timestamp is before the currently set nowTime
     * @param _timeStamp
     * @return
     */
    public final boolean tsIsBeforeNowTime(long _timeStamp) {
        return _timeStamp <= nowTime;
    }
    
    /**
     * Simulation name
     * @return
     */
    public String getName() {return name;}
    
    public final double getNowTime() {return nowTime;} 

    public final String getNowTimeForDisp() {return String.format("%08d", (long)nowTime);}
    
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
    
    /**
     * Create the passed directory as a subdirectory within the given base directory, and return the new directory's full name
     * @param _baseDirStr directory to create within
     * @param _newDirName subdir to create
     * @return fully qualified new directory or error message if failed
     */
    protected final String setAndCreateRptExpDir(String _baseDirStr, String _newDirName) {
        String newDir = _baseDirStr + File.separatorChar + _newDirName;
        boolean success = exec.createRptDir(newDir);
        if (success) {return newDir;}
        return "::::Unable to create directory!::::";
    }    
    
}//class Base_Simulator
