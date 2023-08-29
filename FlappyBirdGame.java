import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import javax.imageio.ImageIO;

public class FlappyBirdGame extends JPanel implements ActionListener, KeyListener, ComponentListener {
    private int WIDTH = 800;
    private int HEIGHT = 600;
    private static final int PIPE_WIDTH = 70;
    private static final int PIPE_SPACING = 250;
    private static double POLE_MOVE_SPEED = 3.0;
    private static final int JUMP_FORCE = -15;
    private static final int BIRD_WIDTH = 40;
    private static final int BIRD_HEIGHT = 30;
    private static final int BIRD_START_X = 50;
    private static final double GRAVITY = 1.5;

    private int birdY;
    private int velocity;
    private List<Rectangle> poles;
    private int gapSize;
    private int score;
    private int highScore;
    private int birdAnimationCounter;
    private Image birdImage;
    private Image bird1Image;
    private Image birdCollisionImage;
    private Image background;
    private Image upperPipeImage;
    private Image lowerPipeImage;

    private boolean isGameOver;
    private boolean isColliding;
    private boolean isPaused;
    private boolean gameOverPopupShownPersistent;
    
    public FlappyBirdGame(Image birdImage, Image bird1Image, Image birdCollisionImage,Image background, Image upperPipeImage, Image lowerPipeImage) {
        this.birdImage = birdImage;
        this.bird1Image = bird1Image;
        this.birdCollisionImage = birdCollisionImage;
        this.background = background;
        this.upperPipeImage = upperPipeImage;
        this.lowerPipeImage = lowerPipeImage;

        poles = new ArrayList<>();
        birdY = HEIGHT / 2;
        velocity = 0;
        gapSize = 6 * BIRD_HEIGHT;
        score = 0;
        highScore = 0;
        birdAnimationCounter = 0;
        isGameOver = false;
        isColliding = false;
        isPaused = false;
        gameOverPopupShownPersistent = false; // Initialize to false


        
        Timer timer = new Timer(30, this);
        timer.start();

        addComponentListener(this);

        generatePoles();
    }

    private void generatePoles() {
        Random random = new Random();
        int poleX = WIDTH + PIPE_WIDTH;
        while (poleX < WIDTH * 2) {
            int minY = gapSize / 2;
            int maxY = HEIGHT - gapSize / 2;
            int gapYPosition = minY + random.nextInt(maxY - minY + 1);

            Rectangle upperPole = new Rectangle(poleX, 0, PIPE_WIDTH, gapYPosition - gapSize / 2);
            Rectangle lowerPole = new Rectangle(poleX, gapYPosition + gapSize / 2, PIPE_WIDTH, HEIGHT - gapYPosition - gapSize / 2);
            poles.add(upperPole);
            poles.add(lowerPole);
            poleX += PIPE_SPACING;
        }
    }

    private void movePoles() {
        for (Rectangle pole : poles) {
            pole.x -= POLE_MOVE_SPEED;
        }

        if (poles.get(0).x + PIPE_WIDTH < 0) {
            poles.remove(0);
            poles.remove(0);
            int minY = gapSize / 2;
            int maxY = HEIGHT - gapSize / 2;
            int gapYPosition = minY + new Random().nextInt(maxY - minY + 1);

            Rectangle upperPole = new Rectangle(poles.get(poles.size() - 1).x + PIPE_SPACING, 0, PIPE_WIDTH, gapYPosition - gapSize / 2);
            Rectangle lowerPole = new Rectangle(poles.get(poles.size() - 1).x + PIPE_SPACING, gapYPosition + gapSize / 2, PIPE_WIDTH, HEIGHT - gapYPosition - gapSize / 2);
            poles.add(upperPole);
            poles.add(lowerPole);
            score++;
            if (score > highScore) {
                highScore = score;
            }
            POLE_MOVE_SPEED += 0.2;
        }
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, WIDTH, HEIGHT, null);
        for (int i = 0; i < poles.size(); i += 2) {
            Rectangle upperPole = poles.get(i);
            Rectangle lowerPole = poles.get(i + 1);
            g.drawImage(upperPipeImage, upperPole.x, upperPole.y, PIPE_WIDTH, upperPole.height, null);
            g.drawImage(lowerPipeImage, lowerPole.x, lowerPole.y, PIPE_WIDTH, lowerPole.height, null);
        }

