package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerAirCannon;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCannon;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class GuiAirCannon extends GuiPneumaticContainerBase<ContainerAirCannon,TileEntityAirCannon> {
    private WidgetAnimatedStat statusStat;
    private WidgetAnimatedStat strengthTab;
    private int gpsX;
    private int gpsY;
    private int gpsZ;

    public GuiAirCannon(ContainerAirCannon container, PlayerInventory inventoryPlayer, ITextComponent displayName) {
        super(container, inventoryPlayer, displayName);

        gpsX = te.gpsX;
        gpsY = te.gpsY;
        gpsZ = te.gpsZ;
    }

    @Override
    public void init() {
        super.init();

        statusStat = this.addAnimatedStat("Cannon Status", new ItemStack(ModBlocks.AIR_CANNON.get()), 0xFFFFAA00, false);

        strengthTab = this.addAnimatedStat("Force", new ItemStack(ModItems.AIR_CANISTER.get()), 0xFF2080FF, false);
        strengthTab.addPadding(3, 22);
        strengthTab.addSubWidget(new WidgetButtonExtended(16, 16, 20, 20, "--").withTag("--"));
        strengthTab.addSubWidget(new WidgetButtonExtended(38, 16, 20, 20, "-").withTag("-"));
        strengthTab.addSubWidget(new WidgetButtonExtended(60, 16, 20, 20, "+").withTag("+"));
        strengthTab.addSubWidget(new WidgetButtonExtended(82, 16, 20, 20, "++").withTag("++"));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        font.drawString("GPS", 50, 20, 4210752);
        font.drawString("Upgr.", 13, 19, 4210752);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_AIR_CANNON_LOCATION;
    }

    @Override
    public void tick() {
        super.tick();
        statusStat.setText(getStatusText());
        strengthTab.setTitle("Force: "+ te.forceMult + "%%");

        if (gpsX != te.gpsX || gpsY != te.gpsY || gpsZ != te.gpsZ) {
            gpsX = te.gpsX;
            gpsY = te.gpsY;
            gpsZ = te.gpsZ;
            statusStat.openWindow();
        }
    }

    private List<String> getStatusText() {
        List<String> text = new ArrayList<>();
        text.add("\u00a77Current Aimed Coordinate:");
        if (te.gpsX != 0 || te.gpsY != 0 || te.gpsZ != 0) {
            text.add("\u00a70X: " + te.gpsX + ", Y: " + te.gpsY + ", Z: " + te.gpsZ);
        } else {
            text.add("\u00a70- No coordinate selected -");
        }
        text.add("\u00a77Current Heading Angle:");
        text.add("\u00a70" + Math.round(te.rotationAngle) + " degrees.");
        text.add("\u00a77Current Height Angle:");
        text.add("\u00a70" + (90 - Math.round(te.heightAngle)) + " degrees.");
        text.add(TextFormatting.GRAY + "Range");
        text.add(TextFormatting.BLACK + "About " + PneumaticCraftUtils.roundNumberTo(te.getForce() * 25F, 0) + "m");
        return text;
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);

        if (te.isLeaking()) {
            textList.add("\u00a77No air input connected.");
            textList.add("\u00a70Add pipes / machines");
            textList.add("\u00a70to the input.");
        }
        if (container.inventorySlots.get(0).getStack().isEmpty()) {
            textList.add("\u00a77No items to fire");
            textList.add("\u00a70Add items in the");
            textList.add("\u00a70cannon slot.");
        }
        if (!te.hasCoordinate()) {
            textList.add("\u00a77No destination coordinate set");
            textList.add("\u00a70Put a GPS Tool with a");
            textList.add("\u00a70coordinate set in the GPS slot.");
        } else if (!te.coordWithinReach) {
            textList.add("\u00a77Selected coordinate");
            textList.add("\u00a77can't be reached");
            textList.add("\u00a70Select a coordinate");
            textList.add("\u00a70closer to the cannon.");
        } else if (te.getRedstoneMode() == 0 && !te.doneTurning) {
            textList.add("\u00a77Cannon still turning");
            textList.add("\u00a70Wait for the cannon");
        } else if (te.getRedstoneMode() == 2 && !te.insertingInventoryHasSpace) {
            textList.add("\u00a77The last shot inventory does not have space for the items in the Cannon.");
        }
    }

    @Override
    protected void addInformation(List<String> curInfo) {
        super.addInformation(curInfo);
        if (curInfo.isEmpty()) {
            curInfo.add("\u00a70Apply a redstone signal to fire.");
        }
    }
}
