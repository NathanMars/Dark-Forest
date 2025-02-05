import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class Game {
    // Componente de inicialização do jogo. Jogo feito por Nathan Marques Silva
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Dark Forest");
        config.setWindowedMode(1200,720);
        config.useVsync(true);
        new Lwjgl3Application(new GameScreen(), config);
    }
}
