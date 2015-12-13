package com.example.artem.project;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class DeviceChooser extends AppCompatActivity {

    private static final String TAG = "DEVICE_CHOOSER";

    private static final UUID MY_UUID = UUID.fromString("27e86a38-a29c-421e-9d17-fe9c0c3bf2e6");

    private BluetoothService btService;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_DISCOVERABLE = 2;

    private ArrayAdapter<String> arrayAdapter;

    private Button enableDiscoverable, scanForDevices;
    private ListView deviceList;
    private MenuItem progressBar;
    private int devicesFound;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_chooser);

        enableDiscoverable = (Button) findViewById(R.id.enable_discoverable);
        enableDiscoverable.setOnClickListener(onEnableDiscoverable);

        scanForDevices = (Button) findViewById(R.id.scan_for_devices);
        scanForDevices.setOnClickListener(onScanForDevices);

        deviceList = (ListView) findViewById(R.id.device_list);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.device);
        deviceList.setAdapter(arrayAdapter);
        deviceList.setOnItemClickListener(onItemClickListener);

        bindService(new Intent(this, BluetoothService.class), connection, Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = (BluetoothService) service;
            initBt();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        progressBar = menu.findItem(R.id.menu_item_progress_bar);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_chooser, menu);
        return true;
    }

    private void initBt() {
        Set<BluetoothDevice> paired = btService.getBluetoothAdapter().getBondedDevices();
        for (BluetoothDevice device : paired) {
            arrayAdapter.add(device.getName() + "\n" + device.getAddress());
        }
        btService.startAcceptThread();
    }

    private void makeToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private View.OnClickListener onEnableDiscoverable = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE),
                    REQUEST_ENABLE_DISCOVERABLE);
        }
    };

    private View.OnClickListener onScanForDevices = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            arrayAdapter.clear();
            btService.getBluetoothAdapter().startDiscovery();

            progressBar.setVisible(true);
            devicesFound = 0;
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            btService.getBluetoothAdapter().cancelDiscovery();

            String str = ((TextView) view).getText().toString();
            String addr = str.substring(str.length() - 17);

            makeToast(addr);

            btService.startConnectThread();

            connectThread = new ConnectThread(bluetoothAdapter.getRemoteDevice(addr));
            connectThread.start();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_CANCELED) {
                    setResult(RESULT_CANCELED);
                    finish();
                    break;
                }
                initBt();
                break;
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arrayAdapter.add(device.getName() + "\n" + device.getAddress());
                ++devicesFound;
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                progressBar.setVisible(false);
                makeToast(Integer.toString(devicesFound) +
                        " " + getString(R.string.n_devices_found_suffix));
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }

        unregisterReceiver(broadcastReceiver);
    }

    public synchronized void connected(BluetoothSocket socket) {
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        Log.d(TAG, Boolean.toString(socket.isConnected()));

        Data.bluetoothSocket = socket;
        setResult(RESULT_OK);

        finish();
    }

    /**
     *
     *
     *
     * Accept thread
     *
     *
     *
     */
    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        public AcceptThread() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(TAG, MY_UUID);
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

        public ConnectThread(BluetoothDevice device) {
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException ignored) { }
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

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
}
