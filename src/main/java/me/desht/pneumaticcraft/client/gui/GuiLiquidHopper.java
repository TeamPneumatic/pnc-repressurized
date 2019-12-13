package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerLiquidHopper;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidHopper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiLiquidHopper extends GuiPneumaticContainerBase<ContainerLiquidHopper,TileEntityLiquidHopper> {
    private WidgetAnimatedStat statusStat;
    private final WidgetButtonExtended[] modeButtons = new WidgetButtonExtended[2];

    public GuiLiquidHopper(ContainerLiquidHopper container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        addButton(new WidgetTank(guiLeft + 116, guiTop + 15, te.getTank()));
        statusStat = addAnimatedStat("gui.tab.hopperStatus", new ItemStack(ModBlocks.LIQUID_HOPPER), 0xFFFFAA00, false);

        WidgetAnimatedStat optionStat = addAnimatedStat("gui.tab.gasLift.mode", new ItemStack(Blocks.LEVER), 0xFFFFCC00, false);
        optionStat.addPadding(4, 14);

        WidgetButtonExtended button = new WidgetButtonExtended(5, 20, 20, 20, "").withTag("empty");
        button.setRenderStacks(new ItemStack(Items.BUCKET));
        button.setTooltipText(I18n.format("gui.tab.liquidHopper.mode.empty"));
        optionStat.addSubWidget(button);
        modeButtons[0] = button;

        button = new WidgetButtonExtended(30, 20, 20, 20, "").withTag("leave");
        button.setRenderStacks(new ItemStack(Items.WATER_BUCKET));
        button.setTooltipText(I18n.format("gui.tab.liquidHopper.mode.leaveLiquid"));
        optionStat.addSubWidget(button);
        modeButtons[1] = button;
    }

    @Override
    protected Point getInvNameOffset() {
        return new Point(0, -1);
    }

    @Override
    public void tick() {
        super.tick();
        statusStat.setText(getStatus());
        modeButtons[0].active = te.doesLeaveMaterial();
        modeButtons[1].active = !te.doesLeaveMaterial();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        font.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_LIQUID_HOPPER;
    }

    private List<String> getStatus() {
        List<String> textList = new ArrayList<>();
        int itemsPer = te.getMaxItems();
        if (itemsPer > 1) {
            textList.add(I18n.format("gui.tab.hopperStatus.liquidTransferPerTick", itemsPer * 100));
        } else {
            int transferInterval = te.getItemTransferInterval();
            textList.add(I18n.format("gui.tab.hopperStatus.liquidTransferPerSecond", transferInterval == 0 ? "2000" : PneumaticCraftUtils.roundNumberTo(2000F / transferInterval, 1)));
        }
        return textList;
    }

    @Override
    protected void addExtraUpgradeText(List<String> text) {
        if (PNCConfig.Common.Machines.liquidHopperDispenser) {
            text.add("gui.tab.upgrades.tile.liquid_hopper.dispenser");
        }
    }
}
