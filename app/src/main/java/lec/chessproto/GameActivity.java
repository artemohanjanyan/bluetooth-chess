package lec.chessproto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

import lec.chessproto.chess.Chess;
import lec.chessproto.chess.Desk;
import lec.chessproto.chess.Move;
import lec.chessproto.chess.Player;

public abstract class GameActivity extends AppCompatActivity  implements Chess.Listener {

    protected GameView gameView;
    protected Chess chess;

    protected Player whitePlayer;
    protected Player blackPlayer;

    protected abstract void init();
    protected abstract GameView.DeskListener getCurrentDeskListener(boolean color);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        init();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chess_view);
        gameView = (GameView) findViewById(R.id.chess);

        chess = new Chess(Desk.getClassicStartPosition(), Chess.WHITE, whitePlayer, blackPlayer);
        chess.setListener(this);
        gameView.desk = chess.getDesk();
        gameView.deskListener = getCurrentDeskListener(Chess.WHITE);
        gameView.invalidate();
    }

    @Override
    public void onFigureChosen(List<Move> moves) {
        gameView.markerMoves = moves;
        gameView.invalidate();
    }

    @Override
    public void onMoveExecuted() {
        gameView.markerMoves = null;
        gameView.deskListener = getCurrentDeskListener(chess.getDesk().getTurn());
        gameView.invalidate();
    }
}
