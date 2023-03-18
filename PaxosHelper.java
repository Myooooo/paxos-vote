/** 
* PaxosHelper.java
* This file implements some helper functions for debugging
*/

package ds.assignment3;

import java.util.Date;
import java.util.Random;
import java.text.SimpleDateFormat;

public class PaxosHelper {
    /**
    * Obtain current timestamp
    * 
    * @return current timestamp formatted into string
    */
    public static String getCurrentTimeStamp() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }

    /**
    * Print line to terminal with timestamp
    */
    public static void printlnTime(String msg) {
        System.out.println("[" + getCurrentTimeStamp() + "] " + msg);
    }

    /**
    * Print error message to terminal with timestamp
    */
    public static void printErrTime(String msg) {
        System.err.println("[" + getCurrentTimeStamp() + "] Error: " + msg);
    }
}