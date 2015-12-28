package itmo.courseproject;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import itmo.courseproject.chess.Desk;
import itmo.courseproject.chess.Move;
import itmo.courseproject.chess.Player;

/**
 * Service handling bluetooth interaction
 */
public class BluetoothService extends Service {

    private static final String TAG = "BluetoothService";
    private static final UUID MY_UUID = UUID.fromString("27e86a38-a29c-421e-9d17-fe9c0c3bf2e6");

    private static final int NOTIFICATION_ID = 1;

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    public Desk desk;

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException ignored) {
            }
            btSocket = null;
        }
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

    public static class BtUnavailableException extends Exception {
        public BtUnavailableException() {
            super("bluetooth is not supported");
        }
    }

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

    public interface OnConnected {
        void success();
    }

    private OnConnected onConnected;

    public void setOnConnected(OnConnected onConnected) {
        this.onConnected = onConnected;
    }

    public void startAcceptThread() {
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    public void startConnectThread(String address) {
        connectThread = new ConnectThread(address);
        connectThread.start();
    }

    public interface OnMessageReceivedListener {
        void process(int bytes, byte[] buffer);
    }

    private OnMessageReceivedListener onMessageReceivedListener;
    private final ArrayList<Integer> sizes = new ArrayList<>();
    private final ArrayList<byte[]> cache = new ArrayList<>();

    public void setOnMessageReceivedListener(OnMessageReceivedListener onMessageReceivedListener) {
        this.onMessageReceivedListener = onMessageReceivedListener;

        for (int i = 0; i < cache.size(); ++i) {
            onMessageReceivedListener.process(sizes.get(i), cache.get(i));
        }
        cache.clear();
        sizes.clear();
    }

    public void write(byte[] bytes) {
        connectedThread.write(bytes);
    }

    public BluetoothSocket getBluetoothSocket() {
        return btSocket;
    }

    /*
        Called to indicate that activity is in foreground and notification is unnecessary
     */
    public void registerActivity(Class<?> activityClass) {
        lastClass = activityClass;
        changeRegCount(1);
    }

    /*
        Called to indicate that activity is no longer visible and notification may be needed
     */
    public void unregisterActivity() {
        changeRegCount(-1);
    }

    private int regCount = 0;
    private Class<?> lastClass;

    private synchronized void changeRegCount(int d) {
        int newRegCount = regCount + d;

        if (newRegCount == 0 && regCount != 0) {
            showNotification(lastClass, getString(R.string.bt_game),
                    isConnected() ?
                            getBluetoothSocket().getRemoteDevice().getName() :
                            getString(R.string.not_connected));
        }
        if (newRegCount != 0 && regCount == 0) {
            hideNotification();
        }

        regCount = newRegCount;
    }

    public boolean isServer() {
        return isServer;
    }





    private void showNotification(Class<?> aClass, String title, String text) {
        Log.d(TAG, "notification shown");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_bt_notification)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, aClass).setFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        0));

        startForeground(NOTIFICATION_ID, builder.build());
    }

    private void hideNotification() {
        Log.d(TAG, "notification hidden");

        stopForeground(true);
    }

    private boolean isServer;

    private synchronized void connected(BluetoothSocket socket, boolean isServer) {
        this.btSocket = socket;

        this.isServer = isServer;

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        onConnected.success();

        connectedThread = new ConnectedThread();
        connectedThread.start();
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        public AcceptThread() {
            try {
                serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(TAG, MY_UUID);
            } catch (IOException ignored) {
            }
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
                    connected(socket, true);
                    try {
                        serverSocket.close();
                    } catch (IOException ignored) {
                    }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket socket;
        public static final String TAG = "ConnectThread";

        public ConnectThread(String address) {
            try {
                socket = btAdapter.getRemoteDevice(address).createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException ignored) {
            }
        }

        public void run() {
            btAdapter.cancelDiscovery();

            try {
                socket.connect();
                Log.d(TAG, Boolean.toString(socket.isConnected()));
            } catch (IOException connectException) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                return;
            }

            BluetoothSocket tmp = socket;
            socket = null;
            connected(tmp, false);
        }

        public void cancel() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread() {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = btSocket.getInputStream();
                tmpOut = btSocket.getOutputStream();
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
                    try {
                        onMessageReceivedListener.process(bytes, buffer);
                    } catch (NullPointerException ignored) {
                        sizes.add(bytes);
                        cache.add(buffer);
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public synchronized void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                Log.d(TAG, "write");
            } catch (IOException ignored) {
            }
        }

        public void cancel() {
            try {
                btSocket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
