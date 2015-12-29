package itmo.courseproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class BtGameActivity extends GameActivity {
    private static final String TAG = "BtGameActivity";
    private static final byte CHANNEL_ID = 1;

    public static final String LOCAL_PLAYER_COLOR = "local_player_color";

    private boolean localPlayerColor;
    private BluetoothService btService;
    private BluetoothService.MessageChannel channel;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            btService.registerActivity(BtGameActivity.class);

            channel = btService.getChannel(CHANNEL_ID);

            BtLocalPlayer btLocalPlayer = new BtLocalPlayer(gameView, channel);
            BtRemotePlayer btRemotePlayer = new BtRemotePlayer(channel, BtGameActivity.this);
            whitePlayer = localPlayerColor ? btRemotePlayer : btLocalPlayer;
            blackPlayer = localPlayerColor ? btLocalPlayer : btRemotePlayer;

            if (initialIntent.getIntExtra(GAME, ALREADY_EXIST) == ALREADY_EXIST) {
                desk = btService.desk;
            } else {
                btService.desk = desk;
            }
            btService.localPlayerColor = localPlayerColor;

            onPlayersInitialized();
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
    protected void initPlayers() {
        Log.d(TAG, "initPlayers");
        localPlayerColor = initialIntent.getBooleanExtra(LOCAL_PLAYER_COLOR, btService != null && btService.isServer());
        Intent btServiceIntent = new Intent(this, BluetoothService.class);
        startService(btServiceIntent);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (btService != null) {
            btService.registerActivity(this.getClass());
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
        unbindService(connection);
        super.onDestroy();
    }
}
