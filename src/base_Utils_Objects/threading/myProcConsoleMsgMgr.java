package base_Utils_Objects.threading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.Callable;

import base_Utils_Objects.io.messaging.MessageObject;
import base_Utils_Objects.io.messaging.MsgCodes;

/**
 * manage a message stream from a launched external process
 * @author john
 *
 */
public abstract class myProcConsoleMsgMgr implements Callable<Boolean> {
    protected MessageObject msgObj;
    protected BufferedReader rdr;
    protected StringBuilder strbld;
    protected String type;
    protected MsgCodes msgType;//for display of output
    protected int iter = 0;
    public myProcConsoleMsgMgr(MessageObject _msgObj, String _type) {
        msgObj = _msgObj;
        strbld = new StringBuilder();
        type=_type;
        msgType = (type.equals("Input")) ? MsgCodes.info3 : MsgCodes.error4;
    }//ctor    
    
    /**
     * Set buffered reader for this ConsoleMsgManager
     * @param _in
     */
    public void setReader(Reader _in) {
        rdr = new BufferedReader(_in); 
    }
    
    
    /**
     * return string with process output type as prefix
     * @param rawStr
     * @return
     */
    protected abstract String getStreamType(String rawStr);
    //access owning map manager's message display function if it exists, otherwise just print to console
    private void  dispMessage(String str, MsgCodes useCode) {
        String typStr = getStreamType(str);    
        if(msgObj != null) {        
            msgObj.dispMessage("myProcConsoleMsgMgr","call ("+typStr+" Stream Handler)", str, useCode);}
        else {                System.out.println("myProcConsoleMsgMgr::call ("+typStr+" Stream Handler) : " + str);    }
    }//msgObj.dispMessage
    
    public String getResults() {    return strbld.toString();    }
    @Override
    public Boolean call() throws Exception {
        String sIn = null;
        try {
            while ((sIn = rdr.readLine()) != null) {
                String typStr = getStreamType(sIn);        
                dispMessage("Stream " + typStr+" Line : " + String.format("%04d",iter++) + " | Msg : " + sIn, msgType);
                strbld.append(sIn);            
                strbld.append(System.getProperty("line.separator"));                
            }
        } catch (IOException e) { 
            e.printStackTrace();
            dispMessage("Process IO failed with exception : " + e.toString() + "\n\t"+ e.getMessage(), MsgCodes.error1);
        }
        return true;
    }//call

}//messageMgr
