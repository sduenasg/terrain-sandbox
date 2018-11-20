
package com.sdgapps.terrainsandbox.utils;

import android.util.Log;

public class Logger {
    public static final boolean debug = true;

    public static void log(String s) {
        if (debug)
            Log.d("CDLODViewer: ", s);
    }

    public static void err(String s) {
        if (debug)
            Log.e("CDLODViewer: ", s);
    }
    public static void warning(String s) {
        if (debug)
            Log.w("CDLODViewer: ", s);
    }
}
