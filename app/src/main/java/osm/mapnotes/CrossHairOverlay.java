package osm.mapnotes;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

public class CrossHairOverlay extends Overlay {

    private static float mLenght=20;

    @Override
    public void draw(Canvas canvas, MapView map, boolean shadow) {

        if (shadow) {
            return;
        }

        int centerX=canvas.getWidth()/2;
        int centerY=canvas.getHeight()/2;

        Paint paint=new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(10);

        canvas.drawLine(centerX-mLenght, centerY, centerX+mLenght, centerY, paint);
        canvas.drawLine(centerX, centerY-mLenght, centerX, centerY+mLenght, paint);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);

        canvas.drawLine(centerX-mLenght, centerY, centerX+mLenght, centerY, paint);
        canvas.drawLine(centerX, centerY-mLenght, centerX, centerY+mLenght, paint);
    }
}
