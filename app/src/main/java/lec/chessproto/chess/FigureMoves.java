package lec.chessproto.chess;


import java.util.LinkedList;

public class FigureMoves {

    Figure figure;

    public LinkedList<Move> moves;

    public FigureMoves(Figure figure) {
        this.figure = figure;

        moves = new LinkedList<>();
    }

    void add(Move move) {
        moves.add(move);
    }
}
