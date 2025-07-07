package base_Utils_Objects.threading;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import base_Utils_Objects.io.messaging.MessageObject;
import base_Utils_Objects.io.messaging.MsgCodes;

/**
 * This class will manage a process builder and enable the user to launch a process
 * @author John Turner
 */
public class myProcessManager<T extends myProcConsoleMsgMgr> {
    public final String ownerName;
    private MessageObject msgObj;
    private List<Future<Boolean>> procMsgMgrsFtrs;
    private List<myProcConsoleMsgMgr> procMsgMgrs;
    protected ExecutorService th_exec;
    
    public myProcessManager(String _ownerName, MessageObject _msgObj, ExecutorService _th_exec) {
        ownerName = _ownerName;
        msgObj = _msgObj;
        th_exec = _th_exec;
        procMsgMgrsFtrs = new ArrayList<Future<Boolean>>();
        procMsgMgrs = new ArrayList<myProcConsoleMsgMgr>(); 
    }
    
    /**
     * Launch a process
     * @param execStrAra
     * @param wkDirStr
     * @param inMsgs
     * @param errMsgs
     * @return
     */
    public boolean launch(String[] execStrAra, String wkDirStr, T inMsgs, T errMsgs) {
        //monitor in multiple threads, either msgs or errors
        procMsgMgrsFtrs.clear();
        procMsgMgrs.clear();
        boolean success = true;
        //http://stackoverflow.com/questions/10723346/why-should-avoid-using-runtime-exec-in-java        
        ProcessBuilder pb = new ProcessBuilder(execStrAra);        
        File wkDir = new File(wkDirStr); 
        pb.directory(wkDir);
        
        String //resultIn = "",
                resultErr = "";
        Process process = null;
        try {
            process = pb.start();            
            inMsgs.setReader(new InputStreamReader(process.getInputStream()));
            errMsgs.setReader(new InputStreamReader(process.getErrorStream()));
        
            procMsgMgrs.add(inMsgs);
            procMsgMgrs.add(errMsgs);            
            procMsgMgrsFtrs = th_exec.invokeAll(procMsgMgrs);for(Future<Boolean> f: procMsgMgrsFtrs) { f.get(); }

            //resultIn = inMsgs.getResults(); 
            resultErr = errMsgs.getResults() ;//results of running map TODO save to log?    
            if(resultErr.toLowerCase().contains("error:")) {throw new InterruptedException("SOM Executable aborted");}
        } 
        catch (SecurityException e) {        msgObj.dispMessage(ownerName,"buildNewMap","buildNewMap Process failed with SecurityException : \n" + e.toString() + "\n\t"+ e.getMessage(), MsgCodes.error1);success = false;} 
        catch (IOException e) {                msgObj.dispMessage(ownerName,"buildNewMap","buildNewMap Process failed with IOException : \n" + e.toString() + "\n\t"+ e.getMessage(), MsgCodes.error1);success = false;} 
        catch (InterruptedException e) {    msgObj.dispMessage(ownerName,"buildNewMap","buildNewMap Process failed with InterruptedException : \n" + e.toString() + "\n\t"+ e.getMessage(), MsgCodes.error1);success = false;}
        catch (ExecutionException e) {        msgObj.dispMessage(ownerName,"buildNewMap","buildNewMap Process failed with ExecutionException : \n" + e.toString() + "\n\t"+ e.getMessage(), MsgCodes.error1);success = false;}    
        catch (Exception e) {                msgObj.dispMessage(ownerName,"buildNewMap","buildNewMap Process failed with Exception : \n" + e.toString() + "\n\t"+ e.getMessage(), MsgCodes.error1);success = false;}    
        finally {                
            if(process != null) {
                msgObj.dispMessage(ownerName,"buildNewMap","Shutting down process with success == "+success, MsgCodes.info5);
                process.destroy();
                if (process.isAlive()) {
                    msgObj.dispMessage(ownerName,"buildNewMap","Forcing Process Destroy!.", MsgCodes.info5);
                    process.destroyForcibly();
                }
                msgObj.dispMessage(ownerName,"buildNewMap","Finished Shutting down process", MsgCodes.info5);
            }            
        }        
        
        return success;
    }
}//class myProcessManager
