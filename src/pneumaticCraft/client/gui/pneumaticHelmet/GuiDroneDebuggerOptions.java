package pneumaticCraft.client.gui.pneumaticHelmet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.ChunkPosition;

import org.lwjgl.input.Keyboard;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.client.pneumaticHelmet.IGuiScreen;
import pneumaticCraft.api.client.pneumaticHelmet.IOptionPage;
import pneumaticCraft.client.KeyHandler;
import pneumaticCraft.client.gui.GuiUnitProgrammer;
import pneumaticCraft.client.render.pneumaticArmor.DroneDebugUpgradeHandler;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.entity.living.DebugEntry;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetStart;
import pneumaticCraft.lib.NBTKeys;

public class GuiDroneDebuggerOptions extends Gui implements IOptionPage{
    private final DroneDebugUpgradeHandler upgradeHandler;
    private EntityDrone selectedDrone;
    private GuiUnitProgrammer programmerUnit;
    private static final int PROGRAMMING_START_Y = 40;
    private int programmingStartX, programmingWidth, programmingHeight;
    private IProgWidget areaShowingWidget;
    private int screenWidth, screenHeight;

    public GuiDroneDebuggerOptions(DroneDebugUpgradeHandler upgradeHandler){
        this.upgradeHandler = upgradeHandler;
    }

    @Override
    public String getPageName(){
        return "Drone Debugging";
    }

    @Override
    public void initGui(IGuiScreen gui){
        GuiScreen guiScreen = (GuiScreen)gui;
        screenWidth = guiScreen.width;
        screenHeight = guiScreen.height;

        if(PneumaticCraft.proxy.getPlayer() != null) {
            ItemStack helmet = PneumaticCraft.proxy.getPlayer().getCurrentArmor(3);
            if(helmet != null) {
                int entityId = NBTUtil.getInteger(helmet, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE);
                Entity entity = PneumaticCraft.proxy.getClientWorld().getEntityByID(entityId);
                if(entity instanceof EntityDrone) {
                    selectedDrone = (EntityDrone)entity;
                }
            }
        }

        int spacing = 20;
        programmingStartX = spacing;
        programmingWidth = guiScreen.width - spacing * 2;
        programmingHeight = guiScreen.height - spacing - PROGRAMMING_START_Y;
        programmerUnit = new DebugInfoProgrammerUnit(selectedDrone != null ? selectedDrone.getProgWidgets() : new ArrayList<IProgWidget>(), gui.getFontRenderer(), 0, 0, guiScreen.width, guiScreen.height, 100, programmingStartX, PROGRAMMING_START_Y, programmingWidth, programmingHeight, 0, 0, 0);
        if(selectedDrone != null) {
            for(IProgWidget widget : selectedDrone.getProgWidgets()) {
                if(widget instanceof ProgWidgetStart) {
                    programmerUnit.gotoPiece(widget);
                    break;
                }
            }
        }
    }

    @Override
    public void actionPerformed(GuiButton button){

    }

    @Override
    public void drawPreButtons(int x, int y, float partialTicks){
        drawRect(programmingStartX, PROGRAMMING_START_Y, programmingStartX + programmingWidth, PROGRAMMING_START_Y + programmingHeight, 0x55000000);
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks){
        if(selectedDrone != null) {
            Minecraft.getMinecraft().fontRenderer.drawString("Drone name: " + selectedDrone.getCommandSenderName(), 20, screenHeight - 15, 0xFFFFFFFF, true);
            Minecraft.getMinecraft().fontRenderer.drawString("Routine: " + selectedDrone.getLabel(), screenWidth / 2, screenHeight - 15, 0xFFFFFFFF, true);
        }

        programmerUnit.render(x, y, true, true, true);
        programmerUnit.renderForeground(x, y, null);

        if(selectedDrone == null) {
            drawCenteredString(Minecraft.getMinecraft().fontRenderer, "Press '" + Keyboard.getKeyName(KeyHandler.getInstance().keybindDebuggingDrone.getKeyCode()) + "' on a Drone when tracked by an Entity Tracker to debug the Drone.", screenWidth / 2, screenHeight / 2, 0xFFFF0000);
        }

        IProgWidget widget = programmerUnit.getHoveredWidget(x, y);
        if(widget == null) widget = areaShowingWidget;
        upgradeHandler.getShowingPositions().clear();
        if(widget != null) {
            int widgetId = selectedDrone.getProgWidgets().indexOf(widget);
            for(DebugEntry entry : selectedDrone.getDebugEntries()) {
                if(entry.getProgWidgetId() == widgetId && !entry.getPos().equals(new ChunkPosition(0, 0, 0))) {
                    upgradeHandler.getShowingPositions().add(entry.getPos());
                }
            }
        }
    }

    @Override
    public void keyTyped(char ch, int key){

    }

    @Override
    public void mouseClicked(int x, int y, int button){
        if(button == 0) {
            areaShowingWidget = programmerUnit.getHoveredWidget(x, y);
        }
    }

    @Override
    public void handleMouseInput(){
        programmerUnit.getScrollBar().handleMouseInput();
    }

    private class DebugInfoProgrammerUnit extends GuiUnitProgrammer{

        public DebugInfoProgrammerUnit(List<IProgWidget> progWidgets, FontRenderer fontRendererObj, int guiLeft,
                int guiTop, int width, int height, int xSize, int startX, int startY, int areaWidth, int areaHeight,
                int translatedX, int translatedY, int lastZoom){
            super(progWidgets, fontRendererObj, guiLeft, guiTop, width, height, xSize, startX, startY, areaWidth, areaHeight, translatedX, translatedY, lastZoom);
        }

        @Override
        protected void addAdditionalInfoToTooltip(IProgWidget widget, List<String> tooltip){
            int widgetId = selectedDrone.getProgWidgets().indexOf(widget);
            Map<String, Integer> messageTimesMap = new LinkedHashMap<String, Integer>();
            boolean hasCoords = false;
            for(DebugEntry entry : selectedDrone.getDebugEntries()) {
                if(entry.getProgWidgetId() == widgetId) {
                    Integer oldTimes = messageTimesMap.get(entry.getMessage());
                    if(oldTimes == null) oldTimes = 0;
                    messageTimesMap.put(entry.getMessage(), oldTimes + 1);
                    if(!entry.getPos().equals(new ChunkPosition(0, 0, 0))) {
                        hasCoords = true;
                    }
                }
            }
            for(Map.Entry<String, Integer> entry : messageTimesMap.entrySet()) {
                tooltip.add(entry.getValue() + "x " + I18n.format(entry.getKey()));
            }
            if(hasCoords) {
                tooltip.add(EnumChatFormatting.GREEN + I18n.format("gui.progWidget.debug.hasPositions"));
                if(widget != areaShowingWidget) tooltip.add(EnumChatFormatting.GREEN + I18n.format("gui.progWidget.debug.clickToShow"));
            }

        }

        @Override
        protected void renderAdditionally(){
            IProgWidget widget = selectedDrone != null ? selectedDrone.getActiveWidget() : null;
            if(widget != null) drawBorder(widget, 0xFF00FF00);
        }
    }

    @Override
    public boolean canBeTurnedOff(){
        return false;
    }

    @Override
    public boolean displaySettingsText(){
        return false;
    }

}
