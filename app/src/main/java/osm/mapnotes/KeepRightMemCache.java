package osm.mapnotes;

import java.util.ArrayList;

public class KeepRightMemCache {

    private int mMaxObjects=100;

    private ArrayList<String> mKeys=new ArrayList<String>();
    private ArrayList<KeepRightErrorSet> mData=new ArrayList<KeepRightErrorSet>();

    public KeepRightMemCache(int maxObjects) {

        mMaxObjects=maxObjects;
    }

    public int size() {

        return mKeys.size();
    }

    public void add(KeepRightErrorSet dataSet) {

        add(dataSet.getKey(), dataSet);
    }

    public void add(String key, KeepRightErrorSet data) {

        int pos=mKeys.indexOf(key);

        if (pos>=0) {

            mKeys.remove(pos);
            mData.remove(pos);
        }

        mKeys.add(0, key);
        mData.add(0, data);

        int size=mKeys.size();

        if (size>mMaxObjects) {

            mKeys.remove(size-1);
            mData.remove(size-1);
        }
    }

    public KeepRightErrorSet get(String key) {

        int pos=mKeys.indexOf(key);

        if (pos<0) {
            return null;
        }

        mKeys.remove(pos);
        KeepRightErrorSet object=mData.remove(pos);

        mKeys.add(0, key);
        mData.add(0, object);

        return object;
    }
}
