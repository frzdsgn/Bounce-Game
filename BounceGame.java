import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

public class BounceGame extends JFrame {
    private GamePanel gamePanel;
    private Timer gameTimer;
    private final int WINDOW_WIDTH = 800;
    private final int WINDOW_HEIGHT = 600;
    
    public BounceGame() {
        setTitle("Bounce Game");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        gamePanel = new GamePanel();
        add(gamePanel);
        
        gameTimer = new Timer(1000/60, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gamePanel.update();
                gamePanel.repaint();
            }
        });
        gameTimer.start();
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                gamePanel.handleKeyPress(e);
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                gamePanel.handleKeyRelease(e);
            }
        });
        
        setFocusable(true);
    }
    
    static class Platform {
        int x, y, width, height;
        Color color;
        boolean isMoving;
        boolean hasSpike;
        int moveDirection = 1;
        int moveSpeed = 2;
        int originalX;
        int moveRange = 200;
        
        Platform(int x, int y, int width, int height, Color color, boolean isMoving, boolean hasSpike) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.isMoving = isMoving;
            this.hasSpike = hasSpike;
            this.originalX = x;
        }
        
        Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }
        
        Rectangle getSpikeBounds() {
            if (hasSpike) {
                return new Rectangle(x, y - 10, width, 10);
            }
            return new Rectangle(0, 0, 0, 0);
        }
        
        void move() {
            if (isMoving) {
                x += moveSpeed * moveDirection;
                if (x > originalX + moveRange || x < originalX - moveRange) {
                    moveDirection *= -1;
                }
            }
        }
    }
    
    static class Coin {
        int x, y;
        static final int SIZE = 15;
        boolean isCollected = false;
        
        Coin(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        Rectangle getBounds() {
            return new Rectangle(x, y, SIZE, SIZE);
        }
    }
    
    class GamePanel extends JPanel {
        private int ballX = 100;
        private int ballY = 100;
        private int ballSize = 30;
        private double velocityY = 0;
        private double velocityX = 0;
        private boolean isJumping = false;
        private final double GRAVITY = 0.5;
        private final double JUMP_FORCE = -12;
        private final double BOUNCE_FACTOR = -0.7;
        
        private ArrayList<Platform> platforms;
        private ArrayList<Coin> coins;
        private int score = 0;
        private int currentLevel = 1;
        private int lives = 3;
        private boolean gameOver = false;
        
        public GamePanel() {
            setBackground(Color.WHITE);
            initializeLevel(currentLevel);
        }
        
        private void initializeLevel(int level) {
            platforms = new ArrayList<>();
            coins = new ArrayList<>();
            
            switch(level) {
                case 1:
                    // Level 1 layout
                    platforms.add(new Platform(300, 400, 200, 20, Color.BLUE, false, false));
                    platforms.add(new Platform(100, 300, 200, 20, Color.BLUE, true, false));
                    platforms.add(new Platform(500, 200, 200, 20, Color.BLUE, false, true));
                    
                    coins.add(new Coin(350, 350));
                    coins.add(new Coin(150, 250));
                    coins.add(new Coin(550, 150));
                    break;
                    
                case 2:
                    // Level 2 layout - more difficult
                    platforms.add(new Platform(200, 450, 150, 20, Color.BLUE, true, true));
                    platforms.add(new Platform(400, 350, 150, 20, Color.BLUE, true, false));
                    platforms.add(new Platform(600, 250, 150, 20, Color.BLUE, false, true));
                    platforms.add(new Platform(200, 150, 150, 20, Color.BLUE, true, true));
                    
                    coins.add(new Coin(250, 400));
                    coins.add(new Coin(450, 300));
                    coins.add(new Coin(650, 200));
                    coins.add(new Coin(250, 100));
                    break;
                
                case 3:
                    // Level 3 layout - even more difficult
                    platforms.add(new Platform(100, 500, 100, 20, Color.BLUE, true, true));
                    platforms.add(new Platform(300, 400, 100, 20, Color.BLUE, true, true));
                    platforms.add(new Platform(500, 300, 100, 20, Color.BLUE, true, false));
                    platforms.add(new Platform(700, 200, 100, 20, Color.BLUE, true, true));
                    platforms.add(new Platform(300, 100, 100, 20, Color.BLUE, false, true));
                    
                    coins.add(new Coin(150, 450));
                    coins.add(new Coin(350, 350));
                    coins.add(new Coin(550, 250));
                    coins.add(new Coin(750, 150));
                    coins.add(new Coin(350, 50));
                    break;
            }
            
            // Reset ball position
            ballX = 100;
            ballY = 100;
            velocityX = 0;
            velocityY = 0;
        }
        
        public void update() {
            if (gameOver) return;
            
            // Update platforms
            for (Platform platform : platforms) {
                platform.move();
            }
            
            // Update physics
            velocityY += GRAVITY;
            
            // Update position Y
            double nextY = ballY + velocityY;
            Rectangle nextBallBounds = new Rectangle(ballX, (int)nextY, ballSize, ballSize);
            
            boolean collision = false;
            
            // Check platform collisions
            for (Platform platform : platforms) {
                // Check spike collision
                if (platform.hasSpike && nextBallBounds.intersects(platform.getSpikeBounds())) {
                    lives--;
                    if (lives <= 0) {
                        gameOver = true;
                    } else {
                        // Reset position
                        ballX = 100;
                        ballY = 100;
                        velocityX = 0;
                        velocityY = 0;
                    }
                    return;
                }
                
                // Check platform collision
                if (nextBallBounds.intersects(platform.getBounds())) {
                    if (velocityY > 0 && ballY + ballSize <= platform.y) {
                        ballY = platform.y - ballSize;
                        velocityY *= BOUNCE_FACTOR; // Bounce effect
                        isJumping = false;
                        collision = true;
                    } else if (velocityY < 0 && ballY >= platform.y + platform.height) {
                        ballY = platform.y + platform.height;
                        velocityY = 0;
                        collision = true;
                    }
                }
            }
            
            if (!collision) {
                ballY = (int)nextY;
            }
            
            // Update position X
            ballX += velocityX;
            
            // Collect coins
            Iterator<Coin> coinIterator = coins.iterator();
            while (coinIterator.hasNext()) {
                Coin coin = coinIterator.next();
                if (!coin.isCollected && new Rectangle(ballX, ballY, ballSize, ballSize).intersects(coin.getBounds())) {
                    coin.isCollected = true;
                    score += 100;
                    coinIterator.remove();
                }
            }
            
            // Check if level is complete
            if (coins.isEmpty()) {
                currentLevel++;
                if (currentLevel <= 3) {
                    initializeLevel(currentLevel);
                } else {
                    gameOver = true;
                }
            }
            
            // Ground collision
            if (ballY > WINDOW_HEIGHT - ballSize - 50) {
                ballY = WINDOW_HEIGHT - ballSize - 50;
                velocityY *= BOUNCE_FACTOR; // Bounce effect
                isJumping = false;
            }
            
            // Wall collision
            if (ballX < 0) {
                ballX = 0;
            }
            if (ballX > WINDOW_WIDTH - ballSize) {
                ballX = WINDOW_WIDTH - ballSize;
            }
        }
        
        public void handleKeyPress(KeyEvent e) {
            if (gameOver) return;
            
            switch(e.getKeyCode()) {
                case KeyEvent.VK_SPACE:
                    if (!isJumping) {
                        velocityY = JUMP_FORCE;
                        isJumping = true;
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    velocityX = -5;
                    break;
                case KeyEvent.VK_RIGHT:
                    velocityX = 5;
                    break;
            }
        }
        
        public void handleKeyRelease(KeyEvent e) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_RIGHT:
                    velocityX = 0;
                    break;
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Draw platforms and spikes
            for (Platform platform : platforms) {
                // Draw platform
                g2d.setColor(platform.color);
                g2d.fillRect(platform.x, platform.y, platform.width, platform.height);
                
                // Draw spikes
                if (platform.hasSpike) {
                    g2d.setColor(Color.RED);
                    int[] xPoints = new int[3];
                    int[] yPoints = new int[3];
                    for (int i = 0; i < platform.width; i += 10) {
                        xPoints[0] = platform.x + i;
                        xPoints[1] = platform.x + i + 5;
                        xPoints[2] = platform.x + i + 10;
                        yPoints[0] = platform.y;
                        yPoints[1] = platform.y - 10;
                        yPoints[2] = platform.y;
                        g2d.fillPolygon(xPoints, yPoints, 3);
                    }
                }
            }
            
            // Draw coins
            g2d.setColor(Color.YELLOW);
            for (Coin coin : coins) {
                if (!coin.isCollected) {
                    g2d.fillOval(coin.x, coin.y, Coin.SIZE, Coin.SIZE);
                }
            }
            
            // Draw ground
            g2d.setColor(Color.GREEN);
            g2d.fillRect(0, WINDOW_HEIGHT - 50, WINDOW_WIDTH, 50);
            
            // Draw ball
            g2d.setColor(Color.RED);
            g2d.fillOval(ballX, ballY, ballSize, ballSize);
            
            // Draw score and lives
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("Score: " + score, 20, 30);
            g2d.drawString("Level: " + currentLevel, 20, 60);
            g2d.drawString("Lives: " + lives, 20, 90);
            
            // Draw game over message
            if (gameOver) {
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 40));
                String message = currentLevel > 3 ? "You Win!" : "Game Over!";
                g2d.drawString(message, WINDOW_WIDTH/2 - 100, WINDOW_HEIGHT/2);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BounceGame game = new BounceGame();
            game.setVisible(true);
        });
    }
}