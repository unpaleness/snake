package com.example.snake;

public class Point2D implements Cloneable {
    public int x = 0;
    public int y = 0;

    public Point2D() {}

    public Point2D(int newX, int newY) {
        x = newX;
        y = newY;
    }

    @Override
    protected Point2D clone() {
        return new Point2D(x, y);
    }

    @Override
    public boolean equals(Object other) {
        if (super.equals(other)) {
            return true;
        }

        if (!(other instanceof Point2D otherPoint2D)) {
            return false;
        }

        return x == otherPoint2D.x && y == otherPoint2D.y;
    }
}
