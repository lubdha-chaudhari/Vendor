package com.example.quickvendadmin.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.quickvendadmin.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;

public class HomeFragment extends Fragment {

    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        Context ctx = requireContext();

        // Setup osmdroid configuration
        File osmdroidBasePath = new File(ctx.getCacheDir(), "osmdroid");
        File tileCache = new File(osmdroidBasePath, "tiles");
        Configuration.getInstance().setOsmdroidBasePath(osmdroidBasePath);
        Configuration.getInstance().setOsmdroidTileCache(tileCache);
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mapView = rootView.findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(18.0);
        GeoPoint defaultPoint = new GeoPoint(19.0760, 72.8777); // Mumbai
        mapController.setCenter(defaultPoint);

        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            initMyLocation();
        }

        return rootView;
    }

    private void initMyLocation() {
        GpsMyLocationProvider gpsProvider = new GpsMyLocationProvider(requireContext());
        myLocationOverlay = new MyLocationNewOverlay(gpsProvider, mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        mapView.getOverlays().add(myLocationOverlay);


        myLocationOverlay.runOnFirstFix(() -> {
            GeoPoint currentPoint = myLocationOverlay.getMyLocation();
            if (currentPoint != null) {
                requireActivity().runOnUiThread(() -> {
                    mapView.getController().setCenter(currentPoint);
                    mapView.invalidate();
                });
            }
        });

        // Continuously update the marker on location change
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        myLocationOverlay.setEnableAutoStop(false);
        myLocationOverlay.runOnFirstFix(() -> {
            while (myLocationOverlay.isMyLocationEnabled()) {
                GeoPoint currentPoint = myLocationOverlay.getMyLocation();
                if (currentPoint != null) {
                    requireActivity().runOnUiThread(() -> {
                        mapView.invalidate();
                    });
                }
                try {
                    Thread.sleep(3000); // update every 3 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (myLocationOverlay != null) myLocationOverlay.enableMyLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (myLocationOverlay != null) myLocationOverlay.disableMyLocation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (myLocationOverlay != null) myLocationOverlay.disableMyLocation();
    }
}
