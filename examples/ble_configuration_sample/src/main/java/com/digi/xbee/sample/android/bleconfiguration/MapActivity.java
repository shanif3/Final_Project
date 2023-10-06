package com.digi.xbee.sample.android.bleconfiguration;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private DatabaseGateway databaseGateway;
    private GeoLocation cameraLocation;

    List<Marker> vehiclesNearbyMarkers = new ArrayList<>();

    private Consumer<List<DocumentSnapshot>> updateVehiclesNearByOnMap = documentSnapshots -> {
        try {
            List<Marker> newList = new ArrayList<>();
            if (documentSnapshots != null)
                for (DocumentSnapshot vehicles : documentSnapshots) {
                    GeoLocation geoLocation = vehicles.get("Location", GeoLocation.class);
                    newList.add(googleMap.addMarker(new MarkerOptions().position(new LatLng(geoLocation.latitude, geoLocation.longitude))));
                }
            vehiclesNearbyMarkers.forEach(Marker::remove);
            vehiclesNearbyMarkers = newList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        databaseGateway = DatabaseGateway.getINSTANCE(getApplicationContext());
        googleMap.setOnCameraMoveListener(() -> {
            try {
                CameraPosition newCameraLocation = googleMap.getCameraPosition();
                LatLng currentPosition = newCameraLocation.target;
                float[] results = new float[3];
                Location.distanceBetween(currentPosition.latitude, currentPosition.longitude, cameraLocation.latitude, cameraLocation.longitude, results);
                if (results[0] > 1000) {
                    cameraLocation = new GeoLocation(currentPosition.latitude, currentPosition.longitude);
                    databaseGateway.setVehiclesNearbySnapshotListener(new GeoLocation(currentPosition.latitude, currentPosition.longitude), updateVehiclesNearByOnMap);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
