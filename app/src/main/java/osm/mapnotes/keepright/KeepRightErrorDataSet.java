package osm.mapnotes.keepright;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class KeepRightErrorDataSet {

    private String mKey;
    private ArrayList<KeepRightErrorData> mData = null;

    public KeepRightErrorDataSet(String key) {

        mKey = key;
    }

    public String getKey() {

        return mKey;
    }

    public int getCount() {

        if (mData == null)
            return -1;

        return mData.size();
    }

    public boolean containsData() {

        if (mData == null) {

            return false;
        }

        return true;
    }

    public void setData(ArrayList<KeepRightErrorData> data) {

        mData = data;
    }

    public ArrayList<KeepRightErrorData> getData() {

        return mData;
    }

    public boolean readFromDisk(File dataFile) {

        /*
        try {
            FileOutputStream fos = new FileOutputStream(dataFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mData);
            oos.close();
            fos.close();
        }
        catch(FileNotFoundException e) {

        }
        catch(IOException e) {

        }
        */

        return false;
    }

    public boolean writeToDisk(File dataFile) {

        if (mData == null) {

            return false;
        }

        try {
            FileOutputStream fos = new FileOutputStream(dataFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            Iterator<KeepRightErrorData> iterator = mData.iterator();

            while(iterator.hasNext()) {

                KeepRightErrorData errorData=iterator.next();

                oos.writeObject(errorData);
            }

            oos.close();
            fos.close();
        }
        catch(FileNotFoundException e) {

            return false;
        }
        catch(IOException e) {

            return false;
        }

        return true;
    }
}
