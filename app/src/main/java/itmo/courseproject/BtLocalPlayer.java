package itmo.courseproject;

import itmo.courseproject.chess.Move;

public class BtLocalPlayer extends LocalPlayer {

    private final BluetoothService.MessageChannel channel;

    public BtLocalPlayer(GameView gameView, BluetoothService.MessageChannel channel) {
        super(gameView);
        this.channel = channel;
    }

    @Override
    public boolean moveFigure(Move move) {
        boolean moved = super.moveFigure(move);
        if (moved) {
            channel.send(move.toBytes());
        }
        return moved;
    }
}
