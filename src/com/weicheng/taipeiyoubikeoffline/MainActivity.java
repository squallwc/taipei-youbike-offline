package com.weicheng.taipeiyoubikeoffline;

import java.util.ArrayList;
import java.util.Scanner;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG="YoubikeOffline MainActivity";
	
    private MapView         mapView;
    private MapController   mapController;
    private ItemizedOverlayWithBubble<ExtendedOverlayItem> currentLocationOverlayBubble;
    private GeoPoint currentLocation;
    private OverlayItem myCurrentLocationOverlayItem;
    private MyLocationListener locationListener;
    private LocationManager  locationManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        
        //map zoom limit
        mapView.setMinZoomLevel(12);
        mapView.setMaxZoomLevel(16);
        
        //map range limit
        BoundingBoxE6 boundingBox = new BoundingBoxE6(25.10, 121.645, 24.98, 121.48);
        mapView.setScrollableAreaLimit(boundingBox);
        
        mapView.setUseDataConnection(false);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(13);
        
        //get user location or show taipei city hall if user not in taipei
        locationListener = new MyLocationListener();        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        final ArrayList<ExtendedOverlayItem> items = new ArrayList<ExtendedOverlayItem>();
        
//      TODO make sure user is within border of taipei map, else show general map
        if( location != null) {
        	Log.d(TAG, "initial latitude:"+location.getLatitude());
        	Log.d(TAG, "initial longitude:"+location.getLongitude());
        	
        	if(location.getLongitude()>=121.48 &&location.getLongitude()<=121.645
        			&& location.getLatitude()>=24.98 && location.getLatitude()<=25.10)
        	{
        		currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
//        		currentLocation =  new GeoPoint((int) (25.0412 * 1E6), (int) (121.5407 * 1E6));
        		//TODO attribute icon marker from https://www.iconfinder.com/iconsets/Map-Markers-Icons-Demo-PNG
        		//TODO credit bicycle icon http://www.softicons.com/
        		//first marker in items is the user location
        		Drawable pinkMarker = getResources().getDrawable(R.drawable.pink_marker);
        		ExtendedOverlayItem currentLocationMarker = new ExtendedOverlayItem("你在這裡", "", currentLocation, this);
        		currentLocationMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
        		currentLocationMarker.setMarker(pinkMarker);
        		items.add(currentLocationMarker);
        		
        		//zoom in if get user location
        		mapController.setZoom(15);
        	}
        	else
        	{
        		//taipei city hall as center of the map if user is not within range of the taipei map
        		currentLocation =  new GeoPoint((int) (25.0412 * 1E6), (int) (121.5407 * 1E6));
        		
        		Toast.makeText(getApplicationContext(), "抱歉,你不在地圖的範圍内", Toast.LENGTH_LONG).show();
        	}
        }
        else
        {
        	//taipei city hall as center of the map if fail to get user location
        	currentLocation =  new GeoPoint((int) (25.0412 * 1E6), (int) (121.5407 * 1E6));
        }
        
        mapController.setCenter(currentLocation);
        
        //read bicycle station location data from csv files
        Scanner scanner=null;
        try{
        	Drawable marker = getResources().getDrawable(R.drawable.marker_node);
        	scanner = new Scanner(getResources().openRawResource(R.raw.youbike), "UTF-8");
        	
        	while (scanner.hasNextLine()) {
        		String nextLine = scanner.nextLine();
        		Scanner scannerNextLine=null;
        		try
        		{
        			scannerNextLine = new Scanner(nextLine);
        			scannerNextLine.useDelimiter(",");

        			String chineseName = scannerNextLine.next();
        			String chineseDesc = scannerNextLine.next();
        			String name=scannerNextLine.next();
        			int lat = scannerNextLine.nextInt();
        			int lng = scannerNextLine.nextInt();

        			ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem(chineseName, chineseDesc, new GeoPoint(lat, lng), this);
        			nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
        			nodeMarker.setMarker(marker);
        			items.add(nodeMarker);
        		}finally{
        			if(scannerNextLine!=null)
        			{
        				scannerNextLine.close();
        			}
        		}
        	}
        }finally
        {
        	if(scanner!=null)
			{
				scanner.close();
			}
        }
        
        currentLocationOverlayBubble = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(getApplicationContext(), items, mapView);
        mapView.getOverlays().add(currentLocationOverlayBubble);
        
        mapView.invalidate();
    }

    public class MyLocationListener implements LocationListener {
    	public void onLocationChanged(Location location) {
            currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            mapController.setCenter(currentLocation);
            mapView.invalidate();
        }

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	protected void onPause() {
		super.onPause();
		
		if(locationManager!=null && locationListener!=null)
		{
			locationManager.removeUpdates(locationListener);
		}
	}
}
