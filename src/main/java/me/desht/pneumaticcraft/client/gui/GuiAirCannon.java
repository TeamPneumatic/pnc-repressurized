package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerAirCannon;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCannon;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

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

        statusStat = this.addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.airCannon.status"),
                new ItemStack(ModBlocks.AIR_CANNON.get()), 0xFFFF8000, false);

        strengthTab = this.addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.airCannon.force", te.forceMult),
                new ItemStack(ModItems.AIR_CANISTER.get()), 0xFF2080FF, false);
        strengthTab.addPadding(3, 22);
        strengthTab.addSubWidget(new WidgetButtonExtended(16, 16, 20, 20, "--").withTag("--"));
        strengthTab.addSubWidget(new WidgetButtonExtended(38, 16, 20, 20, "-").withTag("-"));
        strengthTab.addSubWidget(new WidgetButtonExtended(60, 16, 20, 20, "+").withTag("+"));
        strengthTab.addSubWidget(new WidgetButtonExtended(82, 16, 20, 20, "++").withTag("++"));

        addLabel(new StringTextComponent("GPS"),  guiLeft + 50, guiTop + 20);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_AIR_CANNON;
    }

    @Override
    public void tick() {
        super.tick();

        statusStat.setText(getStatusText());
        strengthTab.setMessage(xlate("pneumaticcraft.gui.tab.info.airCannon.force", te.forceMult));

        if (gpsX != te.gpsX || gpsY != te.gpsY || gpsZ != te.gpsZ) {
            gpsX = te.gpsX;
            gpsY = te.gpsY;
            gpsZ = te.gpsZ;
            statusStat.openStat();
        }
    }

    private List<String> getStatusText() {
        List<String> text = new ArrayList<>();
        if (te.gpsX != 0 || te.gpsY != 0 || te.gpsZ != 0) {
            text.add(TextFormatting.BLACK + I18n.format("pneumaticcraft.gui.tab.info.airCannon.coord", te.gpsX, te.gpsY, te.gpsZ));
        } else {
            text.add(TextFormatting.BLACK + I18n.format("pneumaticcraft.gui.tab.info.airCannon.no_coord"));
        }
        text.add(TextFormatting.BLACK + I18n.format("pneumaticcraft.gui.tab.info.airCannon.heading", Math.round(te.rotationAngle)));
        text.add(TextFormatting.BLACK + I18n.format("pneumaticcraft.gui.tab.info.airCannon.height", Math.round(te.heightAngle)));
        text.add(TextFormatting.BLACK + I18n.format("pneumaticcraft.gui.tab.info.airCannon.range", Math.round(te.getForce() * 25F)));
        return text;
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);

        if (te.hasNoConnectedAirHandlers()) {
            textList.add(I18n.format("pneumaticcraft.gui.tab.problems.airLeak"));
        }
        if (container.inventorySlots.get(5).getStack().isEmpty() && te.getUpgrades(EnumUpgrade.ENTITY_TRACKER) == 0) {
            textList.add(I18n.format("pneumaticcraft.gui.tab.problems.air_cannon.no_items"));
        }
        if (!te.hasCoordinate()) {
            textList.add(I18n.format("pneumaticcraft.gui.tab.problems.air_cannon.no_coordinate"));
        } else if (!te.coordWithinReach) {
            textList.add(I18n.format("pneumaticcraft.gui.tab.problems.air_cannon.out_of_range"));
        } else if (te.getRedstoneMode() == 0 && !te.doneTurning) {
            textList.add(I18n.format("pneumaticcraft.gui.tab.problems.air_cannon.still_turning"));
        } else if (te.getRedstoneMode() == 2 && !te.insertingInventoryHasSpace) {
            textList.add(I18n.format("pneumaticcraft.gui.tab.problems.air_cannon.inv_space"));
        }
    }

    @Override
    protected void addInformation(List<String> curInfo) {
        super.addInformation(curInfo);
        if (curInfo.isEmpty()) {
            curInfo.add(I18n.format("pneumaticcraft.gui.tooltip.apply_redstone"));
        }
    }
}
