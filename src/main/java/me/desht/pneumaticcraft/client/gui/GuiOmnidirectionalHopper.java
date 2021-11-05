package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.tileentity.TileEntityOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiOmnidirectionalHopper extends GuiPneumaticContainerBase<ContainerOmnidirectionalHopper,TileEntityOmnidirectionalHopper> {
    private static final ITextComponent ARROW_NO_RR = new StringTextComponent(Symbols.ARROW_RIGHT);
    private static final ITextComponent ARROW_RR = new StringTextComponent(Symbols.CIRCULAR_ARROW).withStyle(TextFormatting.GREEN);

    private WidgetAnimatedStat statusStat;
    private final WidgetButtonExtended[] modeButtons = new WidgetButtonExtended[2];
    private WidgetButtonExtended rrButton;

    public GuiOmnidirectionalHopper(ContainerOmnidirectionalHopper container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.hopperStatus"), new ItemStack(ModBlocks.OMNIDIRECTIONAL_HOPPER.get()), 0xFFFFAA00, false);

        WidgetAnimatedStat optionStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.gasLift.mode"), new ItemStack(Blocks.LEVER), 0xFFFFCC00, false);
        optionStat.setMinimumExpandedDimensions(50, 43);

        WidgetButtonExtended button = new WidgetButtonExtended(5, 20, 20, 20, StringTextComponent.EMPTY).withTag("empty");
        button.setRenderStacks(new ItemStack(Items.BUCKET));
        button.setTooltipText(xlate("pneumaticcraft.gui.tab.omnidirectionalHopper.mode.empty"));
        optionStat.addSubWidget(button);
        modeButtons[0] = button;

        button = new WidgetButtonExtended(30, 20, 20, 20, StringTextComponent.EMPTY).withTag("leave");
        button.setRenderStacks(new ItemStack(Items.WATER_BUCKET));
        button.setTooltipText(xlate("pneumaticcraft.gui.tab.omnidirectionalHopper.mode.leaveItem"));
        optionStat.addSubWidget(button);
        modeButtons[1] = button;

        addButton(rrButton = new WidgetButtonExtended(leftPos + 143, topPos + 55, 14, 14, StringTextComponent.EMPTY).withTag("rr"));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_OMNIDIRECTIONAL_HOPPER;
    }

    @Override
    public void tick() {
        super.tick();

        statusStat.setText(getStatus());
        modeButtons[0].active = te.doesLeaveMaterial();
        modeButtons[1].active = !te.doesLeaveMaterial();
        rrButton.setMessage(te.roundRobin ? ARROW_RR : ARROW_NO_RR);
        rrButton.setTooltipKey("pneumaticcraft.gui.tooltip.omnidirectional_hopper.roundRobin." + (te.roundRobin ? "on" : "off"));
    }

    private List<ITextComponent> getStatus() {
        List<ITextComponent> textList = new ArrayList<>();
        int itemsPer = te.getMaxItems();
        if (itemsPer > 1) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.hopperStatus.itemTransferPerTick", itemsPer));
        } else {
            int transferInterval = te.getItemTransferInterval();
            String s = transferInterval == 0 ? "20" : PneumaticCraftUtils.roundNumberTo(20F / transferInterval, 1);
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.hopperStatus.itemTransferPerSecond", s));
        }
        return textList;
    }

    @Override
    protected boolean isUpgradeAvailable(EnumUpgrade upgrade) {
        return upgrade != EnumUpgrade.DISPENSER || ConfigHelper.common().machines.omniHopperDispenser.get();
    }
}
