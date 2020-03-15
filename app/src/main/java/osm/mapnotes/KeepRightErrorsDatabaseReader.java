package osm.mapnotes;

import android.os.AsyncTask;

import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class KeepRightErrorsDatabaseReader {

    final static int STATE_IDLE=0;
    final static int STATE_RUNNING=1;

    public int mRead=0;
    public int mTotalCount=0;

    int mState=STATE_IDLE;

    ArrayList<String> mRequestList=new ArrayList<String>();

    KeepRightErrorsDatabase mDatabase=null;

    String mLastErrorString=null;

    private boolean mCancel=false;

    public long mStartTime;
    public long mElapsedTime;

    class DatabaseReaderTask extends AsyncTask<String, Void, KeepRightErrorSet> {

        @Override
        protected void onPreExecute() {

            mStartTime=System.currentTimeMillis();
        }

        protected KeepRightErrorSet doInBackground(String... params) {

            if (params.length==0) {

                return new KeepRightErrorSet("ERROR");
            }

            String key=params[0];

            KeepRightErrorSet dataSet=new KeepRightErrorSet(key);

            if (mDatabase==null)
                return dataSet;

            mDatabase.getErrors(dataSet);

            return dataSet;
        }

        protected void onPostExecute(KeepRightErrorSet result) {

            if (mCancel)
                return;

            if (mListener==null)
                return;

            mElapsedTime=System.currentTimeMillis()-mStartTime;

            mRead++;

            mListener.onDatabaseData(result, mElapsedTime);

            mState=STATE_IDLE;

            launchNextRequest();
        }
    };

    private DatabaseReaderTask mTask=null;

    public interface DatabaseReaderListener {

        void onDatabaseData(KeepRightErrorSet data, long elapsedTime);
    }

    private MapView mMapView=null;

    private DatabaseReaderListener mListener=null;

    public KeepRightErrorsDatabaseReader(MapView mapView, DatabaseReaderListener listener) {

        mMapView=mapView;

        mListener=listener;
    }

    public void clearRequests() {

        mRequestList.clear();

        mRead=0;
        mTotalCount=0;
    }

    public boolean openDatabase(String fileName) {

        mDatabase=new KeepRightErrorsDatabase(mMapView);

        if (!mDatabase.openDatabase(fileName)) {

            mLastErrorString="Error opening 'keepright_errors.db':" +mDatabase.mLastErrorString;

            mDatabase.close();

            mDatabase=null;

            return false;
        }

        mLastErrorString="Database 'keepright_error.db' opened Ok!";

        return true;
    }

    void close() {

        mCancel=true;

        while(mTask.getStatus()== AsyncTask.Status.RUNNING) {

            try {
                Thread.sleep(100);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //mTask.cancel(true);
    }

    public void get(String key) {

        boolean found=false;

        for(int i=0; i<mRequestList.size(); i++) {

            if (mRequestList.get(i)==key) {

                // Request already exists...

                found=true;

                break;
            }
        }

        if (!found) {

            mRequestList.add(key);
        }

        launchNextRequest();

        mTotalCount++;
    }

    private void launchNextRequest() {

        if (mState==STATE_RUNNING) {

            return;
        }

        if (mRequestList.size()==0) {

            return;
        }

        mState=STATE_RUNNING;

        String key=mRequestList.remove(0);

        mTask=new DatabaseReaderTask();
        mTask.execute(key);
    }

}
