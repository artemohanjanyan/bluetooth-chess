package itmo.courseproject.chess;


public class SimpleMove extends Move {

    static final byte MOVE_ID;
    static private final MoveFactory f = new MoveFactory() {
        @Override
        public Move create(byte... bytes) {
            Point s = byteToField(bytes[0]);
            Point f = byteToField(bytes[1]);
            return new SimpleMove(s.row, s.column, f.row, f.column);
        }
    };

    static {
        MOVE_ID = (byte) factoryMap.size();
        factoryMap.add(f);
    }

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
        return new byte[]{MOVE_ID, fieldToByte(startRow, startColumn), fieldToByte(endRow, endColumn)};
    }
}
