package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.api.item.IPressurizable;
import me.desht.pneumaticcraft.client.IKeyListener;
import me.desht.pneumaticcraft.client.KeyHandler;
import me.desht.pneumaticcraft.client.gui.pneumaticHelmet.GuiHelmetMainScreen;
import me.desht.pneumaticcraft.client.gui.widget.GuiKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.RenderProgressBar;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmorBase;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketToggleHelmetFeature;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class HUDHandler implements IKeyListener {

    private final List<ArmorMessage> messageList = new ArrayList<>();
    private boolean[] gaveEmptyWarning = new boolean[4];  // per-slot
    private boolean[] gaveNearlyEmptyWarning = new boolean[4];  // per-slot

    private static final HUDHandler INSTANCE = new HUDHandler();

    public static HUDHandler instance() {
        return INSTANCE;
    }

    public <T extends IUpgradeRenderHandler> T getSpecificRenderer(Class<T> clazz) {
        return UpgradeRenderHandlerList.instance().getRenderHandler(clazz);
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        if (!GuiKeybindCheckBox.getCoreComponents().checked) return;

        Minecraft mc = FMLClientHandler.instance().getClient();
        EntityPlayer player = mc.player;
        double playerX = player.prevPosX + (player.posX - player.prevPosX) * event.getPartialTicks();
        double playerY = player.prevPosY + (player.posY - player.prevPosY) * event.getPartialTicks();
        double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * event.getPartialTicks();

        GL11.glPushMatrix();
        GL11.glTranslated(-playerX, -playerY, -playerZ);

        for (EntityEquipmentSlot slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            ItemStack armorStack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            if (armorStack.getItem() instanceof ItemPneumaticArmorBase) {
                if (((IPressurizable) armorStack.getItem()).getPressure(armorStack) > 0F) {
                    CommonHUDHandler comHudHandler = CommonHUDHandler.getHandlerForPlayer(player);
                    if (comHudHandler.getTicksSinceEquipped(slot) > comHudHandler.getStartupTime(slot)) {

                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
                        for (int i = 0; i < renderHandlers.size(); i++) {
                            if (comHudHandler.isUpgradeRendererInserted(slot, i) && GuiKeybindCheckBox.fromKeyBindingName(GuiKeybindCheckBox.UPGRADE_PREFIX + renderHandlers.get(i).getUpgradeName()).checked)
                                renderHandlers.get(i).render3D(event.getPartialTicks());
                        }

                        GL11.glEnable(GL11.GL_TEXTURE_2D);

                    }
                }
            }
        }
        GL11.glPopMatrix();
    }

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = FMLClientHandler.instance().getClient();
            if (mc != null && mc.player != null) {
                for (EntityEquipmentSlot slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
                    render2D(event.renderTickTime, slot);
                }
            }
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = FMLClientHandler.instance().getClient();
            EntityPlayer player = event.player;
            if (player == mc.player && player.world.isRemote) {
                for (EntityEquipmentSlot slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
                    if (player.getItemStackFromSlot(slot).getItem() instanceof ItemPneumaticArmorBase) {
                        update(mc.player, slot);
                    } else {
                        CommonHUDHandler.getHandlerForPlayer(player).resetTicksSinceEquip(slot);
                    }
                }
            }
        }
    }

    private void render2D(float partialTicks, EntityEquipmentSlot slot) {
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        EntityPlayer player = minecraft.player;
        ItemStack armorStack = player.getItemStackFromSlot(slot);
        if (minecraft.inGameHasFocus && armorStack.getItem() instanceof ItemPneumaticArmorBase) {
            ScaledResolution sr = new ScaledResolution(minecraft);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glPushMatrix();
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glColor4d(0, 1, 0, 0.8D);
            CommonHUDHandler comHudHandler = CommonHUDHandler.getHandlerForPlayer(player);
            if (comHudHandler.getTicksSinceEquipped(slot) <= comHudHandler.getStartupTime(slot)) {
                // blockTrackInfo = null;
                gaveEmptyWarning[slot.getIndex()] = false;
                gaveNearlyEmptyWarning[slot.getIndex()] = false;
                int yOffset = (3 - slot.getIndex()) * 21;
                RenderProgressBar.render(sr.getScaledWidth() / 2, 10 + yOffset, sr.getScaledWidth() - 10, 30 + yOffset, -90F, comHudHandler.getTicksSinceEquipped(slot) * 100 / comHudHandler.getStartupTime(slot));
            } else {
                String itemName = armorStack.getDisplayName();
                float pressure = comHudHandler.armorPressure[slot.getIndex()];
                if (pressure < 0.05F && !gaveEmptyWarning[slot.getIndex()]) {
                    addMessage(new ArmorMessage("Your " + itemName + " is out of air!", new ArrayList<>(), 100, 0x70FF0000));
                    gaveEmptyWarning[slot.getIndex()] = true;
                }
                if (pressure > 0.2F && pressure < 0.5F && !gaveNearlyEmptyWarning[slot.getIndex()]) {
                    addMessage(new ArmorMessage("Your " + itemName + " is almost out of air!", new ArrayList<>(), 60, 0x70FF0000));
                    gaveNearlyEmptyWarning[slot.getIndex()] = true;
                }
                if (GuiKeybindCheckBox.getCoreComponents().checked) {
                    List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
                    for (int i = 0; i < renderHandlers.size(); i++) {
                        IUpgradeRenderHandler upgradeRenderHandler = renderHandlers.get(i);
                        if (comHudHandler.isUpgradeRendererInserted(slot, i)
                                && GuiKeybindCheckBox.fromKeyBindingName(GuiKeybindCheckBox.UPGRADE_PREFIX + upgradeRenderHandler.getUpgradeName()).checked) {
                            IGuiAnimatedStat stat = upgradeRenderHandler.getAnimatedStat();
                            if (stat != null) {
                                stat.render(-1, -1, partialTicks);
                            }
                            upgradeRenderHandler.render2D(partialTicks, pressure > 0F);
                        }
                    }
                }
            }

            // render every item in the list.
            for (ArmorMessage message : messageList) {
                message.renderMessage(minecraft.fontRenderer, partialTicks);
            }

            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            if (comHudHandler.getTicksSinceEquipped(slot) <= comHudHandler.getStartupTime(slot))
                minecraft.fontRenderer.drawString(CommonHUDHandler.getHandlerForPlayer().getTicksSinceEquipped(slot) * 100 / comHudHandler.getStartupTime(slot) + "%", sr.getScaledWidth() * 3 / 4 - 8, 16 + 21 * (3 - slot.getIndex()), 0x000000);
        } else if (armorStack.isEmpty()) {
            messageList.clear();
        }
    }

    private void update(EntityPlayer player, EntityEquipmentSlot slot) {
        for (ArmorMessage message : messageList) {
            message.getStat().update();
        }
        CommonHUDHandler comHudHandler = CommonHUDHandler.getHandlerForPlayer(player);
        boolean armorEnabled = GuiKeybindCheckBox.getCoreComponents().checked;
        List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
        if (comHudHandler.getTicksSinceEquipped(slot) == 1) {
            for (IUpgradeRenderHandler handler : UpgradeRenderHandlerList.instance().getHandlersForSlot(slot)) {
                handler.reset();
            }
            for (int i = 0; i < renderHandlers.size(); i++) {
                NetworkHandler.sendToServer(new PacketToggleHelmetFeature(
                        (byte) i,
                        armorEnabled && GuiKeybindCheckBox.fromKeyBindingName(GuiKeybindCheckBox.UPGRADE_PREFIX + renderHandlers.get(i).getUpgradeName()).checked,
                        slot)
                );
            }
        }

        if (slot == EntityEquipmentSlot.HEAD) {
            if (comHudHandler.getTicksSinceEquipped(slot) > comHudHandler.getStartupTime(slot) && armorEnabled) {
                for (int i = 0; i < renderHandlers.size(); i++) {
                    IUpgradeRenderHandler upgradeRenderHandler = renderHandlers.get(i);
                    if (comHudHandler.isUpgradeRendererInserted(slot, i) && GuiKeybindCheckBox.fromKeyBindingName(GuiKeybindCheckBox.UPGRADE_PREFIX + upgradeRenderHandler.getUpgradeName()).checked) {
                        IGuiAnimatedStat stat = upgradeRenderHandler.getAnimatedStat();
                        if (stat != null) {
                            if (comHudHandler.armorPressure[slot.getIndex()] > upgradeRenderHandler.getMinimumPressure()) {
                                stat.openWindow();
                            } else {
                                stat.closeWindow();
                            }
                            stat.update();
                        }
                        upgradeRenderHandler.update(player, comHudHandler.rangeUpgradesInstalled);
                    }
                }
            }

            // clean the message list
            for (int i = 0; i < messageList.size(); i++) {
                ArmorMessage message = messageList.get(i);
                if (message == null || --message.lifeSpan <= 0) {
                    messageList.remove(i);
                    i--;
                }
            }
        }

        // Display found/not found message for each possible upgrade (actual message display needs helmet equipped)
        for (int i = 0; i < renderHandlers.size(); i++) {
            if (comHudHandler.getTicksSinceEquipped(slot) == comHudHandler.getStartupTime(slot) / (renderHandlers.size() + 2) * (i + 1)) {
                player.world.playSound(player.posX, player.posY, player.posZ, Sounds.HUD_INIT, SoundCategory.PLAYERS, 0.1F, 0.5F + (float) (i + 1) / (renderHandlers.size() + 2) * 0.5F, true);
                boolean upgradeEnabled = comHudHandler.isUpgradeRendererInserted(slot, i);
                addMessage(new ArmorMessage(I18n.format(GuiKeybindCheckBox.UPGRADE_PREFIX + renderHandlers.get(i).getUpgradeName()) + (upgradeEnabled ? " found" : " not installed"), new ArrayList<>(), 50, upgradeEnabled ? 0x7000AA00 : 0x70FF0000));
            }
        }

        if (slot == EntityEquipmentSlot.HEAD && comHudHandler.getTicksSinceEquipped(slot) == 1) {
            player.world.playSound(player.posX, player.posY, player.posZ, Sounds.HUD_INIT, SoundCategory.PLAYERS, 0.1F, 0.5F, true);
            addMessage(new ArmorMessage("Initializing head-up display...", Collections.emptyList(), 50, 0x7000AA00));
        }

        if (comHudHandler.getTicksSinceEquipped(slot) == comHudHandler.getStartupTime(slot)) {
            player.world.playSound(player.posX, player.posY, player.posZ, Sounds.HUD_INIT_COMPLETE, SoundCategory.PLAYERS, 0.1F, 1.0F, true);
            ItemStack stack = player.getItemStackFromSlot(slot);
            addMessage(new ArmorMessage(stack.getDisplayName() + " initialization complete!", Collections.emptyList(), 50, 0x7000AA00));
        }
    }

    public void addMessage(String title, List<String> message, int duration, int backColor) {
        addMessage(new ArmorMessage(title, message, duration, backColor));
    }

    public void addMessage(ArmorMessage message) {
        if (messageList.size() > 0) {
            message.setDependingMessage(messageList.get(messageList.size() - 1).getStat()); //set the depending stat of the new stat to the last stat.
        }
        messageList.add(message);
    }

    @Override
    public void onKeyPress(KeyBinding key) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        if (mc.inGameHasFocus) {
            if (key == KeyHandler.getInstance().keybindOpenOptions) {
                if (ItemPneumaticArmorBase.isPlayerWearingAnyPneumaticArmor(mc.player)) {
                    FMLCommonHandler.instance().showGuiScreen(GuiHelmetMainScreen.getInstance());
                }
            } else if (key == KeyHandler.getInstance().keybindHack && HackUpgradeRenderHandler.enabledForPlayer(mc.player)) {
                getSpecificRenderer(BlockTrackUpgradeHandler.class).hack();
                getSpecificRenderer(EntityTrackUpgradeHandler.class).hack();
            } else if (key == KeyHandler.getInstance().keybindDebuggingDrone && DroneDebugUpgradeHandler.enabledForPlayer(PneumaticCraftRepressurized.proxy.getPlayer())) {
                getSpecificRenderer(EntityTrackUpgradeHandler.class).selectAsDebuggingTarget();
            }
        }
    }

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        boolean isCaptured = getSpecificRenderer(BlockTrackUpgradeHandler.class).scroll(event);
        if (!isCaptured) isCaptured = getSpecificRenderer(EntityTrackUpgradeHandler.class).scroll(event);
        if (isCaptured) event.setCanceled(true);
    }
}
