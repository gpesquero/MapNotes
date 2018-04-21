package osm.mapnotes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.LocationProvider;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

public class DebugOverlay extends Overlay {

    public LocationStatus mLocationStatus=null;

    public DebugOverlay(LocationStatus locationStatus) {

        mLocationStatus=locationStatus;
    }

    @Override
    public void draw(Canvas canvas, MapView map, boolean shadow) {

        if (!isEnabled()) {
            return;
        }

        if (shadow) {
            return;
        }

        Paint paint=new Paint();

        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 200, 500, 600, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(30);

        String text;

        text="Loc Perm: "+mLocationStatus.hasPermissions();
        canvas.drawText(text,5, 250, paint);

        text="Loc Enab: "+mLocationStatus.isProviderEnabled();
        canvas.drawText(text,5, 280, paint);

        text="Loc Stat: ";

        switch(mLocationStatus.getStatus()) {
            case LocationProvider.OUT_OF_SERVICE:
                text+="OutOfService";
                break;

            case LocationProvider.AVAILABLE:
                text+="Available";
                break;

            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                text+="Temp.Unavail";
                break;

            default:
                text+="Unknown";
                break;
        }

        canvas.drawText(text,5, 310, paint);

        text="Loc Sats: "+mLocationStatus.getNumSats();
        canvas.drawText(text,5, 340, paint);
    }
}
