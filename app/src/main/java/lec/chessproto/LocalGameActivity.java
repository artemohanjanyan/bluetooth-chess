package lec.chessproto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LocalGameActivity extends GameActivity {

    @Override
    protected void initPlayers() {
        whitePlayer = new LocalPlayer(gameView);
        blackPlayer = new LocalPlayer(gameView);
    }
}
