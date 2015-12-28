package itmo.courseproject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

import itmo.courseproject.chess.Desk;
import itmo.courseproject.chess.Figure;
import itmo.courseproject.chess.Move;
import itmo.courseproject.chess.SimpleMove;


public class GameView extends View {

    private static final String TAG = "GameView";
    private static final List<Move> EMPTY_MOVE_LIST = new LinkedList<>();

    private static final int FIGURES_COUNT = Figure.figures.size();

    public static final double DRG_SCALE = 1.5;

    public Desk desk;

    private Paint whiteFieldPaint, blackFieldPaint, whiteFieldPressedPaint, blackFieldPressedPaint, figurePaint;
    private int fieldSize, boardSize;
    private Rect dstRect, srcRect, drgRect;

    private Bitmap[] figureBitmapMap;


    private boolean selected, dragged, touchDowned;
    private int sRow = -1, sColumn = -1; // selected field position
    private boolean moveShown;
    private Move shownMove;

    private Resources res;
    private Resources.Theme theme;

    public List<Move> markerMoves;

    LocalPlayer localPlayer;


    public GameView(Context context) {
        super(context);

        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }


    private void init() {

        whiteFieldPaint = new Paint();
        blackFieldPaint = new Paint();
        whiteFieldPressedPaint = new Paint();
        blackFieldPressedPaint = new Paint();
        figurePaint = new Paint();

        res = getResources();
        theme = res.newTheme();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            whiteFieldPaint.setColor(res.getColor(R.color.whiteField, theme));
            blackFieldPaint.setColor(res.getColor(R.color.blackField, theme));
            whiteFieldPressedPaint.setColor(res.getColor(R.color.whiteFieldPressed, theme));
            blackFieldPressedPaint.setColor(res.getColor(R.color.blackFieldPressed, theme));
        } else {
            whiteFieldPaint.setColor(res.getColor(R.color.whiteField));
            blackFieldPaint.setColor(res.getColor(R.color.blackField));
            whiteFieldPressedPaint.setColor(res.getColor(R.color.whiteFieldPressed));
            blackFieldPressedPaint.setColor(res.getColor(R.color.blackFieldPressed));
        }
        figureBitmapMap = new Bitmap[FIGURES_COUNT];
        markerMoves = EMPTY_MOVE_LIST;

        loadFigureBitmap(Figure.WHITE_PAWN.getID(), R.drawable.white_pawn);
        loadFigureBitmap(Figure.WHITE_KNIGHT.getID(), R.drawable.white_knight);
        loadFigureBitmap(Figure.WHITE_BISHOP.getID(), R.drawable.white_bishop);
        loadFigureBitmap(Figure.WHITE_ROOK.getID(), R.drawable.white_rook);
        loadFigureBitmap(Figure.WHITE_QUEEN.getID(), R.drawable.white_queen);
        loadFigureBitmap(Figure.WHITE_KING.getID(), R.drawable.white_king);
        loadFigureBitmap(Figure.BLACK_PAWN.getID(), R.drawable.black_pawn);
        loadFigureBitmap(Figure.BLACK_KNIGHT.getID(), R.drawable.black_knight);
        loadFigureBitmap(Figure.BLACK_BISHOP.getID(), R.drawable.black_bishop);
        loadFigureBitmap(Figure.BLACK_ROOK.getID(), R.drawable.black_rook);
        loadFigureBitmap(Figure.BLACK_QUEEN.getID(), R.drawable.black_queen);
        loadFigureBitmap(Figure.BLACK_KING.getID(), R.drawable.black_king);

