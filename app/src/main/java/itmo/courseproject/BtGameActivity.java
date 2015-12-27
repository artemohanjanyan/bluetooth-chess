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

    public static final String LOCAL_PLAYER_COLOR = "local_player_color";

    private boolean serverPlayerColor;
    private BluetoothService btService;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            btService.registerActivity(BtGameActivity.class, getString(R.string.chat_name));

            BtLocalPlayer btLocalPlayer = new BtLocalPlayer(gameView, btService);
            RemotePlayer remotePlayer = new RemotePlayer(btService, BtGameActivity.this);
            serverPlayerColor = btService.isServer();
            whitePlayer = serverPlayerColor ? remotePlayer : btLocalPlayer;
            blackPlayer = serverPlayerColor ? btLocalPlayer : remotePlayer;
            onPlayersInitialized();

            Log.d(TAG, "restoration...");
            if (btService.shouldRestore()) {
                btService.restorePosition(whitePlayer, blackPlayer);
                Log.d(TAG, "restored!");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
        Intent btServiceIntent = new Intent(this, BluetoothService.class);
        startService(btServiceIntent);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (btService != null) {
            btService.registerActivity(this.getClass(), getString(R.string.chat_name));
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
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
