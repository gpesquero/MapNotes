package osm.mapnotes;

import android.app.Application;
import android.content.Context;

public class MapNotesApplication extends Application {

    private final String mAppVersion = BuildConfig.VERSION_NAME;

    MarkerDatabase mMarkerDatabase = null;

    public String getAppName(Context context) {

        return context.getString(R.string.app_name);
    }

    public String getAppVersion() {

        return mAppVersion;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mMarkerDatabase=new MarkerDatabase();
    }

    public MarkerDatabase getMarkerDatabase() {

        return mMarkerDatabase;
    }

    public void destroy() {

        if (mMarkerDatabase != null) {

            mMarkerDatabase.close();
        }
    }
}
