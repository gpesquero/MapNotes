package osm.mapnotes;

import java.util.ArrayList;

public class KeepRightErrorsDatabaseReader extends BaseThread {

    final static int STATE_IDLE=0;
    final static int STATE_RUNNING=1;

    public int mRead=0;
    public int mTotalCount=0;

    int mState = STATE_IDLE;

    KeepRightErrorsDatabase mDatabase=null;

    String mLastErrorString=null;

    public long mStartTime;
    public long mElapsedTime;

    public interface DatabaseReaderListener {

        void onDatabaseData(KeepRightErrorDataSet data, long elapsedTime);
    }

    public static class DatabaseEvent extends ThreadEvent {

        public String mKey;

        KeepRightErrorDataSet mDataSet;
    }

    private DatabaseReaderListener mListener=null;

    public KeepRightErrorsDatabaseReader(DatabaseReaderListener listener) {

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

    public boolean openDatabase(String fileName) {

        mDatabase = new KeepRightErrorsDatabase();

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

        // Interrupt thread
        interrupt();
    }

    public void requestData(String key) {

        DatabaseEvent databaseEvent = new DatabaseEvent();

        databaseEvent.mKey = key;

        mState = STATE_RUNNING;

        addInputEvent(databaseEvent);
    }

    public boolean isIdle() {

        return (mState == STATE_IDLE);
    }

    public ArrayList<String> getDbInfo() {

        return mDatabase.getDbInfo();
    }
}
