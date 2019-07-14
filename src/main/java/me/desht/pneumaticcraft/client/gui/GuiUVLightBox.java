package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.inventory.ContainerUVLightBox;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class GuiUVLightBox extends GuiPneumaticContainerBase<ContainerUVLightBox,TileEntityUVLightBox> {

    public GuiUVLightBox(ContainerUVLightBox container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        font.drawString("Upgr.", 28, 19, 4210752);
        font.drawString("PCB", 70, 25, 4210752);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_UV_LIGHT_BOX;
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);
        if (te.getPrimaryInventory().getStackInSlot(TileEntityUVLightBox.PCB_SLOT).isEmpty()) {
            textList.add(TextFormatting.GRAY + "No PCB to expose");
            textList.addAll(PneumaticCraftUtils.convertStringIntoList(TextFormatting.GRAY + "Insert an Empty PCB", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
        }
    }
}
