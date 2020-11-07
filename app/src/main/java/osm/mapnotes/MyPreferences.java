package osm.mapnotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;

public class MyPreferences {

    public boolean mShowKeepRightErrors=false;
    public boolean mShowDebugOverlay=false;

    public float mLon=0;
    public float mLat=0;
    public float mZoom=0;

    public int mTileSource=0;

    final static int TILE_SOURCE_DEFAULT = 0;
    final static int TILE_SOURCE_FIRST = 0;
    final static int TILE_SOURCE_MAPNIK = 0;
    final static int TILE_SOURCE_HIKEBIKEMAP = 1;
    final static int TILE_SOURCE_PUBLIC_TRANSPORT = 2;
    final static int TILE_SOURCE_USGS_MAP = 3;
    final static int TILE_SOURCE_USGS_TOPO = 4;
    final static int TILE_SOURCE_OPEN_TOPO = 5;
    final static int TILE_SOURCE_LAST = 5;

    public String mInternalDataPath=null;
    public String mExternalDataPath=null;
    public String mMarkerDatabaseName=null;
    public String mMarkerDatabasePath=null;

    private boolean mInternalDataPathIsOk=false;
    private boolean mExternalDataPathIsOk=false;

    void loadPreferences(Context context) {

        /*
        Configuration.getInstance().load(context,
                PreferenceManager.getDefaultSharedPreferences(context));
        */

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.key_preference_file), Context.MODE_PRIVATE);

        mLat = sharedPref.getFloat(context.getString(R.string.key_lat), (float) 0.0);
        mLon = sharedPref.getFloat(context.getString(R.string.key_lon), (float) 0.0);
        mZoom = sharedPref.getFloat(context.getString(R.string.key_zoom), (float) 5.0);

        mShowKeepRightErrors = sharedPref.getBoolean(context.getString(R.string.key_errors), false);

        mShowDebugOverlay = sharedPref.getBoolean(context.getString(R.string.key_debug), false);

        mTileSource = sharedPref.getInt(context.getString(R.string.key_tile_source), TILE_SOURCE_MAPNIK);

        // Internal Data Path

        mInternalDataPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+
                context.getString(R.string.app_name)+"/";

        mMarkerDatabaseName=context.getString(R.string.marker_database_name);

        mMarkerDatabasePath=mInternalDataPath+mMarkerDatabaseName;

        File internalDir=new File(mInternalDataPath);

        mInternalDataPathIsOk=false;

        if (!internalDir.exists()) {

            if (!internalDir.mkdir()) {

                mInternalDataPathIsOk=false;

                //mLastErrorString="Cannot create database dir <"+databaseDir+">";
                //return false;
            }
            else {

                mInternalDataPathIsOk=true;
            }
        }
        else if (!internalDir.isDirectory()) {

            mInternalDataPathIsOk=false;

            //mLastErrorString="<"+databaseDir+"> is not a directory";
            //return false;
        }
        else {

            mInternalDataPathIsOk=true;
        }

    }

    void savePreferences(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.key_preference_file), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putFloat(context.getString(R.string.key_lon), mLon);
        editor.putFloat(context.getString(R.string.key_lat), mLat);
        editor.putFloat(context.getString(R.string.key_zoom), mZoom);

        editor.putBoolean(context.getString(R.string.key_debug), mShowDebugOverlay);

        editor.putBoolean(context.getString(R.string.key_errors), mShowKeepRightErrors);

        editor.putInt(context.getString(R.string.key_tile_source), mTileSource);

        editor.apply();
    }
}
