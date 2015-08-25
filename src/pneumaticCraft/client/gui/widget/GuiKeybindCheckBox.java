package pneumaticCraft.client.gui.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import pneumaticCraft.client.render.pneumaticArmor.HUDHandler;
import pneumaticCraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketToggleHelmetFeature;
import pneumaticCraft.lib.Names;
import pneumaticCraft.proxy.ClientProxy;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;

public class GuiKeybindCheckBox extends GuiCheckBox{
    private boolean isAwaitingKey;
    private String oldCheckboxText;
    private KeyBinding keyBinding;
    public final String keyBindingName;
    public static final Map<String, GuiKeybindCheckBox> trackedCheckboxes = new HashMap<String, GuiKeybindCheckBox>();

    public GuiKeybindCheckBox(int id, int x, int y, int color, String text){
        this(id, x, y, color, text, text);
    }

    public GuiKeybindCheckBox(int id, int x, int y, int color, String text, String keyBindingName){
        super(id, x, y, color, text);
        this.keyBindingName = keyBindingName;
        keyBinding = setOrAddKeybind(keyBindingName, -1);//get the saved value.
        if(!trackedCheckboxes.containsKey(keyBindingName)) {
            checked = Config.config.get("pneumatic_helmet_widgetDefaults", keyBindingName, true).getBoolean();
            trackedCheckboxes.put(keyBindingName, this);
            FMLCommonHandler.instance().bus().register(this);
        } else {
            checked = trackedCheckboxes.get(keyBindingName).checked;
        }
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button){
        if(button == 0) {
            super.onMouseClicked(mouseX, mouseY, button);
            GuiKeybindCheckBox trackedBox = trackedCheckboxes.get(keyBindingName);
            if(trackedBox != this) {
                trackedBox.onMouseClicked(mouseX, mouseY, button);
            } else {
                Config.config.get("pneumatic_helmet_widgetDefaults", keyBindingName, true).set(checked);
                Config.config.save();

                for(int i = 0; i < UpgradeRenderHandlerList.instance().upgradeRenderers.size(); i++) {
                    IUpgradeRenderHandler upgradeRenderHandler = UpgradeRenderHandlerList.instance().upgradeRenderers.get(i);
                    if(("pneumaticHelmet.upgrade." + upgradeRenderHandler.getUpgradeName()).equals(keyBindingName)) {
                        NetworkHandler.sendToServer(new PacketToggleHelmetFeature((byte)i, GuiKeybindCheckBox.trackedCheckboxes.get("pneumaticHelmet.upgrade.coreComponents").checked && checked));
                    }
                }
                if(keyBindingName.equals("pneumaticHelmet.upgrade.coreComponents")) {
                    for(int i = 0; i < UpgradeRenderHandlerList.instance().upgradeRenderers.size(); i++) {
                        NetworkHandler.sendToServer(new PacketToggleHelmetFeature((byte)i, checked && GuiKeybindCheckBox.trackedCheckboxes.get("pneumaticHelmet.upgrade." + UpgradeRenderHandlerList.instance().upgradeRenderers.get(i).getUpgradeName()).checked));
                    }
                }
            }
        } else {
            isAwaitingKey = !isAwaitingKey;
            if(isAwaitingKey) {
                oldCheckboxText = text;
                text = "gui.setKeybind";
            } else {
                text = oldCheckboxText;
            }
        }
    }

    @Override
    public boolean onKey(char key, int keyCode){
        if(isAwaitingKey) {
            isAwaitingKey = false;
            keyBinding = setOrAddKeybind(keyBindingName, keyCode);
            text = oldCheckboxText;
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event){
        Minecraft mc = FMLClientHandler.instance().getClient();
        if(mc.inGameHasFocus && keyBinding != null && keyBinding.isPressed()) {
            onMouseClicked(0, 0, 0);
            HUDHandler.instance().addMessage(I18n.format("pneumaticHelmet.message." + (checked ? "enable" : "disable") + "Setting", I18n.format(text)), new ArrayList<String>(), 60, 0x7000AA00);
        }
    }

    /**
     *
     * @param keybindName
     * @param keyCode when a value of <0 is parsed, this will function as a getter, with a chance of returning null.
     * @return
     */
    public static KeyBinding setOrAddKeybind(String keybindName, int keyCode){
        GameSettings gameSettings = FMLClientHandler.instance().getClient().gameSettings;
        for(KeyBinding keyBinding : gameSettings.keyBindings) {
            if(keyBinding != null && keyBinding.getKeyDescription().equals(keybindName)) {
                if(keybindName.equals(keyBinding.getKeyDescription())) {
                    if(keyCode >= 0) {
                        keyBinding.setKeyCode(keyCode);
                        KeyBinding.resetKeyBindingArrayAndHash();
                        gameSettings.saveOptions();
                    }
                    return keyBinding;
                }
            }
        }
        //When the keybind wasn't added yet
        if(keyCode < 0) {
            if(((ClientProxy)PneumaticCraft.proxy).keybindToKeyCodes.containsKey(keybindName)) {//If the keybind can be found in the options file
                keyCode = ((ClientProxy)PneumaticCraft.proxy).keybindToKeyCodes.get(keybindName);
            } else {
                return null;
            }
        }
        KeyBinding keyBinding = new KeyBinding(keybindName, keyCode, Names.PNEUMATIC_KEYBINDING_CATEGORY);
        ClientRegistry.registerKeyBinding(keyBinding);
        KeyBinding.resetKeyBindingArrayAndHash();
        gameSettings.saveOptions();
        return keyBinding;
    }

    @Override
    public void addTooltip(int mouseX, int mouseY, List<String> curTooltip, boolean shiftPressed){
        if(keyBinding != null) {
            curTooltip.add(I18n.format("gui.keybindBoundKey", Keyboard.getKeyName(keyBinding.getKeyCode())));
        } else if(!isAwaitingKey) {
            curTooltip.add("gui.keybindRightClickToSet");
        }
    }
}
