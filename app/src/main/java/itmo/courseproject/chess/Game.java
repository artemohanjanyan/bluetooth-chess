package itmo.courseproject.chess;


import java.util.List;

public abstract class Game {
    public static final boolean WHITE = false;
    public static final boolean BLACK = true;

    final Player whitePlayer;
    final Player blackPlayer;

    final Desk desk;

    Game(Figure[][] d, boolean turn, Player whitePlayer, Player blackPlayer) {

        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;

        desk = new Desk(this, d, turn);

        whitePlayer.game = this;
        whitePlayer.color = Chess.WHITE;

        blackPlayer.game = this;
        blackPlayer.color = Chess.BLACK;

        whitePlayer.onYourTurn();
    }

    public interface Listener {
        void onFigureChosen(List<Move> moves);

        void onMoveExecuted(Move move);

        void onGameOver(boolean winner);
    }

    Listener listener;

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    abstract List<Move> chooseFigure(Player player, int row, int column);

    synchronized boolean moveFigure(Player player, Move move) {
        if (player != whitePlayer && player != blackPlayer && desk.turn ^ player.color) {
            return false;
        }
        move.execute(desk);
        onMoveExecution(player, move);
        return true;
    }

    synchronized void onMoveExecution(Player player, Move move) {
        if (move.terminal) {
            ((player == whitePlayer) ? blackPlayer : whitePlayer).onYourTurn();
        }
        if (listener != null) {
            listener.onMoveExecuted(move);
        }
    }

    public static String getColorName(boolean color) {
        return color ? "black" : "white";
    }

    public Desk getDesk() {
        return desk;
    }

    void gameOver(boolean _winner) {
        Player winner = (_winner) ? blackPlayer : whitePlayer;
        Player loser = (_winner) ? whitePlayer : blackPlayer;
        winner.gameOver(true);
        loser.gameOver(false);
        if (listener != null) {
            listener.onGameOver(_winner);
        }
    }
}
