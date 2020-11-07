package osm.mapnotes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

public class CrossHairOverlay extends Overlay {

    Bitmap mBitmap;

    CrossHairOverlay(Bitmap bitmap) {

        mBitmap=bitmap;
    }

    @Override
    public void draw(Canvas canvas, MapView map, boolean shadow) {

        float CROSS_HAIR_SIZE=30;
        float BIG_STROKE_WIDTH=15;
        float SMALL_STROKE_WIDTH=5;

        if (shadow) {
            return;
        }

        int centerX=canvas.getWidth()/2;
        int centerY=canvas.getHeight()/2;

        Paint paint=new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(BIG_STROKE_WIDTH);

        canvas.drawLine(centerX-CROSS_HAIR_SIZE, centerY,
                centerX+CROSS_HAIR_SIZE, centerY, paint);

        canvas.drawLine(centerX, centerY-CROSS_HAIR_SIZE,
                centerX, centerY+CROSS_HAIR_SIZE, paint);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(SMALL_STROKE_WIDTH);

        canvas.drawLine(centerX-CROSS_HAIR_SIZE, centerY,
                centerX+CROSS_HAIR_SIZE, centerY, paint);

        canvas.drawLine(centerX, centerY-CROSS_HAIR_SIZE,
                centerX, centerY+CROSS_HAIR_SIZE, paint);
    }
}
