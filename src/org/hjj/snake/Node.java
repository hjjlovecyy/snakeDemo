package org.hjj.snake;

import java.awt.*;

/**
 * 蛇的节点
 * Created by hejiajun
 * On 2018/1/19
 */
public class Node {
    // 每个节点的位置。
    private int x;
    private int y;

    public Node(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
