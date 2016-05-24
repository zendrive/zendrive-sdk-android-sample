package com.zendrive.zendrivesdkdemo;

import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.zendrive.sdk.DriveInfo;
import com.zendrive.sdk.LocationPoint;
import com.zendrive.sdk.ZendriveEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by girishkadli on 5/24/16.
 */
public class TripMapFragment extends SupportMapFragment {

    public void drawTripOnMap(final DriveInfo driveInfo) {
        // draw trip details on map.
        final List<LatLng> points = new ArrayList<LatLng>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int i = 0;
        for (LocationPoint pt : driveInfo.waypoints) {
            LatLng latLng = new LatLng(pt.latitude, pt.longitude);
            // Adding the taped point to the ArrayList
            points.add(latLng);
            builder.include(latLng);
        }
        final LatLngBounds bounds = builder.build();
        this.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gMap) {
                PolylineOptions polylineOptions = new PolylineOptions();
                // Setting the color of the polyline
                polylineOptions.color(Color.RED);
                // Setting the width of the polyline
                polylineOptions.width(10);
                polylineOptions.addAll(points);
                gMap.addPolyline(polylineOptions);
                int padding = 100; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                gMap.animateCamera(cu);
                // mark trip start and trip end.
                if (!points.isEmpty()) {
                    LatLng tripStartLocation = points.get(0);
                    LatLng tripEndLocation = points.get(points.size() - 1);
                    markPoint(gMap, tripStartLocation, BitmapDescriptorFactory.HUE_GREEN, "Trip Start");
                    markPoint(gMap, tripEndLocation, BitmapDescriptorFactory.HUE_GREEN, "Trip End");
                }
                // mark events.
                markEvents(gMap, driveInfo.events);
            }
        });
    }

    private void markEvents(GoogleMap gMap, List<ZendriveEvent> events) {
        if (null == gMap) {
            return;
        }
        for (ZendriveEvent e : events) {
            LatLng eventLocation = new LatLng(e.startLocation.latitude, e.startLocation.longitude);
            String eventType = e.eventType.name();
            markPoint(gMap, eventLocation, BitmapDescriptorFactory.HUE_RED, eventType);
        }
    }

    // marks point google map.
    private void markPoint(GoogleMap gMap, LatLng pointLocation, float color, String infoMsg) {
        MarkerOptions markerOptions = new MarkerOptions().position(pointLocation).
                icon(BitmapDescriptorFactory.defaultMarker(color)).alpha(0.7f).title(infoMsg);
        gMap.addMarker(markerOptions);
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });
    }
}
