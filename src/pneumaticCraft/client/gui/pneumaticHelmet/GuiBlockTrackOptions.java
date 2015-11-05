package pneumaticCraft.client.gui.pneumaticHelmet;

import net.minecraft.client.gui.GuiButton;
import pneumaticCraft.api.client.pneumaticHelmet.IGuiScreen;
import pneumaticCraft.api.client.pneumaticHelmet.IOptionPage;
import pneumaticCraft.client.gui.widget.GuiKeybindCheckBox;
import pneumaticCraft.client.render.pneumaticArmor.BlockTrackUpgradeHandler;
import pneumaticCraft.client.render.pneumaticArmor.blockTracker.BlockTrackEntryList;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;

public class GuiBlockTrackOptions implements IOptionPage{

    private final BlockTrackUpgradeHandler renderHandler;

    public GuiBlockTrackOptions(BlockTrackUpgradeHandler renderHandler){
        this.renderHandler = renderHandler;
    }

    @Override
    public String getPageName(){
        return "Block Tracker";
    }

    @Override
    public void initGui(IGuiScreen gui){
        gui.getButtonList().add(new GuiButton(10, 30, 128, 150, 20, "Move Stat Screen..."));
        for(int i = 0; i < BlockTrackEntryList.instance.trackList.size(); i++) {
            ((GuiHelmetMainScreen)gui).addWidget(new GuiKeybindCheckBox(i, 5, 32 + i * 12, 0xFFFFFFFF, BlockTrackEntryList.instance.trackList.get(i).getEntryName()));
        }
    }

    @Override
    public void actionPerformed(GuiButton button){
        if(button.id == 10) {
            FMLClientHandler.instance().getClient().thePlayer.closeScreen();
            FMLCommonHandler.instance().showGuiScreen(new GuiMoveStat(renderHandler));
        }
    }

    @Override
    public void drawPreButtons(int x, int y, float partialTicks){}

    @Override
    public void drawScreen(int x, int y, float partialTicks){}

    @Override
    public void keyTyped(char ch, int key){}

    @Override
    public void mouseClicked(int x, int y, int button){}

    @Override
    public void handleMouseInput(){}

    @Override
    public boolean canBeTurnedOff(){
        return true;
    }

    @Override
    public boolean displaySettingsText(){
        return true;
    }

}
