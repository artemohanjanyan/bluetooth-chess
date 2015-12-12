package lec.chessproto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public final class LocalGameActivity extends GameActivity {

    @Override
    protected void init() {
        whitePlayer = new LocalPlayer();
        blackPlayer = new LocalPlayer();
    }

    @Override
    public GameView.DeskListener getCurrentDeskListener(boolean color) {
        return  (LocalPlayer) (color ? blackPlayer : whitePlayer);
    }
}
