package base_Utils_Objects.appManager.consoleMode;

public class ConsoleMode {
    private int currConsoleMode;
    
    public ConsoleMode(){
        saveCurrentConsoleMode();
    }
    
    public native boolean setConsoleMode(int mode);
     
    public native int getConsoleMode();
     
    public final void saveCurrentConsoleMode() {
        currConsoleMode = getConsoleMode();
    }
    
    /**
     * Reset the console mode to the original setting
     */
    public final void resetConsoleMode() {
        setConsoleMode(currConsoleMode);
    }

    static {
        System.loadLibrary("libConsoleModeFunctions"); 
    }
    
    

 public static void main(String[] args) {
    ConsoleMode consoleMode = new ConsoleMode();
    int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 0x0004;
     
    boolean result = consoleMode.setConsoleMode(ENABLE_VIRTUAL_TERMINAL_PROCESSING);
    
    if (result) {
        System.out.println("Console mode set successfully!");
    } else {
        System.out.println("Failed to set console mode.");
    }
}
}