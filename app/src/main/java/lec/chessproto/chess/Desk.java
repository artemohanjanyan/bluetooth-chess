package lec.chessproto.chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Desk {

    private class FieldChangesScope {
        List<Point> fields;
        Figure[] figures;

        public FieldChangesScope(List<Point> fields) {
            this.fields = fields;
            figures = new Figure[fields.size()];
            for (int i = 0; i < fields.size(); i++) {
                Point field = fields.get(i);
                figures[i] = Desk.this.d[field.row][field.column];
            }
        }

        void change() {
            Figure[][] f = Desk.this.d;
            int i = 0;
            while (i < fields.size()) {
                Point field = fields.get(i);
                Figure figure = f [field.row][field.column];
                f[field.row][field.column] = figures[i];
                figures[i] = figure;
                i++;
            }
        }
    }

    ArrayList<FieldChangesScope> undoScopes, redoScopes;

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

    Desk getMoveExecutedDesk(Move move) {
        Figure[][] fCopy = new Figure[Desk.SIZE][Desk.SIZE];

        for (int i = 0; i < Desk.SIZE; i++) {
            System.arraycopy(d[i], 0, fCopy[i], 0, Desk.SIZE);
        }
        Desk ret = new Desk(game, fCopy, turn);
        ret.executeMove(move);

        return ret;
    }

    boolean executeMove(Move move) {
        FieldChangesScope scope = new FieldChangesScope(move.getChangedFields());
        undoScopes.add(scope);
        move.execute(this);
        if (move.terminal) {
            nextTurn();
        }
        return true;
    }

    boolean undoMove() {
        return moveFieldChangeScope(undoScopes, redoScopes);
    }

    boolean redoMove() {
        return moveFieldChangeScope(undoScopes, redoScopes);
    }


    boolean moveFieldChangeScope(ArrayList<FieldChangesScope> from, ArrayList<FieldChangesScope> to) {
        if (from.isEmpty()) {
            return false;
        }
        FieldChangesScope scope = from.remove(from.size() - 1);
        scope.change();
        to.add(scope);
        return true;
    }


}
