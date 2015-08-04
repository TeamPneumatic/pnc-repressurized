package pneumaticCraft.client.gui;

import java.awt.Point;

import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.inventory.ContainerPneumaticBase;
import pneumaticCraft.common.tileentity.TileEntityCreativeCompressor;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class GuiCreativeCompressor extends GuiPneumaticContainerBase<TileEntityCreativeCompressor>{

    public GuiCreativeCompressor(TileEntityCreativeCompressor te){
        super(new ContainerPneumaticBase(te), te, null);
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }

    @Override
    public void initGui(){
        int y = height / 2 - 5;
        int x = width / 2;
        buttonList.add(new GuiButton(0, x - 90, y, 30, 20, "-1"));
        buttonList.add(new GuiButton(1, x - 58, y, 30, 20, "-0.1"));
        buttonList.add(new GuiButton(2, x + 28, y, 30, 20, "+0.1"));
        buttonList.add(new GuiButton(3, x + 60, y, 30, 20, "+1"));
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks){
        super.drawScreen(x, y, partialTicks);
        drawCenteredString(fontRendererObj, PneumaticCraftUtils.roundNumberTo(te.getPressure(ForgeDirection.UNKNOWN), 1) + " bar", width / 2, height / 2, 0xFFFFFF);
    }

    @Override
    protected boolean shouldDrawBackground(){
        return false;
    }

    @Override
    protected Point getInvTextOffset(){
        return null;
    }
}
