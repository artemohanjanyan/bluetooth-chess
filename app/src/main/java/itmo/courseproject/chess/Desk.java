package itmo.courseproject.chess;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class Desk {

    public String lastMoveNotation;


    private class FieldChangesScope {
        final Move move;
        final List<Point> fields;
        final Figure[] figures;

        public FieldChangesScope(Move move) {
            this.move = move;
            this.fields = move.getChangedFields();
            figures = new Figure[fields.size()];
            for (int i = 0; i < fields.size(); i++) {
                Point field = fields.get(i);
                figures[i] = Desk.this.d[field.row][field.column];
            }
        }

        void change() {
            Desk desk = Desk.this;
            int i = 0;
            while (i < fields.size()) {
                Point field = fields.get(i);
                Figure figure = desk.d[field.row][field.column];
                desk.d[field.row][field.column] = figures[i];
                figures[i] = figure;
                i++;
            }
            if (move.terminal) {
                desk.switchTurn();
            }
        }
    }

    private final LinkedList<FieldChangesScope> moveScopes;
    private ListIterator<FieldChangesScope> iterator;

    public static final int SIZE = 8;

    public static Desk getClassicStartPosition() {
        return new Desk(new Figure[][]{
                new Figure[]{Figure.WHITE_ROOK, Figure.WHITE_KNIGHT, Figure.WHITE_BISHOP, Figure.WHITE_QUEEN, Figure.WHITE_KING, Figure.WHITE_BISHOP, Figure.WHITE_KNIGHT, Figure.WHITE_ROOK},
                new Figure[]{Figure.WHITE_PAWN, Figure.WHITE_PAWN, Figure.WHITE_PAWN, Figure.WHITE_PAWN, Figure.WHITE_PAWN, Figure.WHITE_PAWN, Figure.WHITE_PAWN, Figure.WHITE_PAWN},
                new Figure[]{null, null, null, null, null, null, null, null},
                new Figure[]{null, null, null, null, null, null, null, null},
                new Figure[]{null, null, null, null, null, null, null, null},
                new Figure[]{null, null, null, null, null, null, null, null},
                new Figure[]{Figure.BLACK_PAWN, Figure.BLACK_PAWN, Figure.BLACK_PAWN, Figure.BLACK_PAWN, Figure.BLACK_PAWN, Figure.BLACK_PAWN, Figure.BLACK_PAWN, Figure.BLACK_PAWN},
                new Figure[]{Figure.BLACK_ROOK, Figure.BLACK_KNIGHT, Figure.BLACK_BISHOP, Figure.BLACK_QUEEN, Figure.BLACK_KING, Figure.BLACK_BISHOP, Figure.BLACK_KNIGHT, Figure.BLACK_ROOK}
        },
                Chess.WHITE);
    }

    public static Desk getRandomFisherStartPosition(long seed) {
        Random rnd = new Random(seed);

        Figure[][] d = new Figure[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            d[1][i] = Figure.WHITE_PAWN;
            d[6][i] = Figure.BLACK_PAWN;
        }

        boolean[] placed = new boolean[8];
        int field;

        field = rnd.nextInt(4) * 2 + 1;     // first bishop position
        placed[field] = true;
        d[0][field] = Figure.WHITE_BISHOP;
        d[7][field] = Figure.BLACK_BISHOP;

        field = rnd.nextInt(4) * 2;         // second bishop position
        placed[field] = true;
        d[0][field] = Figure.WHITE_BISHOP;
        d[7][field] = Figure.BLACK_BISHOP;

        field = rnd.nextInt(6);             // queen position
        for (int i = 0; i <= field; i++) if (placed[i]) field++;
        placed[field] = true;
        d[0][field] = Figure.WHITE_QUEEN;
        d[7][field] = Figure.BLACK_QUEEN;

        field = rnd.nextInt(5);             // first knight position
        for (int i = 0; i <= field; i++) if (placed[i]) field++;
        placed[field] = true;
        d[0][field] = Figure.WHITE_KNIGHT;
        d[7][field] = Figure.BLACK_KNIGHT;

        field = rnd.nextInt(4);             // second knight position
        for (int i = 0; i <= field; i++) if (placed[i]) field++;
        placed[field] = true;
        d[0][field] = Figure.WHITE_KNIGHT;
        d[7][field] = Figure.BLACK_KNIGHT;

        field = 0;

        while (placed[field]) field++;      // first rook position
        placed[field] = true;
        d[0][field] = Figure.WHITE_ROOK;
        d[7][field] = Figure.BLACK_ROOK;

        while (placed[field]) field++;      // king position
        placed[field] = true;
        d[0][field] = Figure.WHITE_KING;
        d[7][field] = Figure.BLACK_KING;

        while (placed[field]) field++;      // second rook position
        placed[field] = true;
        d[0][field] = Figure.WHITE_ROOK;
        d[7][field] = Figure.BLACK_ROOK;

        return new Desk(d, Game.WHITE);
    }

    public static Desk getTestPosition() {
        Figure[][] d = new Figure[SIZE][SIZE];
        d[0][2] = Figure.WHITE_KING;
        d[0][0] = Figure.BLACK_KING;
        d[7][1] = Figure.WHITE_QUEEN;
        return new Desk(d, Game.WHITE);
    }

    final Figure[][] d;
    Game game;
    boolean turn;       // false - white player, false - black.

    public Desk(Figure[][] d, boolean turn) {
        this(d, turn, null);
    }

    public Desk(Figure[][] d, boolean turn, Game game) {
        this.game = game;
        this.d = d;
        this.turn = turn;

        moveScopes = new LinkedList<>();
        iterator = moveScopes.listIterator();
    }

    public Figure getFigure(int row, int column) {
        return d[row][column];
    }

    public boolean getTurn() {
        return turn;
    }

    private void switchTurn() {
        turn = !turn;
    }

    void executeMove(Move move) {
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }

        FieldChangesScope scope = new FieldChangesScope(move);
        lastMoveNotation = move.getNotation(this);
        iterator.add(scope);
        move.execute(this);
        if (move.terminal) {
            switchTurn();
        }
    }

    public void undoMove() {
        if (!iterator.hasPrevious()) {
            return;
        }
        iterator.previous().change();
    }

    public void redoMove() {
        if (!iterator.hasNext()) {
            return;
        }
        FieldChangesScope scope = iterator.next();
        lastMoveNotation = scope.move.getNotation(this);
        scope.change();
    }


    public boolean hasRedoMoves() {
        return iterator.hasNext();
    }

    public boolean hasUndoMoves() {
        return iterator.hasPrevious();
    }

    public void redoAllMoves() {
        while (iterator.hasNext()) {
            iterator.next().change();
        }
    }

    public void undoAllMoves() {
        while (iterator.hasPrevious()) {
            iterator.previous().change();
        }
    }

    public void gotoMove(int i) {
        if (i == iterator.previousIndex()) {
            return;
        }
        if (i < iterator.previousIndex()) {
            while(iterator.previousIndex() != i) {
                iterator.previous().change();
            }
        } else {
            while (iterator.previousIndex() != i) {
                iterator.next().change();
            }
        }
    }


    public int getMoveCount() {
        return moveScopes.size();
    }
    public int getCurrentIndex() { return iterator.previousIndex(); }
}
