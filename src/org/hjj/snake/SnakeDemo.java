package org.hjj.snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

/**
 * 一个简单版本的贪食蛇。
 * Created by hejiajun
 * On 2018/1/18
 */
public class SnakeDemo extends JPanel {
    // 小方格的长和宽。
    private static final int BLOCK_WIDTH = 22;
    private static final int BLOCK_HEIGHT = 22;
    // 小方格的行数和列数。
    private static final int Y = 25;
    private static final int X = 25;
    // 总界面宽度在总网格宽度上面的扩充值。
    private static final int TOTAL_ADD_WIDTH = 140;
    // 总界面高度在总网格高度上面的扩充值。
    private static final int TOTAL_ADD_HEIGHT = 25;
    // 蛇的初始方向默认向上。
    private Direction curDirection = Direction.UP;
    // 键盘按下的方向也同样默认向上。
    private Direction direction = Direction.UP;
    // 蛇的初始长度为2。
    private int length = 2;
    // 蛇头的初始位置。
    private Node head = new Node(12, 12);
    // 蛇最大长度。
    private static final int MAX_LENGTH = 250;
    // 蛇。
    private Node[] snakeBody = new Node[MAX_LENGTH];
    // 食物的坐标。
    private int randomX, randomY;
    // 是否初始化的判断值。
    private boolean isInit = true;
    // 是否在游戏的判断值。
    private boolean isRun = true;
    // 蛇移动线程。
    private Thread move;
    // 移动线程是否应该停止的判断值。
    private boolean stopMove = false;
    // 各种组件。
    private JLabel scoreLabel = new JLabel("当前得分：");
    private JLabel score = new JLabel("0");
    private JLabel timeLabel = new JLabel("所花时间：");
    private JLabel time = new JLabel("00:00:00");
    private JTextArea explain = new JTextArea("说明：一个简单版\n本的贪食蛇，键盘\n的上下左右控制方\n向，ESC重新开始。");
    // 字体。
    private Font font = new Font("宋体",Font.BOLD , 16);
    // 双缓存模式。
    private Image offScreenImage;
    // 计时。
    private int sec = 0;
    private int min = 0;
    private int hour = 0;

    public static void main(String[] args) {
        SnakeDemo snake = new SnakeDemo();
        snake.moveThread();

        JFrame game = new JFrame();
        game.setTitle("贪食蛇");
        game.setSize(BLOCK_WIDTH * X + TOTAL_ADD_WIDTH, BLOCK_HEIGHT * Y + TOTAL_ADD_HEIGHT);
        game.setLocationRelativeTo(null);
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.setResizable(false);

        game.add(snake);
        game.setVisible(true);
    }

    public SnakeDemo() {
        String lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        setLayout(null);

        // 往里加入各种组件。
        add(scoreLabel);
        scoreLabel.setFont(font);
        scoreLabel.setBounds(X * BLOCK_WIDTH + 5, 10, TOTAL_ADD_WIDTH, 20);
        add(score);
        score.setFont(font);
        score.setBounds(X * BLOCK_WIDTH + 5, 35, TOTAL_ADD_WIDTH, 20);
        add(timeLabel);
        timeLabel.setFont(font);
        timeLabel.setBounds(X * BLOCK_WIDTH + 5, 60, TOTAL_ADD_WIDTH, 20);
        add(time);
        time.setFont(font);
        time.setBounds(X * BLOCK_WIDTH + 5, 85, TOTAL_ADD_WIDTH, 20);
        add(explain);
        explain.setFont(font);
        explain.setBounds(X * BLOCK_WIDTH + 5, 110, TOTAL_ADD_WIDTH, 100);
        explain.setLineWrap(true);
        explain.setEditable(false);
        explain.setFocusable(false);

        for (int i = 0; i < MAX_LENGTH; i++) {
            snakeBody[i] = new Node(0, 0);
        }

        // 键盘上下左右控制蛇移动，ESC 重新开始。
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                switch (key) {
                    case KeyEvent.VK_LEFT:
                        if (isRun && Direction.RIGHT != curDirection) {
                            direction = Direction.LEFT;
                        }
                        break;
                    case KeyEvent.VK_UP:
                        if (isRun && Direction.DOWN != curDirection) {
                            direction = Direction.UP;
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (isRun && Direction.LEFT != curDirection) {
                            direction = Direction.RIGHT;
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (isRun && Direction.UP != curDirection) {
                            direction = Direction.DOWN;
                        }
                        break;
                    case KeyEvent.VK_ESCAPE: {
                        if (isRun) {
                            initGame();
                            break;
                        }
                    }
                    default: break;
                }

            }
        });

