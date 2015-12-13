package com.example.artem.project;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by artem on 12/13/15.
 * Service handling bluetooth interaction
 */
public class BluetoothService extends Service {

    public static final String TAG = "BluetoothService";
    private static final UUID MY_UUID = UUID.fromString("27e86a38-a29c-421e-9d17-fe9c0c3bf2e6");

    public static final int REQUEST_CONNECT = 1;

    private BluetoothAdapter btAdapter;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    public class BtBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }
    private final IBinder binder = new BtBinder();

    //
    // TODO
    // Public methods
    //

    public void initBtAdapter() throws BtUnavailableException {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            throw new BtUnavailableException();
        }
    }

    public boolean isConnected() {
        return connectedThread != null;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return btAdapter;
    }

    public void startAcceptThread() {
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    public interface OnConnectedExit {
        void exit();
    }

    public void startConnectThread(OnConnectedExit onConnectedExit) {
        connectedThread = new ConnectThread(onConnectedExit);
        runOnUiThread
    }

    //
    // Public methods
    //

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        public AcceptThread() {
            try {
                serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(TAG, MY_UUID);
            } catch (IOException ignored) { }
        }

        public void run() {
            BluetoothSocket socket;
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }
                if (socket != null) {
                    connected(socket);
                    try {
                        serverSocket.close();
                    } catch (IOException ignored) { }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException ignored) { }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket;
        public static final String TAG = "ConnectThread";

        public ConnectThread(BluetoothDevice device) {
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException ignored) { }
        }

        public void run() {
            btAdapter.cancelDiscovery();

            try {
                socket.connect();
                Log.d(TAG, Boolean.toString(socket.isConnected()));
            } catch (IOException connectException) {
                try {
                    socket.close();
                } catch (IOException ignored) { }
                return;
            }

            BluetoothSocket tmp = socket;
            socket = null;
            connected(tmp);
        }

        public void cancel() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) { }
            }
        }
    }

    public static class BtUnavailableException extends Exception {
        public BtUnavailableException() {
            super("bluetooth is not supported");
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException ignored) {
                Log.d(TAG, "error");
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    Log.d(TAG, "read");
                    handler.obtainMessage(HANDLER_MESSAGE_GET, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                Log.d(TAG, "write");
            } catch (IOException ignored) { }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException ignored) { }
        }
    }
}
