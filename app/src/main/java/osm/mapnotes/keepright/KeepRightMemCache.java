package osm.mapnotes.keepright;

import java.util.ArrayList;

public class KeepRightMemCache {

    private int mMaxObjects = 100;

    private final ArrayList<String> mKeys = new ArrayList<>();
    private final ArrayList<KeepRightErrorDataSet> mData = new ArrayList<>();

    // Statistics
    private int mRequestCount = 0;
    private int mHitCount = 0;

    public KeepRightMemCache(int maxObjects) {

        mMaxObjects = maxObjects;
    }

    public int size() {

        return mKeys.size();
    }

    public int maxSize() {

        return mMaxObjects;
    }

    public int requestCount() {

        return mRequestCount;
    }

    public int hitCount() {

        return mHitCount;
    }

    public void add(KeepRightErrorDataSet dataSet) {

        add(dataSet.getKey(), dataSet);
    }

    public void add(String key, KeepRightErrorDataSet data) {

        int pos = mKeys.indexOf(key);

        if (pos >= 0) {

            // Item has been found. Remove it from cache
            mKeys.remove(pos);
            mData.remove(pos);
        }

        // Add item at the beginning of cache
        mKeys.add(0, key);
        mData.add(0, data);

        // Get size of cache
        int size = mKeys.size();

        if (size > mMaxObjects) {

            // Remove last item of cache
            mKeys.remove(size-1);
            mData.remove(size-1);
        }
    }

    public KeepRightErrorDataSet get(String key) {

        // For statistics, store the number of get requests
        mRequestCount++;

        int pos = mKeys.indexOf(key);

        if (pos<0) {

            // Item not found in cache
            return null;
        }

        // Increment hit count
        mHitCount++;

        // Remove item from cache
        mKeys.remove(pos);
        KeepRightErrorDataSet object=mData.remove(pos);

        // Add item at the beginning of cache
        mKeys.add(0, key);
        mData.add(0, object);

        return object;
    }
}
