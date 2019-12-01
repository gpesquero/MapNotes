package osm.mapnotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import org.osmdroid.config.Configuration;

public class MyPreferences {

    public boolean mShowDebugOverlay=false;

    public float mLon=0;
    public float mLat=0;
    public float mZoom=0;

    public int mTileSource=0;

    final static int TILE_SOURCE_DEFAULT=0;
    final static int TILE_SOURCE_FIRST=0;
    final static int TILE_SOURCE_MAPNIK=0;
    final static int TILE_SOURCE_HIKEBIKEMAP=1;
    final static int TILE_SOURCE_PUBLIC_TRANSPORT=2;
    final static int TILE_SOURCE_USGS_MAP=3;
    final static int TILE_SOURCE_USGS_TOPO=4;
    final static int TILE_SOURCE_LAST=4;

    public String mDatabaseDir=null;
    public String mDatabaseName=null;

    void loadPreferences(Context context) {

        Configuration.getInstance().load(context,
                PreferenceManager.getDefaultSharedPreferences(context));

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.key_preference_file), Context.MODE_PRIVATE);

        mLat = sharedPref.getFloat(context.getString(R.string.key_lat), (float) 0.0);
        mLon = sharedPref.getFloat(context.getString(R.string.key_lon), (float) 0.0);
        mZoom = sharedPref.getFloat(context.getString(R.string.key_zoom), (float) 5.0);

        mShowDebugOverlay = sharedPref.getBoolean(context.getString(R.string.key_debug), false);

        mTileSource = sharedPref.getInt(context.getString(R.string.key_tile_source), TILE_SOURCE_MAPNIK);

        mDatabaseDir=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+
                context.getString(R.string.app_name)+"/";

        mDatabaseName=context.getString(R.string.database_name);
    }

    void savePreferences(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.key_preference_file), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putFloat(context.getString(R.string.key_lon), mLon);
        editor.putFloat(context.getString(R.string.key_lat), mLat);
        editor.putFloat(context.getString(R.string.key_zoom), mZoom);

        editor.putBoolean(context.getString(R.string.key_debug), mShowDebugOverlay);

        editor.putInt(context.getString(R.string.key_tile_source), mTileSource);

        editor.apply();
    }
}
