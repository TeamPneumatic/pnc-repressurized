package pneumaticCraft.client.gui.pneumaticHelmet;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import pneumaticCraft.api.client.pneumaticHelmet.IGuiScreen;
import pneumaticCraft.api.client.pneumaticHelmet.IOptionPage;
import pneumaticCraft.client.render.pneumaticArmor.ArmorMessage;
import pneumaticCraft.client.render.pneumaticArmor.CoordTrackUpgradeHandler;
import pneumaticCraft.client.render.pneumaticArmor.HUDHandler;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCoordinateTrackerOptions implements IOptionPage{
    private GuiButton wirePath;
    private GuiButton pathEnabled;
    private GuiButton xRayEnabled;
    private GuiButton pathUpdateRate;

    @Override
    public String getPageName(){
        return "Coordinate Tracker Upgrade";
    }

    @Override
    public void initGui(IGuiScreen gui){
        gui.getButtonList().add(new GuiButton(10, 30, 40, 150, 20, "Select Target..."));
        gui.getButtonList().add(new GuiButton(11, 30, 62, 150, 20, "Navigate to Surface..."));
        pathEnabled = new GuiButton(12, 30, 128, 150, 20, "");
        wirePath = new GuiButton(13, 30, 150, 150, 20, "");
        xRayEnabled = new GuiButton(14, 30, 172, 150, 20, "");
        pathUpdateRate = new GuiButton(15, 30, 194, 150, 20, "");
        gui.getButtonList().add(pathEnabled);
        gui.getButtonList().add(wirePath);
        gui.getButtonList().add(xRayEnabled);
        gui.getButtonList().add(pathUpdateRate);
        updateButtonTexts();
    }

    @Override
    public void actionPerformed(GuiButton button){
        Minecraft mc = FMLClientHandler.instance().getClient();
        CoordTrackUpgradeHandler coordHandler = HUDHandler.instance().getSpecificRenderer(CoordTrackUpgradeHandler.class);
        switch(button.id){
            case 10:
                mc.displayGuiScreen((GuiScreen)null);
                mc.setIngameFocus();
                coordHandler.isListeningToCoordTrackerSetting = true;
                HUDHandler.instance().addMessage(new ArmorMessage("Changing Coordinate Tracker coordinate...", Arrays.asList("Right-click the desired coordinate"), 90, 0x7000AA00));
                break;
            case 11:
                mc.displayGuiScreen((GuiScreen)null);
                mc.setIngameFocus();
                switch(coordHandler.navigateToSurface(mc.thePlayer)){
                    case EASY_PATH:
                        HUDHandler.instance().addMessage(new ArmorMessage(I18n.format("pneumaticHelmet.message.coordinateTracker.routeFound"), new ArrayList<String>(), 90, 0x7000AA00));
                        break;
                    case DRONE_PATH:
                        HUDHandler.instance().addMessage(new ArmorMessage(I18n.format("pneumaticHelmet.message.coordinateTracker.harderRouteFound"), new ArrayList<String>(), 90, 0x7044AA00));
                        break;
                    case NO_PATH:
                        HUDHandler.instance().addMessage(new ArmorMessage(I18n.format("pneumaticHelmet.message.coordinateTracker.noRouteFound"), new ArrayList<String>(), 90, 0x70FF0000));
                        break;
                }

                break;
            case 12:
                coordHandler.pathEnabled = !coordHandler.pathEnabled;
                break;
            case 13:
                coordHandler.wirePath = !coordHandler.wirePath;
                break;
            case 14:
                coordHandler.xRayEnabled = !coordHandler.xRayEnabled;
                break;
            case 15:
                coordHandler.pathUpdateSetting++;
                if(coordHandler.pathUpdateSetting > 2) {
                    coordHandler.pathUpdateSetting = 0;
                }
                break;
        }
        updateButtonTexts();
        coordHandler.saveToConfig();
    }

    @Override
    public void drawPreButtons(int x, int y, float partialTicks){}

    @Override
    public void drawScreen(int x, int y, float partialTicks){}

    private void updateButtonTexts(){
        CoordTrackUpgradeHandler coordHandler = HUDHandler.instance().getSpecificRenderer(CoordTrackUpgradeHandler.class);
        pathEnabled.displayString = coordHandler.pathEnabled ? "Navigation Enabled" : "Navigation Disabled";
        wirePath.displayString = coordHandler.wirePath ? "Wire Navigation" : "Tile Navigation";
        xRayEnabled.displayString = coordHandler.xRayEnabled ? "X-Ray Enabled" : "X-Ray Disabled";
        switch(coordHandler.pathUpdateSetting){
            case 0:
                pathUpdateRate.displayString = "Path update rate: Low";
                break;
            case 1:
                pathUpdateRate.displayString = "Path update rate: Normal";
                break;
            case 2:
                pathUpdateRate.displayString = "Path update rate: Fast";
                break;
        }
        wirePath.enabled = coordHandler.pathEnabled;
        xRayEnabled.enabled = coordHandler.pathEnabled;
        pathUpdateRate.enabled = coordHandler.pathEnabled;
    }

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
