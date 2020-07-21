package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.tileentity.TileEntityOmnidirectionalHopper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class GuiOmnidirectionalHopper extends GuiPneumaticContainerBase<ContainerOmnidirectionalHopper,TileEntityOmnidirectionalHopper> {
    private WidgetAnimatedStat statusStat;
    private final WidgetButtonExtended[] modeButtons = new WidgetButtonExtended[2];
    private WidgetButtonExtended rrButton;

    public GuiOmnidirectionalHopper(ContainerOmnidirectionalHopper container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        statusStat = addAnimatedStat("pneumaticcraft.gui.tab.hopperStatus", new ItemStack(ModBlocks.OMNIDIRECTIONAL_HOPPER.get()), 0xFFFFAA00, false);

        WidgetAnimatedStat optionStat = addAnimatedStat("pneumaticcraft.gui.tab.gasLift.mode", new ItemStack(Blocks.LEVER), 0xFFFFCC00, false);
        optionStat.addPadding(4, 14);

        WidgetButtonExtended button = new WidgetButtonExtended(5, 20, 20, 20, "").withTag("empty");
        button.setRenderStacks(new ItemStack(Items.BUCKET));
        button.setTooltipText(I18n.format("pneumaticcraft.gui.tab.omnidirectionalHopper.mode.empty"));
        optionStat.addSubWidget(button);
        modeButtons[0] = button;

        button = new WidgetButtonExtended(30, 20, 20, 20, "").withTag("leave");
        button.setRenderStacks(new ItemStack(Items.WATER_BUCKET));
        button.setTooltipText(I18n.format("pneumaticcraft.gui.tab.omnidirectionalHopper.mode.leaveItem"));
        optionStat.addSubWidget(button);
        modeButtons[1] = button;

        addButton(rrButton = new WidgetButtonExtended(guiLeft + 143, guiTop + 55, 14, 14, "").withTag("rr"));
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
        rrButton.setMessage(te.roundRobin ? TextFormatting.GREEN + GuiConstants.CIRCULAR_ARROW : GuiConstants.ARROW_RIGHT);
        rrButton.setTooltipText(PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.tooltip.omnidirectional_hopper.roundRobin." + (te.roundRobin ? "on" : "off"), 40)));
    }

    private List<String> getStatus() {
        List<String> textList = new ArrayList<>();
        int itemsPer = te.getMaxItems();
        if (itemsPer > 1) {
            textList.add(I18n.format("pneumaticcraft.gui.tab.hopperStatus.itemTransferPerTick", itemsPer));
        } else {
            int transferInterval = te.getItemTransferInterval();
            textList.add(I18n.format("pneumaticcraft.gui.tab.hopperStatus.itemTransferPerSecond", transferInterval == 0 ? "20" : PneumaticCraftUtils.roundNumberTo(20F / transferInterval, 1)));
        }
        return textList;
    }

    @Override
    protected void addExtraUpgradeText(List<String> text) {
        if (PNCConfig.Common.Machines.omniHopperDispenser) {
            text.add("pneumaticcraft.gui.tab.upgrades.tile.omnidirectional_hopper.dispenser");
        }
        text.add("pneumaticcraft.gui.tab.upgrades.creative");
    }
}
