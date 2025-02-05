import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashMap;

public class Enemies {
    private Vector2 position;
    private Rectangle bounds;
    private float SPEED = 5f;
    private float SCALE;
    private final float HITBOX_SCALE = 0.7f;

    private boolean isFalling = false;
    private Animation<TextureRegion> fallingAnimation;
    private float fallingStateTime;
    private boolean isKilled = false;

    private Animation<TextureRegion> walkingAnimation;
    private Animation<TextureRegion> attackingAnimation;
    private HashMap<Integer, Animation<TextureRegion>> fallingAnimations;
    private int enemyType;
    private boolean isAttacking = false;
    private float stateTime;

    public Enemies(float x, float y) {
        // Alterna entre 5 tipos possiveis de inimigos
        enemyType = MathUtils.random(0, 4);
        switch (enemyType) {
            case 0:
                walkingAnimation = loadAnimation("assets/enemies/Werewolf/Walk.png", 11, 1, 0.1f);
                attackingAnimation = loadAnimation("assets/enemies/Werewolf/Attack.png", 4, 1, 0.1f);
                SCALE = 1.8f;
                break;
            case 1:
                walkingAnimation = loadAnimation("assets/enemies/Werewolf/Run.png", 9, 1, 0.1f);
                attackingAnimation = loadAnimation("assets/enemies/Werewolf/Run_Attack.png", 7, 1, 0.1f);
                SCALE = 1.8f;
                break;
            case 2:
                walkingAnimation = loadAnimation("assets/enemies/Hound/Run.png", 9, 1, 0.1f);
                attackingAnimation = loadAnimation("assets/enemies/Hound/Run_Attack.png", 7, 1, 0.1f);
                SCALE = 1.9f;
                break;
            case 3:
                walkingAnimation = loadAnimation("assets/enemies/Knight/Run.png", 7, 1, 0.1f);
                attackingAnimation = loadAnimation("assets/enemies/Knight/Attack.png", 4, 1, 0.1f);
                SCALE = 2.8f;
                break;
            case 4:
                walkingAnimation = loadAnimation("assets/enemies/Demon/Run.png", 8, 1, 0.1f);
                attackingAnimation = loadAnimation("assets/enemies/Demon/Attack.png", 3, 1, 0.1f);
                SCALE = 1.7f;
                break;
        }

        fallingAnimations = new HashMap<>();
        fallingAnimations.put(0, loadAnimation("assets/enemies/Werewolf/Dead.png", 2, 1, 0.1f));
        fallingAnimations.put(1, loadAnimation("assets/enemies/Werewolf/Dead.png", 2, 1, 0.1f));
        fallingAnimations.put(2, loadAnimation("assets/enemies/Hound/Dead.png", 2, 1, 0.1f));
        fallingAnimations.put(3, loadAnimation("assets/enemies/Knight/Dead.png", 6, 1, 0.1f));
        fallingAnimations.put(4, loadAnimation("assets/enemies/Demon/Dead.png", 6, 1, 0.1f));

        fallingAnimation = fallingAnimations.get(enemyType);

        position = new Vector2(x, y);

        // Inicializa o tamanho e a posição da hitbox ajustada para cada tipo de inimigo
        TextureRegion firstFrame = walkingAnimation.getKeyFrame(0);
        float hitboxWidth = firstFrame.getRegionWidth() * SCALE * HITBOX_SCALE;
        float hitboxHeight;
        float offsetY;

        switch (enemyType) {
            case 0:
                hitboxHeight = firstFrame.getRegionHeight() * SCALE * HITBOX_SCALE * 0.5f;
                offsetY = (firstFrame.getRegionHeight() * SCALE - hitboxHeight) / 2 - 30;
                break;
            case 1:
                hitboxHeight = firstFrame.getRegionHeight() * SCALE * HITBOX_SCALE * 0.6f;
                offsetY = (firstFrame.getRegionHeight() * SCALE - hitboxHeight) / 2 - 60;
                break;
            case 2:
                hitboxHeight = firstFrame.getRegionHeight() * SCALE * HITBOX_SCALE * 0.7f;
                offsetY = (firstFrame.getRegionHeight() * SCALE - hitboxHeight) / 2 - 60;
                break;
            case 3:
                hitboxHeight = firstFrame.getRegionHeight() * SCALE * HITBOX_SCALE * 1.0f;
                offsetY = (firstFrame.getRegionHeight() * SCALE - hitboxHeight) / 2 - 30;
                break;
            case 4:
                hitboxHeight = firstFrame.getRegionHeight() * SCALE * HITBOX_SCALE * 0.7f;
                offsetY = (firstFrame.getRegionHeight() * SCALE - hitboxHeight) / 2 - 30;
                break;
            default:
                hitboxHeight = firstFrame.getRegionHeight() * SCALE * HITBOX_SCALE;
                offsetY = (firstFrame.getRegionHeight() * SCALE - hitboxHeight) / 2 - 30;
                break;
        }

        float offsetX = (firstFrame.getRegionWidth() * SCALE - hitboxWidth) / 2;

        bounds = new Rectangle(
            position.x + offsetX,
            position.y + offsetY, 
            hitboxWidth,          
            hitboxHeight          
        );
    }

