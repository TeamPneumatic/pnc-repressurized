package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;

public class GuiStatBalloon {
    public final int x, y;
    int slotNumber;
    public String text = "";

    private GuiStatBalloon(int x, int y) {
        this.x = x;
        this.y = y;
    }

    GuiStatBalloon(int x, int y, int slotNumber) {
        this(x, y);
        this.slotNumber = slotNumber;
    }

    public void render(MatrixStack matrixStack) {
        Minecraft.getInstance().font.draw(matrixStack, text, x, y, -90);
    }

}
