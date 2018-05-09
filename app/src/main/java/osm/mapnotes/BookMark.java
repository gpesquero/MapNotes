package osm.mapnotes;

import org.osmdroid.api.IGeoPoint;

public class BookMark {

    IGeoPoint mPosition=null;
    String mName=null;

    public BookMark() {

    }

    void setPosition(IGeoPoint position) {

        mPosition=position;
    }

    IGeoPoint getPosition() {

        return mPosition;
    }

    void setName(String name) {

        mName=name;
    }

    String getName() {

        return mName;
    }
}
