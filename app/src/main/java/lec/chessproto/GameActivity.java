package lec.chessproto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

import lec.chessproto.chess.Chess;
import lec.chessproto.chess.Desk;
import lec.chessproto.chess.Figure;
import lec.chessproto.chess.Game;
import lec.chessproto.chess.Move;
import lec.chessproto.chess.Player;

public class GameActivity extends AppCompatActivity  implements Chess.Listener {

    protected GameView gameView;
    protected Game game;

    protected Player whitePlayer;
    protected Player blackPlayer;

    public static final int LOCALGAME = 0;
    public static final int BLUETOOTH = 1;
    public static final String TYPE = "type";
    private int type;

    public static final int CHESS_CLS = 0;
    public static final int CHESS_960 = 1;
    public static final String GAME = "game";
    private int gameid;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int type   = intent.getIntExtra(TYPE, LOCALGAME);
        int gameid = intent.getIntExtra(GAME, CHESS_CLS);

        setContentView(R.layout.activity_chess_view);
        gameView = (GameView) findViewById(R.id.chess);
        if (type == LOCALGAME) {
            whitePlayer = new LocalPlayer(gameView);
            blackPlayer = new LocalPlayer(gameView);
        }
        Figure[][] f;
        switch (gameid) {
            case CHESS_CLS : f = Desk.getClassicStartPosition(); break;
            case CHESS_960: f = Desk.getRandomFisherStartPosition(); break;
            default: throw new RuntimeException("this game doesn't supported");
        }
        game = new Chess(f, Chess.WHITE, whitePlayer, blackPlayer);

        game.setListener(this);
        gameView.desk = game.getDesk();
    }

    @Override
    public void onFigureChosen(List<Move> moves) {
        gameView.markerMoves = moves;
        gameView.invalidate();
    }

    @Override
    public void onMoveExecuted() {
        gameView.markerMoves = null;
        gameView.invalidate();
    }

}
