package osm.mapnotes;

import android.content.Context;

import org.osmdroid.util.BoundingBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class KeepRightErrorsManager implements /* KeepRightDiskCache.DiskCacheListener, */
        KeepRightErrorsDatabaseReader.DatabaseReaderListener {

    String mLastErrorString=null;

    //String mDatabaseDir=null;

    String mMemCacheDataString=null;
    String mDiskDataString=null;
    String mDatabaseDataString=null;

    KeepRightErrorsManagerListener mListener = null;

    //MapView mMapView=null;

    //public long mElapsedTime;

    private final int MEM_CACHE_MAX_OBJECTS = 100;

    KeepRightMemCache mKeepRightMemCache = new KeepRightMemCache(MEM_CACHE_MAX_OBJECTS);

    //KeepRightDiskCache mKeepRightDiskCache = new KeepRightDiskCache(this);

    MapNotesApplication mApp;

    KeepRightErrorsDatabaseReader mDatabaseReader = null;

    ArrayList<String> mRequestList=new ArrayList<String>();

    boolean mCancel=false;

    public interface KeepRightErrorsManagerListener {

        void onDataSet(KeepRightErrorDataSet dataSet);
        void reportCacheData(String data);
        void reportDatabaseMsg(String message);
    }

    public KeepRightErrorsManager(MapNotesApplication app) {

        mApp = app;

        mListener = null;

        //mMapView=mapView;

        // Create
        mDatabaseReader = new KeepRightErrorsDatabaseReader(/*mMapView,*/ this);

        // Start thread
        mDatabaseReader.start();
    }

    public boolean openErrorDatabase(Context context, MyPreferences preferences) {

        File[] externalDirs = context.getExternalFilesDirs(null);

        if (externalDirs==null) {

            //Toast.makeText(this, "No external dirs found!!", Toast.LENGTH_LONG).show();

            mLastErrorString="No external dirs found!!";

            return false;
        }

        String filePath=null;
        String fileName=null;

        for (File dir : externalDirs) {

            filePath=dir.getAbsolutePath();

            int pos=filePath.indexOf("Android/data");

            if (pos>=0) {

                filePath=filePath.substring(0, pos);
            }

            filePath+="MapNotes/";

            fileName=filePath+"keepright_errors.db";

            File dbFile=new File(fileName);

            if (dbFile.exists()) {

                break;
            }
            else {

                fileName=null;
            }
        }

        if (fileName==null) {

            mLastErrorString="No 'KeepRight_errors.db' file found!!";

            return false;
        }

        String cacheDirName=filePath;

        /*
        //cacheDirName=context.getFilesDir().getAbsolutePath();

        cacheDirName=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+
                context.getString(R.string.app_name)+"/cache";
        */

        cacheDirName=preferences.mInternalDataPath+"/cache";

        boolean result=true;

        File cacheDir=new File(cacheDirName);

        if (!cacheDir.exists()) {

            if (!cacheDir.mkdirs()) {

                result=false;
            }
        }

        if (result) {

            File testFile = new File(cacheDir, "prueba.dat");

            try {

                FileOutputStream fos = new FileOutputStream(testFile);

                byte[] out = new byte[]{1, 2, 3};

                fos.write(out);

                fos.close();

            }
            catch (FileNotFoundException e) {

                String text = e.getMessage();
            }
            catch (IOException e) {

                String text = e.getMessage();
            }
        }

        //mKeepRightDiskCache.setDir(filePath);

        mDatabaseReader.openDatabase(fileName);

        mLastErrorString=mDatabaseReader.mLastErrorString;

        return true;
    }

    public void setListener(KeepRightErrorsManagerListener listener) {

        mListener = listener;
    }

    public boolean databaseIsOpen() {

        return mDatabaseReader.databaseIsOpen();
    }

    public void getErrors(BoundingBox mapBounds) {

        double minLonCoord = mapBounds.getLonWest();
        double maxLonCoord = mapBounds.getLonEast();

        double minLatCoord = mapBounds.getLatSouth();
        double maxLatCoord = mapBounds.getLatNorth();

        long minLon = (long)Math.round(minLonCoord*10000000.0);
        int minLonIndex = (int)(minLon/100000);

        long maxLon = (long)Math.round(maxLonCoord*10000000.0);
        int maxLonIndex = (int)(maxLon/100000);

        long minLat = (long)Math.round(minLatCoord*10000000.0);
        int minLatIndex = (int)(minLat/100000);

        long maxLat = (long)Math.round(maxLatCoord*10000000.0);
        int maxLatIndex = (int)(maxLat/100000);

        //int totalCount = 0;
        //int memHits = 0;

        //mKeepRightDiskCache.resetCounters();
        //mDatabaseReader.resetCounters();

        //mKeepRightDiskCache.clearRequests();
        //mDatabaseReader.clearRequests();

        clearRequests();

        /*
        for(int lat1=minLat1; lat1<=maxLat1; lat1++) {

            for(int lon1=minLon1; lon1<=maxLon1; lon1++) {

                for(int lat2=minLat2; lat2<=maxLat2; lat2++) {

                    for(int lon2=minLon2; lon2<=maxLon2; lon2++) {

                        totalCount++;

                    }
                }
            }
        }
        */

        for(int lat=minLatIndex; lat<=maxLatIndex; lat++) {

            for(int lon=minLonIndex; lon<=maxLonIndex; lon++) {

                String key = getKey(lat, lon);

                KeepRightErrorDataSet dataSet = mKeepRightMemCache.get(key);

                if (dataSet == null) {

                    //Key not found in cache

                    if (mDatabaseReader.isIdle()) {

                        // DatabaseReader is in idle state
                        // Request data
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

                    //memHits++;
                }

                //totalCount++;
            }
        }

        //mMemCacheDataString="Mem ("+memHits+"/"+totalCount+"/"+ mKeepRightMemCache.size()+") ";

        /*
        mDiskDataString="Disk ("+ mKeepRightDiskCache.mHits+"/"+
                mKeepRightDiskCache.mTotalCount+") ";
        */

        mDatabaseDataString="Db ("+mDatabaseReader.mRead+"/"+mDatabaseReader.mTotalCount+")";

        reportCacheData();

        /*
        mErrorsDataBase.getErrors(
                minLon1, minLon2, minLon3, maxLon1, maxLon2, maxLon3,
                minLat1, minLat2, minLat3, maxLat1, maxLat2, maxLat3);
        */
    }

    private void reportCacheData() {

        String text;

        text=mMemCacheDataString+mDiskDataString+mDatabaseDataString;

        mListener.reportCacheData(text);
    }

    private String getKey(int lat, int lon) {

        String key = String.format("%d,%d", lat, lon);

        return key;
    }

    private void clearRequests() {

        mRequestList.clear();
    }

    /*
    @Override
    public void onDiskData(KeepRightErrorDataSet dataSet) {

        if (mCancel)
            return;

        if (!dataSet.containsData()) {

            // Data not found on disk
            // Send request to database reader

            mDatabaseReader.get(dataSet.getKey());
        }
        else {

            // Data found on disk
            // Include it in memory cache

            mKeepRightMemCache.add(dataSet);

            mDiskDataString="Disk ("+ mKeepRightDiskCache.mHits+"/"+
                    mKeepRightDiskCache.mTotalCount+") ";

            reportCacheData();

            mListener.onDataSet(dataSet);
        }
    }
    */

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

            // Save it in disk
            //mKeepRightDiskCache.writeToDisk(dataSet);

            // Include it in cache memory
            mKeepRightMemCache.add(dataSet);

            msg=String.format("KeepRight DB: Read %d errors (%.1f s)",
                    dataSet.getCount(), ((double)elapsedTime)/1000.0);

            mApp.logInfo(msg);
        }

        mListener.reportDatabaseMsg(msg);

        mDatabaseDataString="Db ("+mDatabaseReader.mRead+"/"+mDatabaseReader.mTotalCount+")";

        reportCacheData();

        mListener.onDataSet(dataSet);
    }

    void close() {

        mCancel=true;

        clearRequests();

        //mKeepRightDiskCache.close();

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
}
