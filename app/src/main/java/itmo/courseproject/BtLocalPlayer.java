package itmo.courseproject;

import itmo.courseproject.chess.Move;

public class BtLocalPlayer extends LocalPlayer {

    private final BluetoothService btService;

    public BtLocalPlayer(GameView gameView, BluetoothService btService) {
        super(gameView);
        this.btService = btService;
    }

    @Override
    public boolean moveFigure(Move move) {
        boolean moved = super.moveFigure(move);
        if (moved) {
            btService.write(move.toBytes());
        }
        return moved;
    }
}
