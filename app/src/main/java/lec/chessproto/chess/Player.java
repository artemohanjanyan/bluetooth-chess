package lec.chessproto.chess;

import java.util.List;

public abstract class Player {

    Game game;
    boolean color;

    public Game getGame() {
        return game;
    }

    public boolean getColor() {
        return color;
    }

    public List<Move> chooseFigure(int row, int column) {
        return game.chooseFigure(this, row, column);
    }

    public boolean moveFigure(Move move) {
        return game.moveFigure(this, move);
    }


}
