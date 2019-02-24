package osm.mapnotes;

import android.view.MotionEvent;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MyMarker extends Marker {

    // Container Activity must implement this interface
    public interface OnMyMarkerListener {

        public void onLongPress(MyMarker marker);
        //public void onHideSoftInput();
    }

    OnMyMarkerListener mListener=null;

    public MyMarker(MapView mapView, OnMyMarkerListener listener) {
        super(mapView);

        mListener=listener;
    }

    @Override
    public boolean onLongPress(final MotionEvent event, final MapView mapView) {

        boolean touched = hitTest(event, mapView);

        if (touched) {

            mListener.onLongPress(this);

        }

        return touched;
    }

    /*
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
    */
}
