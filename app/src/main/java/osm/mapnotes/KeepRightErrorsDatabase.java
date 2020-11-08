package osm.mapnotes;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class KeepRightErrorsDatabase {

    private SQLiteDatabase mDatabase=null;

    public String mLastErrorString=null;

    private String mSqlGetErrors="SELECT FROM errors(error_id, error_name, lon1, lon2, lon3, lat1, lat2, lat3, msg_id) VALUES(?,?,?,?,?,?,?,?,?)";

    private ArrayList<String> mDbInfoList;

    public KeepRightErrorsDatabase() {
    }

    public boolean openDatabase(String fileName) {

        mDatabase = SQLiteDatabase.openDatabase(fileName, null, SQLiteDatabase.OPEN_READONLY);

        /*
        if (!mDatabase.isDatabaseIntegrityOk()) {

            mLastErrorString="Database integrity error_circle";

            mDatabase.close();
            mDatabase=null;

            return false;
        }
        */

        return readDbInfo();
    }

    private boolean readDbInfo() {

        String table="info";
        String[] tableColumns=null;
        String whereClause=null;
        String[] whereArgs=null;
        String groupBy=null;
        String having=null;
        String orderBy=null;

        if (mDatabase == null) {

            mLastErrorString="Error readDbInfo(): mDatabase==null";

            return false;
        }

        mDbInfoList = new ArrayList<>();

        try {

            Cursor cursor = mDatabase.query(table, tableColumns, whereClause, whereArgs, groupBy,
                    having, orderBy);

            while(cursor.moveToNext()) {

                String infoKey = cursor.getString(cursor.getColumnIndex("info_key"));

                String infoValue = cursor.getString(cursor.getColumnIndex("info_value"));

                mDbInfoList.add(infoKey+" = "+infoValue);
            }

            cursor.close();
        }
        catch(SQLException e) {

            mLastErrorString="Error readDbInfo(): "+e.getMessage();

            return false;
        }

        return true;
    }

    public void close() {

        if (mDatabase != null) {

            mDatabase.close();

            mDatabase = null;
        }
    }

    public long getNumberOfErrors() {

        long numRows;

        if (mDatabase == null) {

            numRows=-1;
        }
        else {

            numRows = DatabaseUtils.queryNumEntries(mDatabase, "errors");
        }

        return numRows;
    }

    public KeepRightErrorDataSet getErrors(String key) {

        KeepRightErrorDataSet dataSet = new KeepRightErrorDataSet(key);

        getErrors(dataSet);

        return dataSet;
    }

    public void getErrors(KeepRightErrorDataSet dataSet) {

        String key = dataSet.getKey();
        int separatorPos = key.indexOf(",");

        int latIndex = Integer.parseInt(key.substring(0, separatorPos));
        int lonIndex = Integer.parseInt(key.substring(separatorPos+1));

        ArrayList<KeepRightErrorData> data = getErrors(lonIndex, latIndex);

        dataSet.setData(data);
    }

    public ArrayList<KeepRightErrorData> getErrors(int lonIndex, int latIndex) {

        String table="errors";
        String[] tableColumns=null;

        String whereClause=
                "lon1='"+lonIndex+"' AND "+
                "lat1='"+latIndex+"'";

        String[] whereArgs=null;
        String groupBy=null;
        String having=null;
        String orderBy=null;

        if (mDatabase == null) {

            mLastErrorString="Error getMarkers(): mDatabase==null";

            return null;
        }

        ArrayList<KeepRightErrorData> data = new ArrayList<>();

        try {

            Cursor cursor = mDatabase.query(table, tableColumns, whereClause, whereArgs, groupBy,
                    having, orderBy);

            while(cursor.moveToNext()) {

                String errorId = cursor.getString(cursor.getColumnIndex("error_id"));

                String errorName = cursor.getString(cursor.getColumnIndex("error_name"));

                int lon1 = cursor.getInt(cursor.getColumnIndex("lon1"));
                int lon2 = cursor.getInt(cursor.getColumnIndex("lon2"));
                int lat1 = cursor.getInt(cursor.getColumnIndex("lat1"));
                int lat2 = cursor.getInt(cursor.getColumnIndex("lat2"));

                String msgId = cursor.getString(cursor.getColumnIndex("msg_id"));

                double lon=(lon1*100000.0+lon2)/10000000.0;
                double lat=(lat1*100000.0+lat2)/10000000.0;

                GeoPoint pos = new GeoPoint(lat, lon);

                KeepRightErrorData errorData = new KeepRightErrorData();

                errorData.mErrorId = errorId;
                errorData.mPosition = pos;
                errorData.mErrorName = errorName;
                errorData.mMsgId = msgId;

                data.add(errorData);
            }

            cursor.close();
        }
        catch(SQLException e) {

            mLastErrorString="Error getMarkers(): "+e.getMessage();
        }

        return data;
    }

    public int getErrors(int minLon1, int minLon2, int minLon3,
                   int maxLon1, int maxLon2, int maxLon3,
                   int minLat1, int minLat2, int minLat3,
                   int maxLat1, int maxLat2, int maxLat3) {

        String sqlQuery="SELECT * FROM errors WHERE "+
                "lon1>='"+minLon1+"' AND "+
                "lon1<='"+maxLon1+"' AND "+
                "lat1>='"+minLat1+"' AND "+
                "lat1<='"+maxLat1+"' AND "+
                "lon2>='"+minLon2+"' AND "+
                "lon2<='"+maxLon2+"' AND "+
                "lat2>='"+minLat2+"' AND "+
                "lat2<='"+maxLat2+"' AND "+
                "lon3>='"+minLon3+"' AND "+
                "lon3<='"+maxLon3+"' AND "+
                "lat3>='"+minLat3+"' AND "+
                "lat3<='"+maxLat3+"'";

        String table="errors";
        String[] tableColumns=null;

        String whereClause=
                "lon1>='"+minLon1+"' AND "+
                "lon1<='"+maxLon1+"' AND "+
                "lat1>='"+minLat1+"' AND "+
                "lat1<='"+maxLat1+"' AND "+
                "lon2>='"+minLon2+"' AND "+
                "lon2<='"+maxLon2+"' AND "+
                "lat2>='"+minLat2+"' AND "+
                "lat2<='"+maxLat2+"'";

        String[] whereArgs=null;
        String groupBy=null;
        String having=null;
        String orderBy=null;

        if (mDatabase==null) {

            mLastErrorString="Error getMarkers(): mDatabase==null";

            return -1;
        }

        int count=1;

        try {

            Cursor cursor = mDatabase.query(table, tableColumns, whereClause, whereArgs, groupBy,
                    having, orderBy);

            count=cursor.getCount();

            cursor.close();
        }
        catch(SQLException e) {

            mLastErrorString="Error getMarkers(): "+e.getMessage();
        }

        return count;
    }

    public ArrayList<String> getDbInfo() {

        return mDbInfoList;
    }
}
