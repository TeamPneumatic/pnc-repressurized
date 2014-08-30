package pneumaticCraft.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketGuiButton;
import pneumaticCraft.common.tileentity.TileEntityCreativeCompressor;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class GuiCreativeCompressor extends GuiPneumaticScreenBase{

    private final TileEntityCreativeCompressor te;

    public GuiCreativeCompressor(TileEntityCreativeCompressor te){
        this.te = te;
    }

    @Override
    protected ResourceLocation getTexture(){
        return null;
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
    protected void actionPerformed(GuiButton button){
        NetworkHandler.sendToServer(new PacketGuiButton(te, button.id));
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks){
        drawDefaultBackground();
        super.drawScreen(x, y, partialTicks);
        drawCenteredString(fontRendererObj, PneumaticCraftUtils.roundNumberTo(te.getPressure(ForgeDirection.UNKNOWN), 1) + " bar", width / 2, height / 2, 0xFFFFFF);
    }
}
