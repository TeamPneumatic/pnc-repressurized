package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.ClientTickHandler;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.ContainerSecurityStationHacking;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSecurityStationFailedHack;
import me.desht.pneumaticcraft.common.network.PacketUseItem;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiSecurityStationHacking extends GuiSecurityStationBase {
    private GuiAnimatedStat statusStat;

    private NetworkConnectionBackground playerBackgroundBridges;
    private NetworkConnectionBackground aiBackgroundBridges;
    private NetworkConnectionPlayerHandler hackerBridges;
    private NetworkConnectionAIHandler aiBridges;

    private int stopWorms = 0;
    private int nukeViruses = 0;

    private final ItemStack stopWorm = new ItemStack(Itemss.STOP_WORM);
    private final ItemStack nukeVirus = new ItemStack(Itemss.NUKE_VIRUS);

    public GuiSecurityStationHacking(InventoryPlayer player, TileEntitySecurityStation te) {

        super(new ContainerSecurityStationHacking(player, te), te, Textures.GUI_HACKING);
        ySize = 238;
    }

    @Override
    public void initGui() {
        super.initGui();

        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;

        statusStat = addAnimatedStat("Security Status", new ItemStack(Blockss.SECURITY_STATION), 0xFFFFAA00, false);
        addAnimatedStat("gui.tab.info", Textures.GUI_INFO_LOCATION, 0xFF8888FF, true).setText("gui.tab.info.tile.security_station.hacking");
        addAnimatedStat("gui.tab.upgrades", Textures.GUI_UPGRADES_LOCATION, 0xFF0000FF, true).setText("gui.tab.upgrades.tile.security_station.hacking");
        addAnimatedStat(Itemss.NUKE_VIRUS.getTranslationKey() + ".name", new ItemStack(Itemss.NUKE_VIRUS), 0xFF18c9e8, false).setText("gui.tab.info.tile.security_station.nukeVirus");
        addAnimatedStat(Itemss.STOP_WORM.getTranslationKey() + ".name", new ItemStack(Itemss.STOP_WORM), 0xFFc13232, false).setText("gui.tab.info.tile.security_station.stopWorm");

        if (playerBackgroundBridges == null) {
            playerBackgroundBridges = new NetworkConnectionBackground(this, te, xStart + 21, yStart + 26, 31, 0xAA4444FF);
            aiBackgroundBridges = new NetworkConnectionBackground(this, te, xStart + 23, yStart + 27, 31, 0xAA4444FF);
            hackerBridges = new NetworkConnectionPlayerHandler(this, te, xStart + 21, yStart + 26, 31, 0xFF00FF00);
            aiBridges = new NetworkConnectionAIHandler(this, te, xStart + 23, yStart + 27, 31, 0xFFFF0000);
        } else {
            playerBackgroundBridges = new NetworkConnectionBackground(playerBackgroundBridges, xStart + 21, yStart + 26);
            aiBackgroundBridges = new NetworkConnectionBackground(aiBackgroundBridges, xStart + 23, yStart + 27);
            hackerBridges = new NetworkConnectionPlayerHandler(hackerBridges, xStart + 21, yStart + 26);
            aiBridges = new NetworkConnectionAIHandler(aiBridges, xStart + 23, yStart + 27);
        }
    }

    @Override
    protected boolean shouldAddInfoTab() {
        return false;
    }

    @Override
    protected boolean shouldAddUpgradeTab() {
        return false;
    }

    @Override
    protected boolean shouldAddRedstoneTab() {
        return false;
    }

    @Override
    protected Point getInvNameOffset() {
        return null;
    }

    @Override
    protected Point getInvTextOffset() {
        return null;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString((aiBridges.isTracing() ? TextFormatting.RED : TextFormatting.GREEN) + "Tracing: " + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(aiBridges.getRemainingTraceTime(), true), 15, 7, 4210752);
        renderConsumables(x, y);
    }

    private void renderConsumables(int x, int y) {
        stopWorms = 0;
        nukeViruses = 0;
        EntityPlayer player = FMLClientHandler.instance().getClient().player;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack.getItem() == Itemss.STOP_WORM) stopWorms += stack.getCount();
            if (stack.getItem() == Itemss.NUKE_VIRUS) nukeViruses += stack.getCount();
        }
        GuiUtils.drawItemStack(nukeVirus, 155, 30);
        GuiUtils.drawItemStack(stopWorm, 155, 55);
        fontRenderer.drawString(PneumaticCraftUtils.convertAmountToString(nukeViruses), 155, 45, 0xFFFFFFFF);
        fontRenderer.drawString(PneumaticCraftUtils.convertAmountToString(stopWorms), 155, 70, 0xFFFFFFFF);

    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        statusStat.setText(getStatusText());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int x, int y) {
        super.drawGuiContainerBackgroundLayer(opacity, x, y);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        playerBackgroundBridges.render();
        aiBackgroundBridges.render();
        hackerBridges.render();
        aiBridges.render();

        if (x >= guiLeft + 155 && x <= guiLeft + 171 && y >= guiTop + 30 && y <= guiTop + 50) {
            List<String> text = new ArrayList<>();
            text.add("Nuke Virus");
            if (hasNukeViruses()) {
                text.add(TextFormatting.GRAY + "Middle-click a hackable node to use.");
            } else {
                text.add(TextFormatting.RED + "You don't have any Nuke Viruses.");
            }
            drawHoveringString(text, x, y, fontRenderer);
        }
        if (x >= guiLeft + 155 && x <= guiLeft + 171 && y >= guiTop + 55 && y <= guiTop + 75) {
            List<String> text = new ArrayList<>();
            text.add("STOP! Worm");
            if (stopWorms > 0) {
                if (aiBridges.isTracing()) {
                    text.add(TextFormatting.GRAY + "Left-click to use.");
                } else {
                    text.add(TextFormatting.GRAY + "STOP! Worms can only be used when being traced.");
                }
            } else {
                text.add(TextFormatting.RED + "You don't have any STOP! Worms.");
            }
            drawHoveringString(text, x, y, fontRenderer);
        }
    }

    @Override
    protected void addProblems(List<String> text) {
        super.addProblems(text);
        if (aiBridges.isTracing()) {
            text.add(TextFormatting.GRAY + "Intrusion detected!");
            text.add(TextFormatting.BLACK + "Time till trace: " + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(aiBridges.getRemainingTraceTime(), false));
        }
    }

    private List<String> getStatusText() {
        List<String> text = new ArrayList<>();
        text.add(TextFormatting.GRAY + "Security Level");
        text.add(TextFormatting.BLACK + "Level " + te.getSecurityLevel());
        text.add(TextFormatting.GRAY + "Security Range");
        text.add(TextFormatting.BLACK.toString() + te.getSecurityRange() + "m (square)");
        return text;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 2) super.mouseClicked(mouseX, mouseY, mouseButton);
        hackerBridges.mouseClicked(mouseX, mouseY, mouseButton, getSlotAtPosition(mouseX, mouseY));
        if (aiBridges.isTracing() && mouseX >= guiLeft + 155 && mouseX <= guiLeft + 171 && mouseY >= guiTop + 55 && mouseY <= guiTop + 75) {
            EntityPlayer player = FMLClientHandler.instance().getClient().player;
            NetworkHandler.sendToServer(new PacketUseItem(Itemss.STOP_WORM, 1));
            PneumaticCraftUtils.consumeInventoryItem(player.inventory, Itemss.STOP_WORM);
            aiBridges.applyStopWorm();
        }
    }

    public void addExtraHackInfo(List<String> currenttip) {
        int mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
        Slot slot = getSlotAtPosition(mouseX, mouseY);
        if (slot != null) {
            if (hackerBridges.slotHacked[slot.slotNumber]) {
                if (!hackerBridges.slotFortified[slot.slotNumber]) {
                    currenttip.add(TextFormatting.RED + "DETECTION: " + te.getDetectionChance() + "%");
                    currenttip.add(TextFormatting.YELLOW + "Right-click to fortify");
                }
            } else if (hackerBridges.canHackSlot(slot.slotNumber)) {
                currenttip.add(TextFormatting.RED + "DETECTION: " + te.getDetectionChance() + "%");
                currenttip.add(TextFormatting.GREEN + "Left-click to hack");

            }
        }
    }

    public boolean hasNukeViruses() {
        return nukeViruses > 0;
    }

    public void onSlotHack(int slot) {
        if (Math.random() < te.getDetectionChance() / 100D) {
            aiBridges.setTracing(true);
        }
    }

    public void onSlotFortification(int slot) {
        aiBridges.slotFortified[slot] = true;
        if (Math.random() < te.getDetectionChance() / 100D) {
            aiBridges.setTracing(true);
        }
    }

    @Override
    public void onGuiClosed() {
        if (aiBridges.isTracing() && !hackerBridges.hackedSuccessfully)
            NetworkHandler.sendToServer(new PacketSecurityStationFailedHack(te.getPos()));
        removeUpdatesOnConnectionHandlers();
        super.onGuiClosed();
    }

    public void removeUpdatesOnConnectionHandlers() {
        ClientTickHandler.instance().removeUpdatedObject(hackerBridges);
        ClientTickHandler.instance().removeUpdatedObject(aiBridges);
    }

}
