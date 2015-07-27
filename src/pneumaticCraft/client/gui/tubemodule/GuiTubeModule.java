package pneumaticCraft.client.gui.tubemodule;

import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.client.gui.GuiPneumaticScreenBase;
import pneumaticCraft.common.block.BlockPressureTube;
import pneumaticCraft.common.block.tubes.TubeModule;

public abstract class GuiTubeModule extends GuiPneumaticScreenBase{
    protected final TubeModule module;

    public GuiTubeModule(EntityPlayer player, int x, int y, int z){
        this(BlockPressureTube.getLookedModule(player.worldObj, x, y, z, player));
    }

    public GuiTubeModule(TubeModule module){
        this.module = module;
        xSize = 183;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }
}
