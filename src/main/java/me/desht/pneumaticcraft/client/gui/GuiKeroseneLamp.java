package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTank;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerKeroseneLamp;
import me.desht.pneumaticcraft.common.tileentity.TileEntityKeroseneLamp;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiKeroseneLamp extends GuiPneumaticContainerBase<ContainerKeroseneLamp,TileEntityKeroseneLamp> implements Slider.ISlider {

    private WidgetLabel rangeLabel;
    private Slider slider;

    public GuiKeroseneLamp(ContainerKeroseneLamp container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addButton(new WidgetTank(guiLeft + 152, guiTop + 15, te.getTank()));
        addButton(rangeLabel = new WidgetLabel(guiLeft + 20, guiTop + 55, StringTextComponent.EMPTY));

        addButton(slider = new Slider(guiLeft + 7, guiTop + 30, 118, 20,
                xlate("pneumaticcraft.gui.keroseneLamp.maxRange").appendString(" "), StringTextComponent.EMPTY,
                1, TileEntityKeroseneLamp.MAX_RANGE, te.getTargetRange(), false, true,
                b -> { }, this));
    }

    @Override
    public void tick() {
        super.tick();

        if (firstUpdate) {
            // te sync packet hasn't necessarily arrived when init() is called; need to set it up here
            slider.setValue(te.getTargetRange());
            slider.updateSlider();
        }

        rangeLabel.setMessage(xlate("pneumaticcraft.gui.keroseneLamp.currentRange", te.getRange()));
    }

    @Override
    public void onChangeSliderValue(Slider slider) {
        sendDelayed(5);
    }

    @Override
    protected void doDelayedAction() {
        sendGUIButtonPacketToServer(Integer.toString(slider.getValueInt()));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_KEROSENE_LAMP;
    }

    @Override
    protected void addProblems(List<ITextComponent> curInfo) {
        super.addProblems(curInfo);
        if (te.getTank().getFluidAmount() == 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.keroseneLamp.noFuel"));
        } else if (te.getFuelQuality() == 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.keroseneLamp.badFuel"));
        }
    }

    @Override
    protected void addWarnings(List<ITextComponent> curInfo) {
        super.addWarnings(curInfo);
        if (te.getTank().getFluidAmount() < 30 && te.getTank().getFluidAmount() > 0) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.keroseneLamp.lowFuel"));
        }
    }
}
