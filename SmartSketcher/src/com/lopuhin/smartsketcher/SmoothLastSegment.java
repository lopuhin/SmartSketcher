package com.lopuhin.smartsketcher;

import java.util.ArrayList;
import android.graphics.PointF;


public class SmoothLastSegment implements IAction {
    /**
     * Smoothig drawng segment. Spawns a new thread to do it,
     * that adds shapes as they are ready
     */
    private ArrayList<PointF> lastSegment;
    private ArrayList<Long> lastSegmentTimes;
    private SmoothingThread thread;
    private Curve tempCurve;
        
    SmoothLastSegment(ArrayList<PointF> lastSegment, ArrayList<Long> lastSegmentTimes) {
        /**
         * Copies lastSegment and lastSegmentTimes to do smoothing in a thread later
         */
        this.lastSegment = new ArrayList<PointF>();
        this.lastSegmentTimes = new ArrayList<Long>();
        synchronized (lastSegment) {
            assert lastSegment.size() == lastSegmentTimes.size();                        
            for (PointF p: lastSegment) {
                this.lastSegment.add(p);
            }
            for (Long t: lastSegmentTimes) {
                this.lastSegmentTimes.add(t);
            }        
        }        
    }
        
    public void doAction(Sheet sheet) {
        /**
         * Spawn smooth thread
         */
        thread = new SmoothingThread(sheet, this);
        thread.start();
    }

    public void undoAction(Sheet sheet) {
        if (thread != null) {
            thread.addShapes = false;
            thread.interrupt();
        }
        if (tempCurve != null) {
            sheet.removeShape(tempCurve);
        }
    }

    class SmoothingThread extends Thread {
        /**
         * Perform one-time smoothing of lastSegment, adding it first not smoothed,
         * and then adding in smoothed form, removing temporary not smoothed curve.
         */
        public boolean addShapes; // can be canceled
                
        private Sheet sheet;
        private SmoothLastSegment action;
                
        SmoothingThread(Sheet sheet, SmoothLastSegment action) {
            super();
            synchronized (sheet) {
                tempCurve = Curve.approximated(lastSegment, sheet, true);
                sheet.addShape(tempCurve);
            }
            this.sheet = sheet;
            this.action = action;
            addShapes = true;
        }
                
        @Override
        public void run() {
            // TODO - maybe interrupt somewhere here?
            ArrayList<Shape> shapes = BezierCurveSet.approximated(
                lastSegment, lastSegmentTimes, sheet);
            if (addShapes) {
                synchronized (sheet) {
                    sheet.doAction(new AddShapes(shapes));
                    sheet.removeShape(tempCurve);
                    tempCurve = null;
                }
                thread = null;
            }
            sheet.removeDoneAction(action);
        }
    }
        
}
