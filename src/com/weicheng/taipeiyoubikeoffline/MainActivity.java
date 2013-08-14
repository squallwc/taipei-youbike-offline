package com.weicheng.taipeiyoubikeoffline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import org.osmdroid.ResourceProxy.string;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG="YoubikeOffline MainActivity";
	
    private MapView         mapView;
    private MapController   mapController;
    private ItemizedOverlayWithBubble<ExtendedOverlayItem> currentLocationOverlayBubble;
    private GeoPoint currentLocation;
    private MyLocationListener locationListener;
    private LocationManager  locationManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //load map data
        try{
        	//check external storage
        	boolean isExternalStorageAvailable = false;
        	boolean isExternalStorageWriteable = false;
        	String state = Environment.getExternalStorageState();

        	if (Environment.MEDIA_MOUNTED.equals(state)) {
        		// We can read and write the media
        		isExternalStorageAvailable = isExternalStorageWriteable = true;
        	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
        		// We can only read the media
        		isExternalStorageAvailable = true;
        		isExternalStorageWriteable = false;
        	} else {
        		// Something else is wrong. It may be one of many other states, but all we need
        		//  to know is we can neither read nor write
        		isExternalStorageAvailable = isExternalStorageWriteable = false;
        	}
        	
        	if(!isExternalStorageAvailable && !isExternalStorageWriteable)
        	{
        		showTransferErrorDialog();
        	}
        	else
        	{
        		Log.d(TAG, "getExternalStorageDirectory:"+Environment.getExternalStorageDirectory());
        		
        		//create the directory if not exist
        		File osmDirectory = new File(Environment.getExternalStorageDirectory()+"/osmdroid/");
        		Log.d(TAG, "osmDirectory.exists():"+osmDirectory.exists());
        		if(!osmDirectory.exists())
        		{
        			osmDirectory.mkdir();
        			Log.d(TAG, "osmDirectory.exists2():"+osmDirectory.exists());
        			Log.d(TAG, "osmDirectory.isDirectory():"+osmDirectory.isDirectory());
        			//if fail to create directory
        			if(!osmDirectory.exists())
        			{
        				throw new IOException("Error create directory "+Environment.getExternalStorageDirectory()+"/osmdroid/");
        			}
        		}
        		
        		File zip1 = new File(Environment.getExternalStorageDirectory()+"/osmdroid/taipei_1.zip");
        		File zip2 = new File(Environment.getExternalStorageDirectory()+"/osmdroid/taipei_2.zip");
        		//check whether the map data exist
        		if(!zip1.exists() || !zip2.exists())
        		{
        			Log.d(TAG, "map data not found");
        			
        			copyFile(getResources().openRawResource(R.raw.taipei_1), zip1);
        			copyFile(getResources().openRawResource(R.raw.taipei_2), zip2);

        			//restart activity to load the map tiles
        			Log.d(TAG, "restart activity");
        			Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
        			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        			startActivity(i);
        		}
        	}
        }catch (NotFoundException e) {
			Log.e(TAG, "Error loading map file", e);
			showTransferErrorDialog();
		}catch (IOException e) {
			Log.e(TAG, "Error loading map file", e);
			showTransferErrorDialog();
		}
    	
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
        
//      make sure user is within border of taipei map, else show general map
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
    
    class CustomTileSource extends BitmapTileSourceBase {

        public CustomTileSource(String aName, string aResourceId,
                int aZoomMinLevel, int aZoomMaxLevel, int aTileSizePixels,
                String aImageFilenameEnding) {
            super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
                    aImageFilenameEnding);
        }

    }
    
    private void showTransferErrorDialog()
    {
    	new AlertDialog.Builder(this)
	    .setMessage("無法將地圖資料存到手機上")
	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	//do nothing
	        }
	     })
	     .show();
    }

    private static void copyFile(InputStream in,File mapfile) throws IOException
    {
    	OutputStream out = null;
    	try
    	{
    		out = new FileOutputStream(mapfile);

    		// Transfer bytes from in to out
    		byte[] buf = new byte[1024];
    		int len;
    		while ((len = in.read(buf)) > 0) {
    			out.write(buf, 0, len);
    		}
    	}
    	finally
    	{
    		if(in!=null)
    		{
    			in.close();
    		}
    		if(out!=null)
    		{
    			out.flush();
    			out.close();
    		}
    	}
    }
    
    private class MyLocationListener implements LocationListener {
    	public void onLocationChanged(Location location) {
    		
    		if(location.getLongitude()>=121.48 &&location.getLongitude()<=121.645
        			&& location.getLatitude()>=24.98 && location.getLatitude()<=25.10)
    		{
    			currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
    			mapController.setCenter(currentLocation);
    			mapView.invalidate();
    		}
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
		
//		if(currentLocationOverlayBubble!=null)
//		{
//			mapView.getOverlays().remove(currentLocationOverlayBubble);	
//			currentLocationOverlayBubble.removeAllItems();
//			currentLocationOverlayBubble = null;
//		}
	}
	
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//		
//		if(locationManager!=null && locationListener!=null)
//		{
//			locationManager.removeUpdates(locationListener);
//		}
		
//		if(currentLocationOverlayBubble!=null)
//		{
//			mapView.getOverlays().remove(currentLocationOverlayBubble);	
//			currentLocationOverlayBubble.removeAllItems();
//			currentLocationOverlayBubble = null;
//		}
//	}
	
//	@Override
//	protected void onStart() {
//	    super.onStart();
//	    locationListener = new MyLocationListener();
//	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//	}
}
