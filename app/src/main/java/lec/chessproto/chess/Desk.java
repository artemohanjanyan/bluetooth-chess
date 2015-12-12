package lec.chessproto.chess;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class Desk {

    public static final int SIZE = 8;

    private static final Random rnd = new Random();

    public static Figure[][] getClassicStartPosition() {
        return new Figure[][] {
                new Figure[]{Figure.WHITE_ROOK, Figure.WHITE_KNIGHT, Figure.WHITE_BISHOP, Figure.WHITE_QUEEN, Figure.WHITE_KING, Figure.WHITE_BISHOP, Figure.WHITE_KNIGHT, Figure.WHITE_ROOK},
                new Figure[]{Figure.WHITE_PAWN, Figure.WHITE_PAWN, Figure.WHITE_PAWN, Figure.WHITE_PAWN, Figure.WHITE_PAWN, Figure.WHITE_PAWN, Figure.WHITE_PAWN, Figure.WHITE_PAWN},
                new Figure[]{null, null, null, null, null, null, null, null},
                new Figure[]{null, null, null, null, null, null, null, null},
                new Figure[]{null, null, null, null, null, null, null, null},
                new Figure[]{null, null, null, null, null, null, null, null},
                new Figure[]{Figure.BLACK_PAWN, Figure.BLACK_PAWN, Figure.BLACK_PAWN, Figure.BLACK_PAWN, Figure.BLACK_PAWN, Figure.BLACK_PAWN, Figure.BLACK_PAWN, Figure.BLACK_PAWN},
                new Figure[]{Figure.BLACK_ROOK, Figure.BLACK_KNIGHT, Figure.BLACK_BISHOP, Figure.BLACK_QUEEN, Figure.BLACK_KING, Figure.BLACK_BISHOP, Figure.BLACK_KNIGHT, Figure.BLACK_ROOK}
        };
    }

    public static Figure[][] getRandomFisherStartPosition() {
        Figure[][] d = new Figure[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            d[1][i] = Figure.WHITE_PAWN;
            d[6][i] = Figure.BLACK_PAWN;
        }

        boolean[] plased = new boolean[8];
        int field;

        field = rnd.nextInt(4) * 2 + 1;     // first bishop position
        plased[field] = true;
        d[0][field] = Figure.WHITE_BISHOP;
        d[7][field] = Figure.BLACK_BISHOP;

        field = rnd.nextInt(4) * 2;         // second bishop position
        plased[field] = true;
        d[0][field] = Figure.WHITE_BISHOP;
        d[7][field] = Figure.BLACK_BISHOP;

        field = rnd.nextInt(6);             // queen position
        for (int i = 0; i <= field; i++)   if (plased[i]) field++;
        plased[field] = true;
        d[0][field] = Figure.WHITE_QUEEN;
        d[7][field] = Figure.BLACK_QUEEN;

        field = rnd.nextInt(5);             // first knight position
        for (int i = 0; i <= field; i++)  if (plased[i]) field++;
        plased[field] = true;
        d[0][field] = Figure.WHITE_KNIGHT;
        d[7][field] = Figure.BLACK_KNIGHT;

        field = rnd.nextInt(4);             // second knight position
        for (int i = 0; i <= field; i++)  if (plased[i]) field++;
        plased[field] = true;
        d[0][field] = Figure.WHITE_KNIGHT;
        d[7][field] = Figure.BLACK_KNIGHT;

        field = 0;

        while (plased[field]) field++;      // first rook position
        plased[field] = true;
        d[0][field] = Figure.WHITE_ROOK;
        d[7][field] = Figure.BLACK_ROOK;

        while (plased[field]) field++;      // king position
        plased[field] = true;
        d[0][field] = Figure.WHITE_KING;
        d[7][field] = Figure.BLACK_KING;

        while (plased[field]) field++;      // second rook position
        plased[field] = true;
        d[0][field] = Figure.WHITE_ROOK;
        d[7][field] = Figure.BLACK_ROOK;

        return d;
    }

    Figure[][] d;
    Game game;
    boolean turn;       // false - white player, false - black.

    public Desk(Game game, Figure[][] d, boolean turn) {
        this.game = game;
        this.d = d;
        this.turn = turn;
    }

    public Figure getFigure(int row, int column) {
        return d[row][column];
    }

    public boolean getTurn() {
        return turn;
    }

    void nextTurn() {
        turn = !turn;
    }
}
