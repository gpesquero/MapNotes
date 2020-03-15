package osm.mapnotes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

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

        if (mData==null)
            return -1;

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

        /*
        try {
            FileOutputStream fos = new FileOutputStream(dataFileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mData);
            oos.close();
            fos.close();
        }
        catch(FileNotFoundException e) {


        }
        */

        return false;
    }

    public boolean writeToDisk(File dataFile) {

        if (mData==null) {

            return false;
        }

        try {
            FileOutputStream fos=new FileOutputStream(dataFile);
            ObjectOutputStream oos=new ObjectOutputStream(fos);

            Iterator<KeepRightError> iter=mData.iterator();

            while(iter.hasNext()) {

                KeepRightError error=iter.next();

                oos.writeObject(error);
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
