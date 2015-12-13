package lec.chessproto.chess;


import java.util.List;

public abstract class Game {
    public static final boolean WHITE = false;
    public static final boolean BLACK = true;

    Player whitePlayer, blackPlayer;

    Desk desk;

    public Game(Figure[][] d, boolean turn, Player whitePlayer, Player blackPlayer) {

        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;

        desk = new Desk(this, d, turn);

        whitePlayer.game = this;
        whitePlayer.color = Chess.WHITE;

        blackPlayer.game = this;
        blackPlayer.color = Chess.BLACK;
    }

    public interface Listener {
        void onFigureChosen(List<Move> moves);
        void onMoveExecuted();
    }

    protected Listener listener;

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    abstract List<Move> chooseFigure(Player player, int row, int column);

    synchronized boolean  moveFigure(Player player, Move move) {
        move.execute(desk);
        if (move.terminal) {
            desk.nextTurn();
        }

        if (listener != null) {
            listener.onMoveExecuted();
        }

        return true;
    }

}