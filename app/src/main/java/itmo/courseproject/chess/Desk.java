package itmo.courseproject.chess;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Desk {

    private class FieldChangesScope {
        final List<Point> fields;
        final Figure[] figures;
        final boolean turnChanged;

        public FieldChangesScope(List<Point> fields, boolean turnChanged) {
            this.fields = fields;
            this.turnChanged = turnChanged;
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
            if (turnChanged) {
                desk.switchTurn();
            }
        }
    }

    private final LinkedList<FieldChangesScope> undoScopes;
    private final LinkedList<FieldChangesScope> redoScopes;

    public static final int SIZE = 8;

    private static final Random rnd = new Random();

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

    public static Desk getRandomFisherStartPosition() {
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

        undoScopes = new LinkedList<>();
        redoScopes = new LinkedList<>();
    }

    public Figure getFigure(int row, int column) {
        return d[row][column];
    }

    private void switchTurn() {
        turn = !turn;
    }

    boolean executeMove(Move move) {
        redoScopes.clear();
        FieldChangesScope scope = new FieldChangesScope(move.getChangedFields(), move.terminal);
        undoScopes.add(scope);
        move.execute(this);
        if (move.terminal) {
            switchTurn();
        }
        return true;
    }

    public void undoMove() {
        moveFieldChangeScope(undoScopes, redoScopes);
    }

    public void redoMove() {
        moveFieldChangeScope(redoScopes, undoScopes);
    }

    public boolean hasRedoMoves() {
        return !redoScopes.isEmpty();
    }

    public boolean hasUndoMoves() {
        return !undoScopes.isEmpty();
    }

    public void redoAllMoves() {
        while (hasRedoMoves()) {
            redoMove();
        }
    }

    public void undoAllMoves() {
        while (hasUndoMoves()) {
            undoMove();
        }
    }

    private static void moveFieldChangeScope(LinkedList<FieldChangesScope> from, LinkedList<FieldChangesScope> to) {
        if (from.isEmpty()) {
            return;
        }
        FieldChangesScope scope = from.remove(from.size() - 1);
        scope.change();
        to.add(scope);
    }
}
