package osm.mapnotes;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

public class ErrorsDatabase {

    private SQLiteDatabase mDatabase=null;

    public String mLastErrorString=null;

    public ErrorsDatabase() {

    }

    public boolean openDatabase(String fileName) {

        mDatabase=SQLiteDatabase.openDatabase(fileName, null, SQLiteDatabase.OPEN_READONLY);

        /*
        if (!mDatabase.isDatabaseIntegrityOk()) {

            mLastErrorString="Database integrity error";

            mDatabase.close();
            mDatabase=null;

            return false;
        }
        */

        return true;
    }

    public void close() {

        if (mDatabase!=null) {

            mDatabase.close();

            mDatabase=null;
        }
    }

    public long getNumberOfErrors() {

        long numRows;

        if (mDatabase==null) {

            numRows=-1;
        }
        else {

            numRows=DatabaseUtils.queryNumEntries(mDatabase, "errors");
        }

        return numRows;
    }
}
