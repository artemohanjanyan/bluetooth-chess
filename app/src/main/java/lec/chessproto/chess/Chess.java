package lec.chessproto.chess;


import java.util.LinkedList;
import java.util.List;

public class Chess extends Game {



    int cfRow, cfColumn;
    List<Move> moves;

    public Chess(Figure[][] d, boolean turn, Player whitePlayer, Player blackPlayer) {
        super(d, turn, whitePlayer, blackPlayer);

        cfRow = -1;
        cfColumn = -1;

    }


    public Desk getDesk() {
        return desk;
    }

    synchronized List<Move> chooseFigure(Player player, int row, int column) {
        if (!(player == whitePlayer || player == blackPlayer) ||
                player.color ^ desk.turn ||
                desk.d[row][column] == null ||
                player.color ^ desk.d[row][column].getColor()) {
            return new LinkedList<>();
        }

        cfRow = row;
        cfColumn = column;
        moves = desk.d[row][column].getMoves(desk, row, column);

        //TODO :: check are moves valid

        if (listener != null) {
            listener.onFigureChosen(moves);
        }

        return moves;
    }

    synchronized boolean  moveFigure(Player player, Move move) {
        if (!(player == whitePlayer || player == blackPlayer)
                || player.color ^ desk.turn
                || moves == null
                ||!moves.contains(move)) {
            return false;
        }

        return super.moveFigure(player, move);
    }



}