        int figureBitmapSize = figureBitmapMap[0].getWidth();
        srcRect = new Rect(0, 0, figureBitmapSize, figureBitmapSize);
        dstRect = new Rect();
        drgRect = new Rect();
    }

    private void loadFigureBitmap(int k, int id) {
        BitmapDrawable drawable = (BitmapDrawable)
                ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        ? res.getDrawable(id, theme)
                        : res.getDrawable(id));
        if (drawable != null)
            figureBitmapMap[k] = drawable.getBitmap();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        boardSize = Math.min(width, height);
        fieldSize = boardSize / Desk.SIZE;
        boardSize = fieldSize * Desk.SIZE;

        drgRect.set(0, 0, 2 * (int) (DRG_SCALE * fieldSize), 2 * (int) (DRG_SCALE * fieldSize));

        int measureSpec = MeasureSpec.makeMeasureSpec(boardSize, MeasureSpec.EXACTLY);
        super.onMeasure(measureSpec, measureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < Desk.SIZE; i++) {
            for (int j = 0; j < Desk.SIZE; j++) {
                drawField(canvas, i, j, getFieldPaint(i, j));
            }
        }

        if (isInEditMode())
            return;

        if (sRow != -1 && sColumn != -1) {
            drawField(canvas, sRow, sColumn, getFieldPressedPaint(sRow, sColumn));
        }

        if (moveShown) {
            drawField(canvas, shownMove.startRow, shownMove.startColumn, getFieldPressedPaint(shownMove.startRow, shownMove.startColumn));
            drawField(canvas, shownMove.endRow, shownMove.endColumn, getFieldPressedPaint(shownMove.endRow, shownMove.endColumn));
        } else {
            if (markerMoves != null) {
                for (Move m : markerMoves) {
                    int row = m.endRow, column = m.endColumn;
                    drawField(canvas, row, column, getFieldPressedPaint(row, column));
                }
            }
        }


        if (desk == null)
            return;

        dstRect.set(0, 0, fieldSize, fieldSize);

        if (localPlayer.getColor()) {
            for (int i = 0; i < Desk.SIZE; i++) {
                for (int j = Desk.SIZE - 1; j >= 0; j--) {
                    drawFigure(canvas, i, j);
                    dstRect.offset(fieldSize, 0);
                }
                dstRect.offset(-boardSize, fieldSize);
            }
        } else {
            for (int i = Desk.SIZE - 1; i >= 0; i--) {
                for (int j = 0; j < Desk.SIZE; j++) {
                    drawFigure(canvas, i, j);
                    dstRect.offset(fieldSize, 0);
                }
                dstRect.offset(-boardSize, fieldSize);
            }
        }
        if (dragged) {
            canvas.drawBitmap(figureBitmapMap[desk.getFigure(sRow, sColumn).getID()], srcRect, drgRect, figurePaint);
        }
    }

    private void drawFigure(Canvas canvas, int i, int j) {
        Figure figure = desk.getFigure(i, j);
        if (figure != null && !(dragged && i == sRow && j == sColumn)) {
            Bitmap bitmap = figureBitmapMap[figure.getID()];
            canvas.drawBitmap(bitmap, srcRect, dstRect, figurePaint);
        }
    }

    private Paint getFieldPressedPaint(int row, int column) {
        return (row + column) % 2 == 0 ? blackFieldPressedPaint : whiteFieldPressedPaint;
    }

    private Paint getFieldPaint(int row, int column) {
        return (row + column) % 2 == 0 ? blackFieldPaint : whiteFieldPaint;
    }

    private void drawField(Canvas canvas, int row, int column, Paint paint) {
        if (localPlayer != null && localPlayer.getColor()) {
            row = Desk.SIZE - row - 1;
            column = Desk.SIZE - column - 1;
        }
        canvas.drawRect(
                fieldSize * column,
                fieldSize * (Desk.SIZE - row - 1),
                fieldSize * (column + 1),
                fieldSize * (Desk.SIZE - row),
                paint
        );
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        Log.d(TAG, "touch event triggered with params" +
                        ": x=" + Integer.toString(x) +
                        ", y=" + Integer.toString(y) +
                        ", action=" + ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) ?
                        MotionEvent.actionToString(event.getAction()) :
                        Integer.toString(event.getAction()))
        );

        int column = localPlayer.getColor() ? Desk.SIZE - x / fieldSize - 1 : x / fieldSize;
        int row = localPlayer.getColor() ?  y / fieldSize : Desk.SIZE - y / fieldSize - 1;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x < 0 || y < 0 || x >= boardSize || y >= boardSize) {
                    return false;
                }
                if (desk.getFigure(row, column) != null && desk.getFigure(row, column).getColor() == localPlayer.getColor()) {
                    trySelect(row, column);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (selected) {
                    if (touchDowned) {
                        touchDowned = false;
                        dragged = true;
                    }
                    if (dragged) {
                        updateDragPos(x, y);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (x < 0 || y < 0 || x >= boardSize || y >= boardSize) {
                    dragged = false;
                    return false;
                }
                if (selected) {
                    boolean moved = (!desk.hasRedoMoves()) && localPlayer.moveFigure(new SimpleMove(sRow, sColumn, row, column));
                    touchDowned = false;
                    dragged = false;
                    if (moved) {
                        deselect();
                    }
                }
                break;
            default:
                return super.onTouchEvent(event);
        }
        invalidate();
        return true;
    }

    private void trySelect(int row, int column) {
        if (desk.getFigure(row, column) != null) {
            if (!desk.hasRedoMoves()) {
                markerMoves = localPlayer.chooseFigure(row, column);
                if (!markerMoves.isEmpty()) {
                    moveShown = false;
                }
            }
            selected = true;
            sRow = row;
            sColumn = column;
            touchDowned = true;
        }
    }

    private void deselect() {
        selected = false;
        markerMoves = EMPTY_MOVE_LIST;
        sRow = -1;
        sColumn = -1;
    }



    private void updateDragPos(int x, int y) {
        drgRect.offsetTo(x - (int)(DRG_SCALE * fieldSize), y - (int)((1.5 * DRG_SCALE) * fieldSize));
    }

    public void showMove(Move move) {
        moveShown  = true;
        shownMove = move;
        invalidate();
    }

    public void deskStateChanged() {
        markerMoves = null;
        selected = false;
        dragged = false;
        touchDowned = false;
        moveShown = false;
        invalidate();
    }
}
