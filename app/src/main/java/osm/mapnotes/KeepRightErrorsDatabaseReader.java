package osm.mapnotes;

import android.os.AsyncTask;

import java.util.ArrayList;

public class KeepRightErrorsDatabaseReader extends BaseThread {

    final static int STATE_IDLE=0;
    final static int STATE_RUNNING=1;

    public int mRead=0;
    public int mTotalCount=0;

    int mState = STATE_IDLE;

    //ArrayList<String> mRequestList=new ArrayList<String>();

    KeepRightErrorsDatabase mDatabase=null;

    String mLastErrorString=null;

    private boolean mCancel=false;

    public long mStartTime;
    public long mElapsedTime;

    /*
    class DatabaseReaderTask extends AsyncTask<String, Void, KeepRightErrorDataSet> {

        @Override
        protected void onPreExecute() {

            mStartTime=System.currentTimeMillis();
        }

        protected KeepRightErrorDataSet doInBackground(String... params) {

            if (params.length==0) {

                return new KeepRightErrorDataSet("ERROR");
            }

            String key=params[0];

            KeepRightErrorDataSet dataSet=new KeepRightErrorDataSet(key);

            if (mDatabase==null)
                return dataSet;

            mDatabase.getErrors(dataSet);

            return dataSet;
        }

        protected void onPostExecute(KeepRightErrorDataSet result) {

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
    */

    //private DatabaseReaderTask mTask=null;

    public interface DatabaseReaderListener {

        void onDatabaseData(KeepRightErrorDataSet data, long elapsedTime);
    }

    public class DatabaseEvent extends ThreadEvent {

        public String mKey;

        KeepRightErrorDataSet mDataSet;
    }

    //private MapView mMapView=null;

    private DatabaseReaderListener mListener=null;

    public KeepRightErrorsDatabaseReader(/* MapView mapView, */DatabaseReaderListener listener) {

        //mMapView=mapView;

        setListener(listener);
    }

    public void setListener(DatabaseReaderListener listener) {

        mListener=listener;

        createHandler();
    }

    @Override
    public void run() {

        try {

            while(!isInterrupted()) {

                ThreadEvent event = waitForInputEvent();

                if (!(event instanceof DatabaseEvent)) {

                    // Received event is not of type DatabaseEvent
                    continue;
                }

                DatabaseEvent databaseEvent = (DatabaseEvent) event;

                String key = databaseEvent.mKey;

                KeepRightErrorDataSet dataSet=new KeepRightErrorDataSet(key);

                if (mDatabase != null) {

                    mStartTime=System.currentTimeMillis();

                    mDatabase.getErrors(dataSet);
                }
                else {

                    dataSet.setData(null);
                }

                databaseEvent.mDataSet = dataSet;

                addOutputEvent(databaseEvent);
            }

        } catch (InterruptedException e) {

            // Thread has been interrupted

            // Close database
            closeDatabase();
        }
    }

    @Override
    protected void dispatchEvent(ThreadEvent event) {

        mState = STATE_IDLE;

        if (event == null) {
            return;
        }

        if (!(event instanceof DatabaseEvent)) {

            // Received event is not of type DatabaseEvent
            return;
        }

        DatabaseEvent databaseEvent = (DatabaseEvent)event;

        mElapsedTime = System.currentTimeMillis()-mStartTime;

        if (mListener != null) {

            mListener.onDatabaseData(databaseEvent.mDataSet, mElapsedTime);
        }
    }

    /*
    public void clearRequests() {

        mRequestList.clear();

        mRead=0;
        mTotalCount=0;
    }
    */

    public boolean openDatabase(String fileName) {

        mDatabase = new KeepRightErrorsDatabase(/*mMapView*/);

        if (!mDatabase.openDatabase(fileName)) {

            mLastErrorString="Error opening 'keepright_errors.db':" +mDatabase.mLastErrorString;

            mDatabase.close();

            mDatabase = null;

            return false;
        }

        mLastErrorString="Database 'keepright_error.db' opened Ok!";

        return true;
    }

    public void closeDatabase() {

        mDatabase.close();

        mDatabase = null;
    }

    public boolean databaseIsOpen() {

        return (mDatabase != null);
    }

    void close() {

        mCancel = true;

        // Interrupt thread
        interrupt();

        /*
        while(mTask.getStatus()== AsyncTask.Status.RUNNING) {

            try {
                Thread.sleep(100);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        */

        //mTask.cancel(true);
    }

    public void requestData(String key) {

        /*
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
        */

        DatabaseEvent databaseEvent = new DatabaseEvent();

        databaseEvent.mKey = key;

        mState = STATE_RUNNING;

        addInputEvent(databaseEvent);
    }

    public boolean isIdle() {

        return (mState == STATE_IDLE);
    }

    /*
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
    */
}
