package lec.chessproto;

import lec.chessproto.chess.Move;

public class BtLocalPlayer extends LocalPlayer {

    private BluetoothService btService;

    public BtLocalPlayer(GameView gameView, BluetoothService btServive) {
        super(gameView);
        this.btService = btServive;
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
