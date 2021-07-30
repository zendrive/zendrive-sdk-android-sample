package com.zendrive.zendrivesdkdemo;

import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.zendrive.sdk.DriveInfo;
import com.zendrive.sdk.LocationPoint;
import com.zendrive.sdk.LocationPointWithTimestamp;
import com.zendrive.sdk.ZendriveEvent;
import com.zendrive.sdk.ZendriveExtrapolationDetails;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        try {
            ApplicationInfo applicationInfo = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo.metaData.getString("com.google.android.maps.v2.API_KEY", "").equals("")) {
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.invalid_maps_key))
                        .setMessage(getResources().getString(R.string.default_maps_key))
                        .create().show();
                return;
            }
        } catch (PackageManager.NameNotFoundException e) {
            // can't be
        }
        supportmapfragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        drawTripOnMap(((MyApplication) getApplication()).driveInfo);
    }

    @Override
    public void finish() {
        ((MyApplication) getApplication()).driveInfo = null;
        super.finish();
    }

    public void drawTripOnMap(final DriveInfo driveInfo) {
        // draw trip details on map.
        final List<LatLng> points = new ArrayList<>();
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LocationPointWithTimestamp pt : driveInfo.waypoints) {
            LatLng latLng = new LatLng(pt.location.latitude, pt.location.longitude);
            // Adding the taped point to the ArrayList
            points.add(latLng);
            builder.include(latLng);
        }

        final List<LatLng> extrapolatedWaypoints = getExtrapolatedLatLngPoints(driveInfo);
        for (LatLng point: extrapolatedWaypoints) {
            builder.include(point);
        }

        final LatLngBounds bounds = builder.build();
        supportmapfragment.getMapAsync(gMap -> {
            PolylineOptions polylineOptions = new PolylineOptions();
            // Setting the color of the polyline
            polylineOptions.color(Color.RED);
            // Setting the width of the polyline
            polylineOptions.width(10);
            polylineOptions.addAll(points);
            gMap.addPolyline(polylineOptions);

            if (!extrapolatedWaypoints.isEmpty()) {
                PolylineOptions extrapolatedPolylineOptions = new PolylineOptions();
                extrapolatedPolylineOptions.color(Color.GRAY);
                extrapolatedPolylineOptions.width(5);
                extrapolatedPolylineOptions.addAll(extrapolatedWaypoints);
                gMap.addPolyline(extrapolatedPolylineOptions);
            }

            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (0.13 * width); // offset from edges of the map 13% of screen
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            gMap.animateCamera(cu);
            // mark trip start and trip end.
            if (!points.isEmpty()) {
                LatLng tripStartLocation = points.get(0);
                LatLng tripEndLocation = points.get(points.size() - 1);
                markPoint(gMap, tripStartLocation, BitmapDescriptorFactory.HUE_GREEN, "Trip Start");
                markPoint(gMap, tripEndLocation, BitmapDescriptorFactory.HUE_GREEN, "Trip End");
            }
            // mark the estimated trip start location
            if (!extrapolatedWaypoints.isEmpty()) {
                markPoint(gMap, extrapolatedWaypoints.get(0), BitmapDescriptorFactory.HUE_GREEN,
                        "Estimated Trip Start");
            }
            // mark events.
            markEvents(gMap, driveInfo.events);
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
        gMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return true;
        });
    }

    private List<LatLng> getExtrapolatedLatLngPoints(DriveInfo driveInfo) {
        List<LatLng> extrapolatedWaypoints = new ArrayList<>();
        ZendriveExtrapolationDetails extrapolationDetails = driveInfo.extrapolationDetails;
        if (extrapolationDetails != null && extrapolationDetails.estimatedStartLocation != null) {
            LocationPoint estimatedStartLocation = extrapolationDetails.estimatedStartLocation;
            LatLng estimatedStartLatLng = new LatLng(
                    estimatedStartLocation.latitude, estimatedStartLocation.longitude);
            extrapolatedWaypoints.add(estimatedStartLatLng);

            LocationPoint actualStartLocation = driveInfo.waypoints.get(0).location;
            LatLng actualStartLocationLatLng = new LatLng(
                    actualStartLocation.latitude, actualStartLocation.longitude);
            extrapolatedWaypoints.add(actualStartLocationLatLng);
        }
        return extrapolatedWaypoints;
    }

    private SupportMapFragment supportmapfragment;
}
