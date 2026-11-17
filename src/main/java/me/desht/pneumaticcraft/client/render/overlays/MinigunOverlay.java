package me.desht.pneumaticcraft.client.render.overlays;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.item.minigun.MinigunItem;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class MinigunOverlay implements GuiLayer {
    private static final float MINIGUN_TEXT_SIZE = 0.55f;

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        Player player = mc.player;

        if (player == null || !(player.getMainHandItem().getItem() instanceof MinigunItem itemMinigun) || !Minecraft.getInstance().options.getCameraType().isFirstPerson())
            return;
        ItemStack heldStack = player.getMainHandItem();
        Minigun minigun = itemMinigun.getMinigun(heldStack, player);

        ItemStack ammo = minigun.getAmmoStack();
        if (!ammo.isEmpty()) {
            graphics.item(ammo, width / 2 + 16, height / 2 - 7);
            int remaining = ammo.getMaxDamage() - ammo.getDamageValue();
            graphics.pose().pushMatrix();
            graphics.pose().translate(width / 2f + 32, height / 2f - 1);
            graphics.pose().scale(MINIGUN_TEXT_SIZE, MINIGUN_TEXT_SIZE);
            String text = remaining + "/" + ammo.getMaxDamage();
            graphics.text(mc.font, text, 1, 0, 0xFF000000, false);
            graphics.text(mc.font, text, -1, 0, 0xFF000000, false);
            graphics.text(mc.font, text, 0, 1, 0xFF000000, false);
            graphics.text(mc.font, text, 0, -1, 0xFF000000, false);
            graphics.text(mc.font, text, 0, 0, GuiUtils.textColor(minigun.getAmmoColor()), false);
            graphics.pose().popMatrix();
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, Textures.MINIGUN_CROSSHAIR, width / 2 - 7, height / 2 - 7, 0, 0, 16, 16, 16, 16, ARGB.colorFromFloat(0.75f, 0.2f, 0.7f, 0.2f));
    }

    @EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
    public static class Listener {
        @SubscribeEvent
        public static void crosshairsEvent(RenderGuiLayerEvent.Pre event) {
            boolean firstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
            if (event.getName().equals(VanillaGuiLayers.CROSSHAIR)
                    && ClientUtils.getClientPlayer().getMainHandItem().getItem() instanceof MinigunItem
                    && firstPerson) {
                event.setCanceled(true);
            }
        }
    }
}
