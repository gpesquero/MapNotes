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
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener, Runnable,
        View.OnClickListener, MarkerDialogFragment.OnMarkerDialogListener,
        MyMarker.OnMyMarkerListener, GoToDialogFragment.OnGoToDialogListener,
        KeepRightErrorsManager.KeepRightErrorsManagerListener {

    // OsmDroid objects

    private MapView mMapView = null;
    private IMapController mMapController;

    MyLocationNewOverlay mLocationOverlay = null;

    CompassOverlay mCompassOverlay = null;

    CrossHairOverlay mCrossHairOverlay = null;
    DebugOverlay mDebugOverlay = null;

    ArrayList<OverlayItem> mMarkerItems=new ArrayList<OverlayItem>();

    ArrayList<OverlayItem> mErrorItems=new ArrayList<OverlayItem>();

    private long mPreviousCancelTime = 0;

    private final static long CANCEL_TIMEOUT = 2000;

    private final static int TIMEOUT_1_SEC = 1000;

    private LocationManager mLocationManager = null;

    LocationStatus mLocationStatus = new LocationStatus();

    Location mLocation = null;

    KeepRightErrorsManager mErrorsManager=null;

    Drawable mMarkerIcon=null;

    final private static int REQUEST_CODE_LOCATION = 0;
    final private static int REQUEST_CODE_EXTERNAL_STORAGE = 1;
    final private static int REQUEST_CODE_PREFERENCES = 2;

    int mZoomLevel=0;

    // Controls

    ImageView mImageViewLocation = null;
    ImageView mImageViewPreferences = null;
    ImageView mImageViewBookmark= null;
    ImageView mImageViewGoTo = null;
    ImageView mImageViewTest = null;

    TextView mTextViewLog1 = null;
    TextView mTextViewLog2 = null;
    TextView mTextViewLog3 = null;

    private boolean mTick = true;

    private Bitmap grayLocationIcon = null;
    private Bitmap redLocationIcon = null;
    private Bitmap greenLocationIcon = null;

    public static MyPreferences mPreferences = new MyPreferences();

    final private static double DEFAULT_ANIMATE_TO_ZOOM=16.0;

    final private static float BOUNDING_BOX_SCALE=(float)1.5;

    final private static int MIN_ERRORS_ZOOM_LEVEL=16;

    // Map movement delay
    final int MAP_LISTENER_DELAY = 500;

    private static boolean mFirstTime=true;

    GpxManager mGpxManager=new GpxManager();

    private MarkerDatabase mMarkerDatabase;

    private MapNotesApplication mApp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApp = (MapNotesApplication) getApplication();

        mMarkerDatabase = mApp.getMarkerDatabase();

        Context context = getApplicationContext();

        mPreferences.loadPreferences(context);

        setContentView(R.layout.activity_main);

        mMapView = (MapView) findViewById(R.id.map);
        setMapTileSource();
        mMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mMapView.setMultiTouchControls(true);

        // Add map listener with a delay between zoom/scroll updates
        mMapView.addMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {

                //Toast.makeText(getBaseContext(), "onScroll", Toast.LENGTH_SHORT).show();

                onScrollZoomChange();

                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {

                //Toast.makeText(getBaseContext(), "onZoom", Toast.LENGTH_SHORT).show();

                onScrollZoomChange();

                return false;
            }
        }, MAP_LISTENER_DELAY));

        mImageViewLocation = (ImageView) findViewById(R.id.imageViewLocation);
        mImageViewLocation.setOnClickListener(this);

        mImageViewPreferences = (ImageView) findViewById(R.id.imageViewPreferences);
        mImageViewPreferences.setOnClickListener(this);

        mImageViewBookmark = (ImageView) findViewById(R.id.imageViewAddMarker);
        mImageViewBookmark.setOnClickListener(this);

        mImageViewGoTo = (ImageView) findViewById(R.id.imageViewGoTo);
        mImageViewGoTo.setOnClickListener(this);

        mImageViewTest = (ImageView) findViewById(R.id.imageViewTest);
        mImageViewTest.setOnClickListener(this);

        mTextViewLog1 = (TextView) findViewById(R.id.textViewLog1);
        mTextViewLog2 = (TextView) findViewById(R.id.textViewLog2);
        mTextViewLog3 = (TextView) findViewById(R.id.textViewLog3);

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

        /*
        mMarkersOverlay = new ItemizedIconOverlay<OverlayItem>(mMarkerItems,
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

        mMarkersOverlay.setEnabled(true);

        mMapView.getOverlays().add(mMarkersOverlay);
        */

        mMarkerIcon = getResources().getDrawable(android.R.drawable.btn_star_big_on);

        /*
        mErrorsOverlay = new ItemizedIconOverlay<OverlayItem>(mErrorItems,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {

                        //Marker marker=item.getMarker(OverlayItem .ITEM_STATE_PRESSED_MASK);

                        //do something
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, this);

        mErrorsOverlay.setEnabled(true);

        mMapView.getOverlays().add(mErrorsOverlay);
        */

        updateLocationStatus();

        if (mPreferences.mShowKeepRightErrors) {
            mErrorsManager=new KeepRightErrorsManager(this, mMapView);
        }
        else {

            mErrorsManager=null;
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // We don't have writing permission for the external storage, so we have to request it...
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_EXTERNAL_STORAGE);

        }
        else {

            loadMarkers();

            openGpxFiles();

            openErrorDatabase();
        }

        if (mPreferences.mShowDebugOverlay) {

            mTextViewLog1.setVisibility(View.VISIBLE);
            mTextViewLog2.setVisibility(View.VISIBLE);
            mTextViewLog3.setVisibility(View.VISIBLE);
        }
        else {

            mTextViewLog1.setVisibility(View.INVISIBLE);
            mTextViewLog2.setVisibility(View.INVISIBLE);
            mTextViewLog3.setVisibility(View.INVISIBLE);
        }

        if (mFirstTime) {

            mTextViewLog1.setText("First Time=true");

            mFirstTime=false;
        }
        else {

            mTextViewLog1.setText("First Time=false");
        }

        String text;

        text="Markers database: "+mMarkerDatabase.mLastErrorString+"\n";

        text+="GpxManager: "+mGpxManager.mLastErrorString;

        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void loadMarkers() {

        mMarkerDatabase = mApp.getMarkerDatabase();

        if (!mMarkerDatabase.isOpen()) {

            // Marker database is closed. Let's try to open it...
            if (!mMarkerDatabase.openOrCreate(mPreferences.mMarkerDatabasePath)) {

                String text = "Error opening marker database";

                Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            }
        }

        List<MyMarker> markers=mMarkerDatabase.getMarkers(mMapView, this, mMarkerIcon);

        if (markers != null) {

            // Map database is ok...

            mMarkerDatabase.mLastErrorString=getString(R.string.loaded)+" "+markers.size()+" "+getString(R.string.markers)+"...";

            Iterator<MyMarker> iter=markers.iterator();

            while(iter.hasNext()) {

                MyMarker marker=iter.next();

                mMapView.getOverlays().add(marker);
            }
        }

        //Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void openGpxFiles() {

        mGpxManager.readFiles(mPreferences);

        mMapView.getOverlayManager().addAll(mGpxManager.getPolylines());
    }

    private void openErrorDatabase() {

        if (mErrorsManager==null) {
            return;
        }

        String msgString;

        mErrorsManager.openErrorDatabase(this, mPreferences);

        msgString=mErrorsManager.mLastErrorString;

        if (mPreferences.mShowDebugOverlay) {

            mTextViewLog1.setText(msgString);
        }
        else {

            mTextViewLog1.setVisibility(View.INVISIBLE);
            mTextViewLog2.setVisibility(View.INVISIBLE);
            mTextViewLog3.setVisibility(View.INVISIBLE);
        }
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
        else if (requestCode == REQUEST_CODE_EXTERNAL_STORAGE) {

            if ((grantResults.length==1) && grantResults[0]==PackageManager.PERMISSION_GRANTED) {

                //mApp.openMarkerDatabase();

                loadMarkers();

                openErrorDatabase();
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

        if (mErrorsManager!=null) {

            mErrorsManager.cancel();
        }
    }

    @Override
    public void onBackPressed() {

        long currentTime = System.currentTimeMillis();

        if ((currentTime - mPreviousCancelTime) > CANCEL_TIMEOUT) {

            Toast.makeText(this, R.string.press_again_back, Toast.LENGTH_SHORT).show();

            mPreviousCancelTime = currentTime;
        }
        else {

            // Pressed back button twice.
            // Close activity

            mApp.destroy();

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

        /*
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

        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        */

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

            Intent intent = new Intent(this, MainPreferencesActivity.class);

            startActivityForResult(intent, REQUEST_CODE_PREFERENCES);
        }
        else if (view == mImageViewBookmark) {

            GeoPoint center=(GeoPoint)mMapView.getMapCenter();

            MarkerDialogFragment fragment=new MarkerDialogFragment();

            fragment.mIsNewMarker=true;
            fragment.mLon=center.getLongitude();
            fragment.mLat=center.getLatitude();
            fragment.mName="";

            Date d=new Date(System.currentTimeMillis());
            //fragment.mTimeStamp=d.toString();

            DateFormat df=DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.ENGLISH);
            fragment.mTimeStamp=df.format(d);

            fragment.show(getSupportFragmentManager(), getString(R.string.dialog_marker));
        }
        else if (view == mImageViewGoTo) {

            GoToDialogFragment fragment=new GoToDialogFragment();

            fragment.show(getSupportFragmentManager(), getString(R.string.dialog_go_to));
        }
        else if (view == mImageViewTest) {

            testErrorsDatabase();
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

            case MyPreferences.TILE_SOURCE_OPEN_TOPO:
                tileSource=TileSourceFactory.OpenTopo;
                break;

            default:
                mPreferences.mTileSource=MyPreferences.TILE_SOURCE_MAPNIK;
                tileSource=TileSourceFactory.MAPNIK;
                break;
        }

        String userAgent=BuildConfig.APPLICATION_ID;

        Configuration.getInstance().setUserAgentValue(userAgent);

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

        if (!mMarkerDatabase.addMarker(marker)) {

            Toast.makeText(this, "MapDatabase.addMarker() error: "+
                    mMarkerDatabase.mLastErrorString, Toast.LENGTH_LONG).show();
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

                        if (!mMarkerDatabase.deleteMarker(marker)) {

                            Toast.makeText(this, "MapDatabase.deleteMarker() error_circle: " +
                                    mMarkerDatabase.mLastErrorString, Toast.LENGTH_LONG).show();
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

            Toast.makeText(this, "MapDatabase.deleteMarker() error_circle. Marker not found",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUpdateMarker(Bundle markerDataBundle) {

        String timeStamp=markerDataBundle.getString(MarkerDialogFragment.KEY_TIME_STAMP);

        // First, search marker

        boolean markerFound = false;

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

                    if (!mMarkerDatabase.updateMarker(marker)) {

                        Toast.makeText(this, "MapDatabase.updateMarker() error_circle: " +
                                mMarkerDatabase.mLastErrorString, Toast.LENGTH_LONG).show();
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

            Toast.makeText(this, "MapDatabase.updateMarker() error_circle. Marker not found",
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode==REQUEST_CODE_PREFERENCES) {

            if (resultCode== TilePreferencesActivity.RESULT_CLEAR_TILE_CACHE) {

                clearTileCache();
            }
        }
    }

    public void clearTileCache() {

        mMapView.getTileProvider().clearTileCache();

        Toast.makeText(this, R.string.tile_cache_cleared, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGoToOldestMarker() {

        goToMarker(true);
    }

    @Override
    public void onGoToNewestMarker() {

        goToMarker(false);
    }

    private void goToMarker(boolean oldest) {

        List<Overlay> overlays=mMapView.getOverlays();

        MyMarker selMarker=null;
        Date selDate=null;

        DateFormat df=DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG,
                Locale.ENGLISH);

        Iterator<Overlay> iterator=overlays.iterator();

        while(iterator.hasNext()) {

            Overlay overlay=iterator.next();

            if (overlay instanceof MyMarker) {

                MyMarker marker=(MyMarker) overlay;

                if (selMarker==null) {

                    selMarker=marker;

                    try {
                        selDate=df.parse(selMarker.getId());

                    } catch (ParseException e) {

                        selDate=null;

                        Toast.makeText(this, "DateTime parse error_circle", Toast.LENGTH_LONG).show();

                        mMapController.animateTo(selMarker.getPosition());

                        return;
                    }
                }
                else {

                    String id=marker.getId();

                    Date date=null;

                    try {
                        date=df.parse(id);

                    } catch (ParseException e) {

                        date=null;

                        Toast.makeText(this, "DateTime parse error_circle", Toast.LENGTH_LONG).show();

                        mMapController.animateTo(marker.getPosition());

                        return;
                    }

                    if ((selDate==null) || (date==null)) {

                        Toast.makeText(this, "Dates are null", Toast.LENGTH_LONG).show();

                        return;
                    }

                    if (oldest) {

                        if (date.before(selDate)) {

                            selMarker=marker;
                            selDate=date;
                        }
                    }
                    else {

                        if (date.after(selDate)) {

                            selMarker = marker;
                            selDate = date;
                        }
                    }
                }
            }
        }

        if (selMarker==null) {

            Toast.makeText(this, R.string.there_are_no_markers, Toast.LENGTH_LONG).show();

            return;
        }

        Toast.makeText(this, getString(R.string.go_to_marker_)+"\n"+
                        "<"+selMarker.getTitle()+">\n"+
                        "["+DateFormat.getDateInstance().format(selDate)+"]",
                        Toast.LENGTH_LONG).show();

        mMapController.animateTo(selMarker.getPosition(), DEFAULT_ANIMATE_TO_ZOOM, null);
    }

    private void onScrollZoomChange() {

        mZoomLevel=(int)Math.round(mMapView.getZoomLevelDouble());

        //mTextViewLog1.setText("ZoomLevel: "+mZoomLevel);

        if (mErrorsManager==null) {

            return;
        }

        if (mZoomLevel<MIN_ERRORS_ZOOM_LEVEL) {

            deleteAllKeepRightErrors();

            //mErrorsOverlay.setEnabled(false);

            reportCacheData("");
        }
        else {

            //mErrorsOverlay.setEnabled(true);

            deleteKeepRightErrorsOutOfBounds();

            BoundingBox mapBounds=mMapView.getBoundingBox();

            mErrorsManager.getErrors(mapBounds);
        }

        int count=mMapView.getOverlays().size();

        mTextViewLog3.setText("Number of overlays: "+count);
    }

    /*
    @Override
    public boolean onScroll(ScrollEvent event) {

        Toast.makeText(this, "onScroll", Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {

        Toast.makeText(this, "onZoom", Toast.LENGTH_SHORT).show();

        return true;
    }
    */

    private void testErrorsDatabase() {

        //mErrorsDataBaseManager.getNumberOfErrors();
    }

    /*
    @Override
    public void onGetNumberOfErrors(Long numberOfErrors) {

        double elapsedTime=(double)mErrorsDataBaseManager.mElapsedTime/1000.0;

        String text=String.format("mErrorsDataBase has %d rows (%.1f s)", numberOfErrors,
                elapsedTime);

        if (mPreferences.mShowDebugOverlay) {

            mTextViewLog1.setText(text);
        }
        else {

            mTextViewLog1.setVisibility(View.INVISIBLE);
        }
    }
    */

    @Override
    public void reportCacheData(String data) {

        String zoomString="Z"+mZoomLevel+" - ";

        mTextViewLog2.setText(zoomString+" "+data);
    }

    @Override
    public void reportDatabaseMsg(String msg) {

        mTextViewLog1.setText(msg);
    }

    @Override
    public void onDataSet(KeepRightErrorSet dataSet) {

        if (mZoomLevel<MIN_ERRORS_ZOOM_LEVEL) {
            return;
        }

        BoundingBox bbox=mMapView.getBoundingBox().increaseByScale(BOUNDING_BOX_SCALE);

        Iterator<KeepRightError> iter=dataSet.getData().iterator();

        while(iter.hasNext()) {

            KeepRightError error=iter.next();

            if (!bbox.contains(error.getPosition()))
                continue;

            if (errorAlreadyExists(error.getId()))
                continue;

            /*
            error_circle.setMarker(getResources().getDrawable(R.drawable.error_circle));

            mErrorsOverlay.addItem(error_circle);
            */

            error.selectIcon(getResources());

            //error_circle.setIcon(getResources().getDrawable(R.drawable.error_circle));

            mMapView.getOverlays().add(error);
        }

        //int count=mErrorsOverlay.size();

        int count=mMapView.getOverlays().size();

        mTextViewLog3.setText("Number of overlays: "+count);
    }

    private boolean errorAlreadyExists(String error_id) {

        List<Overlay> overlays=mMapView.getOverlays();

        Iterator<Overlay> iter=overlays.iterator();

        while(iter.hasNext()) {

            Overlay overlay=iter.next();

            if (overlay instanceof KeepRightError) {

                KeepRightError error=(KeepRightError)overlay;

                if (error.getId().compareTo(error_id)==0) {

                    return true;
                }
            }
        }

        return false;
    }

    private void deleteAllKeepRightErrors() {

        List<Overlay> overlays=mMapView.getOverlays();

        Iterator<Overlay> iter=overlays.iterator();

        while(iter.hasNext()) {

            Overlay overlay=iter.next();

            if (overlay instanceof KeepRightError) {

                overlays.remove(overlay);
            }
        }
    }

    private void deleteKeepRightErrorsOutOfBounds() {

        BoundingBox bbox=mMapView.getBoundingBox().increaseByScale(BOUNDING_BOX_SCALE);

        List<Overlay> overlays=mMapView.getOverlays();

        Iterator<Overlay> iter=overlays.iterator();

        while(iter.hasNext()) {

            Overlay overlay=iter.next();

            if (overlay instanceof KeepRightError) {

                KeepRightError error=(KeepRightError)overlay;

                if (!bbox.contains(error.getPosition())) {
                    overlays.remove(overlay);
                }
            }
        }
    }
}
