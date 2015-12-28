package itmo.courseproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class BtGameConfigurationClientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_configuration_client);
    }

    private static final byte CHANNEL_ID = 2;

    private BluetoothService btService;

    private BluetoothService.MessageChannel messageChannel;

    private static long fromByteArray(byte[] array) {
        long result = 0;
        for (int i = 0; i < 8; ++i) {
            result |= array[i];
            result <<= 8;
        }
        return result;
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            btService.registerActivity(BtGameConfigurationActivity.class);

            messageChannel = btService.getChannel(CHANNEL_ID);

            messageChannel.setOnMessageReceivedListener(new BluetoothService.OnMessageReceivedListener() {
                @Override
                public void process(final byte[] buffer) {
                    BtGameConfigurationClientActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long seed = fromByteArray(buffer);
                            byte color = buffer[8];
                            byte type = buffer[9];

                            // TODO Launch game
                        }
                    });
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chess, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (btService != null) {
            btService.stopSelf();
            btService = null;
        }
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (btService != null) {
            btService.registerActivity(BtGameConfigurationActivity.class);
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
        if (btService != null) {
            btService.unregisterChannel(CHANNEL_ID);
        }
        btService = null;
        unbindService(connection);
        super.onDestroy();
    }
}
