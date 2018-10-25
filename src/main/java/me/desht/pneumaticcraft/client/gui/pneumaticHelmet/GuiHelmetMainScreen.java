package me.desht.pneumaticcraft.client.gui.pneumaticHelmet;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.gui.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.widget.GuiKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.renderHandler.MainHelmetHandler;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.Itemss;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiHelmetMainScreen extends GuiPneumaticScreenBase implements IGuiScreen {
    private static final String TITLE_PREFIX = TextFormatting.AQUA + "" + TextFormatting.UNDERLINE;

    public static final ItemStack[] ARMOR_STACKS = new ItemStack[]{
            new ItemStack(Itemss.PNEUMATIC_BOOTS),
            new ItemStack(Itemss.PNEUMATIC_LEGGINGS),
            new ItemStack(Itemss.PNEUMATIC_CHESTPLATE),
            new ItemStack(Itemss.PNEUMATIC_HELMET)
    };
    private final List<UpgradeOption> upgradeOptions = new ArrayList<>();
    private static int pageNumber;
    private boolean inInitPhase = true;

    private static GuiHelmetMainScreen instance;//Creating a static instance, as we can use it to handle keybinds when the GUI is closed.

    public static GuiHelmetMainScreen getInstance() {
        return instance;
    }

    public static void init() {
        instance = new GuiHelmetMainScreen();
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        ScaledResolution scaledresolution = new ScaledResolution(minecraft);
        int width = scaledresolution.getScaledWidth();
        int height = scaledresolution.getScaledHeight();
        instance.setWorldAndResolution(minecraft, width, height);

        for (int i = 1; i < instance.upgradeOptions.size(); i++) {
            pageNumber = i;
            instance.initGui();
        }
        pageNumber = 0;
        instance.inInitPhase = false;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        upgradeOptions.clear();
        addPages();
        for (int i = 0; i < upgradeOptions.size(); i++) {
            GuiButtonSpecial button = new GuiButtonSpecial(100 + i, 210, 20 + i * 22, 120, 20, upgradeOptions.get(i).page.getPageName());
            button.setRenderStacks(upgradeOptions.get(i).icons);
            button.setIconPosition(GuiButtonSpecial.IconPosition.RIGHT);
            if (pageNumber == i) button.enabled = false;
            buttonList.add(button);
        }
        if (pageNumber > upgradeOptions.size() - 1) {
            pageNumber = upgradeOptions.size() - 1;
        }
        GuiKeybindCheckBox checkBox = new GuiKeybindCheckBox(100, 40, 25, 0xFFFFFFFF,
                I18n.format("gui.enableModule", I18n.format(GuiKeybindCheckBox.UPGRADE_PREFIX + upgradeOptions.get(pageNumber).text)),
                GuiKeybindCheckBox.UPGRADE_PREFIX + upgradeOptions.get(pageNumber).text);
        if (upgradeOptions.get(pageNumber).page.canBeTurnedOff()) {
            addWidget(checkBox);
        }
        upgradeOptions.get(pageNumber).page.initGui(this);
    }

    @Override
    protected ResourceLocation getTexture() {
        return null;
    }

    private void addPages() {
        for (EntityEquipmentSlot slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
            for (int i = 0; i < renderHandlers.size(); i++) {
                if (inInitPhase || CommonHUDHandler.getHandlerForPlayer().isUpgradeRendererInserted(slot, i)) {
                    IUpgradeRenderHandler upgradeRenderHandler = renderHandlers.get(i);
                    if (inInitPhase
                            || FMLClientHandler.instance().getClient().player.getItemStackFromSlot(slot).getItem() instanceof ItemPneumaticArmor
                            || upgradeRenderHandler instanceof MainHelmetHandler) {
                        IOptionPage optionPage = upgradeRenderHandler.getGuiOptionsPage();
                        if (optionPage != null) {
                            List<ItemStack> stacks = new ArrayList<>();
                            stacks.add(ARMOR_STACKS[upgradeRenderHandler.getEquipmentSlot().getIndex()]);
                            Arrays.stream(upgradeRenderHandler.getRequiredUpgrades()).map(ItemStack::new).forEach(stacks::add);
                            upgradeOptions.add(new UpgradeOption(optionPage, upgradeRenderHandler.getUpgradeName(), stacks.toArray(new ItemStack[0])));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        drawDefaultBackground();
        IOptionPage optionPage = upgradeOptions.get(pageNumber).page;
        optionPage.drawPreButtons(x, y, partialTicks);
        super.drawScreen(x, y, partialTicks);
        optionPage.drawScreen(x, y, partialTicks);
        drawCenteredString(fontRenderer, TITLE_PREFIX + upgradeOptions.get(pageNumber).page.getPageName(), 100, 12, 0xFFFFFFFF);
        if (optionPage.displaySettingsText()) drawCenteredString(fontRenderer, "Settings", 100, optionPage.settingsYposition(), 0xFFFFFFFF);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        IOptionPage optionPage = upgradeOptions.get(pageNumber).page;
        optionPage.updateScreen();
    }

    @Override
    public void keyTyped(char par1, int par2) throws IOException {
        super.keyTyped(par1, par2);
        upgradeOptions.get(pageNumber).page.keyTyped(par1, par2);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id >= 100 && button.id < 100 + upgradeOptions.size()) {
            pageNumber = button.id - 100;
            initGui();
        } else {
            upgradeOptions.get(pageNumber).page.actionPerformed(button);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        upgradeOptions.get(pageNumber).page.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int par3) throws IOException {
        super.mouseClicked(mouseX, mouseY, par3);
        upgradeOptions.get(pageNumber).page.mouseClicked(mouseX, mouseY, par3);
    }

    @Override
    public List getButtonList() {
        return buttonList;
    }

    @Override
    public FontRenderer getFontRenderer() {
        return fontRenderer;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static class UpgradeOption {
        private final IOptionPage page;
        private final String text;
        private final ItemStack[] icons;

        UpgradeOption(IOptionPage page, String text, ItemStack... icons) {
            this.page = page;
            this.text = text;
            this.icons = icons;
        }
    }
}
