package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.common.inventory.ContainerUVLightBox;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiUVLightBox extends GuiPneumaticContainerBase<TileEntityUVLightBox> {

    public GuiUVLightBox(InventoryPlayer player, TileEntityUVLightBox te) {
        super(new ContainerUVLightBox(player, te), te, Textures.GUI_UV_LIGHT_BOX);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        fontRenderer.drawString("Upgr.", 28, 19, 4210752);
        fontRenderer.drawString("PCB", 70, 25, 4210752);
    }

    @Override
    public String getRedstoneButtonText(int mode) {
        if (mode == 0) {
            return "gui.tab.redstoneBehaviour.button.never";
        } else if (mode == 4) {
            return I18n.format("gui.tab.redstoneBehaviour.uvLightBox.button.chance") + " = 100%%";
        } else {
            return I18n.format("gui.tab.redstoneBehaviour.uvLightBox.button.chance") + " > " + (10 * mode + 60) + "%%";
        }
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);
        if (te.getPrimaryInventory().getStackInSlot(TileEntityUVLightBox.PCB_INDEX).isEmpty()) {
            textList.add("\u00a77No PCB to expose.");
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Put in an Empy PCB.", GuiConstants.MAX_CHAR_PER_LINE_LEFT));
        }
    }
}
