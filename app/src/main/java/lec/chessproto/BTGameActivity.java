package lec.chessproto;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import lec.chessproto.chess.Chess;
import lec.chessproto.chess.Move;
import lec.chessproto.chess.Player;

public class BtGameActivity extends GameActivity {

    public static final String LP_COLOR = "lpcolor";

    private boolean localPlayerColor;
    private BluetoothService btService;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            BtLocalPlayer btLocalPlayer = new BtLocalPlayer(gameView, btService);
            RemotePlayer remotePlayer = new RemotePlayer(btService);
            whitePlayer = localPlayerColor ? remotePlayer : btLocalPlayer;
            blackPlayer = localPlayerColor ? btLocalPlayer : remotePlayer;
            onPlayersInitialized();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };


    @Override
    protected void initPlayers() {
        localPlayerColor = initialIntent.getBooleanExtra(LP_COLOR, false);

        Intent btServiceIntent = new Intent(this, BluetoothService.class);
        startService(btServiceIntent);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }



}
