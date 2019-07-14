package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.widget.GuiButtonSpecial;
import me.desht.pneumaticcraft.client.gui.widget.GuiKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.MainHelmetHandler;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiHelmetMainScreen extends GuiPneumaticScreenBase implements IGuiScreen {
    private static final String TITLE_PREFIX = TextFormatting.AQUA + "" + TextFormatting.UNDERLINE;

    public static final ItemStack[] ARMOR_STACKS = new ItemStack[]{
            new ItemStack(ModItems.PNEUMATIC_BOOTS),
            new ItemStack(ModItems.PNEUMATIC_LEGGINGS),
            new ItemStack(ModItems.PNEUMATIC_CHESTPLATE),
            new ItemStack(ModItems.PNEUMATIC_HELMET)
    };
    private final List<UpgradeOption> upgradeOptions = new ArrayList<>();
    private static int pageNumber;
    private boolean inInitPhase = true;

    // A static instance which can handle keybinds when the GUI is closed.
    private static GuiHelmetMainScreen instance;

    private GuiHelmetMainScreen() {
        super(new StringTextComponent("Main Screen"));
    }

    public static GuiHelmetMainScreen getInstance() {
        return instance;
    }

    public static void initHelmetMainScreen() {
        if (instance == null) {
            instance = new GuiHelmetMainScreen();
            MainWindow mw = Minecraft.getInstance().mainWindow;
            int width = mw.getScaledWidth();
            int height = mw.getScaledHeight();
            instance.init(Minecraft.getInstance(), width, height);  // causes init() to be called

            for (int i = 1; i < instance.upgradeOptions.size(); i++) {
                pageNumber = i;
                instance.init();
            }
            pageNumber = 0;
            instance.inInitPhase = false;
        }
    }

    @Override
    public void init() {
        super.init();
        buttons.clear();
        children.clear();
        upgradeOptions.clear();
        addPages();
        for (int i = 0; i < upgradeOptions.size(); i++) {
            final int idx = i;
            GuiButtonSpecial button = new GuiButtonSpecial(210, 20 + i * 22, 120, 20,
                    upgradeOptions.get(i).page.getPageName(), b -> setPage(idx));
            button.setRenderStacks(upgradeOptions.get(i).icons);
            button.setIconPosition(GuiButtonSpecial.IconPosition.RIGHT);
            if (pageNumber == i) button.active = false;
            addButton(button);
        }
        if (pageNumber > upgradeOptions.size() - 1) {
            pageNumber = upgradeOptions.size() - 1;
        }
        if (upgradeOptions.get(pageNumber).page.canBeTurnedOff()) {
            String keybindName = GuiKeybindCheckBox.UPGRADE_PREFIX + upgradeOptions.get(pageNumber).text;
            GuiKeybindCheckBox checkBox = new GuiKeybindCheckBox(40, 25, 0xFFFFFFFF,
                    I18n.format("gui.enableModule", I18n.format(keybindName)),
                    keybindName,
                    null);
            addButton(checkBox);
        }
        upgradeOptions.get(pageNumber).page.initGui(this);
    }

    private void setPage(int newPage) {
        pageNumber = newPage;
        init();
    }

    @Override
    protected ResourceLocation getTexture() {
        return null;
    }

    private void addPages() {
        for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
            for (int i = 0; i < renderHandlers.size(); i++) {
                if (inInitPhase || CommonArmorHandler.getHandlerForPlayer().isUpgradeRendererInserted(slot, i)) {
                    IUpgradeRenderHandler upgradeRenderHandler = renderHandlers.get(i);
                    if (inInitPhase
                            || ItemPneumaticArmor.isPneumaticArmorPiece(Minecraft.getInstance().player, slot)
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
    public void render(int x, int y, float partialTicks) {
        renderBackground();
        IOptionPage optionPage = upgradeOptions.get(pageNumber).page;
        optionPage.renderPre(x, y, partialTicks);
        drawCenteredString(font, TITLE_PREFIX + upgradeOptions.get(pageNumber).page.getPageName(), 100, 12, 0xFFFFFFFF);
        if (optionPage.displaySettingsHeader()) {
            drawCenteredString(font, "Settings", 100, optionPage.settingsYposition(), 0xFFFFFFFF);
        }
        super.render(x, y, partialTicks);
        optionPage.renderPost(x, y, partialTicks);
    }

    @Override
    public void tick() {
        super.tick();

        IOptionPage optionPage = upgradeOptions.get(pageNumber).page;
        optionPage.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return upgradeOptions.get(pageNumber).page.keyPressed(keyCode, scanCode, modifiers)
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return upgradeOptions.get(pageNumber).page.mouseClicked(mouseX, mouseY, mouseButton)
                || super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double dir) {
        return upgradeOptions.get(pageNumber).page.mouseScrolled(mouseX, mouseY, dir)
                || super.mouseScrolled(mouseX, mouseY, dir);
    }

    @Override
    public <T extends Widget> T addWidget(T w) {
        return addButton(w);
    }

    @Override
    public List<Widget> getWidgetList() {
        return buttons;
    }

    @Override
    public FontRenderer getFontRenderer() {
        return font;
    }

    @Override
    public boolean isPauseScreen() {
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
