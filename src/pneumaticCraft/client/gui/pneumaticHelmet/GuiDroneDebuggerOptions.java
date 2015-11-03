package pneumaticCraft.client.gui.pneumaticHelmet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import pneumaticCraft.api.client.pneumaticHelmet.IGuiScreen;
import pneumaticCraft.api.client.pneumaticHelmet.IOptionPage;
import pneumaticCraft.client.render.pneumaticArmor.EntityTrackUpgradeHandler;
import pneumaticCraft.client.render.pneumaticArmor.HUDHandler;
import pneumaticCraft.client.render.pneumaticArmor.RenderTarget;
import pneumaticCraft.common.entity.living.EntityDrone;

public class GuiDroneDebuggerOptions implements IOptionPage{
    private final List<EntityDrone> allDrones = new ArrayList<EntityDrone>();
    private List<GuiButton> droneButtons;
    private static EntityDrone selectedDrone;

    public GuiDroneDebuggerOptions(){
        List<RenderTarget> targets = HUDHandler.instance().getSpecificRenderer(EntityTrackUpgradeHandler.class).getTargets();
        for(RenderTarget target : targets) {
            if(target.entity instanceof EntityDrone) {
                allDrones.add((EntityDrone)target.entity);
            }
        }
        if(!allDrones.contains(selectedDrone)) {
            selectedDrone = null;
        }
    }

    @Override
    public String getPageName(){
        return "Drone Debugging";
    }

    @Override
    public void initGui(IGuiScreen gui){
        droneButtons = new ArrayList<GuiButton>();
        GuiScreen guiScreen = (GuiScreen)gui;

        Map<String, Integer> nameMap = new HashMap<String, Integer>();
        for(int i = 0; i < allDrones.size(); i++) {
            EntityDrone drone = allDrones.get(i);
            String buttonText = drone.getCommandSenderName();
            Integer timesInMap = nameMap.get(buttonText);
            if(timesInMap != null) {
                nameMap.put(buttonText, timesInMap + 1);
                buttonText += " " + (timesInMap + 1);
            } else {
                nameMap.put(buttonText, 1);
            }
            GuiButton button = new GuiButton(i, 30, 22 * i + 40, 100, 20, buttonText);
            button.enabled = drone != selectedDrone;

            gui.getButtonList().add(button);
            droneButtons.add(button);
        }
    }

    @Override
    public void actionPerformed(GuiButton button){
        for(int i = 0; i < droneButtons.size(); i++) {
            droneButtons.get(i).enabled = i != button.id;
        }
        selectedDrone = allDrones.get(button.id);
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks){

    }

    @Override
    public void keyTyped(char ch, int key){

    }

}
