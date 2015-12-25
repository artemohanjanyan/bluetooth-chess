package itmo.courseproject;

import android.util.Log;

import itmo.courseproject.chess.Game;
import itmo.courseproject.chess.Player;


public class LocalPlayer extends Player {

    private final GameView gameView;

    private static final String TAG = "LocalPlayer";

    public LocalPlayer(GameView gameView) {
        this.gameView = gameView;
        this.gameView.localPlayer = this;
    }

    @Override
    public void onYourTurn() {
        gameView.localPlayer = this;
        Log.d(TAG, Game.getColorName(getColor()) + " player turn started");
    }

    @Override
    public void gameOver(boolean isWinner) {

    }
}
