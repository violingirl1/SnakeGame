import javax.swing.JFrame;

public class SnakeGame extends JFrame {
    public SnakeGame() {
        setTitle("Snake Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        add(new GamePanel());

        setSize(600, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
