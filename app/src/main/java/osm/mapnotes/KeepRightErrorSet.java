package osm.mapnotes;

import java.io.File;
import java.util.ArrayList;

public class KeepRightErrorSet {

    private String mKey=null;
    private ArrayList<KeepRightError> mData=null;

    public KeepRightErrorSet(String key) {

        mKey=key;
    }

    public String getKey() {

        return mKey;
    }

    public int getCount() {

        return mData.size();
    }

    boolean containsData() {

        if (mData==null) {

            return false;
        }

        return true;
    }

    public void setData(ArrayList<KeepRightError> data) {

        mData=data;
    }

    public ArrayList<KeepRightError> getData() {

        return mData;
    }

    public boolean readFromDisk(File dataFile) {

        return false;
    }

    public boolean writeToDisk(File dataFile) {

        return false;
    }
}
