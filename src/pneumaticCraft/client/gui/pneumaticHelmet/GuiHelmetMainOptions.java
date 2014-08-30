package pneumaticCraft.client.gui.pneumaticHelmet;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;

import pneumaticCraft.api.client.pneumaticHelmet.IGuiScreen;
import pneumaticCraft.api.client.pneumaticHelmet.IOptionPage;
import pneumaticCraft.client.gui.widget.GuiAnimatedStat;
import pneumaticCraft.client.render.pneumaticArmor.HUDHandler;
import pneumaticCraft.client.render.pneumaticArmor.MainHelmetHandler;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;

public class GuiHelmetMainOptions implements IOptionPage{

    private final MainHelmetHandler renderHandler;
    private GuiButton changeKeybindingButton;
    private boolean changingKeybinding;

    public GuiHelmetMainOptions(MainHelmetHandler renderHandler){
        this.renderHandler = renderHandler;
    }

    @Override
    public String getPageName(){
        return "General Helmet Options";
    }

    @Override
    public void initGui(IGuiScreen gui){
        gui.getButtonList().add(new GuiButton(10, 30, 128, 150, 20, "Move Pressure Stat Screen..."));
        gui.getButtonList().add(new GuiButton(11, 30, 150, 150, 20, "Move Message Screen..."));
        changeKeybindingButton = new GuiButton(12, 30, 172, 150, 20, "Change open menu key...");
        gui.getButtonList().add(changeKeybindingButton);
    }

    @Override
    public void actionPerformed(GuiButton button){
        switch(button.id){
            case 10:
                FMLClientHandler.instance().getClient().thePlayer.closeScreen();
                FMLCommonHandler.instance().showGuiScreen(new GuiMoveStat(renderHandler));
                break;
            case 11:
                FMLClientHandler.instance().getClient().thePlayer.closeScreen();
                renderHandler.testMessageStat = new GuiAnimatedStat(null, "Test Message, keep in mind messages can be long!", renderHandler.messagesStatX, renderHandler.messagesStatY, 0x7000AA00, null, renderHandler.messagesStatLeftSided);
                renderHandler.testMessageStat.openWindow();
                FMLCommonHandler.instance().showGuiScreen(new GuiMoveStat(renderHandler, renderHandler.testMessageStat));
                break;
            case 12:
                changingKeybinding = !changingKeybinding;
                updateKeybindingButtonText();
                break;
        }
    }

    private void updateKeybindingButtonText(){
        if(changingKeybinding) {
            changeKeybindingButton.displayString = "Press button to set keybind.";
        } else {
            changeKeybindingButton.displayString = "Change open menu key...";
        }
    }

    @Override
    public void keyTyped(char ch, int key){
        if(changingKeybinding) {
            changingKeybinding = false;
            updateKeybindingButtonText();

            HUDHandler.instance().keybindOpenOptions.setKeyCode(key);
            KeyBinding.resetKeyBindingArrayAndHash();
            FMLClientHandler.instance().getClient().thePlayer.addChatComponentMessage(new ChatComponentTranslation(EnumChatFormatting.GREEN + "Bound the opening of this menu to the '" + Keyboard.getKeyName(key) + "' key."));
        }
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks){}

}