        Image currentBirdImage;
        if (isColliding) {
            currentBirdImage = birdCollisionImage;
        } else {
            currentBirdImage = birdAnimationCounter % 2 == 0 ? birdImage : bird1Image;
        }
        g.drawImage(currentBirdImage, BIRD_START_X, birdY, BIRD_WIDTH, BIRD_HEIGHT, null);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Rockwell Extra Bold", Font.PLAIN, 20));
        g.drawString("Score: " + score, WIDTH / 2 - 30, 30);
        g.drawString("High Score: " + highScore, WIDTH / 2 - 45, 60);
        
    }
    
    public void actionPerformed(ActionEvent e) {
        if (!isPaused) {
            birdY += velocity;
            velocity += GRAVITY;
            if (birdY > HEIGHT - BIRD_HEIGHT) {
                birdY = HEIGHT - BIRD_HEIGHT;
            }
            if (birdY < 0) {
                birdY = 0;
                velocity = 0;
            }
            movePoles();
            checkCollision();
            birdAnimationCounter++;
            
            repaint();
        }
    }
    

    private void checkCollision() {
        Rectangle bird = new Rectangle(BIRD_START_X, birdY, BIRD_WIDTH, BIRD_HEIGHT);
        for (Rectangle pole : poles) {
            if (bird.intersects(pole)) {
                isColliding = true;
                birdCollision();
                showGameOverPopup();
                return;
            }
        }
    }

    private void birdCollision() {
        if (score > highScore) {
            highScore = score;
        }
        isGameOver = true;
        repaint();
        
    }

    private void showGameOverPopup() {
        if (!gameOverPopupShownPersistent) { // Check if the popup hasn't been shown yet
            int choice = JOptionPane.showConfirmDialog(null, "Game Over!\nYour Score: " + score +
                    "\nHigh Score: " + highScore + "\nDo you want to play again?", "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                resetGame();
            } else if (choice == JOptionPane.NO_OPTION) {
                gameOverPopupShownPersistent = true; // Set the flag to prevent further popups
                // Close only the game window
                JFrame gameFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                gameFrame.dispose();
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (isGameOver) {
                resetGame();
            } else {
                if (!isPaused) {
                    velocity = JUMP_FORCE;
                    isColliding = false;
                }
            }
        } else if (e.getKeyCode() == KeyEvent.VK_P) {
            if (!isGameOver) {
                isPaused = !isPaused;
            }
        }
    }
    
    public void keyTyped(KeyEvent e) {}
    
    public void keyReleased(KeyEvent e) {}
    
    public void componentResized(ComponentEvent e) {
        WIDTH = getWidth();
        HEIGHT = getHeight();
        resetGame();
    }

    
    public void componentMoved(ComponentEvent e) {}
    
    public void componentShown(ComponentEvent e) {}
    
    public void componentHidden(ComponentEvent e) {}

    private void resetGame() {
        birdY = HEIGHT / 2;
        velocity = 0;
        poles.clear();
        generatePoles();
        score = 0;
        isGameOver = false;
        isColliding = false;
        isPaused = false;
        POLE_MOVE_SPEED = 3.0;
        gameOverPopupShownPersistent = false; // Reset the flag when the game is reset
        System.out.println(gameOverPopupShownPersistent);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            
                Image birdImage = null;
                Image bird1Image = null;
                Image birdCollisionImage = null;
                Image background = null;
                Image upperPipeImage = null;
                Image lowerPipeImage = null;
            
            JFrame frame = new JFrame("Flappy Bird");
            FlappyBirdGame game = new FlappyBirdGame(birdImage, bird1Image, birdCollisionImage,background, upperPipeImage, lowerPipeImage);
            frame.add(game);
            frame.setSize(game.WIDTH, game.HEIGHT);
            frame.dispose();
            frame.setVisible(true);
            frame.setResizable(true);
            frame.addKeyListener(game);
        });
    }
}