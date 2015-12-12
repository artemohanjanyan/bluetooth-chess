package lec.chessproto;

import java.util.List;

import lec.chessproto.chess.SimpleMove;
import lec.chessproto.chess.Figure;
import lec.chessproto.chess.Move;
import lec.chessproto.chess.Player;


public class LocalPlayer extends Player implements GameView.DeskListener {

    boolean selected = false;
    int srow, scolumn;

    @Override
    public Figure onFieldDrag(int row, int column) {
        return null;
    }

    @Override
    public boolean onFiledSelect(int row, int column) {
        if (!selected) {
            tryChooseFigure(row, column);
        } else {
            boolean moved = moveFigure(new SimpleMove(srow, scolumn, row, column));
            if (moved) {
                selected = false;
            } else {
                tryChooseFigure(row, column);
            }
        }
        return selected;
    }

    private void tryChooseFigure(int row, int column) {
        srow = row;
        scolumn = column;
        List<Move> moves = chooseFigure(row, column);
        selected = moves != null;
    }

    @Override
    public void onFieldDrop(int row, int column) {

    }

}
