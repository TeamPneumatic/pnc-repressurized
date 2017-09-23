package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.inventory.ContainerLiquidHopper;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidHopper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiLiquidHopper extends GuiPneumaticContainerBase<TileEntityLiquidHopper> {
    private GuiAnimatedStat statusStat;
    private final GuiButtonSpecial[] modeButtons = new GuiButtonSpecial[2];

    public GuiLiquidHopper(InventoryPlayer player, TileEntityLiquidHopper te) {
        super(new ContainerLiquidHopper(player, te), te, Textures.GUI_LIQUID_HOPPER);
    }

    @Override
    public void initGui() {
        super.initGui();
        addWidget(new WidgetTank(0, guiLeft + 116, guiTop + 15, te.getTank()));
        statusStat = addAnimatedStat("gui.tab.hopperStatus", new ItemStack(Blockss.OMNIDIRECTIONAL_HOPPER), 0xFFFFAA00, false);

        GuiAnimatedStat optionStat = addAnimatedStat("gui.tab.gasLift.mode", new ItemStack(Blocks.LEVER), 0xFFFFCC00, false);
        List<String> text = new ArrayList<String>();
        for (int i = 0; i < 4; i++)
            text.add("               ");
        optionStat.setTextWithoutCuttingString(text);

        GuiButtonSpecial button = new GuiButtonSpecial(1, 5, 20, 20, 20, "");
        button.setRenderStacks(new ItemStack(Items.BUCKET));
        button.setTooltipText(I18n.format("gui.tab.liquidHopper.mode.empty"));
        optionStat.addWidget(button);
        modeButtons[0] = button;

        button = new GuiButtonSpecial(2, 30, 20, 20, 20, "");
        button.setRenderStacks(new ItemStack(Items.WATER_BUCKET));
        button.setTooltipText(I18n.format("gui.tab.liquidHopper.mode.leaveLiquid"));
        optionStat.addWidget(button);
        modeButtons[1] = button;
    }

    @Override
    protected Point getInvNameOffset() {
        return new Point(0, -1);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        statusStat.setText(getStatus());
        modeButtons[0].enabled = te.doesLeaveMaterial();
        modeButtons[1].enabled = !te.doesLeaveMaterial();
    }

    private List<String> getStatus() {
        List<String> textList = new ArrayList<String>();
        int itemsPer = te.getMaxItems();
        if (itemsPer > 1) {
            textList.add(I18n.format("gui.tab.hopperStatus.liquidTransferPerTick", itemsPer * 100));
        } else {
            int transferInterval = te.getItemTransferInterval();
            textList.add(I18n.format("gui.tab.hopperStatus.liquidTransferPerSecond", transferInterval == 0 ? "2000" : PneumaticCraftUtils.roundNumberTo(2000F / transferInterval, 1)));
        }
        return textList;
    }
}
