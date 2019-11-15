package me.desht.pneumaticcraft.common.thirdparty.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;

public class ResourceDrawable implements IDrawable {

    private final ResourceLocation resource;
    private final int x, y, u, v, drawWidth, drawHeight, texWidth, texHeight;

    public ResourceDrawable(ResourceLocation resource, int x, int y, int u, int v, int drawWidth, int drawHeight,
                            int texWidth, int texHeight) {
        this.resource = resource;
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
        this.drawWidth = drawWidth;
        this.drawHeight = drawHeight;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }

    public ResourceDrawable(ResourceLocation texture, int x, int y, int u, int v, int drawWidth, int drawHeight) {
        this(texture, x, y, u, v, drawWidth, drawHeight, 256, 256);
    }

    @Override
    public int getWidth() {
        return drawWidth;
    }

    @Override
    public int getHeight() {
        return drawHeight;
    }

    public ResourceLocation getResource() {
        return resource;
    }

    @Override
    public void draw(int xOffset, int yOffset) {
        Minecraft.getInstance().getTextureManager().bindTexture(resource);
        AbstractGui.blit(x + xOffset, y + yOffset, u, v, drawWidth, drawHeight, texWidth, texHeight);
    }
}
