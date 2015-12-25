package itmo.courseproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class BtGameActivity extends GameActivity {

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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @Override
    protected void initPlayers() {
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
        if (btService != null) {
            btService.unregisterActivity();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);

        super.onDestroy();
    }
}
