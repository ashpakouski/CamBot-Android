package com.shpakovskiy.cambot.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

@Deprecated
public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter adapter;

    private static String TAG = "TAG123";

    @SuppressLint("MissingPermission")
    public ConnectThread(BluetoothAdapter adapter) {
        this.adapter = adapter;

        BluetoothDevice device = null;

        for (BluetoothDevice device1 : adapter.getBondedDevices()) {
            Log.d("TAG123", "Device1: " + device1.getName());
            if (device1.getName().equalsIgnoreCase("HC05")) {
                device = device1;
            }
        }

        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(
                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            );
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    @SuppressLint("MissingPermission")
    public void run() {
        adapter.cancelDiscovery();

        try {
            mmSocket.connect();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
    }

    public void write(byte[] bytes) {
        (new Thread(() -> {
            try {
                mmSocket.getOutputStream().write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        })).start();
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            // Log.e(TAG, "Could not close the client socket", e);
        }
    }
}