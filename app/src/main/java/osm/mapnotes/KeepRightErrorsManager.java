package osm.mapnotes;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;

import java.io.File;

public class KeepRightErrorsManager implements KeepRightDiskCache.DiskCacheListener,
        KeepRightErrorsDatabaseReader.DatabaseReaderListener {

    String mLastErrorString=null;

    //String mDatabaseDir=null;

    String mMemCacheDataString=null;
    String mDiskDataString=null;
    String mDatabaseDataString=null;

    KeepRightErrorsManagerListener mListener=null;

    MapView mMapView=null;

    public long mElapsedTime;

    Drawable mErrorIcon=null;

    KeepRightMemCache mKeepRightMemCache=new KeepRightMemCache(1000);

    KeepRightDiskCache mKeepRightDiskCache=new KeepRightDiskCache(this);

    KeepRightErrorsDatabaseReader mDatabaseReader=new KeepRightErrorsDatabaseReader(this);

    public interface KeepRightErrorsManagerListener {

        //void onGetNumberOfErrors(Long numberOfErrors);

        void onDataSet(KeepRightErrorSet dataSet);
        void reportCacheData(String data);
        void reportDatabaseMsg(String message);
    }

    public KeepRightErrorsManager(KeepRightErrorsManagerListener listener,
                                  MapView mapView) {

        mListener=listener;
        mMapView=mapView;
    }

    public boolean openErrorDatabase(Context context) {

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

            mLastErrorString="No 'keepright_errors.db' file found!!";

            return false;
        }

        mKeepRightDiskCache.setDir(filePath);

        mDatabaseReader.openDatabase(fileName);

        mLastErrorString=mDatabaseReader.mLastErrorString;

        return true;

        //mTextViewLog.setText(text);

        //Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    public void getErrors(BoundingBox mapBounds) {

        double minLonCoord=mapBounds.getLonWest();
        double maxLonCoord=mapBounds.getLonEast();

        double minLatCoord=mapBounds.getLatSouth();
        double maxLatCoord=mapBounds.getLatNorth();

        /*
        int rest;

        long minLon=(long)Math.round(minLonCoord*10000000.0);

        int minLon1, minLon2, minLon3;

        minLon1=(int)(minLon/10000000);
        rest=(int)(minLon-minLon1*10000000);
        minLon2=rest/10000;
        minLon3=rest-minLon2*10000;

        long maxLon=(long)Math.round(maxLonCoord*10000000.0);

        int maxLon1, maxLon2, maxLon3;

        maxLon1=(int)(maxLon/10000000);
        rest=(int)(maxLon-maxLon1*10000000);
        maxLon2=rest/10000;
        maxLon3=rest-maxLon2*10000;

        long minLat=(long)Math.round(minLatCoord*10000000.0);

        int minLat1, minLat2, minLat3;

        minLat1=(int)(minLat/10000000);
        rest=(int)(minLat-minLat1*10000000);
        minLat2=rest/10000;
        minLat3=rest-minLat2*10000;

        long maxLat=(long)Math.round(maxLatCoord*10000000.0);

        int maxLat1, maxLat2, maxLat3;

        maxLat1=(int)(maxLat/10000000);
        rest=(int)(maxLat-maxLat1*10000000);
        maxLat2=rest/10000;
        maxLat3=rest-maxLat2*10000;
        */

        long minLon=(long)Math.round(minLonCoord*10000000.0);
        int minLonIndex=(int)(minLon/100000);

        long maxLon=(long)Math.round(maxLonCoord*10000000.0);
        int maxLonIndex=(int)(maxLon/100000);

        long minLat=(long)Math.round(minLatCoord*10000000.0);
        int minLatIndex=(int)(minLat/100000);

        long maxLat=(long)Math.round(maxLatCoord*10000000.0);
        int maxLatIndex=(int)(maxLat/100000);

        int totalCount=0;
        int memHits=0;

        //mKeepRightDiskCache.resetCounters();
        //mDatabaseReader.resetCounters();

        mKeepRightDiskCache.clearRequests();
        mDatabaseReader.clearRequests();

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

                String key=getKey(lat, lon);

                KeepRightErrorSet dataSet=mKeepRightMemCache.get(key);

                if (dataSet==null) {

                    //Key not found

                    mKeepRightDiskCache.get(key);
                }
                else {

                    mListener.onDataSet(dataSet);

                    memHits++;
                }

                totalCount++;
            }
        }

        mMemCacheDataString="Mem ("+memHits+"/"+totalCount+"/"+ mKeepRightMemCache.size()+") ";

        mDiskDataString="Disk ("+ mKeepRightDiskCache.mHits+"/"+
                mKeepRightDiskCache.mTotalCount+") ";

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

        String key=String.format("%d,%d", lat, lon);

        return key;
    }

    @Override
    public void onDiskData(KeepRightErrorSet dataSet) {

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

    @Override
    public void onDatabaseData(KeepRightErrorSet dataSet, long elapsedTime) {

        if (!dataSet.containsData()) {

            // Data not found on database
        }
        else {

            // Data found on database

            // Save it in disk
            mKeepRightDiskCache.writeToDisk(dataSet);

            // Include it in memory cache
            mKeepRightMemCache.add(dataSet);
        }

        mDatabaseDataString="Db ("+mDatabaseReader.mRead+"/"+mDatabaseReader.mTotalCount+")";

        reportCacheData();

        mListener.onDataSet(dataSet);

        String msg=String.format("KeepRight DB: Read %d errors (%.1f s)",
                dataSet.getCount(), ((double)elapsedTime)/1000.0);

        mListener.reportDatabaseMsg(msg);
    }
}
