package itmo.courseproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Random;

public class BtGameConfigurationActivity extends GameConfigurationActivity {
    private static final byte CHANNEL_ID = 2;

    private BluetoothService btService;
    private MenuItem acceptButton;

    private BluetoothService.MessageChannel messageChannel;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            btService.registerActivity(BtGameConfigurationActivity.class);

            messageChannel = btService.getChannel(CHANNEL_ID);

            acceptButton.setEnabled(true);
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
        acceptButton.setEnabled(false);
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
        btService = null;
        unbindService(connection);
        super.onDestroy();
    }

    private static byte[] toByteArray(long x) {
        byte[] array = new byte[8];
        for (int i = 0; i < 8; ++i) {
            array[i] = (byte) (x & 0xFF);
            x >>= 8;
        }
        return array;
    }

    @Override
    protected void launchGame() {
        byte[] seed = toByteArray(System.currentTimeMillis());
        if (color == COLOR_RANDOM) {
            Random random = new Random();
            color = random.nextInt() % 2 + 1;
        }

        byte[] msg = new byte[10];
        System.arraycopy(seed, 0, msg, 0, 8);
        msg[8] = (byte) color;
        msg[9] = (byte) type;

        messageChannel.send(msg);

        // TODO start activity

        btService.unregisterActivity();
        btService = null;
        unbindService(connection);
    }
}
