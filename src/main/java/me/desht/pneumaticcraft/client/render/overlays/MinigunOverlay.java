package me.desht.pneumaticcraft.client.render.overlays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.item.minigun.MinigunItem;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

public class MinigunOverlay implements IIngameOverlay {
    private static final float MINIGUN_TEXT_SIZE = 0.55f;

    @Override
    public void render(ForgeIngameGui gui, PoseStack matrixStack, float partialTicks, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof MinigunItem itemMinigun) || !Minecraft.getInstance().options.getCameraType().isFirstPerson())
            return;
        ItemStack heldStack = player.getMainHandItem();
        Minigun minigun = itemMinigun.getMinigun(heldStack, player);

        ItemStack ammo = minigun.getAmmoStack();
        if (!ammo.isEmpty()) {
            Minecraft.getInstance().getItemRenderer().renderGuiItem(ammo,width / 2 + 16, height / 2 - 7);
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

    @Mod.EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
    public static class Listener {
        @SubscribeEvent
        public static void crosshairsEvent(RenderGameOverlayEvent.PreLayer event) {
            boolean firstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
            if (event.getOverlay() == ForgeIngameGui.CROSSHAIR_ELEMENT
                    && ClientUtils.getClientPlayer().getMainHandItem().getItem() instanceof MinigunItem
                    && firstPerson) {
                event.setCanceled(true);
            }
        }
    }
}
