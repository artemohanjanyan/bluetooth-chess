package lec.chessproto;

import lec.chessproto.chess.Move;
import lec.chessproto.chess.Player;

public class RemotePlayer extends Player implements  BluetoothService.OnMessageReceivedListener {

    BluetoothService btService;
    BtGameActivity activity;


    public RemotePlayer(BluetoothService btService, BtGameActivity activity) {
        this.btService = btService;
        this.activity = activity;
        btService.setOnMessageReceivedListener(this);
    }

    @Override
    public void process(final int bytes, final byte[] buffer) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                moveFigure(Move.getMove(bytes, buffer));
            }
        });
    }

    @Override
    public void onYourTurn() {

    }

    @Override
    public void gameOver(boolean isWinner) {
    }
}
