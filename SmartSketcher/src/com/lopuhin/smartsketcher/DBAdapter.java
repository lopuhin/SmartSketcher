package com.lopuhin.smartsketcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.DatabaseUtils.InsertHelper;


public class DBAdapter {
    /**
     * Responsible for Sheet persistance: adds new shapes to current sheet,
     * restores sheet from database
     */
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "smartsketcher.db";
    private static final String SHEETS_TABLE = "sheets";
    private static final String SHAPES_TABLE = "shapes";
    private static final String POINTS_TABLE = "points";

    public static final String KEY_ID="_id";

    public static final String SHEET_NAME = "sheet_name",
        SHEET_X = "x", SHEET_Y = "y", SHEET_ZOOM = "zoom";
    public static final int SHEET_NAME_COLUMN = 1,
        SHEET_X_COLUMN = 2, SHEET_Y_COLUMN = 3, SHEET_ZOOM_COLUMN = 4;

    public static final String SHEET_ID = "sheet_id", SHAPE_NAME = "shape_name",
        SHAPE_THICKNESS = "shape_thickness";
    public static final int SHEET_ID_COLUMN = 1, SHAPE_NAME_COLUMN = 2,
        SHAPE_THICKNESS_COLUMN = 3;

    public static final String SHAPE_ID = "shape_id", POINT_X = "x", POINT_Y = "y";
    public static final int SHAPE_ID_COLUMN = 1, POINT_X_COLUMN = 2, POINT_Y_COLUMN = 3;

    private static final String SHEETS_TABLE_CREATE = "CREATE TABLE " +
        SHEETS_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        SHEET_NAME + " TEXT NOT NULL, " +
        SHEET_X + " REAL NOT NULL, " +
        SHEET_Y + " REAL NOT NULL, " +
        SHEET_ZOOM + " REAL NOT NULL " +
        ");";
    private static final String SHAPES_TABLE_CREATE = "CREATE TABLE " +
        SHAPES_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        SHEET_ID + " INTEGER NOT NULL, " + 
        SHAPE_NAME + " TEXT NOT NULL, " +
        SHAPE_THICKNESS + " REAL NOT NULL, " + 
        "FOREIGN KEY(" + SHEET_ID + ") REFERENCES " + SHEETS_TABLE + "(" + KEY_ID + ")" +
        ");";
    private static final String POINTS_TABLE_CREATE = "CREATE TABLE " +
        POINTS_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        SHAPE_ID + " INTEGER NOT NULL, " +
        POINT_X + " REAL NOT NULL, " +
        POINT_Y + " REAL NOT NULL, " +
        "FOREIGN KEY(" + SHAPE_ID + ") REFERENCES " + SHAPES_TABLE + "(" + KEY_ID + ")" + 
        ");";
    
    private SQLiteDatabase db;
    private final Context context;
    private DBHelper dbHelper;
    private long currentSheetId;
    
    private final String TAG = "DBAdapter";
    
