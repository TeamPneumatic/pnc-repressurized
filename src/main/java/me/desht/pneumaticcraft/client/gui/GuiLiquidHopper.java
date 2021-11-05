package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerLiquidHopper;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidHopper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiLiquidHopper extends GuiPneumaticContainerBase<ContainerLiquidHopper,TileEntityLiquidHopper> {
    private WidgetAnimatedStat statusStat;
    private final WidgetButtonExtended[] modeButtons = new WidgetButtonExtended[2];

    public GuiLiquidHopper(ContainerLiquidHopper container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        addButton(new WidgetTank(leftPos + 116, topPos + 15, te.getTank()));
        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.hopperStatus"), new ItemStack(ModBlocks.LIQUID_HOPPER.get()), 0xFFFFAA00, false);

        WidgetAnimatedStat optionStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.gasLift.mode"), new ItemStack(Blocks.LEVER), 0xFFFFCC00, false);
        optionStat.setMinimumExpandedDimensions(50, 43);

        WidgetButtonExtended button = new WidgetButtonExtended(20, 20, 20, 20, StringTextComponent.EMPTY).withTag("empty");
        button.setRenderStacks(new ItemStack(Items.BUCKET));
        button.setTooltipText(xlate("pneumaticcraft.gui.tab.liquidHopper.mode.empty"));
        optionStat.addSubWidget(button);
        modeButtons[0] = button;

        button = new WidgetButtonExtended(45, 20, 20, 20, StringTextComponent.EMPTY).withTag("leave");
        button.setRenderStacks(new ItemStack(Items.WATER_BUCKET));
        button.setTooltipText(xlate("pneumaticcraft.gui.tab.liquidHopper.mode.leaveLiquid"));
        optionStat.addSubWidget(button);
        modeButtons[1] = button;
    }

    @Override
    protected boolean isUpgradeAvailable(EnumUpgrade upgrade) {
        return upgrade != EnumUpgrade.DISPENSER || ConfigHelper.common().machines.liquidHopperDispenser.get();
    }

    @Override
    protected PointXY getInvNameOffset() {
        return new PointXY(0, -1);
    }

    @Override
    public void tick() {
        super.tick();
        statusStat.setText(getStatus());
        modeButtons[0].active = te.doesLeaveMaterial();
        modeButtons[1].active = !te.doesLeaveMaterial();
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_LIQUID_HOPPER;
    }

    private List<ITextComponent> getStatus() {
        List<ITextComponent> textList = new ArrayList<>();
        int itemsPer = te.getMaxItems();
        if (itemsPer > 1) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.hopperStatus.liquidTransferPerTick", itemsPer * 100));
        } else {
            int transferInterval = te.getItemTransferInterval();
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.hopperStatus.liquidTransferPerSecond", transferInterval == 0 ? "2000" : PneumaticCraftUtils.roundNumberTo(2000F / transferInterval, 1)));
        }
        return textList;
    }
}
