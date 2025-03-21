package osm.mapnotes.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.LocationProvider;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import osm.mapnotes.LocationStatus;

public class DebugOverlay extends Overlay
{
  private final LocationStatus mLocationStatus;

  public DebugOverlay(LocationStatus locationStatus)
  {
    mLocationStatus = locationStatus;
  }

  @Override
  public void draw(Canvas canvas, MapView map, boolean shadow)
  {
    if (!isEnabled())
      return;

    if (shadow)
      return;

    Paint paint = new Paint();

    paint.setColor(Color.WHITE);
    drawTextLines(canvas, paint, 5, 250);

    paint.setColor(Color.BLACK);
    drawTextLines(canvas, paint, 6, 251);
  }

  private void drawTextLines(Canvas canvas, Paint paint, int x, int y)
  {
    String text;

    paint.setTextSize(40);

    text = "Loc Perm: " + mLocationStatus.hasPermissions();
    canvas.drawText(text, x, y, paint);

    text = "Loc Enab: " + mLocationStatus.isProviderEnabled();
    canvas.drawText(text, x, y + 30, paint);

    text = "Loc Stat: ";

    switch(mLocationStatus.getStatus())
    {
      case LocationProvider.OUT_OF_SERVICE:
        text += "OutOfService";
        break;

      case LocationProvider.AVAILABLE:
        text += "Available";
        break;

      case LocationProvider.TEMPORARILY_UNAVAILABLE:
        text += "Temp.Unavail";
        break;

      default:
        text += "Unknown";
        break;
    }

    canvas.drawText(text, x, y + 60, paint);

    text = "Loc Sats: " + mLocationStatus.getNumSats();
    canvas.drawText(text, x, y + 90, paint);
  }
}
