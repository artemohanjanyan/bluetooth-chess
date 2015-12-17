package lec.chessproto;

import android.util.Log;

import java.util.List;

import lec.chessproto.chess.Game;
import lec.chessproto.chess.SimpleMove;
import lec.chessproto.chess.Figure;
import lec.chessproto.chess.Move;
import lec.chessproto.chess.Player;


public class LocalPlayer extends Player  {

    GameView gameView;

    private static final String TAG = "LocalPlayer";

    public LocalPlayer(GameView gameView) {
        this.gameView = gameView;
    }

    @Override
    public void onYourTurn() {
        gameView.localPlayer = this;
        Log.d(TAG, Game.getColorName(getColor()) + " player turn started");
    }
}
