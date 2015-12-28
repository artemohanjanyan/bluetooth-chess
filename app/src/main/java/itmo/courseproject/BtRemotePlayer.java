package itmo.courseproject;

import itmo.courseproject.chess.Move;
import itmo.courseproject.chess.Player;

public class BtRemotePlayer extends Player implements BluetoothService.OnMessageReceivedListener {

    private final BtGameActivity activity;

    public BtRemotePlayer(BluetoothService.MessageChannel channel, BtGameActivity activity) {
        this.activity = activity;
        channel.setOnMessageReceivedListener(this);
    }

    @Override
    public boolean moveFigure(Move move) {
        return super.moveFigure(move);
    }

    @Override
    public void process(final byte[] buffer) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Move move = Move.getMove(buffer);
                moveFigure(move);
            }
        });
    }

    @Override
    public void onYourTurn() {

    }

    @Override
    public void gameOver(int gameOverMsg) {
    }
}
