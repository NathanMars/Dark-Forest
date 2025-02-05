import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Spell {
    private Texture magicSprite;
    private Animation<TextureRegion> magicAnimation;
    private Vector2 position;
    private float speed = 10f; // Velocidade da magia
    private Rectangle bounds;
    private final float scale = 4f; // Tamanho da magia
    private float stateTime;
    private final float HITBOX_SCALE = 0.1f; // Tamanho da hitbox da magia
    private final float HITBOX_OFFSET_X = 140f; // Posicionamento do hitbox da magia

    public Spell(float x, float y) {
        magicSprite = new Texture(Gdx.files.internal("assets/player/Mago/Charge_2.png"));

        TextureRegion[][] tempFrames = TextureRegion.split(
            magicSprite,
            magicSprite.getWidth() / 6,
            magicSprite.getHeight() / 1
        );
        TextureRegion[] magicFrames = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            magicFrames[i] = tempFrames[0][i];
        }
        magicAnimation = new Animation<>(0.1f, magicFrames);
        magicAnimation.setPlayMode(Animation.PlayMode.LOOP);

        position = new Vector2(x, y - (magicFrames[0].getRegionHeight() * scale / 2));

        bounds = new Rectangle(
            position.x + HITBOX_OFFSET_X,
            position.y + (magicFrames[0].getRegionHeight() * scale * (1 - HITBOX_SCALE) / 2),
            magicFrames[0].getRegionWidth() * scale * HITBOX_SCALE,
            magicFrames[0].getRegionHeight() * scale * HITBOX_SCALE
        );

        stateTime = 0f;
    }

    public void update() {
        position.x += speed;

        bounds.setPosition(position.x + HITBOX_OFFSET_X, position.y + (magicAnimation.getKeyFrame(0).getRegionHeight() * scale * (1 - HITBOX_SCALE) / 2));
        bounds.setSize(magicAnimation.getKeyFrame(0).getRegionWidth() * scale * HITBOX_SCALE, magicAnimation.getKeyFrame(0).getRegionHeight() * scale * HITBOX_SCALE);

        stateTime += Gdx.graphics.getDeltaTime();
    }

    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = magicAnimation.getKeyFrame(stateTime);
        batch.draw(currentFrame, position.x, position.y, currentFrame.getRegionWidth() * scale, currentFrame.getRegionHeight() * scale);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getX() {
        return position.x;
    }

    public void dispose() {
        magicSprite.dispose();
    }
}
