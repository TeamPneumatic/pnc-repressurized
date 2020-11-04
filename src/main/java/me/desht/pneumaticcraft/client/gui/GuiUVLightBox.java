package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.BlockUVLightBox;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerUVLightBox;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiUVLightBox extends GuiPneumaticContainerBase<ContainerUVLightBox,TileEntityUVLightBox> implements Slider.ISlider {
    private Slider slider;

    public GuiUVLightBox(ContainerUVLightBox container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);

        ySize = 196;
    }

    @Override
    public void init() {
        super.init();

        addButton(slider = new Slider(guiLeft + 10, guiTop + 45, 95, 16,
                xlate("pneumaticcraft.gui.uv_light_box.threshold").appendString(" "), new StringTextComponent("%"),
                1, 100, te.getThreshold(), false, true, b -> { }, this));
    }

    @Override
    public void tick() {
        if (firstUpdate) {
            // te sync packet hasn't necessarily arrived when init() is called; need to set it up here
            slider.setValue(te.getThreshold());
            slider.updateSlider();
        }
        super.tick();
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        BlockState state = te.getBlockState();
        return state.getBlock() == ModBlocks.UV_LIGHT_BOX.get() && state.get(BlockUVLightBox.LIT) ?
                Textures.GUI_UV_LIGHT_BOX_ON : Textures.GUI_UV_LIGHT_BOX;
    }

    @Override
    protected PointXY getGaugeLocation() {
        int xStart = (width - xSize) / 2;
        int yStart = (height - ySize) / 2;
        return new PointXY(xStart + xSize * 3 / 4 + 10, yStart + ySize / 4 - 5);
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);

        if (te.getPrimaryInventory().getStackInSlot(TileEntityUVLightBox.PCB_SLOT).isEmpty()) {
            textList.add(TextFormatting.BLACK + I18n.format("pneumaticcraft.gui.tab.problems.uv_light_box.no_item"));
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
