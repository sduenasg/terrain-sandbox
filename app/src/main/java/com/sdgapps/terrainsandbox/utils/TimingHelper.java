
package com.sdgapps.terrainsandbox.utils;

/**
 * Basic time measuring class
 */
public class TimingHelper {
    double start;
    double end;
    String label;
    public double result;
    boolean paused = false;
    boolean nanotime = true;

    public TimingHelper(String label) {
        this.label = label;
    }

    public void start() {
        Logger.log("Starting timer: "+label);
        paused = false;
        result = 0;
        end = 0;
        start = getTime();
    }

    public void end() {

        if (!paused) {
            end = getTime();
            result += (end - start);
        }

        if (result < 1000)
            Logger.log("Timer:: " + label + " :: " + result + " ms");
        else
            Logger.log("Timer:: " + label + " :: " + result / 1000f + " s");
    }

    public void pause() {
        if (!paused) {
            paused = true;
            end = getTime();
            result += (end - start);
        }
    }

    public void resume() {
        if (paused) {
            start = getTime();
            paused = false;
        }
    }


    private double getTime() {
        if (nanotime) {
            return System.nanoTime() / 1000000d;

        } else {
            return System.currentTimeMillis();
        }

    }
}
