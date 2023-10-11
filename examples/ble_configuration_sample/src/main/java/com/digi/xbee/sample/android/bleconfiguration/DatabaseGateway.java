package com.digi.xbee.sample.android.bleconfiguration;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DatabaseGateway {

    private static final String TAG = "DatabaseGateway";
    private static final long MIN_TIME_BETWEEN_UPDATES = 20;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 3; //changed to if the device moves more than 3 meters, a new location update will be generated.
    public final String generatedId = UUID.randomUUID().toString();

    private static DatabaseGateway INSTANCE;
    private LocationManager locationManager;

    private FirebaseFirestore db;
    private LocationListener locationListener;

    ListenerRegistration myLocationListener;

    DocumentReference myLocationDocument;
    List<DocumentSnapshot> vehiclesNearby;

    public Map<String, DeviceInfo> getLinkedVehicles() {
        return linkedVehicles;
    }

    Map<String, DeviceInfo> linkedVehicles = new HashMap<>();

    String hashedMyLocation;
    GeoLocation myLocation;

    Context context;


    public void addDeviceInfoToDeviceCollection(String address, String name, DeviceInfo.VehicleType deviceType, String lastModified) {
        // Create the device information
        DeviceInfo deviceInfo1 = new DeviceInfo(address, name, deviceType, myLocation, hashedMyLocation, lastModified, generatedId);


        // Add the device information to Firestore
        db.collection("vehicles").add(deviceInfo1)
                .addOnSuccessListener(documentReference -> Toast.makeText(context, "Device information added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Log.e(TAG, "Error adding device information to Firestore: " + e.getMessage()));

    }

    public static DatabaseGateway getINSTANCE(Context context) {
        if (INSTANCE == null)
            INSTANCE = new DatabaseGateway(context);
        return INSTANCE;
    }

    private DatabaseGateway(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
//
//        myLocationDocument = db.collection("vehicles").document(generatedId);
//        Map<String, Object> tempMap = new HashMap<>();
//        tempMap.put("Location", new GeoLocation(0, 0));
//        tempMap.put("carType", "Truck");
//        tempMap.put("geoHash", GeoFireUtils.getGeoHashForLocation(new GeoLocation(0, 0)));
//        myLocationDocument.set(tempMap);

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
    }

    public void setVehiclesNearbySnapshotListener(GeoLocation center, Consumer<List<DocumentSnapshot>> listConsumer) {

        final double radiusInM = 2 * 1000;
        // Each item in 'bounds' represents a startAt/endAt pair. We have to issue
// a separate query for each pair. There can be up to 9 pairs of bounds
// depending on overlap, but in most cases there are 4.
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = db.collection("vehicles")
                    .orderBy("geoHash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);

            tasks.add(q.get());
        }

//        @SuppressLint({"NewApi", "LocalSuppress"}) Query query = bounds.stream().map(geoQueryBounds -> Filter.lessThan(). );

// Collect all the query results together into a single list
        Tasks.whenAllComplete(tasks)

                .addOnCompleteListener(t -> {
                    List<DocumentSnapshot> matchingDocs = new ArrayList<>();

                    for (Task<QuerySnapshot> task : tasks) {
                        QuerySnapshot snap = task.getResult();
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            GeoLocation geoLocation = doc.get("Location", GeoLocation.class);
                            double distanceInM = GeoFireUtils.getDistanceBetween(geoLocation, center);
                            if (distanceInM <= radiusInM) {
                                matchingDocs.add(doc);
                            }

                        }
                    }
                    listConsumer.accept(vehiclesNearby);
                    vehiclesNearby = matchingDocs;
                });
//
//        myLocationListener = myLocationDocument.addSnapshotListener((documentSnapshot, error) -> {
//            if (error != null) {
//                Log.w(TAG, "Listen failed.", error);
//                return;
//            }
//
//            if (documentSnapshot != null && documentSnapshot.exists()) {
//                GeoLocation receivedLocation = documentSnapshot.get("Location", GeoLocation.class);
//                if (distance(receivedLocation, myLocation) > 10) {
//
//                }
//
//            } else {
//                Log.d(TAG, "Document does not exist");
//            }
//        });
    }

    public void closeLocationSnapshotListener() {
        myLocationListener.remove();
    }
//
//    public void setMyLocation(GeoLocation myLocation) {
//        this.myLocation = myLocation;
//    }

    public static double distance(GeoLocation location1, GeoLocation location2) {
        double lat1 = location1.latitude;
        double lat2 = location2.longitude;
        double lon1 = location1.latitude;
        double lon2 = location2.longitude;

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        //double height = el1 - el2;

        //distance = Math.sqrt(Math.pow(distance, 2) + Math.pow(height, 2));

        return distance;
    }

    public void addLinkedVehicle(String address, String name, DeviceInfo.VehicleType deviceType, String currentTimeFormatted) {

        this.linkedVehicles.put(address, new DeviceInfo(address, name, deviceType, myLocation, hashedMyLocation, currentTimeFormatted, generatedId));
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            hashedMyLocation = GeoFireUtils.getGeoHashForLocation(new GeoLocation(location.getLatitude(), location.getLongitude()));
            WriteBatch batch = db.batch();
            for (DeviceInfo deviceInfo : linkedVehicles.values()) {
                DocumentReference vehicle = db.collection("vehicles").document(deviceInfo.getAddress());
                batch.set(vehicle, deviceInfo);
            }
            batch.commit().addOnCompleteListener(task -> {
            });
        }


        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    }


}
