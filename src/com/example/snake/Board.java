package com.example.snake;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Graphics;
import java.awt.Toolkit;

public class Board extends JPanel implements ActionListener {

    static private final Point2D cellsAmount = new Point2D(10, 10);
    static private final int CELL_SIZE = 64;
    static private final int TICK_MS = 20;
    static private final int PREPARE_TIME_MS = 3000;
    static private final long SNAKE_STEP_MS = 200;

    private final Timer timer;
    private Snake snake;
    private Apple apple;
    private GameState gameState = GameState.IN_MENU;
    private long prepareMatchTimestamp;
    private long previousSnakeStepTimestamp;
    static private final Font font = new Font("Default", Font.BOLD, 32);
    private final FontMetrics fontMetrics = getFontMetrics(font);

    public Board() {
        setBackground(Color.black);
        setFocusable(true);
        setPreferredSize(new Dimension(cellsAmount.x * CELL_SIZE, cellsAmount.y * CELL_SIZE));

        addKeyListener(new KeyHandler(this));

        timer = new Timer(TICK_MS, this);
        timer.start();

        prepareForMatch();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        snake.draw(graphics, this);
        apple.draw(graphics, this);

        if (gameState == GameState.PRE_MATCH) {
            long timeToMatchStartMS = prepareMatchTimestamp + PREPARE_TIME_MS - System.currentTimeMillis();
            String msg = String.valueOf((int) Math.ceil((float) timeToMatchStartMS / 1000));
            paintText(graphics, msg, Color.red);
        }

        if (gameState == GameState.PAUSE) {
            paintText(graphics, "Game paused", Color.white);
        }

        if (gameState == GameState.POST_MATCH) {
            paintText(graphics, "Game Over, press R to restart", Color.white);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        long currentTimestamp = System.currentTimeMillis();
        switch (gameState) {
            case PRE_MATCH:
                if (currentTimestamp - prepareMatchTimestamp >= PREPARE_TIME_MS) {
                    gameState = GameState.IN_PROGRESS;
                }
                break;
            case IN_PROGRESS:
                if (currentTimestamp - previousSnakeStepTimestamp >= SNAKE_STEP_MS) {
                    if (!snake.tryMove(cellsAmount, apple.getLocation())) {
                        endMatch();
                    }
                    if (apple.getLocation().equals(snake.getHeadPos())) {
                        if (!apple.replace(snake)) {
                            endMatch();
                        }
                    }
                    previousSnakeStepTimestamp = currentTimestamp;
                }
                break;
            default:
                break;
        }
        repaint();
    }

    public void prepareForMatch() {
        prepareMatchTimestamp = System.currentTimeMillis();
        previousSnakeStepTimestamp = System.currentTimeMillis();

        gameState = GameState.PRE_MATCH;

        snake = new Snake(new Point2D(cellsAmount.x / 2, cellsAmount.y / 2), CELL_SIZE);
        apple = new Apple(cellsAmount, CELL_SIZE);
        if (!apple.replace(snake)) {
            endMatch();
        }
    }

    public void tryRestart() {
        if (gameState == GameState.POST_MATCH) {
            prepareForMatch();
        }
    }

    public void processPause() {
        switch (gameState) {
            case IN_PROGRESS:
                gameState = GameState.PAUSE;
                break;
            case PAUSE:
                gameState = GameState.IN_PROGRESS;
                break;
            default:
                break;
        }
    }

    public void addNextDirection(Direction nextDirection) {
        if (gameState == GameState.IN_PROGRESS) {
            snake.tryAddNextDirection(nextDirection);
        }
    }

    private void endMatch() {
        gameState = GameState.POST_MATCH;
    }

    private void paintText(Graphics graphics, String msg, Color color) {
        graphics.setColor(color);
        graphics.setFont(font);
        graphics.drawString(msg, (cellsAmount.x * CELL_SIZE - fontMetrics.stringWidth(msg)) / 2,
                cellsAmount.y * CELL_SIZE / 2);
    }
}
