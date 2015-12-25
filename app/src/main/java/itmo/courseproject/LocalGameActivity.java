package itmo.courseproject;

public class LocalGameActivity extends GameActivity {

    @Override
    protected void initPlayers() {
        whitePlayer = new LocalPlayer(gameView);
        blackPlayer = new LocalPlayer(gameView);

        onPlayersInitialized();
    }
}
