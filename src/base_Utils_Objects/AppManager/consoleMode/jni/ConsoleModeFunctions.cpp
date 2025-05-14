#include "base_Utils_Objects_appManager_consoleMode_ConsoleMode.h"
#include <windows.h>
#include <iostream>
#include <sstream>

using std::cout;
using std::endl;

std::string dispLastError(){
	// Retrieve the system error message for the last-error code
	std::stringstream ss;
    LPSTR lpMsgBuf = nullptr;
    DWORD dw = GetLastError();
    
    size_t size = FormatMessage(
        FORMAT_MESSAGE_ALLOCATE_BUFFER | 
        FORMAT_MESSAGE_FROM_SYSTEM |
        FORMAT_MESSAGE_IGNORE_INSERTS,
        NULL,
        dw,
        MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
        (LPSTR) &lpMsgBuf,
        0, nullptr);
    std::string message(lpMsgBuf, size);
    ss<<""<<dw<<" : "<<message;
    LocalFree(lpMsgBuf);
    return ss.str();
   
}

JNIEXPORT jboolean JNICALL Java_base_1Utils_1Objects_appManager_consoleMode_ConsoleMode_setConsoleMode(JNIEnv *env, jobject obj, jint mode) {
    DWORD consoleMode;
    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE); // Get handle for console output
    std::cout<<"Console output handle :"<<hConsole<<std::endl;
    if (hConsole != INVALID_HANDLE_VALUE) { // Check if handle is valid
        if (GetConsoleMode(hConsole, &consoleMode)) { // Get console mode
            std::cout << "Current console mode: " << consoleMode << std::endl;
        } else {
            std::cout << "Error getting console mode: `" << dispLastError() <<"`"<< std::endl;
            return false;
        }
    } else {
        std::cout << "Error getting console handle: " << dispLastError() << std::endl;
        return false;
    }
    
	std::cout<<"Console mode is : "<<consoleMode <<" for Handle : "<<hConsole<<std::endl;
    consoleMode |= mode; // Modify the mode (e.g., enable VT processing)

    if (!SetConsoleMode(hConsole, consoleMode)) { // Set the mode
     	std::cout<<"Error setting console mode : "<< consoleMode<<" for handle : "<<hConsole<<" : "<< dispLastError()<<std::endl;
        return false;
    }
    return true;
}

JNIEXPORT jint JNICALL Java_base_1Utils_1Objects_appManager_consoleMode_ConsoleMode_getConsoleMode
  (JNIEnv *, jobject){
	HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE); // Get handle for console output
	if(hConsole == INVALID_HANDLE_VALUE) {
        return false;
    }
    DWORD consoleMode = 0;
	GetConsoleMode(hConsole, &consoleMode);
	return consoleMode;	
  }