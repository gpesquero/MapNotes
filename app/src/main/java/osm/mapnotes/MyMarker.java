package osm.mapnotes;

import android.view.MotionEvent;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MyMarker extends Marker {

    // Container Activity must implement this interface
    public interface OnMyMarkerListener {

        void onLongPress(MyMarker marker);
    }

    private OnMyMarkerListener mListener;

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
}
