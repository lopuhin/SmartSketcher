package com.lopuhin.smartsketcher;

import java.util.ArrayList;
import java.nio.FloatBuffer;

import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.opengl.GLSurfaceView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;


public class MainSurfaceView extends GLSurfaceView {
    /**
     * Handles user interaction with touch screen, manages drawing thread,
     * adds shapes to sheet.
     */
        
    public final static int DRAW_INSTRUMENT = 0, ERASE_INSTRUMENT = 1;

    private final static int ZOOM_MODE = 0, DRAW_MODE = 1, IDLE_MODE = 2;
    private final static float SMALL_TOUCH_SPACING = 1.0f;
    
    private int mode, instrument;

    // initial configuration, when user starts dragging with two fingers
    private float prevTouchSpacing, prevViewZoom;
    private PointF prevTouchCenter, prevViewPos;
    private float eraserRadius;
        
    private Sheet sheet;
    private ArrayList<PointF> lastSegment;
    private ArrayList<Long> lastSegmentTimes;
    private ArrayList<PointF> lastEraseTrace;
    private OpenGLRenderer renderer;
    
    private final static String TAG = "MainSurfaceView";

    MainSurfaceView(Context context, DBAdapter dbAdapter) {
        /**
         * Init: load last sheet, or create new one, if there is not last one
         */
        super(context);

        mode = IDLE_MODE;
        instrument = DRAW_INSTRUMENT;

        sheet = dbAdapter.loadLastSheet();
        if (sheet == null)
            sheet = new Sheet(dbAdapter, true);
        
        setEGLContextClientVersion(2); // OpenGL ES 2.0 context
        renderer = new OpenGLRenderer(this);
        setEGLConfigChooser(renderer.getConfigChooser());
        setRenderer(renderer);
        
        lastSegment = new ArrayList<PointF>();
        lastSegmentTimes = new ArrayList<Long>();
        lastEraseTrace = new ArrayList<PointF>();

        //Resources res = getResources();
        //final float eraserRadius = res.getDimension(R.dimen.eraser_radius);
        eraserRadius = 30.0f; // TODO - load from resources
    }
        
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /**
         * Handle touch events from the touchscreen.
         * There can be 3 modes: drawing, zooming, and beeing idle.
         * Drawing mode can use only one instrument: drawing or erazing.
         * Here we change modes and feed touch data to approriate places
         */
        final PointF mainPoint = new PointF(event.getX(), event.getY());
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case (MotionEvent.ACTION_DOWN) :
            mode = DRAW_MODE;
            if (instrument == DRAW_INSTRUMENT) {
                addPoint(mainPoint);
            } else if (instrument == ERASE_INSTRUMENT) {
                eraseAt(mainPoint);
            }
            break;
        case (MotionEvent.ACTION_POINTER_DOWN) :
            final float dst = touchSpacing(event);
            if (dst > 5f) {
                mode = ZOOM_MODE;
                if (instrument == DRAW_INSTRUMENT) {
                    discardSegment();
                } else if (instrument == ERASE_INSTRUMENT) {
                    discardErasing();
                }
                prevViewZoom = sheet.getViewZoom();
                prevViewPos = sheet.getViewPos();
                prevTouchSpacing = dst;
                prevTouchCenter = touchCenter(event);
            }
            break;
        case (MotionEvent.ACTION_MOVE) :
            if (mode == ZOOM_MODE) {
                float touchSpacing = touchSpacing(event);
                if (touchSpacing < SMALL_TOUCH_SPACING)
                    touchSpacing = prevTouchSpacing;
                Log.d(TAG, "touchSpacing " + touchSpacing +
                      " prevTouchSpacing " + prevTouchSpacing);
                final float dZoom = touchSpacing / prevTouchSpacing;
                sheet.setViewZoom(prevViewZoom * dZoom);
                final PointF touchCenter = touchCenter(event);
                sheet.setViewPos(new PointF(prevViewPos.x +
                                            (prevTouchCenter.x - touchCenter.x / dZoom) /
                                            sheet.getViewZoom(),
                                            prevViewPos.y +
                                            (prevTouchCenter.y - touchCenter.y / dZoom) /
                                            sheet.getViewZoom()));
                prevViewZoom = sheet.getViewZoom();
                prevViewPos = sheet.getViewPos();
                prevTouchSpacing = touchSpacing;
                prevTouchCenter = touchCenter;
            } else if (mode == DRAW_MODE) {
                if (instrument == DRAW_INSTRUMENT) {
                    addPoint(mainPoint);
                } else if (instrument == ERASE_INSTRUMENT) {
                    eraseAt(mainPoint);
                }
            }
            break;
        case (MotionEvent.ACTION_UP) :
            if (mode == DRAW_MODE) {
                if (instrument == DRAW_INSTRUMENT) {
                    addPoint(mainPoint);
                    finishSegment();
                } else if (instrument == ERASE_INSTRUMENT) {
                    eraseAt(mainPoint);
                    finishErasing();
                }
            }
            mode = IDLE_MODE;
            break;
        }
        return true;
    }

    public int getInstrument() {
        return instrument;
    }
        
    public void setInstrument(int instrument) {
        this.instrument = instrument;
    }

    public void setDefaultInstrument() {
        instrument = DRAW_INSTRUMENT;
    }

    public void draw(OpenGLRenderer renderer) {
        /**
         * Draw everything
         */
        sheet.draw(renderer);
        drawLastSegment(renderer);
        drawLastEraseTrace(renderer);
    }
    
    private void drawLastSegment(OpenGLRenderer renderer) {
        /**
         * Draw lastSegment, using OpenGL renderer
         */
        Curve curve = null;
        synchronized (lastSegment) {
            if (lastSegment.size() > 0)
                // TODO - cache?
                curve = new Curve(lastSegment, true);
        }
        if (curve != null) {
            curve.draw(renderer);
        }
    }

    private void drawLastEraseTrace(OpenGLRenderer renderer) {
        /**
         * Draw erase trace (with edges for the last point)
         */
        EraseTrace eraseTrace = null;
        synchronized (lastEraseTrace) {
            if (lastEraseTrace.size() > 0)
                // TODO - cache?
                eraseTrace = new EraseTrace(lastEraseTrace, eraserRadius);
        }
        if (eraseTrace != null) {
            eraseTrace.draw(renderer);
            // TODO draw eraser edges (a circle) around last point
        }
    }

    public Bitmap makeScreenshot() {
        /**
         * Return screenshot of current view as a bitmap
         */
        renderer.setScreenshot();
        requestRender();
        return renderer.getLastScreenshot();
    }
    
    public Sheet getSheet() {
        return sheet;
    }
        
    public void setSheet(Sheet sheet) {
        if (sheet != null) {
            this.sheet = sheet;
        }
        setDefaultInstrument();
    }
        
    public void clearSheet() {
        sheet = new Sheet(sheet.getDBAdapter(), true);
        setDefaultInstrument();
    }

    private static float touchSpacing(MotionEvent event) {
        /**
         * Distance between two touch points
         */
        final float x = event.getX(0) - event.getX(1);
        final float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
        
    private static PointF touchCenter(MotionEvent event) {
        /**
         * Center of two touch points
         */
        return new PointF((event.getX(0) + event.getX(1)) / 2.0f,
                          (event.getY(0) + event.getY(1)) / 2.0f);
    }
 
    private void addPoint(PointF p) {
        /**
         * Add point to last segment (the one that is beeing drawn)
         */
        final long t = System.currentTimeMillis();
        synchronized (lastSegment) {
            lastSegment.add(sheet.toSheet(p));
            lastSegmentTimes.add(t);
        }
    }
        
    private void eraseAt(PointF p) {
        /**
         * Add point to last erase trace (the one that is beeing used for erasing)
         */
        lastEraseTrace.add(sheet.toSheet(p));
    }
    
    private void finishSegment() {
        /**
         * Add smoothed lastSegment to sheet, as the user stopped drawing.
         */ 
        synchronized (lastSegment) {
            SmoothLastSegment action = new SmoothLastSegment(lastSegment, lastSegmentTimes);
            lastSegment.clear();
            lastSegmentTimes.clear();
            sheet.doAction(action);
        }
    }

    private void discardSegment() {
        /**
         * lastSegment really was not meant to be drawn 
         */
        synchronized (lastSegment) {
            lastSegment.clear();
            lastSegmentTimes.clear();
        }
    }
        
    private void finishErasing() {
        /**
         * Add erase points to sheet
         */
        synchronized (lastEraseTrace) {
            if (lastEraseTrace.size() > 0) {
                ArrayList<Shape> t = new ArrayList<Shape>();
                t.add(new EraseTrace(lastEraseTrace, eraserRadius));
                sheet.doAction(new AddShapes(t));
                lastEraseTrace.clear();
            }
        }
    }
        
    private void discardErasing() {
        synchronized (lastEraseTrace) {
            lastEraseTrace.clear();
        }
    }

}
