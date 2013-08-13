package com.weicheng.taipeiyoubikeoffline;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
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
        BoundingBoxE6 boundingBox = new BoundingBoxE6(25.10, 121.62, 24.98, 121.48);
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
        
        //TODO read data from csv files
        {
        	Scanner scanner = new Scanner(getResources().openRawResource(R.raw.youbike), "UTF-8");
        	scanner.useDelimiter(",");
        	
        	 while (scanner.hasNext()) {
        	     String chineseName = scanner.next();
        	     String chineseDesc = scanner.next();
        	     String name=scanner.next();
        	     double lat = scanner.nextDouble();
        	     double lng = scanner.nextDouble();
        	     
        	     Log.d(TAG,chineseName+":"+lat);
        	 }
        }
        
        ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("Taipei City Hall", "Nanjing MRT exit 1", gPt, this);
//        nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
        nodeMarker.setMarker(marker);
        items.add(nodeMarker);

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
