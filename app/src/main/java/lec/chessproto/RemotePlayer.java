package lec.chessproto;

import lec.chessproto.chess.Move;
import lec.chessproto.chess.Player;

public class RemotePlayer extends Player implements  BluetoothService.OnMessageReceivedListener {

    BluetoothService btService;

    public RemotePlayer(BluetoothService btService) {
        this.btService = btService;
        btService.setOnMessageReceivedListener(this);
    }

    @Override
    public void process(int bytes, byte[] buffer) {
        moveFigure(Move.getMove(bytes, buffer));
    }

    @Override
    public void onYourTurn() {

    }

    @Override
    public void gameOver(boolean isWinner) {
    }
}