    private Animation<TextureRegion> loadAnimation(String path, int cols, int rows, float frameDuration) {
        Texture sheet = new Texture(Gdx.files.internal(path));
        TextureRegion[][] tempFrames = TextureRegion.split(sheet, sheet.getWidth() / cols, sheet.getHeight() / rows);
        ArrayList<TextureRegion> frames = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                TextureRegion frame = tempFrames[i][j];
                frame.flip(true, false); // Inverte os sprites horizontalmente
                frames.add(frame);
            }
        }
        return new Animation<>(frameDuration, frames.toArray(new TextureRegion[0]));
    }

    public void update(boolean playerDying, boolean playerWon) {
        float deltaTime = Gdx.graphics.getDeltaTime();
        stateTime += deltaTime;

        // Controles de estado
        if (playerDying) {
            return;
        }

        if (playerWon) {
            fall();
        }

        if (isFalling) {
            fallingStateTime += deltaTime;
            if (fallingAnimation.isAnimationFinished(fallingStateTime)) {
                isKilled = true;
            }
        } else {
            position.x -= SPEED;

            float hitboxWidth = bounds.getWidth();
            float hitboxHeight;
            float offsetY;

            switch (enemyType) {
                case 0:
                    hitboxHeight = walkingAnimation.getKeyFrame(0).getRegionHeight() * SCALE * HITBOX_SCALE * 0.5f;
                    offsetY = (walkingAnimation.getKeyFrame(0).getRegionHeight() * SCALE - hitboxHeight) / 2 - 30;
                    break;
                case 1:
                    hitboxHeight = walkingAnimation.getKeyFrame(0).getRegionHeight() * SCALE * HITBOX_SCALE * 0.6f;
                    offsetY = (walkingAnimation.getKeyFrame(0).getRegionHeight() * SCALE - hitboxHeight) / 2 - 60;
                    break;
                case 2:
                    hitboxHeight = walkingAnimation.getKeyFrame(0).getRegionHeight() * SCALE * HITBOX_SCALE * 0.7f;
                    offsetY = (walkingAnimation.getKeyFrame(0).getRegionHeight() * SCALE - hitboxHeight) / 2 - 60;
                    break;
                case 3:
                    hitboxHeight = walkingAnimation.getKeyFrame(0).getRegionHeight() * SCALE * HITBOX_SCALE * 1.0f;
                    offsetY = (walkingAnimation.getKeyFrame(0).getRegionHeight() * SCALE - hitboxHeight) / 2 - 30;
                    break;
                case 4:
                    hitboxHeight = walkingAnimation.getKeyFrame(0).getRegionHeight() * SCALE * HITBOX_SCALE * 0.7f;
                    offsetY = (walkingAnimation.getKeyFrame(0).getRegionHeight() * SCALE - hitboxHeight) / 2 - 30;
                    break;
                default:
                    hitboxHeight = walkingAnimation.getKeyFrame(0).getRegionHeight() * SCALE * HITBOX_SCALE;
                    offsetY = (walkingAnimation.getKeyFrame(0).getRegionHeight() * SCALE - hitboxHeight) / 2 - 30;
                    break;
            }

            float offsetX = (walkingAnimation.getKeyFrame(0).getRegionWidth() * SCALE - hitboxWidth) / 2;

            bounds.setPosition(position.x + offsetX, position.y + offsetY);
            bounds.setSize(hitboxWidth, hitboxHeight);
        }
    }

    public void draw(SpriteBatch batch, boolean playerDying) {
        // Controla animações baseadas em estado dos inimigos e do jogador
        TextureRegion currentFrame;
        if (isFalling) {
            currentFrame = fallingAnimation.getKeyFrame(fallingStateTime);
        } else if (isAttacking) {
            currentFrame = attackingAnimation.getKeyFrame(stateTime);
        } else if (playerDying) {
            currentFrame = walkingAnimation.getKeyFrame(0);
        } else {
            currentFrame = walkingAnimation.getKeyFrame(stateTime, true);
        }
        batch.draw(currentFrame, position.x, position.y, currentFrame.getRegionWidth() * SCALE, currentFrame.getRegionHeight() * SCALE);
    }

    // Metodos gerais
    public void fall() {
        isFalling = true;
        fallingStateTime = 0;
    }

    public void attack() {
        isAttacking = true;
        stateTime = 0;
    }

    public boolean isKilled() {
        return isKilled;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getWidth() {
        return walkingAnimation.getKeyFrame(0).getRegionWidth() * SCALE;
    }

    public void dispose() {
        walkingAnimation.getKeyFrame(0).getTexture().dispose();
    }

    public float getSpeed() {
        return SPEED;
    }

    public void setSpeed(float speed) {
        this.SPEED = speed;
    }
}
