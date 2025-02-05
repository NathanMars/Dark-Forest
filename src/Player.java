import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player {

    private Texture walkingSprite;
    private Animation<TextureRegion> walkingAnimation;
    private Animation<TextureRegion> runningAnimation;
    private Animation<TextureRegion> castingAnimation;
    private Animation<TextureRegion> dyingAnimation;
    private Animation<TextureRegion> victoryAnimation;
    private TextureRegion[] runningJumpFrames;
    private TextureRegion[] deadFrame;
    private float stateTime;
    private Vector2 position;
    private Vector2 velocity;
    private Rectangle bounds;
    private final float GRAVITY = -0.35f; // Velocidade da queda
    private final float JUMP_VELOCITY = 15f; // Velocidade de pulo
    private final float SCALE = 3f; // Tamanho do sprite do jogador
    private final float HITBOX_SCALE = 0.3f; // Tamanho da hitbox
    private final float RUNNING_HITBOX_OFFSET = -40f; // Ajusta a hitbox quando o jogador começa a correr

    // Variaveis de controle de estado
    private boolean isCasting = false;
    private boolean isInAir = false;
    private boolean isRunning = false;
    private boolean isDying = false;
    private boolean GameWon = false;

    public Player() {
        // Animação de Andar
        walkingSprite = new Texture(Gdx.files.internal("assets/player/Mago/Walk.png"));

        TextureRegion[][] tempFrames = TextureRegion.split(
            walkingSprite,
            walkingSprite.getWidth() / 7,
            walkingSprite.getHeight() / 1
        );

        TextureRegion[] walkingFrames = new TextureRegion[7];
        for (int i = 0; i < 7; i++) { 
            walkingFrames[i] = tempFrames[0][i];
        }

        walkingAnimation = new Animation<>(0.1f, walkingFrames);
        walkingAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Animação de correr
        Texture runningSpriteSheet = new Texture(Gdx.files.internal("assets/player/Mago/Run.png"));
        TextureRegion[][] tempRunningFrames = TextureRegion.split(
            runningSpriteSheet,
            runningSpriteSheet.getWidth() / 8, 
            runningSpriteSheet.getHeight() / 1
        );

        TextureRegion[] runningFrames = new TextureRegion[8];
        for (int i = 0; i < 8; i++) {
            runningFrames[i] = tempRunningFrames[0][i];
        }

        runningAnimation = new Animation<>(0.1f, runningFrames);
        runningAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Animação de atacar
        Texture castingSpriteSheet = new Texture(Gdx.files.internal("assets/player/Mago/Attack_1.png"));
        TextureRegion[][] tempCastingFrames = TextureRegion.split(
            castingSpriteSheet,
            castingSpriteSheet.getWidth() / 7, 
            castingSpriteSheet.getHeight() / 1
        );

        TextureRegion[] castingFrames = new TextureRegion[7];
        for (int i = 0; i < 7; i++) {
            castingFrames[i] = tempCastingFrames[0][i];
        }

        castingAnimation = new Animation<>(0.1f, castingFrames);
        castingAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        // Animação de morrer
        Texture dyingSpriteSheet = new Texture(Gdx.files.internal("assets/player/Mago/Dead.png"));
        TextureRegion[][] tempDyingFrames = TextureRegion.split(
            dyingSpriteSheet,
            dyingSpriteSheet.getWidth() / 4, 
            dyingSpriteSheet.getHeight() / 1
        );

        TextureRegion[] dyingFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            dyingFrames[i] = tempDyingFrames[0][i];
        }

        dyingAnimation = new Animation<>(0.1f, dyingFrames);
        dyingAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        deadFrame = new TextureRegion[1];
        deadFrame[0] = dyingFrames[3];

         // Animação de vitória
         Texture victorySpriteSheet = new Texture(Gdx.files.internal("assets/player/Mago/Victory.png"));
         TextureRegion[][] tempVictoryFrames = TextureRegion.split(
            victorySpriteSheet,
            victorySpriteSheet.getWidth() / 4, 
            victorySpriteSheet.getHeight() / 1
         );
 
         TextureRegion[] victoryFrames = new TextureRegion[4];
         for (int i = 0; i < 4; i++) {
            victoryFrames[i] = tempVictoryFrames[0][i];
         }
 
         victoryAnimation = new Animation<>(0.1f, victoryFrames);
         victoryAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Animação de pular
        runningJumpFrames = new TextureRegion[3];
        for (int i = 0; i < 3; i++) {
            runningJumpFrames[i] = runningFrames[i];
        }

        // Controla a posição do jogador
        position = new Vector2(50, 100);
        velocity = new Vector2(0, 0);
        stateTime = 0f;

        float hitboxWidth = walkingFrames[0].getRegionWidth() * SCALE * HITBOX_SCALE;
        float hitboxHeight = hitboxWidth;
        float offsetX = (walkingFrames[0].getRegionWidth() * SCALE - hitboxWidth) / 2;
    
        bounds = new Rectangle(
            position.x + offsetX,
            position.y,
            hitboxWidth,
            hitboxHeight
        );
    }

    public void update(float deltaTime) {

        velocity.y += GRAVITY;
        position.add(velocity);
        
        if (position.y < 100) {
            position.y = 100;
            velocity.y = 0;
            isInAir = false;
        } else {
            isInAir = true;
        }
        
        // Ajusta hitbox se o jogador esta andando ou correndo
        float offsetX = (walkingAnimation.getKeyFrame(0).getRegionWidth() * SCALE - bounds.width) / 2;
        if (isRunning) {
            offsetX += RUNNING_HITBOX_OFFSET;
        }
        
        bounds.setPosition(position.x + offsetX, position.y);
        
        bounds.setSize(
            walkingAnimation.getKeyFrame(0).getRegionWidth() * SCALE * HITBOX_SCALE,
            bounds.getWidth() * 2
            );
            
            stateTime += deltaTime;
            
            if (isCasting && castingAnimation.isAnimationFinished(stateTime)) { // Verifica se a animação de conjurar magia acabou
                isCasting = false; 
            }
    }

    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame;
        if (isDying) {
            currentFrame = dyingAnimation.getKeyFrame(stateTime);
            if (dyingAnimation.isAnimationFinished(stateTime)) {
               currentFrame = deadFrame[0];
            }
        } else if (isCasting) {
            currentFrame = castingAnimation.getKeyFrame(stateTime);
        } else if (isInAir) {
                    if (velocity.y > 0) {
                    currentFrame = runningJumpFrames[1];
                } else {
                    currentFrame = runningJumpFrames[2];
                }
        }  else if (GameWon) {
            currentFrame = victoryAnimation.getKeyFrame(stateTime);
        } else if (isRunning) {
            currentFrame = runningAnimation.getKeyFrame(stateTime);
        } else {
            currentFrame = walkingAnimation.getKeyFrame(stateTime);
        }

        batch.draw(currentFrame, position.x, position.y,
                   currentFrame.getRegionWidth() * SCALE, currentFrame.getRegionHeight() * SCALE);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void reset() {
        position.set(50, 100);
        velocity.set(0, 0);
        stateTime = 0f;
        isDying = false;
        GameWon = false;
        isCasting = false;
        isInAir = false;
        isRunning = false;
    }

    public void dispose() {
        walkingSprite.dispose();
    }

    public void cast() {
        isCasting = true;
        stateTime = 0f;
    }

    public void jump() {
        if (!isInAir) {
            velocity.y = JUMP_VELOCITY;
            isInAir = true;
        }
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isCasting() {
        return isCasting;
    }

    public boolean isInAir() {
        return isInAir;
    }

    public void die() {
        isDying = true;
        stateTime = 0f;
    }

    public boolean isDying() {
        return isDying;
    }

    public void Win() {
        GameWon = true;
        stateTime = 0f;
    }
    
    public boolean GameWon() {
        return GameWon;
    }
}
