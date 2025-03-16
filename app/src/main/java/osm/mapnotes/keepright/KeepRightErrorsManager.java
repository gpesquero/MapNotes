package osm.mapnotes.keepright;

import android.content.Context;

import org.osmdroid.util.BoundingBox;
import osm.mapnotes.MapNotesApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class KeepRightErrorsManager
  implements KeepRightErrorsDatabaseReader.DatabaseReaderListener
{
  public String mLastErrorString = null;

  String mMemCacheDataString = null;
  String mDiskDataString = null;
  String mDatabaseDataString = null;

  KeepRightErrorsManagerListener mListener;

  private final int MEM_CACHE_MAX_OBJECTS = 100;

  KeepRightMemCache mKeepRightMemCache = new KeepRightMemCache(MEM_CACHE_MAX_OBJECTS);

  MapNotesApplication mApp;

  KeepRightErrorsDatabaseReader mDatabaseReader;

  ArrayList<String> mRequestList = new ArrayList<>();

  boolean mCancel = false;

  public interface KeepRightErrorsManagerListener
  {
    void onDataSet(KeepRightErrorDataSet dataSet);
    void reportCacheData(String data);
    void reportDatabaseMsg(String message);
  }

  public KeepRightErrorsManager(MapNotesApplication app)
  {
    mApp = app;

    mListener = null;

    // Create
    mDatabaseReader = new KeepRightErrorsDatabaseReader(this);

    // Start thread
    mDatabaseReader.start();
  }

  public boolean openErrorDatabase(Context context)
  {
    File[] externalDirs = context.getExternalFilesDirs(null);

    if (externalDirs == null)
    {
      mLastErrorString = "No external dirs found!!";

      mApp.logWarning(mLastErrorString);

      return false;
    }

    String filePath;
    String fileName = null;

    for (File dir : externalDirs)
    {
      filePath = dir.getAbsolutePath();

      int pos = filePath.indexOf("Android/data");

      if (pos>=0)
        filePath = filePath.substring(0, pos);

      filePath += "MapNotes/";

      fileName = filePath+"keepright_errors.db";

      File dbFile = new File(fileName);

      if (dbFile.exists())
        break;
      else
        fileName = null;
    }

    if (fileName == null)
    {
      mLastErrorString = "No 'KeepRight_errors.db' file found!!";

      mApp.logWarning(mLastErrorString);

      return false;
    }

    if (!mDatabaseReader.openDatabase(fileName))
      mApp.logWarning("Error KeepRight openDatabase: " + mDatabaseReader.mLastErrorString);

    mLastErrorString = mDatabaseReader.mLastErrorString;

    return true;
  }

  public void setListener(KeepRightErrorsManagerListener listener)
  {
    mListener = listener;
  }

    public boolean databaseIsOpen() {

        if (mDatabaseReader == null) {

            return false;
        }

        return mDatabaseReader.databaseIsOpen();
    }

    public void getErrors(BoundingBox mapBounds) {

        double minLonCoord = mapBounds.getLonWest();
        double maxLonCoord = mapBounds.getLonEast();

        double minLatCoord = mapBounds.getLatSouth();
        double maxLatCoord = mapBounds.getLatNorth();

        long minLon = Math.round(minLonCoord*10000000.0);
        int minLonIndex = (int)(minLon/100000);

        long maxLon = Math.round(maxLonCoord*10000000.0);
        int maxLonIndex = (int)(maxLon/100000);

        long minLat = Math.round(minLatCoord*10000000.0);
        int minLatIndex = (int)(minLat/100000);

        long maxLat = Math.round(maxLatCoord*10000000.0);
        int maxLatIndex = (int)(maxLat/100000);

        clearRequests();

        for(int lat=minLatIndex; lat<=maxLatIndex; lat++) {

            for(int lon=minLonIndex; lon<=maxLonIndex; lon++) {

                String key = getKey(lat, lon);

                KeepRightErrorDataSet dataSet = mKeepRightMemCache.get(key);

                if (dataSet == null) {

                    //Key not found in cache

                    if (mDatabaseReader.isIdle()) {

                        // DatabaseReader is in idle state
                        // Request data...
                        mDatabaseReader.requestData(key);

                        mApp.logInfo("Request DB Data");
                    }
                    else {

                        // Put request in list
                        mRequestList.add(key);
                    }
                }
                else {

                    // Data found in cache

                    // Send data to listener
                    mListener.onDataSet(dataSet);
                }
            }
        }

        mDatabaseDataString="Db ("+mDatabaseReader.mRead+"/"+mDatabaseReader.mTotalCount+")";

        reportCacheData();
    }

    private void reportCacheData() {

        String text;

        text=mMemCacheDataString+mDiskDataString+mDatabaseDataString;

        mListener.reportCacheData(text);
    }

    private String getKey(int lat, int lon) {

        return String.format(Locale.US, "%d,%d", lat, lon);
    }

    private void clearRequests() {

        mRequestList.clear();
    }

    @Override
    public void onDatabaseData(KeepRightErrorDataSet dataSet, long elapsedTime) {

        if (mCancel)
            return;

        String msg;

        if (!dataSet.containsData()) {

            // Data not found on database

            msg="KeepRight DB: Error reading data";

            mApp.logError(msg);
        }
        else {

            // Data found on database

            // Include data in cache memory
            mKeepRightMemCache.add(dataSet);

            msg=String.format(Locale.US, "KeepRight DB: Read %d errors (%.1f s)",
                    dataSet.getCount(), ((double)elapsedTime)/1000.0);

            mApp.logInfo(msg);
        }

        mListener.reportDatabaseMsg(msg);

        mDatabaseDataString="Db ("+mDatabaseReader.mRead+"/"+mDatabaseReader.mTotalCount+")";

        reportCacheData();

        // Report received data to listener...
        mListener.onDataSet(dataSet);

        // Check if there are more pending requests...
        if (mRequestList.size() > 0) {

            if (mDatabaseReader.isIdle()) {

                // Get first key from request list
                String key = mRequestList.remove(0);

                // DatabaseReader is in idle state
                // Request data...
                mDatabaseReader.requestData(key);

                mApp.logInfo("Request DB Data");
            }
            else {

                mApp.logError("Received DB data, but DB is not idle...");
            }
        }
    }

    public void close() {

        mCancel=true;

        clearRequests();

        mDatabaseReader.close();
    }

    public int getCacheMemorySize() {

        return mKeepRightMemCache.size();
    }

    public int getCacheMemoryMaxSize() {

        return mKeepRightMemCache.maxSize();
    }

    public int getCacheMemoryNumberOfRequests() {

        return mKeepRightMemCache.requestCount();
    }

    public int getCacheMemoryNumberOfHits() {

        return mKeepRightMemCache.hitCount();
    }

    public ArrayList<String> getDbInfo() {

        return mDatabaseReader.getDbInfo();
    }
}