    public DBAdapter(Context _context) {
        context = _context;
        dbHelper = new DBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void newSheet(Sheet sheet) {
        /**
         * Create new sheet in db, set currentSheetId
         */
        ContentValues sheetValues = new ContentValues();
        sheetValues.put(SHEET_NAME, "sheet"); // TODO
        PointF viewPos = sheet.getViewPos();
        sheetValues.put(SHEET_X, viewPos.x);
        sheetValues.put(SHEET_Y, viewPos.y);
        sheetValues.put(SHEET_ZOOM, sheet.getViewZoom());
        currentSheetId = db.insert(SHEETS_TABLE, null, sheetValues);
    }

    public long getCurrentSheetId() {
        return currentSheetId;
    }

    public Sheet loadLastSheet() {
        /**
         * Load last saved sheet. Can return null, if there is no last sheet
         */
        Cursor c = db.rawQuery("SELECT MAX(" + KEY_ID + ") FROM " + SHEETS_TABLE, null);
        if (c.moveToFirst()) {
            long sheetId = c.getLong(0);
            c.close();
            return loadSheet(sheetId);
        } else {
            return null;
        }
    }
        
    public Sheet loadSheet(long sheetId) {
        /**
         * Load sheet from db, set currentSheetId to sheetId
         */
        Log.d(TAG, "loadSheet " + sheetId);
        currentSheetId = sheetId;
        Sheet sheet = new Sheet(this, false);
        // load sheet
        Cursor cSheet = db.query(SHEETS_TABLE, null, KEY_ID + "=" + sheetId,
                                 null, null, null, null);
        if (!cSheet.moveToFirst())
            return sheet;
        sheet.setViewPos(new PointF(cSheet.getFloat(SHEET_X_COLUMN),
                                    cSheet.getFloat(SHEET_Y_COLUMN)));
        sheet.setViewZoom(cSheet.getFloat(SHEET_ZOOM_COLUMN));
        cSheet.close();
        // load all shapes
        Cursor cShapes = db.query(SHAPES_TABLE, null, SHEET_ID + "=" + sheetId,
                                  null, null, null, null);
        if (!cShapes.moveToFirst())
            return sheet;
        ArrayList<ShapeInfo> shapes = new ArrayList<ShapeInfo>();
        ArrayList<Long> shapeIds = new ArrayList<Long>();
        if (cShapes.moveToFirst()) {
            do {
                shapeIds.add(cShapes.getLong(0));
                shapes.add(new ShapeInfo(cShapes.getString(SHAPE_NAME_COLUMN),
                                         cShapes.getFloat(SHAPE_THICKNESS_COLUMN)));
            } while (cShapes.moveToNext());
        }
        cShapes.close();
        if (shapes.size() > 0) {
            // load all points and create shapes
            Cursor cPoints = db.query(POINTS_TABLE, null,
                                      SHAPE_ID + " IN (" + join(shapeIds, ",") + ")",
                                      null,null,null, SHAPE_ID); // group by shape_id
            ArrayList<PointF> points = new ArrayList<PointF>();
            long currentShapeId = shapeIds.get(0);
            Log.d(TAG, "currentShapeId " + currentShapeId + " " + shapeIds.size());
            int shapeIndex = 0;
            if (!cPoints.moveToFirst())
                return sheet;
            do {
                long shapeId = cPoints.getLong(SHAPE_ID_COLUMN);
                Log.d(TAG, "shapeId " + shapeId + " " + shapeIndex);
                if (shapeId != currentShapeId) {
                    currentShapeId = shapeId;
                    
                    sheet.addShape(createShape(points, shapes.get(shapeIndex)),
                                   false);
                    shapeIndex += 1;
                    points.clear();
                }
                points.add(new PointF(cPoints.getFloat(POINT_X_COLUMN),
                                      cPoints.getFloat(POINT_Y_COLUMN)));
            } while (cPoints.moveToNext());
            cPoints.close();
            if (points.size() > 0) {
                sheet.addShape(createShape(points, shapes.get(shapeIndex)),
                               false);
            }
        }
        return sheet;
    }

    
    public void addShape(Shape shape) {
        /**
         * Save shape in current sheet
         */
        ContentValues shapeValues = new ContentValues();
        shapeValues.put(SHEET_ID, currentSheetId);
        shapeValues.put(SHAPE_NAME, shape.getClass().getName());
        shapeValues.put(SHAPE_THICKNESS, shape.getThickness());
        long shape_id = db.insert(SHAPES_TABLE, null, shapeValues);
        Log.d(TAG, "Inserting points (" + shape.getPoints().length + ") of " +
              shape.getClass().getName());
        InsertHelper ih = new InsertHelper(db, POINTS_TABLE);
        db.beginTransaction();
        try {
            ContentValues pointValues = null;
            for (PointF point: shape.getPoints()) {
                if (!Float.isNaN(point.x) && !Float.isNaN(point.y)) {
                    pointValues = new ContentValues();
                    pointValues.put(SHAPE_ID, shape_id);
                    pointValues.put(POINT_X, point.x);
                    pointValues.put(POINT_Y, point.y);
                    ih.insert(pointValues);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        Log.d(TAG, "Done inserting points");
    }

    public void removeShape(Shape shape) {
        /**
         * Remove shape from current sheet
         */
        // TODO
    }

    public ArrayList<Long> getSheetIds() {
        /**
         * Return an ArrayList of sheet ids
         */
        ArrayList<Long> sheetIds = new ArrayList<Long>();
        Cursor c = db.query(SHEETS_TABLE, new String[] {KEY_ID},
                            null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                sheetIds.add(c.getLong(0));
            } while (c.moveToNext());
        }
        return sheetIds;
    }
    
    public DBAdapter open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        db.close();
    }

    private class ShapeInfo {
        public String name;
        public float thickness;
        ShapeInfo(String name, float thickness) {
            this.name = name;
            this.thickness = thickness;
        }
    }
    
    private Shape createShape(ArrayList<PointF> points, ShapeInfo shapeInfo) {
        /**
         * Create shape (determine class from shapeName) from given points.
         */
        Log.d(TAG, "createShape " + shapeInfo.name);
        if (shapeInfo.name.equals("com.lopuhin.smartsketcher.Curve")) {
            return new Curve(points, false, shapeInfo.thickness);
        } else if (shapeInfo.name.equals("com.lopuhin.smartsketcher.BezierCurve")) {
            return new BezierCurve(points, shapeInfo.thickness);
        } else if (shapeInfo.name.equals("com.lopuhin.smartsketcher.ErasePoint") ||
                   shapeInfo.name.equals("com.lopuhin.smartsketcher.EraseTrace")) {
            return new EraseTrace(points, shapeInfo.thickness);
        } else {
            throw new RuntimeException("Unknown shape name: " + shapeInfo.name);
        }
    }
    
    private static String join(Collection<?> s, String delimiter) {
        /**
         * Join elements in collection s by given delimiter. Return a string
         */
        StringBuilder builder = new StringBuilder();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (!iter.hasNext()) {
                break;                  
            }
            builder.append(delimiter);
        }
        return builder.toString();
    }

    /*
    public boolean removeEntry(long _rowIndex) {
        return db.delete(DATABASE_TABLE, KEY_ID + "=" + _rowIndex, null) > 0;
    }

    public Cursor getAllEntries () {
        return db.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_NAME},
                        null, null, null, null, null);
    }
    */
    
    private static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context, String name,
                          CursorFactory factory, int version) {
            super(context, name, factory, version);
        }
        
        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(SHEETS_TABLE_CREATE);
            _db.execSQL(SHAPES_TABLE_CREATE);
            _db.execSQL(POINTS_TABLE_CREATE);
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
            // Log the version upgrade.
            Log.w("DBAdapter", "Upgrading from version " +
                  _oldVersion + " to " +
                  _newVersion + ", which will destroy all old data");

	    _db.execSQL("DROP TABLE IF EXISTS " + POINTS_TABLE);
            _db.execSQL("DROP TABLE IF EXISTS " + SHAPES_TABLE);
            _db.execSQL("DROP TABLE IF EXISTS " + SHEETS_TABLE);

            onCreate(_db);
        }
    }
}
