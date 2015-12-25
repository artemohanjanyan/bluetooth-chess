package lec.chessproto;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CONNECT = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int HANDLER_MESSAGE_GET = 3;

    private ArrayAdapter<String> arrayAdapter;
    private EditText editText;

    private BluetoothService btService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrayAdapter = new ArrayAdapter<>(this, R.layout.message);
        ((ListView) findViewById(R.id.messages)).setAdapter(arrayAdapter);
        editText = (EditText) findViewById(R.id.edit_text);
        Button button = (Button) findViewById(R.id.send);
        button.setOnClickListener(onClickListener);

        Intent btServiceIntent = new Intent(this, BluetoothService.class);
        startService(btServiceIntent);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (btService != null) {
            btService.registerActivity(MainActivity.class, getString(R.string.chat_name));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (btService != null) {
            btService.unregisterActivity();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (handler != null) {
            handler = null;
        }

        unbindService(connection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //shouldClose = true;
        if (btService != null) {
            btService.stopSelf();
            btService = null;
        }
        finish();
        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();

            btService.registerActivity(MainActivity.class, getString(R.string.chat_name));

            btService.setOnMessageReceived(new BluetoothService.OnMessageReceived() {
                @Override
                public void process(int bytes, byte[] buffer) {
                    handler.obtainMessage(HANDLER_MESSAGE_GET, bytes, -1, buffer)
                            .sendToTarget();
                }
            });

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
            } else if (!btService.isConnected()) {
                //shouldLaunch = false;
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
                    case RESULT_CANCELED:
                        finish();
                        break;
                }
                break;

            case REQUEST_ENABLE_BT:
                switch (resultCode) {
                    case RESULT_OK:
                        //shouldLaunch = false;
                        startActivityForResult(new Intent(MainActivity.this, DeviceChooser.class),
                                REQUEST_CONNECT);
                        break;

                    default:
                        btService.stopSelf();
                        btService = null;
                        // TODO toast
                        finish();
                        break;
                }
                break;
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btService.write(editText.getText().toString().getBytes());
            arrayAdapter.add("Me: " + editText.getText().toString());
            editText.getText().clear();
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            arrayAdapter.add(btService.getBluetoothSocket().getRemoteDevice().getName() + ": " +
                    new String((byte[]) msg.obj, 0, msg.arg1));
        }
    };
}
