package lec.chessproto.chess;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Chess extends Game {



    private List<MoveTree>[][] deskMoves;

    @SuppressWarnings("unchecked")
    public Chess(Figure[][] d, boolean turn, Player whitePlayer, Player blackPlayer) {
        super(d, turn, whitePlayer, blackPlayer);

        List<Move>[][] firstMoves = genMoves();
        deskMoves = new List[Desk.SIZE][Desk.SIZE];
        genDeskMoves(firstMoves);


    }

    private static class MoveTree {
        Move move;

        List<Move>[][] nextMoves;

        public MoveTree(Move move) {
            this.move = move;
        }

        public MoveTree(Move move, List<Move>[][] nextMoves) {
            this.move = move;
            this.nextMoves = nextMoves;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MoveTree moveTree = (MoveTree) o;

            return move.equals(moveTree.move);
        }
    }


    synchronized List<Move> chooseFigure(Player player, int row, int column) {
        List<MoveTree> moveTrees = deskMoves[row][column];
        if (moveTrees == null ||
                !(player == whitePlayer || player == blackPlayer) ||
                player.color ^ desk.turn ||
                desk.d[row][column] == null ||
                player.color ^ desk.d[row][column].getColor()) {
            return new ArrayList<>();
        }
        List<Move> moves = new ArrayList<>(moveTrees.size());

        for (MoveTree m : moveTrees) {
            moves.add(m.move);
        }

        if (listener != null) {
            listener.onFigureChosen(moves);
        }

        return moves;
    }

    synchronized boolean  moveFigure(Player player, Move move) {
        if (deskMoves[move.startRow][move.startColumn] == null) {
            return false;
        }
        int index = deskMoves[move.startRow][move.startColumn].indexOf(new MoveTree(move));
        if (!(player == whitePlayer || player == blackPlayer)
                || player.color ^ desk.turn
                || index == -1) {
            return false;
        }
        desk.executeMove(move);
        List<Move>[][] nextMoves = deskMoves[move.startRow][move.startColumn].get(index).nextMoves;
        boolean isMate = !genDeskMoves(nextMoves);

        onMoveExecution(player, move);

        if (isMate) {
            gameOver(!desk.turn);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private List<Move>[][] genMoves() {
        List<Move>[][] ret = new LinkedList[Desk.SIZE][Desk.SIZE];

        for (int i = 0; i < Desk.SIZE; i++) {
            for (int j = 0; j < Desk.SIZE; j++) {
                if (desk.d[i][j] != null  && desk.d[i][j].color == desk.turn) {
                    ret[i][j] = desk.d[i][j].getMoves(desk, i, j);
                }
            }
        }

        return ret;
    }

    private boolean isCorrect;

    private List<Move>[][] checkCorrect() {
        Figure king = desk.turn ? Figure.WHITE_KING : Figure.BLACK_KING;
        List<Move>[][] moves = genMoves();

        for (int i = 0; i < Desk.SIZE; i++) {
            for (int j = 0; j < Desk.SIZE; j++) {
                if (moves[i][j] == null) {
                    continue;
                }
                for (Move pmove : moves[i][j]) {
                    if (desk.d[pmove.endRow][pmove.endColumn] == king) {
                        isCorrect =  false;
                        return moves;
                    }
                }
            }
        }
        isCorrect =  true;
        return moves;
    }

    private boolean genDeskMoves(List<Move>[][] moves) {
        boolean hasMoves = false;
        for (int i = 0; i < Desk.SIZE; i++) {
            for (int j = 0; j < Desk.SIZE; j++) {
                if (moves[i][j] == null) {
                    deskMoves[i][j] = null;
                    continue;
                }
                deskMoves[i][j] = new ArrayList<>(moves[i][j].size());
                for (Move m : moves[i][j]) {
                    desk.executeMove(m);
                    List<Move>[][] generatedMoves = checkCorrect();
                    if (isCorrect) {
                        deskMoves[i][j].add(new MoveTree(m, generatedMoves));
                        hasMoves = true;
                    }
                    desk.undoMove();
                }
            }
        }
        return hasMoves;
    }

}
