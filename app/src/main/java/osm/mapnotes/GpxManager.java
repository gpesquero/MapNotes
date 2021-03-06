package osm.mapnotes;

import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.ArrayList;

public class GpxManager {

    public String mLastErrorString=null;

    private int mGpxFileCount=0;

    private ArrayList<Polyline> mPolylines=new ArrayList<>();

    GpxManager() {

    }

    public void readFiles(MyPreferences preferences) {

        String gpxDirPath=preferences.mInternalDataPath+"gpx";

        File gpxDir=new File(gpxDirPath);

        File[] gpxFiles=gpxDir.listFiles();

        if (gpxFiles!=null) {

            for (File file : gpxFiles) {

                String filePath = file.getAbsolutePath();

                if (filePath.endsWith(".gpx")) {

                    GpxFile gpxFile = new GpxFile();

                    if (gpxFile.openFile(filePath)) {

                        mGpxFileCount++;

                        mPolylines.addAll(gpxFile.getPolylines());
                    }
                }
            }
        }

        mLastErrorString="Loaded "+mGpxFileCount+" Gpx files";
    }

    public ArrayList<Polyline> getPolylines() {

        return mPolylines;
    }
}
