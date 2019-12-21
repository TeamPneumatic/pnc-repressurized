package me.desht.pneumaticcraft.client.util;

import java.util.Objects;

public class PointXY {
    public final int x;
    public final int y;

    public static final PointXY ZERO = new PointXY(0, 0);

    public PointXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointXY)) return false;
        PointXY pointXY = (PointXY) o;
        return x == pointXY.x &&
                y == pointXY.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public double distance(PointXY p2) {
        double px = p2.x - this.x;
        double py = p2.y - this.y;
        return Math.sqrt(px * px + py * py);
    }
}
