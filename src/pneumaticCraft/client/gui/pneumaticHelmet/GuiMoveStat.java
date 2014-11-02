package pneumaticCraft.client.gui.pneumaticHelmet;

import net.minecraft.client.gui.GuiScreen;
import pneumaticCraft.api.client.IGuiAnimatedStat;
import pneumaticCraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import cpw.mods.fml.client.FMLClientHandler;

public class GuiMoveStat extends GuiScreen{
    private final IGuiAnimatedStat movedStat;
    private final IUpgradeRenderHandler renderHandler;

    public GuiMoveStat(IUpgradeRenderHandler renderHandler){
        movedStat = renderHandler.getAnimatedStat();
        this.renderHandler = renderHandler;
        if(movedStat == null) {
            System.err.println("OPENING A MOVE STAT GUI WHILE THERE IS NO STAT TO MOVE!");
            FMLClientHandler.instance().getClient().thePlayer.closeScreen();
        }
    }

    public GuiMoveStat(IUpgradeRenderHandler renderHandler, GuiAnimatedStat movedStat){
        this.renderHandler = renderHandler;
        this.movedStat = movedStat;
    }

    @Override
    protected void mouseClickMove(int x, int y, int lastButtonClicked, long timeSinceMouseClick){
        movedStat.setBaseX(x);
        movedStat.setBaseY(y);
        renderHandler.saveToConfig();
    }

    @Override
    protected void mouseClicked(int x, int y, int mouseButton){
        if(mouseButton == 2) movedStat.setLeftSided(!movedStat.isLeftSided());
        else {
            movedStat.setBaseX(x);
            movedStat.setBaseY(y);
        }
        renderHandler.saveToConfig();
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks){
        drawDefaultBackground();
        drawString(fontRendererObj, "Middle mouse click to switch between expansion to the right or left", 5, 5, 0xFFFFFFFF);
        movedStat.render(x, y, partialTicks);
        super.drawScreen(x, y, partialTicks);
    }

    @Override
    public void updateScreen(){
        super.updateScreen();
        movedStat.update();
    }
}
