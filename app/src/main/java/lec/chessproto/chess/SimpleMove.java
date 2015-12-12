package lec.chessproto.chess;


public class SimpleMove extends Move {

    public SimpleMove(int startRow, int startColumn, int endRow, int endColumn) {
        super(startRow, startColumn, endRow, endColumn);
    }

    @Override
    public void execute(Desk desk) {
        Figure f = desk.d[startRow][startColumn];
        desk.d[startRow][startColumn] = null;
        desk.d[endRow][endColumn] = f;
    }
}
