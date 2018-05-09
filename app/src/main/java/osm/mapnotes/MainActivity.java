package osm.mapnotes;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener, Runnable,
        View.OnClickListener, MarkerDialogFragment.OnMarkerDialogListener {

    // OsmDroid objects

    private MapView mMapView = null;
    private IMapController mMapController;

    MyLocationNewOverlay mLocationOverlay = null;

    CompassOverlay mCompassOverlay = null;

    CrossHairOverlay mCrossHairOverlay = null;
    DebugOverlay mDebugOverlay = null;

    //ItemizedOverlayWithFocus mItemOverlay=null;
    ItemizedIconOverlay mItemOverlay=null;

    ArrayList<OverlayItem> mItems=new ArrayList<OverlayItem>();

    private long mPreviousCancelTime = 0;

    private static long CANCEL_TIMEOUT = 2000;

    private static int TIMEOUT_1_SEC = 1000;

    private LocationManager mLocationManager = null;

    LocationStatus mLocationStatus = new LocationStatus();

    Location mLocation = null;

    private static int REQUEST_CODE_LOCATION = 0;

    // Controls

    ImageView mImageViewLocation = null;
    ImageView mImageViewPreferences = null;
    ImageView mImageViewBookmark= null;

    private boolean mTick = true;

    private Bitmap grayLocationIcon = null;
    private Bitmap redLocationIcon = null;
    private Bitmap greenLocationIcon = null;

    public static MyPreferences mPreferences = new MyPreferences();

    //private ArrayList<BookMark> mBookMarks=new ArrayList<BookMark>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getApplicationContext();

        mPreferences.readPreferences(context);

        setContentView(R.layout.activity_main);

        mMapView = (MapView) findViewById(R.id.map);
        setMapTileSource();
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);

        mImageViewLocation = (ImageView) findViewById(R.id.imageViewLocation);
        mImageViewLocation.setOnClickListener(this);

        mImageViewPreferences = (ImageView) findViewById(R.id.imageViewPreferences);
        mImageViewPreferences.setOnClickListener(this);

        mImageViewBookmark= (ImageView) findViewById(R.id.imageViewBookmark);
        mImageViewBookmark.setOnClickListener(this);

        createLocationIcons(context);

        mMapController = mMapView.getController();
        mMapController.setZoom(mPreferences.mZoom);
        GeoPoint startPoint = new GeoPoint(mPreferences.mLat, mPreferences.mLon);
        mMapController.setCenter(startPoint);

        mLocationManager = null;

        if ((ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            mLocationStatus.setHasPermissions(false);

            // We don't have location permissions, so we have to request them...
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_LOCATION);
        } else {

            mLocationStatus.setHasPermissions(true);

            // We have location permissions, so we start to request location updates...
            requestLocationUpdates();
        }

        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mMapView);
        mLocationOverlay.enableMyLocation();
        mMapView.getOverlays().add(mLocationOverlay);

        mCompassOverlay = new CompassOverlay(context,
                new InternalCompassOrientationProvider(context), mMapView);
        mCompassOverlay.enableCompass();
        mMapView.getOverlays().add(this.mCompassOverlay);

        mCrossHairOverlay = new CrossHairOverlay();
        mMapView.getOverlays().add(mCrossHairOverlay);

        mDebugOverlay = new DebugOverlay(mLocationStatus);
        mDebugOverlay.setEnabled(mPreferences.mShowDebugOverlay);
        //mMapView.getOverlays().add(mDebugOverlay);

        //mItemOverlay=new ItemizedOverlayWithFocus<OverlayItem>(mItems,
        mItemOverlay=new ItemizedIconOverlay<OverlayItem>(mItems,
                        new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        //do something
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, this);

        //mItemOverlay.setFocusItemsOnTap(true);
        mItemOverlay.setEnabled(true);

        mMapView.getOverlays().add(mItemOverlay);

        /*
        Timer timer1sec = new Timer();
        timer1sec.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(MainActivity.this);
            }

        }, 0, TIMEOUT_1_SEC);
        */

        //GnssStatus mGnssStatus=new GnssStatus();


        updateLocationStatus();

        //Toast.makeText(this, "Start...", Toast.LENGTH_SHORT).show();
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {

        if (requestCode == REQUEST_CODE_LOCATION) {

            for (int i = 0; i < grantResults.length; i++) {

                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                    mLocationStatus.setHasPermissions(false);

                    updateLocationStatus();
                    return;
                }
            }

            mLocationStatus.setHasPermissions(true);

            requestLocationUpdates();
        }
    }

    private void requestLocationUpdates() {

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationStatus.setProviderEnabled(

                mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        //mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        try {

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 1, this);

            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000, 1, this);

        } catch (SecurityException e) {

            mLocationStatus.setHasPermissions(false);

            updateLocationStatus();

            Toast.makeText(this, "Error en requestLocationUpdates", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPreferences.mLon = (float) mMapView.getMapCenter().getLongitude();
        mPreferences.mLat = (float) mMapView.getMapCenter().getLatitude();
        mPreferences.mZoom = (float) mMapView.getZoomLevelDouble();

        Context context = getApplicationContext();

        mPreferences.storePreferences(context);
    }

    @Override
    public void onBackPressed() {

        long currentTime = System.currentTimeMillis();

        if ((currentTime - mPreviousCancelTime) > CANCEL_TIMEOUT) {

            Toast.makeText(this, R.string.press_again_back, Toast.LENGTH_SHORT).show();

            mPreviousCancelTime = currentTime;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        mLocation = location;

        updateLocationStatus();

        if (mTick) {

            mImageViewLocation.setImageBitmap(redLocationIcon);

            mTick = false;
        } else {

            mImageViewLocation.setImageBitmap(grayLocationIcon);

            mTick = true;
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

        mLocationStatus.setStatus(i);
        mLocationStatus.setNumSats(33);

        String text = "Provider: " + s;

        switch (i) {
            case LocationProvider.OUT_OF_SERVICE:
                text += ", OUT_OF_SERVICE";
                break;

            case LocationProvider.AVAILABLE:
                text += ", AVAILABLE";
                break;

            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                text += ", TEMP_UNAVAILABLE";
                break;

            default:
                text += ", UNKNOWN";
                break;
        }

        int sats=(int)bundle.get("satellites");

        text += ", Sats: " + sats;

        //Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

        updateLocationStatus();
    }

    @Override
    public void onProviderEnabled(String s) {

        mLocationStatus.setProviderEnabled(true);

        updateLocationStatus();
    }

    @Override
    public void onProviderDisabled(String s) {

        mLocationStatus.setProviderEnabled(false);

        updateLocationStatus();
    }

    private void updateLocationStatus() {

        mMapView.invalidate();
    }

    @Override
    public void run() {

        if (mTick) {

            mImageViewLocation.setImageBitmap(redLocationIcon);

            mTick = false;
        } else {

            mImageViewLocation.setImageBitmap(grayLocationIcon);

            mTick = true;
        }
    }

    void createLocationIcons(Context ctx) {

        grayLocationIcon = BitmapFactory.decodeResource(ctx.getResources(),
                R.drawable.ic_menu_mylocation);

        float[] redValues = new float[]{
                1, 1, 1, 1, 0,
                0, 1, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 1, 0};

        ColorMatrixColorFilter redFilter = new ColorMatrixColorFilter(
                new ColorMatrix(redValues));

        Paint paint = new Paint();
        paint.setColorFilter(redFilter);

        redLocationIcon = Bitmap.createBitmap(grayLocationIcon).copy(Bitmap.Config.ARGB_8888, true);

        Canvas redCanvas = new Canvas(redLocationIcon);
        redCanvas.drawBitmap(grayLocationIcon, 0, 0, paint);

        greenLocationIcon = Bitmap.createBitmap(grayLocationIcon).copy(Bitmap.Config.ARGB_8888, true);

        float[] greenValues = new float[]{
                1, 0, 0, 0, 0,
                1, 1, 1, 1, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 1, 0};

        ColorMatrixColorFilter greenFilter = new ColorMatrixColorFilter(
                new ColorMatrix(greenValues));

        paint.setColorFilter(greenFilter);

        Canvas greenCanvas = new Canvas(greenLocationIcon);
        greenCanvas.drawBitmap(grayLocationIcon, 0, 0, paint);
    }

    @Override
    public void onClick(View view) {

        if (view == mImageViewLocation) {

            if (mLocation == null)
                return;

            GeoPoint centerPoint = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());
            //mMapController.setCenter(centerPoint);
            mMapController.animateTo(centerPoint);

        } else if (view == mImageViewPreferences) {

            Intent intent = new Intent(this, PreferencesActivity.class);
            //EditText editText = (EditText) findViewById(R.id.editText);
            //String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);

            startActivity(intent);
        }
        else if (view == mImageViewBookmark) {

            FragmentTransaction ft = getFragmentManager().beginTransaction();

            Fragment prev = getFragmentManager().findFragmentByTag(getString(R.string.dialog_marker));

            if (prev != null) {
                ft.remove(prev);
            }

            ft.addToBackStack(null);

            MarkerDialogFragment fragment=new MarkerDialogFragment();

            Drawable icon=getResources().getDrawable(android.R.drawable.btn_star_big_on);

            GeoPoint center=(GeoPoint)mMapView.getMapCenter();

            /*
            OverlayItem item=new OverlayItem("Title", "Snippet", center);
            item.setMarker(icon);

            mItemOverlay.addItem(item);

            mMapView.invalidate();
            */

            Marker marker = new Marker(mMapView);
            marker.setPosition(center);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            marker.setIcon(icon);
            marker.setTitle(getString(R.string.marker_name));


            fragment.setMarker(marker);
            fragment.setNewMarker(true);

            //bm.setPosition(center);
            //bm.setName("Hello");

            //mBookMarks.add(bm);

            /*
            Bundle args=new Bundle();
            args.putDouble(getString(R.string.key_lat), center.getLatitude());
            args.putDouble(getString(R.string.key_lon), center.getLongitude());

            fragment.setArguments(args);
            */

            fragment.show(getFragmentManager(), getString(R.string.dialog_marker));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //mDebugOverlay.enable(mPreferences.mShowDebugOverlay);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().save(this, prefs);

        //mDebugOverlay.onPause();

        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mDebugOverlay.setEnabled(mPreferences.mShowDebugOverlay);

        setMapTileSource();

        /*
        if (mPreferences.mShowDebugOverlay) {

            if (!mMapView.getOverlays().contains(mDebugOverlay)) {

                mMapView.getOverlays().add(mDebugOverlay);
            }
        }
        else {

            if (mMapView.getOverlays().contains(mDebugOverlay)) {

                mMapView.getOverlays().remove(mDebugOverlay);
            }
        }

        mDebugOverlay.onResume();
        */

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        mMapView.onResume();

        //mMapView.invalidate();

        //mMapController.setZoom(mMapView.getZoomLevelDouble());
    }

    void setMapTileSource() {

        OnlineTileSourceBase tileSource;

        switch (mPreferences.mTileSource) {

            case MyPreferences.TILE_SOURCE_MAPNIK:
                tileSource=TileSourceFactory.MAPNIK;
                break;

            case MyPreferences.TILE_SOURCE_HIKEBIKEMAP:
                tileSource=TileSourceFactory.HIKEBIKEMAP;
                break;

            case MyPreferences.TILE_SOURCE_PUBLIC_TRANSPORT:
                tileSource=TileSourceFactory.PUBLIC_TRANSPORT;
                break;

            case MyPreferences.TILE_SOURCE_USGS_MAP:
                tileSource=TileSourceFactory.USGS_SAT;
                break;

            case MyPreferences.TILE_SOURCE_USGS_TOPO:
                tileSource=TileSourceFactory.USGS_TOPO;
                break;

            default:
                mPreferences.mTileSource=MyPreferences.TILE_SOURCE_MAPNIK;
                tileSource=TileSourceFactory.MAPNIK;
                break;
        }

        mMapView.setTileSource(tileSource);
    }

    @Override
    public void onNewMarker(Marker newMarker) {

        mMapView.getOverlays().add(0, newMarker);
    }
}
