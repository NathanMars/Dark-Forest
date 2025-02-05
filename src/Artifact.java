import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Artifact {
    private Texture texture;
    private Vector2 position;
    private Rectangle bounds;
    private final float SPEED = 5f; // Velocidade do artefato
    private float SCALE; // Tamanho do artefato é diferente para cada sprite
    private float speed = SPEED;

    public Artifact(float x, float y) {
        // Decide randomicamente a textura do artefato
       int randomTexture = MathUtils.random(0, 6);
        switch (randomTexture) {
            case 0:
                texture = new Texture(Gdx.files.internal("assets/itens/Purple_Jewel.png"));
                SCALE = 2f; 
                break;
            case 1:
                texture = new Texture(Gdx.files.internal("assets/itens/Holly_Cross.png"));
                SCALE = 2.3f;  
                break;
            case 2:
                texture = new Texture(Gdx.files.internal("assets/itens/Trinity.png"));
                SCALE = 2.3f;  
                break;
            case 3:
                texture = new Texture(Gdx.files.internal("assets/itens/Solar_Shield.png"));
                SCALE = 2.3f; 
                break;
            case 4:
                texture = new Texture(Gdx.files.internal("assets/itens/Cursed_Mask.png"));
                SCALE = 2.3f;  
                break;
            case 5:
                texture = new Texture(Gdx.files.internal("assets/itens/Blue_Pearl.png"));
                SCALE = 2f;  
                break;
            case 6:
                texture = new Texture(Gdx.files.internal("assets/itens/Golden_Collar.png"));
                SCALE = 2.2f;  
                break;
        }
        position = new Vector2(x, y);

        bounds = new Rectangle(
            position.x,
            position.y,
            texture.getWidth() * SCALE,
            texture.getHeight() * SCALE
        );
    }

    // Metodos gerais

    public void setSpeed(float speed) {
        this.speed = speed; // Para alterar a velocidade do artefato quando o jogador começa a correr
    }

    public void update() {
        position.x -= speed; // Move a bala para a esquerda
        bounds.setPosition(position.x, position.y);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y,
                   texture.getWidth() * SCALE, texture.getHeight() * SCALE);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dispose() {
        texture.dispose();
    }
}
