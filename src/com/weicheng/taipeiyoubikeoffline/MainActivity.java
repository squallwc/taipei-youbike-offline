package com.weicheng.taipeiyoubikeoffline;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {
	private static final String TAG="YoubikeOffline MainActivity";
	
    private MapView         mapView;
    private MapController   mapController;
    private ItemizedOverlayWithBubble<ExtendedOverlayItem> currentLocationOverlayBubble;
    
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
        //taipei
        GeoPoint gPt =  new GeoPoint((int) (25.0412 * 1E6), (int) (121.5407 * 1E6));
        mapController.setCenter(gPt);
        
        Drawable marker = getResources().getDrawable(R.drawable.marker_node);
        final ArrayList<ExtendedOverlayItem> items = new ArrayList<ExtendedOverlayItem>();
        
        //read bicycle station location data from csv files
        Scanner scanner=null;
        try{
        	scanner = new Scanner(getResources().openRawResource(R.raw.youbike), "UTF-8");
        	
        	//use hashset to remove duplicate records
//        	HashSet<Station> stations = new HashSet<Station>();
        	
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

//        			Station sta = new Station(chineseName, chineseDesc, name, lat, lng);

        			ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem(chineseName, chineseDesc, new GeoPoint(lat, lng), this);
//        			nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
