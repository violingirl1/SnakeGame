import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener {
    private static final int SCREEN_WIDTH = 600;
    private static final int SCREEN_HEIGHT = 600;
    private static final int UNIT_SIZE = 25;
    private static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    private static final int STARTING_BODY_PARTS = 3;
    private static final int DELAY = 100;
    private static final int MIN_DELAY = 50;
    private static final int SPEED_CHANGE = 10;
    private static final String HIGH_SCORE_FILE = "highscore.txt";
    private static final String EAT_SOUND_FILE = "eat.wav";
    private static final String GAME_OVER_SOUND_FILE = "gameover.wav";

    private final int[] x = new int[GAME_UNITS];
    private final int[] y = new int[GAME_UNITS];
    private final Random random = new Random();
    private Timer timer;
    private int bodyParts = STARTING_BODY_PARTS;
    private int foodX;
    private int foodY;
    private int score = 0;
    private int highScore = 0;
    private int speedLevel = 1;
    private char direction = 'R';
    private boolean running = false;
    private boolean gameOver = false;

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new MyKeyAdapter());

        loadHighScore();
        createSnake();
        newFood();
        timer = new Timer(DELAY, this);
    }

    private void createSnake() {
        int startX = (SCREEN_WIDTH / 2) - UNIT_SIZE;
        int startY = SCREEN_HEIGHT / 2;

        for (int i = 0; i < bodyParts; i++) {
            x[i] = startX - (i * UNIT_SIZE);
            y[i] = startY;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g);

        if (running || gameOver) {
            drawFood(g);
            drawSnake(g);
        }

        drawScore(g);

        if (!running) {
            drawStartScreen(g);
        }
    }

    private void drawGrid(Graphics g) {
        g.setColor(new Color(35, 35, 35));

        for (int i = 0; i < SCREEN_WIDTH; i += UNIT_SIZE) {
            g.drawLine(i, 0, i, SCREEN_HEIGHT);
        }

        for (int i = 0; i < SCREEN_HEIGHT; i += UNIT_SIZE) {
            g.drawLine(0, i, SCREEN_WIDTH, i);
        }
    }

    private void drawFood(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(foodX + 3, foodY + 5, UNIT_SIZE - 6, UNIT_SIZE - 5);

        g.setColor(new Color(0, 150, 0));
        g.fillOval(foodX + 14, foodY + 1, 8, 7);

        g.setColor(new Color(120, 70, 20));
        g.fillRect(foodX + 12, foodY + 2, 3, 6);
    }

    private void drawSnake(Graphics g) {
        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) {
                g.setColor(new Color(0, 130, 0));
            } else {
                g.setColor(new Color(0, 210, 0));
            }

            g.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 10, 10);
        }

        drawEyes(g);
    }

    private void drawEyes(Graphics g) {
        int eyeSize = 5;
        int firstEyeX = x[0] + 6;
        int firstEyeY = y[0] + 6;
        int secondEyeX = x[0] + 15;
        int secondEyeY = y[0] + 6;

        if (direction == 'D') {
            firstEyeY = y[0] + 14;
            secondEyeY = y[0] + 14;
        } else if (direction == 'L') {
            firstEyeX = x[0] + 5;
            firstEyeY = y[0] + 6;
            secondEyeX = x[0] + 5;
            secondEyeY = y[0] + 15;
        } else if (direction == 'R') {
            firstEyeX = x[0] + 15;
            firstEyeY = y[0] + 6;
            secondEyeX = x[0] + 15;
            secondEyeY = y[0] + 15;
        }

        g.setColor(Color.WHITE);
        g.fillOval(firstEyeX, firstEyeY, eyeSize, eyeSize);
        g.fillOval(secondEyeX, secondEyeY, eyeSize, eyeSize);

        g.setColor(Color.BLACK);
        g.fillOval(firstEyeX + 2, firstEyeY + 2, 2, 2);
        g.fillOval(secondEyeX + 2, secondEyeY + 2, 2, 2);
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        drawCenteredText(g, "Score: " + score + "    High Score: " + highScore, 25);

        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        drawCenteredText(g, "Speed: " + speedLevel, 45);
    }

    private void drawStartScreen(Graphics g) {
        g.setColor(Color.WHITE);

        if (gameOver) {
            g.setFont(new Font("SansSerif", Font.BOLD, 42));
            drawCenteredText(g, "GAME OVER", 190);
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 56));
        drawCenteredText(g, "SNAKE", 280);

        g.setFont(new Font("SansSerif", Font.PLAIN, 20));
        drawCenteredText(g, "Press ENTER to Start", 340);
        drawCenteredText(g, "Press ESC to Exit", 375);
    }

    private void drawCenteredText(Graphics g, String text, int y) {
        FontMetrics metrics = getFontMetrics(g.getFont());
        int x = (SCREEN_WIDTH - metrics.stringWidth(text)) / 2;

        g.drawString(text, x, y);
    }

    private void move() {
        for (int i = bodyParts - 1; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        if (direction == 'U') {
            y[0] = y[0] - UNIT_SIZE;
        } else if (direction == 'D') {
            y[0] = y[0] + UNIT_SIZE;
        } else if (direction == 'L') {
            x[0] = x[0] - UNIT_SIZE;
        } else if (direction == 'R') {
            x[0] = x[0] + UNIT_SIZE;
        }
    }

    private void newFood() {
        foodX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        foodY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    private void checkFood() {
        if (x[0] == foodX && y[0] == foodY) {
            x[bodyParts] = x[bodyParts - 1];
            y[bodyParts] = y[bodyParts - 1];
            bodyParts++;
            score++;
            updateSpeed();
            updateHighScore();
            playSound(EAT_SOUND_FILE);
            newFood();
        }
    }

    private void updateSpeed() {
        speedLevel = (score / 5) + 1;

        int newDelay = DELAY - ((speedLevel - 1) * SPEED_CHANGE);

        if (newDelay < MIN_DELAY) {
            newDelay = MIN_DELAY;
        }

        timer.setDelay(newDelay);
    }

    private void checkCollisions() {
        for (int i = bodyParts - 1; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                gameOver();
            }
        }

        if (x[0] < 0 || x[0] > SCREEN_WIDTH - UNIT_SIZE) {
            gameOver();
        }

        if (y[0] < 0 || y[0] > SCREEN_HEIGHT - UNIT_SIZE) {
            gameOver();
        }
    }

    private void gameOver() {
        if (!running) {
            return;
        }

        running = false;
        gameOver = true;
        timer.stop();
        updateHighScore();
        playSound(GAME_OVER_SOUND_FILE);
        repaint();
    }

    private void startGame() {
        bodyParts = STARTING_BODY_PARTS;
        score = 0;
        speedLevel = 1;
        direction = 'R';
        running = true;
        gameOver = false;
        timer.setDelay(DELAY);

        for (int i = 0; i < GAME_UNITS; i++) {
            x[i] = 0;
            y[i] = 0;
        }

        createSnake();
        newFood();
        timer.start();
        repaint();
    }

    private void loadHighScore() {
        File file = new File(HIGH_SCORE_FILE);

        if (!file.exists()) {
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNextInt()) {
                highScore = scanner.nextInt();
            }
        } catch (IOException e) {
            highScore = 0;
        }
    }

    private void updateHighScore() {
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }
    }

    private void saveHighScore() {
        try (FileWriter writer = new FileWriter(HIGH_SCORE_FILE)) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            System.out.println("Could not save high score.");
        }
    }

    private void playSound(String fileName) {
        try {
            File soundFile = new File(fileName);

            if (!soundFile.exists()) {
                return;
            }

            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(soundFile));
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
        } catch (Exception e) {
            System.out.println("Could not play sound.");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkCollisions();

            if (running) {
                checkFood();
            }

            repaint();
        }
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_ENTER && !running) {
                startGame();
            } else if (key == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            } else if (key == KeyEvent.VK_UP && direction != 'D') {
                direction = 'U';
            } else if (key == KeyEvent.VK_DOWN && direction != 'U') {
                direction = 'D';
            } else if (key == KeyEvent.VK_LEFT && direction != 'R') {
                direction = 'L';
            } else if (key == KeyEvent.VK_RIGHT && direction != 'L') {
                direction = 'R';
            }
        }
    }
}
