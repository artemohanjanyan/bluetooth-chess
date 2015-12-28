package itmo.courseproject;

import itmo.courseproject.chess.Move;
import itmo.courseproject.chess.Player;

public class RemotePlayer extends Player implements BluetoothService.OnMessageReceivedListener {

    private final BtGameActivity activity;
    private final BluetoothService btService;

    public RemotePlayer(BluetoothService btService, BtGameActivity activity) {
        this.activity = activity;
        this.btService = btService;
        btService.setOnMessageReceivedListener(this);
    }

    @Override
    public boolean moveFigure(Move move) {
        return super.moveFigure(move);
    }

    @Override
    public void process(final int bytes, final byte[] buffer) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Move move = Move.getMove(bytes, buffer);
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
