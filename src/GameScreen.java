import java.util.Iterator;
import org.lwjgl.opengl.GL20;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
//import com.badlogic.gdx.math.Rectangle; //Descomentar durante a depuração
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameScreen extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture backgroundA;
    private Texture backgroundB;
    private boolean useBackgroundA = true;
    private Player player;
    private Array<Enemies> enemies;
    private long lastEnemyTime;
    private long enemySpawnInterval;
    private Music runnningSFX;
    private Sound jumpSFX;
    private Sound spellSFX;
    private Sound explosionSFX;
    private Sound enemyHitSFX;
    private Sound deathSFX;
    private Sound artifactSFX;
    private Music menuMusic;
    private Music backgroundMusic;
    private Music victoryMusic;
    private int score;
    private int powers;
    private int life;
    private Array<Spell> spells;
    private BitmapFont font;
    private BitmapFont title;
    private BitmapFont subtitle;
    private ShapeRenderer shapeRenderer;
    private Array<Artifact> artifacts;
    private long lastArtifactTime;
    private long artifactSpawnInterval;
    private boolean isDamaged = false;
    private long lastDamageTime;
    private float backgroundX = 0;
    private final float WALKING_BACKGROUND_SPEED = 200;
    private final float RUNNING_BACKGROUND_SPEED = 300;
    private final float WALKING_ENEMY_SPEED = 6f;
    private final float RUNNING_ENEMY_SPEED = 8f;
    private final float BACKGROUND_OVERLAP = 4.8f;
    private final float WALKING_ARTIFACT_SPEED = 3f;
    private final float RUNNING_ARTIFACT_SPEED = 5f;
    private boolean isMenu = true; // Flag to indicate if the menu is active
    private Texture heartsSpriteSheet;
    private TextureRegion fullHeart;
    private TextureRegion emptyHeart;

    @Override
    public void create() {
        batch = new SpriteBatch();
        backgroundA = new Texture(Gdx.files.internal("assets/background/Background.png"));
        backgroundB = new Texture(Gdx.files.internal("assets/background/BackgroundB.png"));
        spellSFX = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/spell_cast.mp3"));
        explosionSFX = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/explosion.mp3"));
        jumpSFX = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/jump_fall.mp3"));
        artifactSFX = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/artifact_colected.mp3"));
        enemyHitSFX = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/enemy_hit.mp3"));
        deathSFX = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/death_bells.mp3"));
        player = new Player();
        enemies = new Array<>();
        spawnEnemy();
 
        score = 0;
        powers = 0;
        life = 3;
        spells = new Array<>();
        font = new BitmapFont();
        title = new BitmapFont();
        subtitle = new BitmapFont();
        artifacts = new Array<>();
        artifactSpawnInterval = MathUtils.random(5000000000L, 10000000000L); // Entre 5 e 15 segundos
        lastArtifactTime = TimeUtils.nanoTime();
        shapeRenderer = new ShapeRenderer();

        // Carrega e toca a música de fundo e efeitos sonoros continuos
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/menu_music.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.3f);
        
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/background_music.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.3f);
        backgroundMusic.play();

        runnningSFX = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/running_sound.mp3"));
        runnningSFX.setLooping(true); 
        runnningSFX.setVolume(0.3f); 
        runnningSFX.play();

        victoryMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/victory_music.mp3"));
        victoryMusic.setLooping(false); 
        victoryMusic.setVolume(0.3f);

        heartsSpriteSheet = new Texture(Gdx.files.internal("assets/player/Hearts.png"));
        TextureRegion[][] tempHeartsFrames = TextureRegion.split(heartsSpriteSheet, heartsSpriteSheet.getWidth() / 3, heartsSpriteSheet.getHeight() / 1);
        fullHeart = tempHeartsFrames[0][0];
        emptyHeart = tempHeartsFrames[0][2];
    }
 
    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Checa se o jogo está no menu ou em execução
        if (isMenu) {
            renderMenu();
        } else {
            update();
            batch.begin();
            Texture currentBackground = useBackgroundA ? backgroundA : backgroundB;
            Texture nextBackground = useBackgroundA ? backgroundB : backgroundA;
            batch.draw(currentBackground, backgroundX, 0, Gdx.graphics.getWidth() + BACKGROUND_OVERLAP, Gdx.graphics.getHeight());
            batch.draw(nextBackground, backgroundX + Gdx.graphics.getWidth() - BACKGROUND_OVERLAP, 0, Gdx.graphics.getWidth() + BACKGROUND_OVERLAP, Gdx.graphics.getHeight());
            player.draw(batch);

            for (Enemies enemy : enemies) {
                enemy.draw(batch, player.isDying());
            }
            for (Artifact artifact : artifacts) {
                artifact.draw(batch);
            }
            for (Spell spell : spells) {
                spell.draw(batch);
            }

            // Hud do jogador
            for (int i = 0; i < 3; i++) {
                TextureRegion heart = (i < life) ? fullHeart : emptyHeart;
                batch.draw(heart, 10 + i * (fullHeart.getRegionWidth() + 5), Gdx.graphics.getHeight() - fullHeart.getRegionHeight() - 10);
            }
            font.draw(batch, "Artefatos Coletados: " + score, 10, Gdx.graphics.getHeight() - 50);
            font.draw(batch, "Mana: " + powers, 10, Gdx.graphics.getHeight() - 70);

            // Desenha mensagens de Game Over se o jogador estiver morto
            if (player.isDying()) {
                title.getData().setScale(3);
                title.setColor(1, 0, 0, 1); 
                title.draw(batch, "Game Over", Gdx.graphics.getWidth() / 2 - title.getRegion().getRegionWidth() / 2, Gdx.graphics.getHeight() / 2 + 50);

                subtitle.getData().setScale(1.5f); 
                subtitle.setColor(1, 1, 1, 1); 
                subtitle.draw(batch, "Aperte qualquer tecla para reiniciar!", Gdx.graphics.getWidth() / 2 - subtitle.getRegion().getRegionWidth() / 2 - 50, Gdx.graphics.getHeight() / 2 - 30);
            }

            // Desenha mensagens de vitoria se o jogador ganhar
            if (player.GameWon()) {
                title.getData().setScale(3); 
                title.setColor(0, 0, 1, 1);
                title.draw(batch, "Vitória!", Gdx.graphics.getWidth() / 2 + 50 - title.getRegion().getRegionWidth() / 2, Gdx.graphics.getHeight() / 2 + 50);

                subtitle.getData().setScale(1.5f);
                subtitle.setColor(1, 1, 1, 1);
                subtitle.draw(batch, "Aperte qualquer tecla para reiniciar!", Gdx.graphics.getWidth() / 2 - subtitle.getRegion().getRegionWidth() / 2 - 50, Gdx.graphics.getHeight() / 2 - 30);
            }

            batch.end();

            /* //Desenhar as hitboxes para depuração
            // Desenha a hitbox do jogador
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(1, 0, 0, 1); // Cor vermelha
            shapeRenderer.rect(player.getBounds().x, player.getBounds().y,
                    player.getBounds().width, player.getBounds().height);

            // Desenha a hitbox de cada obstáculo
            shapeRenderer.setColor(0, 1, 0, 1); // Verde para os obstáculos
            for (Enemies enemy : enemies) {
                Rectangle bounds = enemy.getBounds();
                shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            // Desenha a hitbox de cada magia
            shapeRenderer.setColor(0, 0, 1, 0); // Azul para os obstáculos
            for (Spell spell : spells) {
                Rectangle bounds = spell.getBounds();
                shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            // Desenha a hitbox de cada artefato
            shapeRenderer.setColor(1, 0, 1, 0); // Roxo para os obstáculos
            for (Artifact artifact : artifacts) {
                Rectangle bounds = artifact.getBounds();
                shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
                
            }

            shapeRenderer.end();
            */
        }
    }

    // Tela de menu
    private void renderMenu() {

        backgroundMusic.stop();
        runnningSFX.stop();
        victoryMusic.stop();
        deathSFX.stop();
        menuMusic.play();
        batch.begin();
        Texture currentBackground = useBackgroundA ? backgroundA : backgroundB;
        batch.draw(currentBackground, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        title.getData().setScale(3);
        title.setColor(1, 0, 10, 1);
        title.draw(batch, "Dark Forest", Gdx.graphics.getWidth() / 2 - title.getRegion().getRegionWidth() / 2, Gdx.graphics.getHeight() / 2 + 250);

        subtitle.getData().setScale(1.2f);
        subtitle.setColor(1, 1, 1, 1);
        subtitle.draw(batch, "por Nathan Marques Silva", Gdx.graphics.getWidth() / 2 - subtitle.getRegion().getRegionWidth() / 2 + 15, Gdx.graphics.getHeight() / 2 + 190);

        subtitle.getData().setScale(1.5f);
        subtitle.setColor(1, 1, 1, 1);
        subtitle.draw(batch, "Aperte qualquer tecla para começar!", Gdx.graphics.getWidth() / 2 - subtitle.getRegion().getRegionWidth() / 2 - 50, Gdx.graphics.getHeight() / 2);

        float messageX = Gdx.graphics.getWidth() - 230;
        float messageY = 55;
        subtitle.draw(batch, "Colete artefatos para ganhar poderes magicos!", messageX - subtitle.getRegion().getRegionWidth(), messageY + 85);
        subtitle.draw(batch, "Colete 20 artefatos para ganhar o jogo.", messageX - subtitle.getRegion().getRegionWidth(), messageY + 50);
        subtitle.draw(batch, "Espaço: Pular", messageX - subtitle.getRegion().getRegionWidth(), messageY + 15);
        subtitle.draw(batch, "Enter: Conjurar Magia", messageX - subtitle.getRegion().getRegionWidth(), messageY - 20);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            isMenu = false;
            menuMusic.stop();
            resetGame();
        }
    }

    private void update() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        // Controles do jogo
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !player.isDying() && !player.GameWon()) {
            runnningSFX.stop();
            player.jump();
            jumpSFX.play(0.3f);
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && powers > 0 && !player.isDying() && !player.GameWon()) {
            userPower();
        }
        
        if (!player.isInAir() && !player.isCasting() && !runnningSFX.isPlaying() && !player.isDying() && !player.GameWon())
        {
            runnningSFX.play();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) && (player.isDying() || player.GameWon())) {
            if (!isMenu) {
                isMenu = true;
            } else {
            resetGame();
        }
        }

        player.update(deltaTime);
        
        // Verifica se é hora de spwnar um novo inimigo
        if (TimeUtils.nanoTime() - lastEnemyTime > enemySpawnInterval) {
            spawnEnemy();
        }

        float currentBackgroundSpeed = player.isRunning() ? RUNNING_BACKGROUND_SPEED : WALKING_BACKGROUND_SPEED;
        float currentEnemySpeed = player.isRunning() ? RUNNING_ENEMY_SPEED : WALKING_ENEMY_SPEED;
        float currentArtifactSpeed = player.isRunning() ? RUNNING_ARTIFACT_SPEED : WALKING_ARTIFACT_SPEED;

        // Para o movimento do fundo, inimigos e artefatos se o personagem estiver atacando, morrendo ou celebrando vitória
        if (player.isCasting() && !player.isInAir()) {
            currentBackgroundSpeed = 0;
            currentArtifactSpeed = 0;
        }
        if (player.isDying() || player.GameWon()) {
            currentBackgroundSpeed = 0;
            currentEnemySpeed = 0;
            currentArtifactSpeed = 0;
        }

        // Atualiza a posição de cada inimigo e verifica colisões
        for (Enemies enemy : enemies) {
            enemy.setSpeed(currentEnemySpeed);
            enemy.update(player.isDying(), player.GameWon());
            if (enemy.getBounds().overlaps(player.getBounds()) && !isDamaged && life != 0 && !player.GameWon()) {
                life--;
                isDamaged = true;
                enemyHitSFX.play();
                lastDamageTime = TimeUtils.nanoTime();
                enemy.attack(); // Animação de ataque dos inimigos
                // Morte do jogador
                if (life == 0) {
                    runnningSFX.stop();
                    deathSFX.play();
                    backgroundMusic.stop();
                    player.die();          
                }

            }

            // Verifica se é hora de criar um novo artefato
            if (TimeUtils.nanoTime() - lastArtifactTime > artifactSpawnInterval) {
                spawnArtifact();
            }

        }

        // Deixa o jogador invuneravel por 1 segundo após tomar dano
        if (isDamaged) {
            if (TimeUtils.nanoTime() - lastDamageTime > 1000000000L) {
                isDamaged = false; 
            }
        }

        // Atualiza a posição de cada magia conjurada e verifica colisões com os inimigos
        for (Spell spell : spells) {
            spell.update();
            for (Enemies enemy : enemies) {
                if (spell.getBounds().overlaps(enemy.getBounds())) {
                    spells.removeValue(spell, true);
                    enemy.fall();
                    explosionSFX.play();
                    break;
                }
            }
        }

        // Remover inimigos fora da tela
        for (Iterator<Enemies> iterator = enemies.iterator(); iterator.hasNext();) {
            Enemies enemy = iterator.next();
            if (enemy.isKilled()) {
                iterator.remove();
                continue;
            }
            if (enemy.getPosition().x < -enemy.getWidth()) {
                iterator.remove();
            }
        }

        // Remover magias fora da tela
        for (Iterator<Spell> iterator = spells.iterator(); iterator.hasNext();) {
            Spell spell = iterator.next();
            if (spell.getX() > Gdx.graphics.getWidth()) {
                iterator.remove();
            }
        }

        // Atualiza os artefatos e verifica colisões com o jogador
        for (Iterator<Artifact> iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = iterator.next();
            artifact.setSpeed(currentArtifactSpeed);
            artifact.update();
            if (artifact.getBounds().overlaps(player.getBounds())) {
                powers++;
                score++;
                iterator.remove();
                artifactSFX.play();
            } else if (artifact.getBounds().x < -artifact.getBounds().width) {
                iterator.remove();
            }

            // Vitória do jogador
            if (score == 20 && !player.GameWon() && !player.isDying()) {
                backgroundMusic.stop();
                runnningSFX.stop();
                victoryMusic.play();
                player.Win();
            }
        }

        // Jogador começa a correr após coletar 3 artefatos
        if (score >= 5) {
            player.setRunning(true);
        } else {
            player.setRunning(false);
        }

        backgroundX -= currentBackgroundSpeed * deltaTime;
        if (backgroundX <= -Gdx.graphics.getWidth()) {
            backgroundX += Gdx.graphics.getWidth();
            useBackgroundA = !useBackgroundA; // Alternate entre backgrouns para que não fiquem repetidos
        }
    }

    private void spawnEnemy() {
        enemies.add(new Enemies(Gdx.graphics.getWidth(), 100)); // Cria o obstáculo fora da tela
        lastEnemyTime = TimeUtils.nanoTime();
        // Define um intervalo aleatório entre 2 e 4 segundos (convertido para nanossegundos)
        enemySpawnInterval = MathUtils.random(2000000000, 4000000000L);
    }

    private void spawnArtifact() {
        float y = 100;
        artifacts.add(new Artifact(Gdx.graphics.getWidth(), y));
        lastArtifactTime = TimeUtils.nanoTime(); // Atualiza o intervalo, o calculo faz com que artefatos tenham a tendencia de spawnar... 
        artifactSpawnInterval = MathUtils.random(5500000000L, 11000000000L); // ... perto dos inimigos, aumentando o risco e desafio de pega-los
    }

    private void resetGame() {
        enemies.clear();
        artifacts.clear();
        backgroundX = 0;
        player.reset();
        runnningSFX.stop();
        backgroundMusic.stop();
        victoryMusic.stop();
        score = 0;
        powers = 0;
        life = 3;
        backgroundMusic.play();
        runnningSFX.play();
    }

    @Override
    public void dispose() {
        batch.dispose();
        backgroundA.dispose();
        backgroundB.dispose();
        player.dispose();
        font.dispose();
        for (Enemies enemy : enemies) {
            enemy.dispose();
        }
        for (Artifact artifact : artifacts) {
            artifact.dispose();
        }
        for (Spell spell : spells) {
            spell.dispose();
        }

        shapeRenderer.dispose();
        backgroundMusic.stop();
        backgroundMusic.dispose();
        runnningSFX.dispose();
        jumpSFX.dispose();
        heartsSpriteSheet.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    // Usa o poder
    private void userPower() {
        if (powers > 0) {
            runnningSFX.stop();
            player.cast();
            Spell spell = new Spell(
                    player.getPosition().x + player.getBounds().width,
                    player.getPosition().y + player.getBounds().height / 2);
                    spellSFX.play();
                    spells.add(spell);
            powers--;
            runnningSFX.play();
        }
    }
}
