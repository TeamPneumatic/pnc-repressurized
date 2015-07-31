package pneumaticCraft.client.gui;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import pneumaticCraft.common.inventory.ContainerUVLightBox;
import pneumaticCraft.common.tileentity.TileEntityUVLightBox;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.GuiConstants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiUVLightBox extends GuiPneumaticContainerBase<TileEntityUVLightBox>{

    public GuiUVLightBox(InventoryPlayer player, TileEntityUVLightBox te){

        super(new ContainerUVLightBox(player, te), te, Textures.GUI_UV_LIGHT_BOX);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y){
        super.drawGuiContainerForegroundLayer(x, y);
        fontRendererObj.drawString("Upgr.", 28, 19, 4210752);
        fontRendererObj.drawString("PCB", 70, 25, 4210752);
    }

    @Override
    public String getRedstoneButtonText(int mode){
        if(mode == 0) {
            return "gui.tab.redstoneBehaviour.button.never";
        } else if(mode == 4) {
            return I18n.format("gui.tab.redstoneBehaviour.uvLightBox.button.chance") + " = 100%%";
        } else {
            return I18n.format("gui.tab.redstoneBehaviour.uvLightBox.button.chance") + " > " + (10 * mode + 60) + "%%";
        }
    }

    @Override
    protected void addProblems(List<String> textList){
        super.addProblems(textList);
        if(te.getStackInSlot(TileEntityUVLightBox.PCB_INDEX) == null) {
            textList.add("\u00a77No PCB to expose.");
            textList.addAll(PneumaticCraftUtils.convertStringIntoList("\u00a70Put in an Empy PCB.", GuiConstants.maxCharPerLineLeft));
        }
    }
}
