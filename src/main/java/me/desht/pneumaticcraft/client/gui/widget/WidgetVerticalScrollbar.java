package me.desht.pneumaticcraft.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.render.ModRenderTypes;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.Validate;

import static me.desht.pneumaticcraft.client.util.RenderUtils.*;

public class WidgetVerticalScrollbar extends Widget implements ICanRender3d {
    public float currentScroll;
    private int states;
    private boolean listening;
    private boolean dragging;

    public WidgetVerticalScrollbar(int x, int y, int height) {
        super(x, y, 14, height, StringTextComponent.EMPTY);
    }

    public WidgetVerticalScrollbar setStates(int states) {
        this.states = states;
        return this;
    }

    public WidgetVerticalScrollbar setCurrentState(int state) {
        Validate.isTrue(state >= 0 && state <= states, "State " + state + " out of range! Valid range [1 - " + states + "] inclusive");
        currentScroll = (float) state / states;
        return this;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dir) {
        if (active && listening) {
            double wheel = MathHelper.clamp(-dir, -1, 1);
            currentScroll = MathHelper.clamp(currentScroll + (float) wheel / states,0f, 1);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(double x, double y) {
        currentScroll = (float) (y - 7 - this.y) / (height - 17);
        currentScroll = MathHelper.clamp(currentScroll, 0, 1);
        dragging = true;
    }

    @Override
    public void onRelease(double x, double y) {
        dragging = false;
    }

    @Override
    protected void onDrag(double x, double y, double dx, double dy) {
        dragging = true;
        currentScroll = (float) (y - 7 - this.y) / (height - 17);
        currentScroll = MathHelper.clamp(currentScroll, 0, 1);
    }

    public WidgetVerticalScrollbar setListening(boolean listening) {
        this.listening = listening;
        return this;
    }

    public int getState() {
        float scroll = currentScroll;
        scroll += 0.5F / states;
        return MathHelper.clamp((int) (scroll * states), 0, states);
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (visible) {
            RenderSystem.color4f(1, 1, 1, 1);
            Minecraft.getInstance().getTextureManager().bindTexture(Textures.WIDGET_VERTICAL_SCROLLBAR);
            blit(matrixStack, x, y, 12, 0, width, 1, 26, 15);
            for (int i = 0; i < height - 2; i++)
                blit(matrixStack, x, y + 1 + i, 12, 1, width, 1, 26, 15);
            blit(matrixStack, x, y + height - 1, 12, 14, width, 1, 26, 15);

            if (!active) RenderSystem.color4f(0.6F, 0.6F, 0.6F, 1);
            blit(matrixStack, x + 1, y + 1 + (int) ((height - 17) * currentScroll), 0, 0, 12, 15, 26, 15);
            RenderSystem.color4f(1, 1, 1, 1);
        }
    }

    public boolean isDragging() {
        return dragging;
    }

    @Override
    public void render3d(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        if (visible) {
            renderWithType(matrixStack, buffer, ModRenderTypes.getTextureRenderColored(Textures.WIDGET_VERTICAL_SCROLLBAR, true), (posMat, builder)-> {
                blit3d(builder, posMat, x, y, 12, 0, width, 1, 26, 15);
                for (int i = 0; i < height - 2; i++) {
                    blit3d(builder, posMat, x, y + 1 + i, 12, 1, width, 1, 26, 15);
                }
                blit3d(builder, posMat, x, y + height - 1, 12, 14, width, 1, 26, 15);
                blit3d(builder, posMat, x + 1, y + 1 + (int) ((height - 17) * currentScroll), 0, 0, 12, 15, 26, 15);
            });
        }
    }

    private void blit3d(IVertexBuilder builder, Matrix4f posMat, int x, int y, int textureX, int textureY, int width, int height, int textureWidth, int textureHeight) {
        float u1 = (float) textureX / textureWidth;
        float u2 = (float) (textureX + width) / textureWidth;
        float v1 = (float) textureY / textureHeight;
        float v2 = (float) (textureY + height) / textureHeight;

        posF(builder, posMat, x, y + height, 0)
                .color(255, 255, 255, 255)
                .tex(u1, v2)
                .lightmap(FULL_BRIGHT)
                .endVertex();
        posF(builder, posMat, x + width, y + height, 0)
                .color(255, 255, 255, 255)
                .tex(u2, v2)
                .lightmap(FULL_BRIGHT)
                .endVertex();
        posF(builder, posMat, x + width, y, 0)
                .color(255, 255, 255, 255)
                .tex(u2, v1)
                .lightmap(FULL_BRIGHT)
                .endVertex();
        posF(builder, posMat, x, y, 0)
                .color(255, 255, 255, 255)
                .tex(u1, v1)
                .lightmap(FULL_BRIGHT)
                .endVertex();
    }

}
