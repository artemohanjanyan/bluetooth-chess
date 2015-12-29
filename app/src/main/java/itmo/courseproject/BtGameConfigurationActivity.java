package itmo.courseproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.nio.ByteBuffer;
import java.util.Random;

public class BtGameConfigurationActivity extends GameConfigurationActivity {

    private static final byte CHANNEL_ID = 2;

    private BluetoothService btService;
    private MenuItem acceptButton;

    private BluetoothService.MessageChannel messageChannel;

    private boolean shouldStop = true;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            btService.registerActivity(BtGameConfigurationActivity.class);

            messageChannel = btService.getChannel(CHANNEL_ID);

            if (acceptButton != null) {
                acceptButton.setEnabled(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent btServiceIntent = new Intent(this, BluetoothService.class);
        startService(btServiceIntent);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        acceptButton = menu.findItem(R.id.accept_button);
        if (btService == null) {
            acceptButton.setEnabled(false);
        }
        return result;
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

    // Because I can
    private static byte[] toByteArray(long x) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; --i) {
            array[i] = (byte) (x & 0xFF);
            x >>>= 8;
        }
        return array;
    }

//    public byte[] toByteArray(long x) {
//        ByteBuffer buffer = ByteBuffer.allocate(8);
//        buffer.putLong(x);
//        return buffer.array();
//    }

    @Override
    protected void launchGame() {
        Random random = new Random();

        long longSeed = random.nextLong();
        byte[] seed = toByteArray(longSeed);
        if (color == COLOR_RANDOM) {
            color = random.nextInt() % 2 + 1;
        }

        byte[] msg = new byte[10];
        System.arraycopy(seed, 0, msg, 0, 8);
        msg[8] = (byte) color;
        msg[9] = (byte) type;

        messageChannel.send(msg);

        Log.d(TAG, Long.toString(longSeed));

        Intent intent = new Intent(this, BtGameActivity.class)
                .putExtra(GameActivity.SEED, longSeed)
                .putExtra(GameActivity.GAME, type)
                .putExtra(BtGameActivity.LOCAL_PLAYER_COLOR, color != COLOR_WHITE);
        startActivity(intent);
        shouldStop = false;
        finish();
    }
}