        // 初始启动计时线程。
        new Thread(new Timer()).start();

        // 聚焦这个组件，不然按键盘没反应。
        setFocusable(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // 抗锯齿。
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        // 蛇头。
        int x = head.getX();
        int y = head.getY();
        g2.setColor(Color.RED);
        g2.fillRoundRect(head.getX() * BLOCK_WIDTH, head.getY() * BLOCK_HEIGHT,
                21, 21, 10, 10);

        g2.setPaint(new GradientPaint(100, 135, Color.CYAN, 200, 135, Color.MAGENTA, true));

        if (isInit) {
            // 初始化蛇。
            for (int i = 0; i < length; i++) {
                y += 1;
                snakeBody[i].setX(x);
                snakeBody[i].setY(y);
                g2.fillRoundRect(snakeBody[i].getX() * BLOCK_WIDTH, snakeBody[i].getY() * BLOCK_HEIGHT,
                        21, 21, 10, 10);
            }

            // 初始化食物位置。
            produceFood();
            g2.fillOval(randomX * BLOCK_WIDTH, randomY * BLOCK_HEIGHT, 21, 21);

            isInit = false;
        } else {
            // 每次刷新蛇。
            for (int i = 0; i < length; i++) {
                g2.fillRoundRect(snakeBody[i].getX() * BLOCK_WIDTH, snakeBody[i].getY() * BLOCK_HEIGHT,
                        21, 21, 10, 10);
            }

            if (eatFood()) {
                produceFood();
                g2.fillOval(randomX * BLOCK_WIDTH, randomY * BLOCK_HEIGHT, 21, 21);
            } else  {
                g2.fillOval(randomX * BLOCK_WIDTH, randomY * BLOCK_HEIGHT, 21, 21);
            }

            // 将界面画成由 Y*X 组合成的方格。
            for (int i = 0; i <= Y; i++) {
                if (i == 0 || i == 25) {
                    g2.setStroke(new BasicStroke(4,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
                } else {
                    g2.setStroke(new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
                }
                g2.drawLine(0, i * BLOCK_HEIGHT, X * BLOCK_WIDTH, i * BLOCK_HEIGHT);
            }
            for (int j = 0; j <= X; j++) {
                if (j == 0 || j == 25) {
                    g2.setStroke(new BasicStroke(4,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
                } else {
                    g2.setStroke(new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
                }
                g2.drawLine(j * BLOCK_WIDTH, 0, j * BLOCK_WIDTH, Y * BLOCK_HEIGHT);
            }
        }
    }

    @Override
    public void update(Graphics g) {
        if (null == offScreenImage)
            offScreenImage = this.createImage(BLOCK_WIDTH * X + TOTAL_ADD_WIDTH, BLOCK_HEIGHT * Y + TOTAL_ADD_HEIGHT);
        Graphics cache = offScreenImage.getGraphics();
        // 先将内容画在虚拟画布上。
        paint(cache);
        // 然后将虚拟画布上的内容一起画在画布上。
        g.drawImage(offScreenImage, 0, 0, null);
    }

    /**
     * 产生食物，产生的食物不能落在蛇上。
     */
    public void produceFood() {
        boolean flag = true;
        Random random = new Random();
        randomX = random.nextInt(25);
        randomY = random.nextInt(25);

        while (flag) {
            // 不能落在蛇头。
            while (head.getX() == randomX && head.getY() == randomY) {
                randomX = random.nextInt(25);
                randomY = random.nextInt(25);
            }
            // 不能落在蛇身。
            for (int i = 0; i < length; i++) {
                if (snakeBody[i].getX() == randomX && snakeBody[i].getY() == randomY) {
                    randomX = random.nextInt(25);
                    randomY = random.nextInt(25);
                    break;
                } else {
                    if (i == length - 1) {
                        flag = false;
                    }
                }
            }
        }

    }

    /**
     * 判断蛇吃了食物没有。
     * @return true：吃了  false：没吃
     */
    private boolean eatFood() {
        if (head.getX() == randomX && head.getY() == randomY) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否撞墙。
     */
    public void hitWall() {
        switch (curDirection) {
            case LEFT: {
                if (head.getX() < 0) {
                    doHitWall();
                }
                break;
            }
            case UP: {
                if (head.getY() < 0) {
                    doHitWall();
                }
                break;
            }
            case RIGHT: {
                if (head.getX() == X) {
                    doHitWall();
                }
                break;
            }
            case DOWN: {
                if (head.getY() == Y) {
                    doHitWall();
                }
                break;
            }
            default: break;
        }
    }

    /**
     * 撞到墙应该处理的逻辑。
     */
    private void doHitWall() {
        isRun = false;
        int result=JOptionPane.showConfirmDialog(
                null, "Game over! Try again?",
                "Information", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            initGame();
        } else {
            stopMove = true;
        }
    }

    /**
     * 判读是否撞到自己。
     */
    public void hitSelf() {
        for (int i = 0; i < length; i++) {
            if (head.getX() == snakeBody[i].getX() && head.getY() == snakeBody[i].getY()) {
                isRun = false;
                int result=JOptionPane.showConfirmDialog(
                        null, "Game over! Try again?",
                        "Information", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    initGame();
                } else {
                    stopMove = true;
                }
            }
        }
    }

    /**
     * 初始游戏，把所有数据置为最初值。
     */
    private void initGame() {
        curDirection = Direction.UP;
        direction = Direction.UP;
        head = new Node(12, 12);
        length = 2;
        isInit = true;
        isRun = true;
        stopMove = false;

        score.setText("" + (length - 2));
        time.setText("00:00:00");
        new Thread(new Timer()).start();

        for (int i = 0; i < MAX_LENGTH; i++) {
            snakeBody[i].setX(0);
            snakeBody[i].setY(0);
        }
    }

    private void moveThread() {
        long mills = 300;
        move = new Thread(() -> {
            while (!stopMove) {

                try {
                    Thread.sleep(mills);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int temp1_X = head.getX();
                int temp1_Y = head.getY();
                // 头部移动。
                switch (direction) {
                    case LEFT: {
                        head.setX(head.getX() - 1);
                        break;
                    }
                    case UP: {
                        head.setY(head.getY() - 1);
                        break;
                    }
                    case RIGHT: {
                        head.setX(head.getX() + 1);
                        break;
                    }
                    case DOWN: {
                        head.setY(head.getY() + 1);
                        break;
                    }
                    default: break;
                }
                // 刷新当前方向。
                curDirection = direction;
                // 身体移动。
                int temp2_X = 0;
                int temp2_Y = 0;
                for (int i = 0; i < length; i++) {
                    temp2_X = snakeBody[i].getX();
                    temp2_Y = snakeBody[i].getY();
                    snakeBody[i].setX(temp1_X);
                    snakeBody[i].setY(temp1_Y);
                    temp1_X = temp2_X;
                    temp1_Y = temp2_Y;
                }
                // 蛇吃了食物变成。
                if (eatFood()) {
                    length++;
                    snakeBody[length - 1].setX(temp2_X);
                    snakeBody[length - 1].setY(temp2_Y);
                    score.setText("" + (length - 2));
                }

                // 判断游戏是否失败。
                hitWall();
                hitSelf();

                repaint();
            }
        });

        move.start();
    }

    /**
     * 计时的内部类。
     */
    private class Timer implements Runnable {
        @Override
        public void run() {
            long mills = 1000;

            while (isRun) {
                try {
                    Thread.sleep(mills);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                sec++;
                if (sec >= 60) {
                    sec = 0;
                    min++;
                }
                if (min >= 60) {
                    min = 0;
                    hour++;
                }

                showTime();
            }
        }

        private void showTime() {
            String timer = "";

            if (hour < 10)
                timer = "0" + hour + ":";
            else
                timer = hour + ":";

            if (min < 10)
                timer += "0" + min + ":";
            else
                timer += min + ":";

            if (sec < 10)
                timer += "0" + sec;
            else
                timer += sec;

            time.setText(timer);
        }
    }

}
