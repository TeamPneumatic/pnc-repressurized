package me.desht.pneumaticcraft.client.render.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class MinigunOverlay implements IIngameOverlay {
    private static final float MINIGUN_TEXT_SIZE = 0.55f;

    @Override
    public void render(ForgeIngameGui gui, PoseStack matrixStack, float partialTicks, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof ItemMinigun itemMinigun) || !Minecraft.getInstance().options.getCameraType().isFirstPerson())
            return;
        ItemStack heldStack = player.getMainHandItem();
        Minigun minigun = itemMinigun.getMinigun(heldStack, player);

//        if (minigun.isMinigunActivated() && minigun.getMinigunSpeed() == Minigun.MAX_GUN_SPEED) {
//            drawBulletTraces2D(player.getRandom(), minigun.getAmmoColor() | 0x40000000, width, height);
//        }

        ItemStack ammo = minigun.getAmmoStack();
        if (!ammo.isEmpty()) {
            GuiUtils.renderItemStack(matrixStack, ammo,width / 2 + 16, height / 2 - 7);
            int remaining = ammo.getMaxDamage() - ammo.getDamageValue();
            matrixStack.pushPose();
            matrixStack.translate(width / 2f + 32, height / 2f - 1, 0);
            matrixStack.scale(MINIGUN_TEXT_SIZE, MINIGUN_TEXT_SIZE, 1f);
            String text = remaining + "/" + ammo.getMaxDamage();
            mc.font.draw(matrixStack, text, 1, 0, 0);
            mc.font.draw(matrixStack, text, -1, 0, 0);
            mc.font.draw(matrixStack, text, 0, 1, 0);
            mc.font.draw(matrixStack, text, 0, -1, 0);
            mc.font.draw(matrixStack, text, 0, 0, minigun.getAmmoColor());
            matrixStack.popPose();
        }
        GuiUtils.bindTexture(Textures.MINIGUN_CROSSHAIR);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(0.2f, 0.7f, 0.2f, 0.75f);
        GuiComponent.blit(matrixStack, width / 2 - 7, height / 2 - 7, 0, 0, 16, 16, 16, 16);
    }


    private static void drawBulletTraces2D(Random rand, int color, int w, int h) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        int x = w / 2;
        int y = h / 2;

        int[] cols = RenderUtils.decomposeColor(color);
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        float f = Minecraft.getInstance().options.mainHand == HumanoidArm.RIGHT ? 0.66F : 0.335F;
        float endX = w * f;
        float endY = h * 0.68F;
        for (int i = 0; i < 5; i++) {
            bb.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            bb.vertex(x + rand.nextInt(12) - 6, y + rand.nextInt(12) - 6, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            bb.vertex(endX, endY, 0).color(cols[1], cols[2], cols[3], cols[0]).endVertex();
            Tesselator.getInstance().end();
        }
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
    public static class Listener {
        @SubscribeEvent
        public static void crosshairsEvent(RenderGameOverlayEvent.PreLayer event) {
            boolean firstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
            if (event.getOverlay() == ForgeIngameGui.CROSSHAIR_ELEMENT
                    && ClientUtils.getClientPlayer().getMainHandItem().getItem() instanceof ItemMinigun
                    && firstPerson) {
                event.setCanceled(true);
            }
        }
    }
}
