package osm.mapnotes;

import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayList;

public class KeepRightDiskCache {

    final static int STATE_IDLE=0;
    final static int STATE_RUNNING=1;

    //final static int CMD_GET_NUMBER_OF_ERRORS=1000;

    public int mHits=0;
    public int mTotalCount=0;

    int mState=STATE_IDLE;

    String mDir=null;

    ArrayList<String> mRequestList=new ArrayList<String>();

    class DiskReaderTask extends AsyncTask<String, Void, KeepRightErrorSet> {

        protected KeepRightErrorSet doInBackground(String... params) {

            if (params.length==0) {

                return new KeepRightErrorSet("ERROR");
            }

            String key=params[0];

            KeepRightErrorSet dataSet=new KeepRightErrorSet(key);

            if (mDir==null)
                return dataSet;

            String fileName=mDir+key+".dat";

            /*
            try {
                Thread.sleep(300);
            }
            catch (InterruptedException e) {

            }
            */

            File dataFile=new File(fileName);

            if (!dataFile.exists())
                return dataSet;

            if (!dataFile.canRead())
                return dataSet;

            dataSet.readFromDisk(dataFile);

            return dataSet;
        }

        protected void onPostExecute(KeepRightErrorSet result) {

            if (mListener==null)
                return;

            mHits++;

            mState=STATE_IDLE;

            launchNextRequest();

            mListener.onDiskData(result);
        }
    };

    private DiskReaderTask mTask=null;

    public interface DiskCacheListener {

        void onDiskData(KeepRightErrorSet data);
    }

    DiskCacheListener mListener=null;

    public KeepRightDiskCache(DiskCacheListener listener) {

        mListener=listener;
    }

    public void setDir(String dir) {

        mDir=dir;
    }

    public void clearRequests() {

        mRequestList.clear();

        mHits=0;
        mTotalCount=0;
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

        mTask=new DiskReaderTask();
        mTask.execute(key);

        /*
        mTask=new AsyncTask<String, Void, Long>() {

            long mStartTime;

            protected Long doInBackground(String... params) {

                mStartTime=System.currentTimeMillis();

                if (mErrorsDataBase==null) {

                    return null;
                }

                //return new Long(200);

                return mErrorsDataBase.getNumberOfErrors();
            }

                protected void onPostExecute(Long result) {

                    mElapsedTime=System.currentTimeMillis()-mStartTime;

                    mListener.onGetNumberOfErrors(result);

                    mState=STATE_IDLE;

                    launchNextCmd();
                }
            };

            task.execute();
        }
        */
    }

    public boolean writeToDisk(KeepRightErrorSet dataSet) {

        if (mDir==null)
            return false;

        String fileName=mDir+dataSet.getKey()+".dat";

        File file=new File(fileName);

        return dataSet.writeToDisk(file);
    }
}
