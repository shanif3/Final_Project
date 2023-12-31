/*
 * Copyright 2019, Digi International Inc.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.digi.xbee.sample.android.bleconfiguration;

import java.lang.System;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.android.XBeeBLEDevice;
import com.digi.xbee.api.exceptions.BluetoothAuthenticationException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDiscoveryListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import android.util.Log;

import java.util.Locale;


public class BluetoothActivity extends AppCompatActivity {

    // Constants.
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    // Variables.
    private ArrayList<BluetoothDevice> bleDevices = new ArrayList<>();
    private ArrayList<DeviceInfo> deviceInfoList = new ArrayList<>();

    private BluetoothDeviceAdapter bluetoothDeviceAdapter;

    private BluetoothAdapter bluetoothAdapter;
    private BleScanCallback scanCallback;

    private BluetoothDevice selectedDevice = null;

    private ProgressBar scanProgress;

    private static XBeeBLEDevice xbeeDevice;

    private static final int REQUEST_BLUETOOTH_PERMISSION = 123;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private DatabaseGateway databaseGateway;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // Initialize Bluetooth stuff.
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null)
                bluetoothAdapter = bluetoothManager.getAdapter();
            scanCallback = new BleScanCallback();

            // Initialize the list view.
            ListView devicesListView = findViewById(R.id.devicesListView);
            bluetoothDeviceAdapter = new BluetoothDeviceAdapter(this, bleDevices);
            devicesListView.setAdapter(bluetoothDeviceAdapter);
            devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    stopScan();
                    selectedDevice = bluetoothDeviceAdapter.getItem(i);
                    askForPassword(false);
                }
            });

            scanProgress = findViewById(R.id.scanProgress);
            databaseGateway = DatabaseGateway.getINSTANCE(getApplicationContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check Bluetooth state
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            // Handle this case (e.g., show a message or exit the app)
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled, prompt the user to enable it
            showEnableBluetoothDialog();
        } else {
            // Bluetooth is enabled, continue with your Bluetooth operations
            // Request Bluetooth permissions if needed
            requestLocationPermission();
            bluetoothDeviceAdapter.clear();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop the Bluetooth scan.
        stopScan();
    }

    /**
     * Requests the location permission to the user and starts the Bluetooth
     * scan when done.
     */

    /**
     * Starts the Bluetooth scan process.
     */

    private void startScan() {
        Log.d("DeviceList", "Starting Bluetooth scan...");

        if (bluetoothAdapter != null) {
            bluetoothAdapter.startLeScan(scanCallback);
        }
        scanProgress.setVisibility(View.VISIBLE);
    }

    /**
     * Stops the Bluetooth scan process.
     */
    private void stopScan() {
        if (bluetoothAdapter != null)
            bluetoothAdapter.stopLeScan(scanCallback);
        scanProgress.setVisibility(View.INVISIBLE);
    }

    private void showEnableBluetoothDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable Bluetooth");
        builder.setMessage("Bluetooth is required for this app. Do you want to enable it?");
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Open Bluetooth settings to enable Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle the case where the user cancels enabling Bluetooth
                // You may choose to exit the app or disable Bluetooth-dependent features
            }
        });
        builder.setCancelable(false); // Prevent the user from dismissing the dialog
        builder.show();
    }

    private void requestBluetoothPermission() {
        String[] perms = {Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN};

        if (EasyPermissions.hasPermissions(this, perms)) {
            // Bluetooth permissions are already granted.
            // You can start using Bluetooth functionality here.
        } else {
            // Request Bluetooth permissions.
            EasyPermissions.requestPermissions(
                    this,
                    "Bluetooth permissions are required",
                    REQUEST_BLUETOOTH_PERMISSION,
                    perms
            );
        }
    }


    /**
     * Returns the selected and open XBee device.
     *
     * @return The selected and open XBee device.
     */
    public static XBeeBLEDevice getXBeeDevice() {
        return xbeeDevice;
    }

    /**
     * Asks the user for the Bluetooth password and tries to connect to the
     * selected device.
     *
     * @param authFailed {@code true} if the first authentication attempt
     *                   failed, {@code false} otherwise.
     */
    private void askForPassword(boolean authFailed) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.enter_password));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText passwordText = new EditText(this);
        passwordText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordText);

        // If the first authentication attempt failed, show an error message.
        if (authFailed) {
            TextView invalidPwdText = new TextView(this);
            invalidPwdText.setText(getResources().getString(R.string.invalid_password));
            layout.addView(invalidPwdText);
        }

        builder.setView(layout);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Hide keyboard.
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(passwordText.getWindowToken(), 0);

                // Connect to the selected device.
                connectToDevice(selectedDevice, passwordText.getText().toString());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Restart the Bluetooth scan.
                startScan();
            }
        });

        builder.show();
    }

    /**
     * Connects to the given Bluetooth device with the given password.
     *
     * @param device   Bluetooth device to connect to.
     * @param password Bluetooth password.
     */
    private void connectToDevice(final BluetoothDevice device, final String password) {
        // Show a progress dialog while connecting to the device.
        final ProgressDialog dialog = ProgressDialog.show(this, getResources().getString(R.string.connecting_device_title),
                getResources().getString(R.string.connecting_device_description), true);

        // The connection process blocks the UI interface, so it must be done in a different thread.
        new Thread(() -> {
            // Instantiate an XBee BLE device with the Bluetooth device and password.
            xbeeDevice = new XBeeBLEDevice(BluetoothActivity.this, device, password);
            try {
                // Open the connection with the device.
                xbeeDevice.open();
                try{
                    XBeeNetwork myXBeeNetwork = xbeeDevice.getNetwork();
                    myXBeeNetwork.setDiscoveryTimeout(15000);
                    myXBeeNetwork.addDiscoveryListener(new IDiscoveryListener(){
                        @Override
                            public void deviceDiscovered(RemoteXBeeDevice discoveredDevice) {
                                Log.d("see", "devices:" + discoveredDevice.toString());
                            }

                        @Override
                        public void discoveryError(String error) {
                                Log.d("see", "There was an error discovering devices: " + error);

                        }

                        @Override
                        public void discoveryFinished(String error) {
                            if (error== null)
                                    Log.d("see","Discovery process finished successfully");
                                else
                                    Log.d("see","Discovery process finished due to the following error: " + error);

                        }
                    });

                        myXBeeNetwork.startDiscoveryProcess();
                        Log.d("yes", "\n>> Discovering remote XBee devices...");







                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // If the open method did not throw an exception, the connection is open.
                BluetoothActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        // Start the Configuration activity.
                        Intent intent = new Intent(BluetoothActivity.this, MapActivity.class);
                        startActivity(intent);
                    }
                });
            } catch (BluetoothAuthenticationException e) {
                // There was a problem in the Bluetooth authentication process, so ask for the password again.
                e.printStackTrace();
                BluetoothActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        askForPassword(true);
                    }
                });
            } catch (final XBeeException e) {
                e.printStackTrace();
                BluetoothActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        new AlertDialog.Builder(BluetoothActivity.this).setTitle(getResources().getString(R.string.error_connecting_title))
                                .setMessage(getResources().getString(R.string.error_connecting_description, e.getMessage()))
                                .setPositiveButton(android.R.string.ok, null).show();
                    }
                });
            }
        }).start();
    }

    /**
     * Custom adapter class to represent the Bluetooth device items in the list.
     */
    private class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {

        private Context context;

        BluetoothDeviceAdapter(@NonNull Context context, ArrayList<BluetoothDevice> devices) {
            super(context, -1, devices);
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            BluetoothDevice device = bleDevices.get(position);

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 30, 40, 30);

            TextView nameText = new TextView(context);
            nameText.setText(device.getName() == null ? getResources().getString(R.string.unknown_name) : device.getName());
            nameText.setTypeface(nameText.getTypeface(), Typeface.BOLD);
            nameText.setTextSize(18);
            layout.addView(nameText);

            TextView macText = new TextView(context);
            macText.setText(getResources().getString(R.string.mac_addr, device.getAddress()));
            layout.addView(macText);

            return layout;
        }
    }

    /**
     * Custom Bluetooth scan callback.
     */
    private class BleScanCallback implements BluetoothAdapter.LeScanCallback {
        // Add a member variable to store the current time formatted
        private String currentTimeFormatted;

        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            // If the Bluetooth device is not in the list yet, add it.
            if (!bleDevices.contains(bluetoothDevice)) {
                if (bluetoothDevice.getName() != null && bluetoothDevice.getName().startsWith("Digi")) {
                    bluetoothDeviceAdapter.add(bluetoothDevice);
                    updateCurrentTime(); // Call the method to update currentTimeFormatted
                    databaseGateway.addDeviceInfoToDeviceCollection(
                            bluetoothDevice.getAddress(),
                            bluetoothDevice.getName(),
                            getDeviceType(bluetoothDevice.getName()),
                            currentTimeFormatted);
                    databaseGateway.addLinkedVehicle(bluetoothDevice.getAddress(),bluetoothDevice.getName(),getDeviceType(bluetoothDevice.getName()),currentTimeFormatted);

//                    for (DeviceInfo deviceInfo : deviceInfoList) {
//                        Log.d("DeviceInfo", "Name: " + deviceInfo.getName());
//                        Log.d("DeviceInfo", "Address: " + deviceInfo.getAddress());
//                        Log.d("DeviceInfo", "Device Type: " + deviceInfo.getDeviceType());
//                        Log.d("DeviceInfo", "Location: " + deviceInfo.getLocation());
//                        Log.d("DeviceInfo", "Last Modified: " + deviceInfo.getLastModified());
//                    }
                }
            }
        }

        // Add this method to update currentTimeFormatted
        private void updateCurrentTime() {
            // Get the current time in milliseconds
            long currentTimeMillis = System.currentTimeMillis();

            // Convert the current time to a human-readable date and time format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            currentTimeFormatted = dateFormat.format(new Date(currentTimeMillis));
        }
    }


    // Define a class to hold device information

    // Function to determine the device type based on its name
    private DeviceInfo.VehicleType getDeviceType(String deviceName) {
        // You can implement logic here to determine the device type (CAR/TRUCK)
        // For example, you can check if the deviceName starts with "CAR" or "TRUCK"
        if (deviceName != null && deviceName.contains("Car")) {
            return DeviceInfo.VehicleType.CAR;
        } else if (deviceName != null && deviceName.startsWith("Truck")) {
            return DeviceInfo.VehicleType.TRUCK;
        } else {
            return DeviceInfo.VehicleType.GHOST_CAR;
        }
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    private void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (EasyPermissions.hasPermissions(this, perms)) {
            // Start the Bluetooth scan.
            startScan();
        } else {
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.location_permission_needed),
                    REQUEST_LOCATION_PERMISSION, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}




