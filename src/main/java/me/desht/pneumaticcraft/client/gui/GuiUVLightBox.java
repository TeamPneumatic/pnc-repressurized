package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.BlockUVLightBox;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerUVLightBox;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiUVLightBox extends GuiPneumaticContainerBase<ContainerUVLightBox,TileEntityUVLightBox> implements Slider.ISlider {
    private Slider slider;

    public GuiUVLightBox(ContainerUVLightBox container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        imageHeight = 196;
    }

    @Override
    public void init() {
        super.init();

        addButton(slider = new Slider(leftPos + 10, topPos + 45, 95, 16,
                xlate("pneumaticcraft.gui.uv_light_box.threshold").append(" "), new StringTextComponent("%"),
                1, 100, te.getThreshold(), false, true, b -> { }, this));
    }

    @Override
    public void tick() {
        boolean interpolate = te.rsController.getCurrentMode() == TileEntityUVLightBox.RS_MODE_INTERPOLATE;
        if (firstUpdate || interpolate) {
            // te sync packet hasn't necessarily arrived when init() is called; need to set it up here
            slider.setValue(te.getThreshold());
            slider.updateSlider();
        }
        slider.active = !interpolate;
        slider.visible = !interpolate || te.getRedstoneController().getCurrentRedstonePower() > 0;

        super.tick();
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        BlockState state = te.getBlockState();
        return state.getBlock() == ModBlocks.UV_LIGHT_BOX.get() && state.getValue(BlockUVLightBox.LIT) ?
                Textures.GUI_UV_LIGHT_BOX_ON : Textures.GUI_UV_LIGHT_BOX;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        return new PointXY(xStart + imageWidth * 3 / 4 + 10, yStart + imageHeight / 4 - 5);
    }

    @Override
    protected void addProblems(List<ITextComponent> textList) {
        super.addProblems(textList);

        if (te.getPrimaryInventory().getStackInSlot(TileEntityUVLightBox.PCB_SLOT).isEmpty()) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.uv_light_box.no_item"));
        }
    }

    @Override
    protected void addPressureStatInfo(List<ITextComponent> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        BlockState state = te.getBlockState();
        if (state.getBlock() instanceof BlockUVLightBox && state.getValue(BlockUVLightBox.LIT)) {
            float usage = PneumaticValues.USAGE_UV_LIGHTBOX * te.getSpeedUsageMultiplierFromUpgrades();
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.airUsage", PneumaticCraftUtils.roundNumberTo(usage, 2)));
        }
    }

    @Override
    public void onChangeSliderValue(Slider slider) {
        sendDelayed(5);
    }

    @Override
    protected void doDelayedAction() {
        sendGUIButtonPacketToServer(Integer.toString(slider.getValueInt()));
    }
}
