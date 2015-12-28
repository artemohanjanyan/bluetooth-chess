package itmo.courseproject;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import itmo.courseproject.chess.Chess;
import itmo.courseproject.chess.Desk;
import itmo.courseproject.chess.Game;
import itmo.courseproject.chess.Move;
import itmo.courseproject.chess.Player;

public abstract class GameActivity extends AppCompatActivity implements Chess.Listener {

    private static final String TAG = "GameActivity";

    protected GameView gameView;
    protected TextView gameOverText;

    protected ImageButton undoAllMovesButton, undoMoveButton, redoMoveButton, redoAllMovesButton;

    private Drawable
            undoAllMovesDrawable, undoAllMovesDisabledDrawable,
            undoMoveDrawable, undoMoveDisabledDrawable,
            redoAllMovesDrawable, redoAllMovesDisabledDrawable,
            redoMoveDrawable, redoMoveDisabledDrawable;

    protected Game game;

    protected Player whitePlayer;
    protected Player blackPlayer;

    static final int CHESS_CLASSIC = 0;
    static final int CHESS_960 = 1;
    static final int CHESS_TEST = -1;
    static final String GAME = "game";

    protected Intent initialIntent;

    protected abstract void initPlayers();

    protected Desk desk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        initialIntent = getIntent();
        int gameId = initialIntent.getIntExtra(GAME, CHESS_CLASSIC);
        setContentView(R.layout.activity_chess_view);

        undoAllMovesButton = (ImageButton) findViewById(R.id.undo_all_moves_button);
        redoAllMovesButton = (ImageButton) findViewById(R.id.redo_all_moves_button);

        undoMoveButton = (ImageButton) findViewById(R.id.undo_move_button);
        redoMoveButton = (ImageButton) findViewById(R.id.redo_move_button);

        Resources res = getResources();
        redoMoveDrawable = res.getDrawable(R.drawable.ic_redo_move);
        undoMoveDrawable = res.getDrawable(R.drawable.ic_undo_move);
        redoMoveDisabledDrawable = res.getDrawable(R.drawable.ic_redo_move_disabled);
        undoMoveDisabledDrawable = res.getDrawable(R.drawable.ic_undo_move_disabled);

        redoAllMovesDrawable = res.getDrawable(R.drawable.ic_redo_all_moves);
        undoAllMovesDrawable = res.getDrawable(R.drawable.ic_undo_all_moves);
        redoAllMovesDisabledDrawable = res.getDrawable(R.drawable.ic_redo_all_moves_disabled);
        undoAllMovesDisabledDrawable = res.getDrawable(R.drawable.ic_undo_all_moves_disabled);


        gameView = (GameView) findViewById(R.id.chess);
        gameOverText = (TextView) findViewById(R.id.game_over_text);

        switch (gameId) {
            case CHESS_CLASSIC:
                desk = Desk.getClassicStartPosition();
                break;
            case CHESS_960:
                desk = Desk.getRandomFisherStartPosition();
                break;
            case CHESS_TEST:
                desk = Desk.getTestPosition();
                break;
        }

        undoAllMovesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desk.undoAllMoves();
                updateViewsState();
            }
        });

        redoAllMovesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desk.redoAllMoves();
                updateViewsState();
            }
        });

        undoMoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desk.undoMove();
                updateViewsState();
            }
        });

        redoMoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desk.redoMove();
                updateViewsState();
            }
        });

        updateViewsState();
        initPlayers();
    }

    public void updateViewsState() {
        setUndoEnabled(desk.hasUndoMoves());
        setRedoEnabled(desk.hasRedoMoves());
        gameView.deskStateChanged();
    }

    public void setUndoEnabled(boolean undoEnabled) {
        undoAllMovesButton.setClickable(undoEnabled);
        undoAllMovesButton.setImageDrawable(undoEnabled ? undoAllMovesDrawable : undoAllMovesDisabledDrawable);

        undoMoveButton.setClickable(undoEnabled);
        undoMoveButton.setImageDrawable(undoEnabled ? undoMoveDrawable : undoMoveDisabledDrawable);
    }

    public void setRedoEnabled(boolean redoEnabled) {
        redoAllMovesButton.setClickable(redoEnabled);
        redoAllMovesButton.setImageDrawable(redoEnabled ? redoAllMovesDrawable : redoAllMovesDisabledDrawable);

        redoMoveButton.setClickable(redoEnabled);
        redoMoveButton.setImageDrawable(redoEnabled ? redoMoveDrawable : redoMoveDisabledDrawable);
    }

    @Override
    public void onFigureChosen(List<Move> moves) {
        gameView.markerMoves = moves;
        gameView.invalidate();
    }

    @Override
    public void onMoveExecuted(Move move) {
        updateViewsState();
        gameView.showMove(move);
    }

    protected void onPlayersInitialized() {
        game = new Chess(desk, whitePlayer, blackPlayer);

        game.setListener(this);
        desk = game.getDesk();
        gameView.desk = desk;
    }

    @Override
    public void onGameOver(int gameOverMsg) {
        String str = "Game over : " + (
                gameOverMsg == Game.DRAW ?
                        "draw" :
                        Game.getColorName(gameOverMsg == Game.BLACK_PLAYER_WINS) + " wins"
        );
        gameOverText.setText(str);
        Log.d(TAG, str);
    }
}
