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
import java.util.Iterator;
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

  private String mLastErrorString;

  public class MarkerData
  {
    public long mId;
    public String mName;
    public double mLat;
    public double mLon;
    public String mTimeStamp;

    MarkerData(long id, String name, double lat, double lon, String timeStamp)
    {
      mId = id;
      mName = name;
      mLat = lat;
      mLon = lon;
      mTimeStamp = timeStamp;
    }
  }

  public MarkerDatabase()
  {
    mLastErrorString = "Database is closed";
  }

  public String getLastErrorString()
  {
    if (mLastErrorString == null)
      mLastErrorString = "Database is closed";

    return mLastErrorString;
  }

  public void setLastErrorString(String errorString)
  {
    mLastErrorString = errorString;
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

      mLastErrorString = "Database is closed";
    }
  }

  public boolean isOpen()
  {
    return (mDatabase != null);
  }

  public List<MarkerData> getMarkersData()
  {
    String table = TABLE_MARKERS;
    String[] tableColumns = null;
    String whereClause = null;
    String[] whereArgs = null;
    String groupBy = null;
    String having = null;
    String orderBy = null;

    if (mDatabase == null)
    {
      mLastErrorString = "Error getMarkersData(): mDatabase==null";
      return null;
    }

    List<MarkerData> markersData = new ArrayList<>();

    try
    {
      Cursor cursor = mDatabase.query(table, tableColumns, whereClause, whereArgs, groupBy,
                                      having, orderBy);

      while(cursor.moveToNext())
      {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));

        String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));

        double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LAT));

        double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LON));

        String timeStamp = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME_STAMP));

        markersData.add(new MarkerData(id, name, lat, lon, timeStamp));
      }

      cursor.close();
    }
    catch(SQLException e)
    {
      mLastErrorString = "Error getMarkers(): " + e.getMessage();
      markersData = null;
    }

    return markersData;
  }

  public List<MyMarker> loadMarkers(MapView mapView,
                                    MyMarker.OnMyMarkerListener listener,
                                    Drawable icon)
  {
    ArrayList<MyMarker> markers = new ArrayList<>();;

    List<MarkerData> markersData = getMarkersData();

    if (markersData == null)
    {
      mLastErrorString = "Error loadMarkers(): getMarkersData failed!!";
      return null;
    }

    Iterator<MarkerData> iter = markersData.iterator();

    while(iter.hasNext())
    {
      MarkerData data = iter.next();

      MyMarker marker = new MyMarker(mapView, listener);

      GeoPoint pos = new GeoPoint(data.mLat, data.mLon);

      marker.setPosition(pos);
      marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
      marker.setIcon(icon);
      marker.setDraggable(false);
      marker.setTitle(data.mName);
      marker.setId(data.mTimeStamp);

      markers.add(marker);
    }

    mLastErrorString = "loadMarkers() Ok!!";

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
