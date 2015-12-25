package itmo.courseproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

import itmo.courseproject.chess.Chess;
import itmo.courseproject.chess.Desk;
import itmo.courseproject.chess.Figure;
import itmo.courseproject.chess.Game;
import itmo.courseproject.chess.Move;
import itmo.courseproject.chess.Player;

public abstract class GameActivity extends AppCompatActivity implements Chess.Listener {

    private static final String TAG = "GameActivity";

    protected GameView gameView;
    protected TextView gameOverText;

    protected Game game;

    protected Player whitePlayer;
    protected Player blackPlayer;

    private static final int CHESS_CLS = 0;
    private static final int CHESS_960 = 1;
    static final String GAME = "game";

    protected Intent initialIntent;

    protected abstract void initPlayers();

    private Figure[][] f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        initialIntent = getIntent();
        int gameId = initialIntent.getIntExtra(GAME, CHESS_CLS);
        setContentView(R.layout.activity_chess_view);
        gameView = (GameView) findViewById(R.id.chess);
        gameOverText = (TextView) findViewById(R.id.game_over_text);

        switch (gameId) {
            case CHESS_CLS:
                f = Desk.getClassicStartPosition();
                break;
            case CHESS_960:
                f = Desk.getRandomFisherStartPosition();
                break;
            default:
                throw new RuntimeException("this game doesn't supported");
        }

        initPlayers();

    }

    @Override
    public void onFigureChosen(List<Move> moves) {
        gameView.markerMoves = moves;
        gameView.invalidate();
    }

    @Override
    public void onMoveExecuted(Move move) {
        gameView.markerMoves = null;
        gameView.invalidate();
    }

    protected void onPlayersInitialized() {
        game = new Chess(f, Chess.WHITE, whitePlayer, blackPlayer);

        game.setListener(this);
        gameView.desk = game.getDesk();
    }

    @Override
    public void onGameOver(boolean winner) {
        String str = "Game over : " + Game.getColorName(winner) + " wins";
        gameOverText.setText(str);
        Log.d(TAG, str);
    }
}
