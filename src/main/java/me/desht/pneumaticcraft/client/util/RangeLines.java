package me.desht.pneumaticcraft.client.util;

import net.minecraft.util.math.BlockPos;

import java.util.*;

public class RangeLines {
    private final List<ProgressingLine> rangeLines = new ArrayList<>();
    private int rangeLinesTimer = 0;
    private final int color;
    private final BlockPos pos;

    public RangeLines(int color) {
        this(color, null);
    }

    public RangeLines(int color, BlockPos pos) {
        this.color = color;
        this.pos = pos;
    }

    public int getColor() {
        return color;
    }

    public Collection<ProgressingLine> getLines() {
        return rangeLines;
    }

    public BlockPos getPos() {
        return pos;
    }

    public void startRendering(double range) {
        rangeLinesTimer = 120;

        rangeLines.clear();
        double r = range + 0.5D;
        for (int i = 0; i < range * 16 + 8; i++) {
            // Add the vertical lines of the walls
            rangeLines.add(new ProgressingLine(-r + i / 8D, -r + 1, -r, -r + i / 8D, r + 1, -r));
            rangeLines.add(new ProgressingLine(r - i / 8D, -r + 1, r, r - i / 8D, r + 1, r));
            rangeLines.add(new ProgressingLine(-r, -r + 1, r - i / 8D, -r, r + 1, r - i / 8D));
            rangeLines.add(new ProgressingLine(r, -r + 1, -r + i / 8D, r, r + 1, -r + i / 8D));

            // Add the horizontal lines of the walls
            rangeLines.add(new ProgressingLine(-r, -r + i / 8D + 1, -r, -r, -r + i / 8D + 1, r));
            rangeLines.add(new ProgressingLine(r, -r + i / 8D + 1, -r, r, -r + i / 8D + 1, r));
            rangeLines.add(new ProgressingLine(-r, r - i / 8D + 1, -r, r, r - i / 8D + 1, -r));
            rangeLines.add(new ProgressingLine(-r, -r + i / 8D + 1, r, r, -r + i / 8D + 1, r));

            // Add the roof and floor
            rangeLines.add(new ProgressingLine(r - i / 8D, -r + 1, -r, r - i / 8D, -r + 1, r));
            rangeLines.add(new ProgressingLine(r - i / 8D, r + 1, -r, r - i / 8D, r + 1, r));
            rangeLines.add(new ProgressingLine(-r, -r + 1, -r + i / 8D, r, -r + 1, -r + i / 8D));
            rangeLines.add(new ProgressingLine(-r, r + 1, -r + i / 8D, r, r + 1, -r + i / 8D));

        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean shouldRender() {
        return rangeLines.size() > 0;
    }

    public void tick(Random rand) {
        if (rangeLinesTimer > 0) {
            rangeLinesTimer--;
            for (ProgressingLine line : rangeLines) {
                if (line.getProgress() > 0.005F || rand.nextInt(15) == 0) {
                    line.incProgress(0.025F);
                }
            }
        } else {
            Iterator<ProgressingLine> iterator = rangeLines.iterator();
            while (iterator.hasNext()) {
                ProgressingLine line = iterator.next();
                if (line.getProgress() > 0.005F) {
                    line.incProgress(0.025F);
                }
                if (rand.nextInt(8) == 0) {
                    iterator.remove();
                }
            }
        }
    }
}
