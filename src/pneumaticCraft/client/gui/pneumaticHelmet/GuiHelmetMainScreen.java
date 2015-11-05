package pneumaticCraft.client.gui.pneumaticHelmet;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.api.client.pneumaticHelmet.IGuiScreen;
import pneumaticCraft.api.client.pneumaticHelmet.IOptionPage;
import pneumaticCraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import pneumaticCraft.client.gui.GuiPneumaticScreenBase;
import pneumaticCraft.client.gui.widget.GuiKeybindCheckBox;
import pneumaticCraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import pneumaticCraft.common.CommonHUDHandler;
import cpw.mods.fml.client.FMLClientHandler;

public class GuiHelmetMainScreen extends GuiPneumaticScreenBase implements IGuiScreen{

    private final List<IOptionPage> upgradePages = new ArrayList<IOptionPage>();
    private final List<String> upgradePageNames = new ArrayList<String>();
    private static int page;
    private boolean init = true;

    private static GuiHelmetMainScreen instance;//Creating a static instance, as we can use it to handle keybinds when the GUI is closed.

    public static GuiHelmetMainScreen getInstance(){
        return instance;
    }

    public static void init(){
        instance = new GuiHelmetMainScreen();
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        ScaledResolution scaledresolution = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
        int width = scaledresolution.getScaledWidth();
        int height = scaledresolution.getScaledHeight();
        instance.setWorldAndResolution(minecraft, width, height);

        for(int i = 1; i < instance.upgradePages.size(); i++) {
            page = i;
            instance.initGui();
        }
        page = 0;
        instance.init = false;
    }

    @Override
    public void initGui(){
        super.initGui();
        buttonList.clear();
        upgradePages.clear();
        upgradePageNames.clear();
        addPages();
        for(int i = 0; i < upgradePages.size(); i++) {
            GuiButton button = new GuiButton(100 + i, 210, 20 + i * 22, 200, 20, upgradePages.get(i).getPageName());
            if(page == i) button.enabled = false;
            buttonList.add(button);
        }
        if(page > upgradePages.size() - 1) page = upgradePages.size() - 1;
        GuiKeybindCheckBox checkBox = new GuiKeybindCheckBox(100, 40, 12, 0xFFFFFFFF, I18n.format("gui.enableModule", I18n.format("pneumaticHelmet.upgrade." + upgradePageNames.get(page))), "pneumaticHelmet.upgrade." + upgradePageNames.get(page));
        if(upgradePages.get(page).canBeTurnedOff()) addWidget(checkBox);
        upgradePages.get(page).initGui(this);
    }

    @Override
    protected ResourceLocation getTexture(){
        return null;
    }

    private void addPages(){
        for(int i = 0; i < UpgradeRenderHandlerList.instance().upgradeRenderers.size(); i++) {
            if(init || CommonHUDHandler.getHandlerForPlayer().upgradeRenderersInserted[i]) {
                IUpgradeRenderHandler upgradeRenderHandler = UpgradeRenderHandlerList.instance().upgradeRenderers.get(i);
                IOptionPage optionPage = upgradeRenderHandler.getGuiOptionsPage();
                if(optionPage != null) {
                    upgradePageNames.add(upgradeRenderHandler.getUpgradeName());
                    upgradePages.add(optionPage);
                }
            }
        }
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks){
        drawDefaultBackground();
        IOptionPage optionPage = upgradePages.get(page);
        optionPage.drawPreButtons(x, y, partialTicks);
        super.drawScreen(x, y, partialTicks);
        optionPage.drawScreen(x, y, partialTicks);
        drawCenteredString(fontRendererObj, upgradePages.get(page).getPageName(), 100, 25, 0xFFFFFFFF);
        if(optionPage.displaySettingsText()) drawCenteredString(fontRendererObj, "Settings", 100, 115, 0xFFFFFFFF);
    }

    @Override
    public void keyTyped(char par1, int par2){
        super.keyTyped(par1, par2);
        if(par2 != 1) {
            for(IOptionPage page : upgradePages) {
                page.keyTyped(par1, par2);
            }
        }
    }

    @Override
    public void actionPerformed(GuiButton button){
        if(button.id >= 100 && button.id < 100 + upgradePages.size()) {
            page = button.id - 100;
            initGui();
        } else {
            upgradePages.get(page).actionPerformed(button);
        }
    }

    @Override
    public void handleMouseInput(){
        super.handleMouseInput();
        upgradePages.get(page).handleMouseInput();
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3){
        super.mouseClicked(par1, par2, par3);
        upgradePages.get(page).mouseClicked(par1, par2, par3);
    }

    @Override
    public List getButtonList(){
        return buttonList;
    }

    @Override
    public FontRenderer getFontRenderer(){
        return fontRendererObj;
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }
}
