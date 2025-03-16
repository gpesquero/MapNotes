package osm.mapnotes;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MarkerDatabase
{
  private static final int DATABASE_VERSION = 1;

  private final String TABLE_MARKERS = "markers";
  private final String COL_ID = "id";
  private final String COL_NAME = "name";
  private final String COL_LAT = "lat";
  private final String COL_LON = "lon";
  private final String COL_TIME_STAMP = "time_stamp";

  private final String SQL_CREATE_TABLE_MARKERS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_MARKERS + " (" +
            COL_ID + " INTEGER PRIMARY KEY, " +
            COL_NAME + " TEXT, " +
            COL_LAT + " REAL, " +
            COL_LON + " REAL, " +
            COL_TIME_STAMP + " TEXT)";

  SQLiteDatabase mDatabase = null;

  public String mLastErrorString;

  public MarkerDatabase()
  {
    mLastErrorString = "Marker database is closed";
  }

  public boolean openOrCreate(String markerDatabasePath)
  {
    // Close database if it's already open.
    close();

    mDatabase = SQLiteDatabase.openOrCreateDatabase(markerDatabasePath, null, null);

    if (!mDatabase.isDatabaseIntegrityOk())
    {
      mLastErrorString = "Database integrity error";

      close();

      return false;
    }

    // Create tables.

    try
    {
      mDatabase.execSQL(SQL_CREATE_TABLE_MARKERS);
    }
    catch(SQLException e)
    {
      mLastErrorString = "Error creating table <" + TABLE_MARKERS + " >: " + e.getMessage();

      close();

      return false;
    }

    return true;
  }

  public void close()
  {
    if (mDatabase != null)
    {
      mDatabase.close();

      mDatabase = null;
    }
  }

  public boolean isOpen()
  {
    return (mDatabase != null);
  }

  public List<MyMarker> getMarkers(MapView mapView, MyMarker.OnMyMarkerListener listener,
                                   Drawable icon)
  {
    ArrayList<MyMarker> markers;

    String table = TABLE_MARKERS;
    String[] tableColumns = null;
    String whereClause = null;
    String[] whereArgs = null;
    String groupBy = null;
    String having = null;
    String orderBy = null;

    if (mDatabase == null)
    {
      mLastErrorString = "Error getMarkers(): mDatabase==null";
      return null;
    }

    try
    {
      Cursor cursor = mDatabase.query(table, tableColumns, whereClause, whereArgs, groupBy,
                                      having, orderBy);

      markers = new ArrayList<>();

      while(cursor.moveToNext())
      {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));

        String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));

        double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LAT));

        double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LON));

        String timeStamp = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME_STAMP));

        GeoPoint pos = new GeoPoint(lat, lon);

        MyMarker marker = new MyMarker(mapView, listener);

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
    catch(SQLException e)
    {
      mLastErrorString = "Error getMarkers(): " + e.getMessage();

      markers = null;
    }

    mLastErrorString = "getMarkers() Ok!!";

    return markers;
  }

  public boolean addMarker(MyMarker marker)
  {
    String table = TABLE_MARKERS;
    String nullColumnHack = null;

    ContentValues values = new ContentValues();
    values.put(COL_NAME, marker.getTitle());
    values.put(COL_LAT, marker.getPosition().getLatitude());
    values.put(COL_LON, marker.getPosition().getLongitude());
    values.put(COL_TIME_STAMP, marker.getId());

    if (mDatabase == null)
    {
      mLastErrorString = "Error addMarker(): mDatabase==null";

      return false;
    }

    try
    {
      mDatabase.insertOrThrow(table, nullColumnHack, values);
    }
    catch (SQLException e)
    {
      mLastErrorString = e.getMessage();
      return false;
    }

    return true;
  }

  public boolean updateMarker(MyMarker marker)
  {
    String table = TABLE_MARKERS;

    ContentValues values = new ContentValues();
    values.put(COL_NAME, marker.getTitle());
    values.put(COL_LAT, marker.getPosition().getLatitude());
    values.put(COL_LON, marker.getPosition().getLongitude());

    String whereClause = COL_TIME_STAMP + "=?";

    String[] whereArgs = {marker.getId()};

    if (mDatabase == null)
    {
      mLastErrorString = "Error updateMarker(): mDatabase==null";

      return false;
    }

    try
    {
      int rows=mDatabase.update(table, values, whereClause, whereArgs);

      if (rows != 1)
      {
        mLastErrorString = "mDatabase.update() has updated " + rows + " rows";
        return false;
      }
    }
    catch (SQLException e)
    {
      mLastErrorString = e.getMessage();
      return false;
    }

    return true;
  }

  public boolean deleteMarker(MyMarker marker)
  {
    String table = TABLE_MARKERS;
    String whereClause = COL_TIME_STAMP + "=?";
    String[] whereArgs = {marker.getId()};

    if (mDatabase==null)
    {
      mLastErrorString = "Error deleteMarker(): mDatabase==null";

      return false;
    }

    try
    {
      int rows = mDatabase.delete(table, whereClause, whereArgs);

      if (rows != 1)
      {
        mLastErrorString = "mDatabase.update() has updated " + rows + " rows";

        return false;
      }
    }
    catch (SQLException e)
    {
      mLastErrorString = e.getMessage();
      return false;
    }

    return true;
  }
}
