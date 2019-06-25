package osm.mapnotes;

import android.Manifest;
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
import android.support.v4.app.FragmentManager;
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
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener, Runnable,
        View.OnClickListener, MarkerDialogFragment.OnMarkerDialogListener,
        MyMarker.OnMyMarkerListener {

    // OsmDroid objects

    private MapView mMapView = null;
    private IMapController mMapController;

    MyLocationNewOverlay mLocationOverlay = null;

    CompassOverlay mCompassOverlay = null;

    CrossHairOverlay mCrossHairOverlay = null;
    DebugOverlay mDebugOverlay = null;

    ItemizedIconOverlay mItemOverlay=null;

    ArrayList<OverlayItem> mItems=new ArrayList<OverlayItem>();

    private long mPreviousCancelTime = 0;

    private static long CANCEL_TIMEOUT = 2000;

    private static int TIMEOUT_1_SEC = 1000;

    private LocationManager mLocationManager = null;

    LocationStatus mLocationStatus = new LocationStatus();

    Location mLocation = null;

    MapDatabase mDatabase=null;

    Drawable mMarkerIcon=null;

    private static int REQUEST_CODE_LOCATION = 0;
    private static int REQUEST_CODE_EXTERNAL_STORAGE = 1;

    // Controls

    ImageView mImageViewLocation = null;
    ImageView mImageViewPreferences = null;
    ImageView mImageViewBookmark= null;

    private boolean mTick = true;

    private Bitmap grayLocationIcon = null;
    private Bitmap redLocationIcon = null;
    private Bitmap greenLocationIcon = null;

    public static MyPreferences mPreferences = new MyPreferences();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getApplicationContext();

        mPreferences.loadPreferences(context);

        setContentView(R.layout.activity_main);

        mMapView = (MapView) findViewById(R.id.map);
        setMapTileSource();
        //mMapView.setBuiltInZoomControls(true);
        mMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mMapView.setMultiTouchControls(true);
        //mMapView.setTilesScaledToDpi(true);

        mImageViewLocation = (ImageView) findViewById(R.id.imageViewLocation);
        mImageViewLocation.setOnClickListener(this);

        mImageViewPreferences = (ImageView) findViewById(R.id.imageViewPreferences);
        mImageViewPreferences.setOnClickListener(this);

        mImageViewBookmark = (ImageView) findViewById(R.id.imageViewAddMarker);
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

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.osm_ic_center_map);

        mCrossHairOverlay = new CrossHairOverlay(bitmap);
        mMapView.getOverlays().add(mCrossHairOverlay);

        mDebugOverlay = new DebugOverlay(mLocationStatus);
        mDebugOverlay.setEnabled(mPreferences.mShowDebugOverlay);

        mItemOverlay = new ItemizedIconOverlay<OverlayItem>(mItems,
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

        mItemOverlay.setEnabled(true);

        mMapView.getOverlays().add(mItemOverlay);

        mMarkerIcon = getResources().getDrawable(android.R.drawable.btn_star_big_on);

        updateLocationStatus();

        mDatabase=new MapDatabase();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // We don't have writing permission for the external storage, so we have to request it...
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_EXTERNAL_STORAGE);

        } else {

            openMapDatabase();
        }
    }

    private void openMapDatabase() {

        String text;

        if (!mDatabase.openOrCreate(mPreferences.mDatabaseDir, mPreferences.mDatabaseName)) {

            text="Database Error: "+mDatabase.mLastErrorString;
        }
        else {

            List<MyMarker> markers=mDatabase.getMarkers(mMapView, this, mMarkerIcon);

            if (markers==null) {

                text="Error getting database markers...";
            }
            else {

                // Map database is ok...

                text=getString(R.string.loaded)+" "+markers.size()+" "+getString(R.string.markers)+"...";

                Iterator<MyMarker> iter=markers.iterator();

                while(iter.hasNext()) {

                    MyMarker marker=iter.next();

                    mMapView.getOverlays().add(marker);
                }
            }
        }

        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

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

        } else if (requestCode == REQUEST_CODE_EXTERNAL_STORAGE) {

            if ((grantResults.length==1) && grantResults[0]==PackageManager.PERMISSION_GRANTED) {

                openMapDatabase();
            }
        }
    }

    private void requestLocationUpdates() {

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationStatus.setProviderEnabled(
                mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));

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

        mPreferences.savePreferences(context);

        if (mDatabase!=null) {

            mDatabase.close();
            mDatabase=null;
        }
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

            mMapController.animateTo(centerPoint);

        } else if (view == mImageViewPreferences) {

            Intent intent = new Intent(this, PreferencesActivity.class);

            startActivity(intent);
        }
        else if (view == mImageViewBookmark) {

            GeoPoint center=(GeoPoint)mMapView.getMapCenter();

            MarkerDialogFragment fragment=new MarkerDialogFragment();

            fragment.mIsNewMarker=true;
            fragment.mLon=center.getLongitude();
            fragment.mLat=center.getLatitude();
            fragment.mName="";

            Date d=new Date(System.currentTimeMillis());
            fragment.mTimeStamp=d.toString();

            fragment.show(getSupportFragmentManager(), getString(R.string.dialog_marker));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().save(this, prefs);

        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mDebugOverlay.setEnabled(mPreferences.mShowDebugOverlay);

        setMapTileSource();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        mMapView.onResume();
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

        String userAgent=BuildConfig.APPLICATION_ID;

        Configuration.getInstance().setUserAgentValue(userAgent);

        //"github-gpesquero-mapnotes/1.0");

        mMapView.setTileSource(tileSource);
    }

    @Override
    public void onNewMarker(Bundle markerDataBundle) {

        MyMarker marker=new MyMarker(mMapView, this);

        GeoPoint center=new GeoPoint(
                markerDataBundle.getDouble(MarkerDialogFragment.KEY_LAT),
                markerDataBundle.getDouble(MarkerDialogFragment.KEY_LON));

        marker.setPosition(center);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setIcon(mMarkerIcon);
        marker.setDraggable(false);
        marker.setTitle(markerDataBundle.getString(MarkerDialogFragment.KEY_NAME));
        marker.setId(markerDataBundle.getString(MarkerDialogFragment.KEY_TIME_STAMP));

        if (!mDatabase.addMarker(marker)) {

            Toast.makeText(this, "MapDatabase.addMarker() error: "+
                    mDatabase.mLastErrorString, Toast.LENGTH_LONG).show();
        }
        else {

            mMapView.getOverlays().add(0, marker);

            Toast.makeText(this, getString(R.string.marker_added_),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDeleteMarker(String markerTimeStamp) {

        // First, search marker

        boolean markerFound=false;

        List<Overlay> overlays=mMapView.getOverlays();

        Iterator<Overlay> iterator=overlays.iterator();

        while(iterator.hasNext()) {

            Overlay overlay=iterator.next();

            if (overlay instanceof MyMarker) {

                MyMarker marker=(MyMarker) overlay;

                if (marker.getId().compareTo(markerTimeStamp)==0) {

                    markerFound=true;

                    if (overlays.contains(marker)) {

                        marker.closeInfoWindow();

                        overlays.remove(marker);

                        if (!mDatabase.deleteMarker(marker)) {

                            Toast.makeText(this, "MapDatabase.deleteMarker() error: " +
                                    mDatabase.mLastErrorString, Toast.LENGTH_LONG).show();
                        } else {

                            Toast.makeText(this, getString(R.string.marker_deleted_),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    else {

                        Toast.makeText(this,"Cannot delete marker !!",
                                Toast.LENGTH_LONG).show();
                    }

                    break;
                }
            }
        }

        if (!markerFound) {

            Toast.makeText(this, "MapDatabase.deleteMarker() error. Marker not found",
                    Toast.LENGTH_LONG).show();


        }
    }

    @Override
    public void onUpdateMarker(Bundle markerDataBundle) {

        String timeStamp=markerDataBundle.getString(MarkerDialogFragment.KEY_TIME_STAMP);

        // First, search marker

        boolean markerFound=false;

        List<Overlay> overlays=mMapView.getOverlays();

        Iterator<Overlay> iterator=overlays.iterator();

        while(iterator.hasNext()) {

            Overlay overlay=iterator.next();

            if (overlay instanceof MyMarker) {

                MyMarker marker=(MyMarker) overlay;

                if (marker.getId().compareTo(timeStamp)==0) {

                    markerFound=true;

                    marker.setTitle(markerDataBundle.getString(MarkerDialogFragment.KEY_NAME));

                    marker.closeInfoWindow();

                    if (!mDatabase.updateMarker(marker)) {

                        Toast.makeText(this, "MapDatabase.updateMarker() error: " +
                                mDatabase.mLastErrorString, Toast.LENGTH_LONG).show();
                    }
                    else {

                        Toast.makeText(this, getString(R.string.marker_updated_),
                                Toast.LENGTH_LONG).show();
                    }

                    break;
                }
            }
        }

        if (!markerFound) {

            Toast.makeText(this, "MapDatabase.updateMarker() error. Marker not found",
                    Toast.LENGTH_LONG).show();


        }
    }

    @Override
    public void onLongPress(MyMarker marker) {

        List<Overlay> overlays=mMapView.getOverlays();

        if (overlays.contains(marker)) {

            MarkerDialogFragment fragment=new MarkerDialogFragment();

            fragment.mIsNewMarker=false;
            fragment.mLon=marker.getPosition().getLongitude();
            fragment.mLat=marker.getPosition().getLatitude();
            fragment.mName=marker.getTitle();
            fragment.mTimeStamp=marker.getId();

            fragment.show(getSupportFragmentManager(), getString(R.string.dialog_marker));
        }
    }

    public void clearTileCache() {

        mMapView.getTileProvider().clearTileCache();
    }
}
