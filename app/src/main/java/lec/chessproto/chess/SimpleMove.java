package lec.chessproto.chess;


public class SimpleMove extends Move {

    public static final byte MOVE_ID = 1;

    public SimpleMove(int startRow, int startColumn, int endRow, int endColumn) {
        super(startRow, startColumn, endRow, endColumn);
    }

    @Override
    public void execute(Desk desk) {
        Figure f = desk.d[startRow][startColumn];
        desk.d[startRow][startColumn] = null;
        desk.d[endRow][endColumn] = f;
    }



    @Override
    public byte[] toBytes() {
        return new byte[]{MOVE_ID ,fieldToByte(startRow, startColumn), fieldToByte(endRow, endColumn)};
    }
}
