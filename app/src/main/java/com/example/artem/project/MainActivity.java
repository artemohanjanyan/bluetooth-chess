package com.example.artem.project;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "Main";

    private final int REQUEST_CONNECT = 1;
    private final int REQUEST_ENABLE_BT = 2;
    private final int HANDLER_MESSAGE_GET = 1;

    private BluetoothSocket bluetoothSocket;

    private ArrayAdapter<String> arrayAdapter;
    private EditText editText;
    private Button button;

    private ConnectedThread connectedThread;

    private BluetoothService btService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrayAdapter = new ArrayAdapter<>(this, R.layout.message);
        ((ListView) findViewById(R.id.messages)).setAdapter(arrayAdapter);
        editText = (EditText) findViewById(R.id.edit_text);
        button = (Button) findViewById(R.id.send);
        button.setOnClickListener(onClickListener);

        Intent btServiceIntent = new Intent(this, BluetoothService.class);
        startService(btServiceIntent);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();

            try {
                btService.initBtAdapter();
            } catch (BluetoothService.BtUnavailableException e) {
                Toast.makeText(MainActivity.this,
                        R.string.bluetooth_absent, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            if (!btService.getBluetoothAdapter().isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                        REQUEST_ENABLE_BT);
            }

            if (!btService.isConnected()) {
                startActivityForResult(new Intent(MainActivity.this, DeviceChooser.class),
                        REQUEST_CONNECT);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CONNECT:
                switch (resultCode) {
                    case RESULT_OK:
                        // TODO delete
                        bluetoothSocket = Data.bluetoothSocket;
                        Data.bluetoothSocket = null;
                        connectedThread = new ConnectedThread(bluetoothSocket);
                        connectedThread.start();
                        break;

                    case RESULT_CANCELED:
                        finish();
                        break;
                }

                break;
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            connectedThread.write(editText.getText().toString().getBytes());
            arrayAdapter.add("Me: " + editText.getText().toString());
            editText.getText().clear();
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            arrayAdapter.add(bluetoothSocket.getRemoteDevice().getName() + ": " +
                    new String((byte[]) msg.obj, 0, msg.arg1));
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException ignored) { }
        }

        if (handler != null) {
            handler = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
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
