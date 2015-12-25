package lec.chessproto;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class TmpActivity extends AppCompatActivity {

    private BluetoothService btService;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT = 2;
    private static final int REQUEST_PLAY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmp);

        Intent btServiceIntent = new Intent(this, BluetoothService.class);
        startService(btServiceIntent);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (btService != null) {
            btService.registerActivity(TmpActivity.class, getString(R.string.chat_name));
        }
    }

    @Override
    protected void onStop() {
        if (btService != null) {
            btService.unregisterActivity();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);

        if (btService != null) {
            btService.stopSelf();
            btService = null;
        }

        super.onDestroy();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();

            btService.registerActivity(TmpActivity.class, getString(R.string.chat_name));

            try {
                btService.initBtAdapter();
            } catch (BluetoothService.BtUnavailableException e) {
                Toast.makeText(TmpActivity.this,
                        R.string.bluetooth_absent, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            if (!btService.getBluetoothAdapter().isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                        REQUEST_ENABLE_BT);
            } else if (!btService.isConnected()) {
                startActivityForResult(new Intent(TmpActivity.this, DeviceChooser.class),
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
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(TmpActivity.this,
                            R.string.bluetooth_request, Toast.LENGTH_LONG).show();
                    finish();
                    break;
                }
                startActivityForResult(new Intent(this, DeviceChooser.class),
                        REQUEST_CONNECT);
                break;

            case REQUEST_CONNECT:
                if (resultCode != RESULT_OK) {
                    finish();
                    break;
                }
                startActivityForResult(new Intent(this, BtGameActivity.class),
                        REQUEST_PLAY);
                finish();
                break;

            case REQUEST_PLAY:
                finish();
                break;
        }
    }
}
