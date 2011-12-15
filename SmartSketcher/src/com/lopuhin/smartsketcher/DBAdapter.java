package com.lopuhin.smartsketcher;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {
    private static final String DATABASE_NAME = "smartsketcher.db";
    private static final int DATABASE_VERSION = 1;
    private static final String SKETCHES_TABLE = "sketches";
    private static final String SHAPES_TABLE = "shapes";
    private static final String POINTS_TABLE = "points";

    public static final String KEY_ID="_id";

    public static final String SKETCH_NAME = "sketch_name";
    public static final int SKETCH_NAME_COLUMN = 1;

    public static final String SKETCH_ID = "sketch_id", SHAPE_NAME = "shape_name";
    public static final int SKETCH_ID_COLUMN = 1, SHAPE_NAME_COLUMN = 2;

    public static final String SHAPE_ID = "shape_id", POINT_X_NAME = "x", POINT_Y_NAME = "y";
    public static final int SHAPE_ID_COLUMN = 1, POINT_X_COLUMN = 2, POINT_Y_COLUMN = 3;

    private static final String SKETCHES_TABLE_CREATE = "CREATE TABLE " +
        SKETCHES_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        SKETCH_NAME + " TEXT NOT NULL, " +
        ");";
    private static final String SHAPES_TABLE_CREATE = "CREATE TABLE " +
        SHAPES_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        SKETCH_ID + " INTEGER, " + 
        SHAPE_NAME + " TEXT NOT NULL, " +
        "FOREIGN KEY(" + SKETCH_ID + ") REFERENCES " + SKETCHES_TABLE + "(" + KEY_ID + ")" +
        ");";
    private static final String POINTS_TABLE_CREATE = "CREATE TABLE " +
        POINTS_TABLE + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        SHAPE_ID + " INTEGER, " +
        POINT_X_NAME + " REAL NOT NULL, " +
        POINT_Y_NAME + " REAL NOT NULL, " +
        "FOREIGN KEY(" + SHAPE_NAME + ") REFERENCES " + SHAPES_TABLE + "(" + KEY_ID + ")" + 
        ");";
    
    private SQLiteDatabase db;
    private final Context context;
    private DBHelper dbHelper;
    
    public DBAdapter(Context _context) {
        context = _context;
        dbHelper = new DBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public DBAdapter open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        db.close();
    }

    /*
    public int insertEntry(MyObject _myObject) {
        // TODO: Create a new ContentValues to represent my row
        // and insert it into the database.
        return index;
    }
    
    public boolean removeEntry(long _rowIndex) {
        return db.delete(DATABASE_TABLE, KEY_ID + "=" + _rowIndex, null) > 0;
    }

    public Cursor getAllEntries () {
        return db.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_NAME},
                        null, null, null, null, null);
    }

    public MyObject getEntry(long _rowIndex) {
        // TODO: Return a cursor to a row from the database and
        // use the values to populate an instance of MyObject
        return objectInstance;
    }
    */
    
    private static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context, String name,
                          CursorFactory factory, int version) {
            super(context, name, factory, version);
        }
        
        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(SKETCHES_TABLE_CREATE);
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
            _db.execSQL("DROP TABLE IF EXISTS " + SKETCHES_TABLE);

            onCreate(_db);
        }
    }
}
