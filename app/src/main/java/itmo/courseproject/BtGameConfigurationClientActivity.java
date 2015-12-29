package itmo.courseproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class BtGameConfigurationClientActivity extends AppCompatActivity {

    private static final String TAG = "ConfigurationClient";

    private boolean shouldStop = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_configuration_client);

        Intent btServiceIntent = new Intent(this, BluetoothService.class);
        startService(btServiceIntent);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    private static final byte CHANNEL_ID = 2;

    private BluetoothService btService;

    private BluetoothService.MessageChannel messageChannel;

    // Because I can
    private static long fromByteArray(byte[] array) {
        long result = 0;
        for (int i = 0; i < 8; ++i) {
            result <<= 8;
            result |= (array[i] & 0xFF);
        }
        return result;
    }

//    public long fromByteArray(byte[] bytes) {
//        ByteBuffer buffer = ByteBuffer.allocate(8);
//        buffer.put(bytes);
//        buffer.flip();
//        return buffer.getLong();
//    }

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
                            long seed = fromByteArray(Arrays.copyOfRange(buffer, 0, 8));
                            byte color = buffer[8];
                            byte type = buffer[9];

                            Log.d(TAG, Long.toString(seed));

                            Intent intent = new Intent(BtGameConfigurationClientActivity.this,
                                    BtGameActivity.class)
                                    .putExtra(GameActivity.SEED, seed)
                                    .putExtra(GameActivity.GAME, (int) type)
                                    .putExtra(BtGameActivity.LOCAL_PLAYER_COLOR,
                                            color == GameConfigurationActivity.COLOR_WHITE);
                            startActivity(intent);
                            shouldStop = false;
                            finish();
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
        if (btService != null && shouldStop) {
            btService.stopSelf();
        }
        unbindService(connection);
        super.onDestroy();
    }
}
