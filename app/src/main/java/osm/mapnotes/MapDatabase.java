package osm.mapnotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MapDatabase /* implements DatabaseErrorHandler */ {

    private static final int DATABASE_VERSION=1;

    private final String TABLE_MARKERS="markers";
    private final String COL_ID="id";
    private final String COL_NAME="name";
    private final String COL_LAT="lat";
    private final String COL_LON="lon";
    private final String COL_TIME_STAMP="time_stamp";

    private final String SQL_CREATE_TABLE_MARKERS =
            "CREATE TABLE IF NOT EXISTS "+TABLE_MARKERS+" (" +
                    COL_ID+" INTEGER PRIMARY KEY, " +
                    COL_NAME+" TEXT, "+
                    COL_LAT+" REAL, "+
                    COL_LON+" REAL, "+
                    COL_TIME_STAMP+ " TEXT)";

    /*
    private class MapDbHelper extends SQLiteOpenHelper {

        public MapDbHelper(Context context, String dbName) {
            super(context, dbName, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE_MARKERS);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
            }
    }
    */

    SQLiteDatabase mDatabase=null;

    //private String DATABASE_PATH;

    public String mLastErrorString=null;

    //MapDbHelper mDbHelper=null;

    public MapDatabase() {

    }


    //public boolean openOrCreate(Context context, String dbName) {

    public boolean openOrCreate(String databaseDir, String databaseName) {

        close();

        /*
        mDbHelper=new MapDbHelper(context, dbName);

        SQLiteDatabase db=mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, "nombre");

        db.insert(TABLE_MARKERS, null, values);
        */

        File dir=new File(databaseDir);

        if (!dir.exists()) {

            if (!dir.mkdir()) {

                mLastErrorString="Cannot create database dir <"+databaseDir+">";
                return false;
            }
        }
        else if (!dir.isDirectory()) {

            mLastErrorString="<"+databaseDir+"> is not a directory";
            return false;
        }

        File dbFile=new File(dir, databaseName);

        mDatabase=SQLiteDatabase.openOrCreateDatabase(dbFile.getPath(), null, null);

        if (!mDatabase.isDatabaseIntegrityOk()) {

            mLastErrorString="Database integrity error";
            return false;
        }

        // Create tables

        try {

            mDatabase.execSQL(SQL_CREATE_TABLE_MARKERS);
        }
        catch(SQLException e) {

            mLastErrorString="Error creating table <"+TABLE_MARKERS+" >: "+e.getMessage();
            return false;
        }

        return true;
    }

    public void close() {

        /*
        if (mDbHelper!=null) {

            mDbHelper.close();
        }
        */

        if (mDatabase!=null) {

            mDatabase.close();

            mDatabase=null;
        }
    }

    public List<MyMarker> getMarkers(MapView mapView, MyMarker.OnMyMarkerListener listener,
                                     Drawable icon) {

        ArrayList<MyMarker> markers=null;

        String table=TABLE_MARKERS;
        //String[] tableColumns = new String[] {COL_ID, COL_NAME};
        String[] tableColumns=null;
        String whereClause=null;
        String[] whereArgs=null;
        String groupBy=null;
        String having=null;
        String orderBy=null;

        try {

            Cursor cursor = mDatabase.query(table, tableColumns, whereClause, whereArgs, groupBy,
                    having, orderBy);

            markers=new ArrayList<MyMarker>();

            while(cursor.moveToNext()) {

                long id=cursor.getLong(cursor.getColumnIndex(COL_ID));

                String name=cursor.getString(cursor.getColumnIndex(COL_NAME));

                double lat=cursor.getDouble(cursor.getColumnIndex(COL_LAT));
                double lon=cursor.getDouble(cursor.getColumnIndex(COL_LON));

                String timeStamp=cursor.getString(cursor.getColumnIndex(COL_TIME_STAMP));

                GeoPoint pos=new GeoPoint(lat, lon);

                MyMarker marker=new MyMarker(mapView, listener);

                marker.setPosition(pos);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                marker.setIcon(icon);
                marker.setDraggable(false);
                marker.setTitle(name);
                marker.setId(timeStamp);

                markers.add(marker);
            }

            cursor.close();
        }
        catch(SQLException e) {

            mLastErrorString="Error getMarkers(): "+e.getMessage();

            markers=null;
        }

        return markers;
    }

    public boolean addMarker(MyMarker marker) {

        String table=TABLE_MARKERS;
        String nullColumnHack=null;

        ContentValues values = new ContentValues();
        values.put(COL_NAME, marker.getTitle());
        values.put(COL_LAT, marker.getPosition().getLatitude());
        values.put(COL_LON, marker.getPosition().getLongitude());
        values.put(COL_TIME_STAMP, marker.getId());

        try {

            mDatabase.insertOrThrow(table, nullColumnHack, values);

        }
        catch (SQLException e) {

            mLastErrorString=e.getMessage();
            return false;
        }

        return true;
    }

    public boolean updateMarker(MyMarker marker) {

        String table=TABLE_MARKERS;

        ContentValues values = new ContentValues();
        values.put(COL_NAME, marker.getTitle());
        values.put(COL_LAT, marker.getPosition().getLatitude());
        values.put(COL_LON, marker.getPosition().getLongitude());
        //values.put(COL_TIME_STAMP, marker.getId());

        String whereClase=COL_TIME_STAMP+"=?";

        String[] whereArgs={marker.getId()};

        try {

            int rows=mDatabase.update(table, values, whereClase, whereArgs);

            if (rows!=1) {

                mLastErrorString="mDatabase.update() has updated "+rows+" rows";

                return false;
            }

        }
        catch (SQLException e) {

            mLastErrorString=e.getMessage();
            return false;
        }

        return true;
    }

    public boolean deleteMarker(MyMarker marker) {

        String table=TABLE_MARKERS;
        String whereClause=COL_TIME_STAMP+"=?";
        String[] whereArgs={marker.getId()};

        try {

            int rows=mDatabase.delete(table, whereClause, whereArgs);

            if (rows!=1) {

                mLastErrorString="mDatabase.update() has updated "+rows+" rows";

                return false;
            }

        }
        catch (SQLException e) {

            mLastErrorString=e.getMessage();
            return false;
        }

        return true;
    }

    /*
    @Override
    public void onCorruption(SQLiteDatabase dbObj) {

    }
    */
}
