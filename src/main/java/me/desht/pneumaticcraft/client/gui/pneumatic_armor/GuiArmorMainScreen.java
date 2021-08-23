package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.option_screens.NullOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.CoreComponentsHandler;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiArmorMainScreen extends GuiPneumaticScreenBase implements IGuiScreen {
    private static final TextFormatting[] TITLE_PREFIX = { TextFormatting.AQUA, TextFormatting.UNDERLINE };

    public static final ItemStack[] ARMOR_STACKS = new ItemStack[]{
            new ItemStack(ModItems.PNEUMATIC_BOOTS.get()),
            new ItemStack(ModItems.PNEUMATIC_LEGGINGS.get()),
            new ItemStack(ModItems.PNEUMATIC_CHESTPLATE.get()),
            new ItemStack(ModItems.PNEUMATIC_HELMET.get())
    };

    private final List<UpgradeOption> upgradeOptions = new ArrayList<>();
    private static int pageNumber;
    private boolean inInitPhase = true;
    private final UpgradeOption nullOptionsPage = new UpgradeOption(new NullOptions(this), RL("null"), new ItemStack(Items.BARRIER));

    // A static instance which can handle keybinds when the GUI is closed.
    private static GuiArmorMainScreen instance;

    private GuiArmorMainScreen() {
        super(new StringTextComponent("Main Screen"));
    }

    public static GuiArmorMainScreen getInstance() {
        return instance;
    }

    public static void initHelmetCoreComponents() {
        if (instance == null) {
            instance = new GuiArmorMainScreen();
            MainWindow mw = Minecraft.getInstance().getWindow();
            instance.init(Minecraft.getInstance(), mw.getGuiScaledWidth(), mw.getGuiScaledHeight());  // causes init() to be called

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

        xSize = width;
        ySize = height;

        buttons.clear();
        children.clear();
        upgradeOptions.clear();
        addPages();

        int xPos = 200;
        int yPos = 5;
        int buttonWidth = font.width(xlate("pneumaticcraft.armor.upgrade.core_components"));
        for (UpgradeOption opt : upgradeOptions) {
            buttonWidth = Math.max(buttonWidth, font.width(opt.page.getPageName()));
        }

        for (int i = 0; i < upgradeOptions.size(); i++) {
            final int idx = i;
            WidgetButtonExtended button = new WidgetButtonExtended(xPos, yPos, buttonWidth + 10, 20,
                    upgradeOptions.get(i).page.getPageName(), b -> setPage(idx));
            button.setRenderStacks(upgradeOptions.get(i).icons).setIconPosition(WidgetButtonExtended.IconPosition.RIGHT).setIconSpacing(12);
            if (pageNumber == i) button.active = false;
            addButton(button);
            yPos += 22;
            if (yPos > ySize - 22) {
                yPos = 5;
                xPos += buttonWidth + 55;
            }
        }
        pageNumber = Math.min(pageNumber, upgradeOptions.size() - 1);
        if (pageNumber < 0 && !upgradeOptions.isEmpty()) pageNumber = 0;
        WidgetKeybindCheckBox checkBox = WidgetKeybindCheckBox.getOrCreate(getCurrentOptionsPage().upgradeID, 40, 25, 0xFFFFFFFF, null);
        if (getCurrentOptionsPage().page.isToggleable()) {
            addButton(checkBox);
        }
        getCurrentOptionsPage().page.populateGui(this);
    }

    private void setPage(int newPage) {
        pageNumber = newPage;
        init();
    }

    private UpgradeOption getCurrentOptionsPage() {
        if (pageNumber >= 0 && pageNumber < upgradeOptions.size()) {
            return upgradeOptions.get(pageNumber);
        } else {
            return nullOptionsPage;
        }
    }

    @Override
    protected ResourceLocation getTexture() {
        return null;
    }

    private void addPages() {
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            List<IArmorUpgradeHandler<?>> upgradeHandlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
            for (int i = 0; i < upgradeHandlers.size(); i++) {
                if (inInitPhase || CommonArmorHandler.getHandlerForPlayer().isUpgradeInserted(slot, i) || slot == EquipmentSlotType.HEAD && i == 0) {
                    IArmorUpgradeHandler<?> handler = upgradeHandlers.get(i);
                    if (inInitPhase
                            || ItemPneumaticArmor.isPneumaticArmorPiece(Minecraft.getInstance().player, slot)
                            || handler instanceof CoreComponentsHandler) {
                        IArmorUpgradeClientHandler<?> clientHandler = ArmorUpgradeClientRegistry.getInstance().getClientHandler(handler.getID());
                        IOptionPage optionPage = clientHandler.getGuiOptionsPage(this);
                        if (optionPage != null) {
                            List<ItemStack> stacks = new ArrayList<>();
                            stacks.add(ARMOR_STACKS[handler.getEquipmentSlot().getIndex()]);
                            Arrays.stream(handler.getRequiredUpgrades()).map(EnumUpgrade::getItemStack).forEach(stacks::add);
                            upgradeOptions.add(new UpgradeOption(optionPage, handler.getID(), stacks.toArray(new ItemStack[0])));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
        renderBackground(matrixStack);
        IOptionPage optionPage = getCurrentOptionsPage().page;
        optionPage.renderPre(matrixStack, x, y, partialTicks);
        drawCenteredString(matrixStack, font, getCurrentOptionsPage().page.getPageName().copy().withStyle(TITLE_PREFIX), 100, 12, 0xFFFFFFFF);
        if (optionPage.displaySettingsHeader()) {
            drawCenteredString(matrixStack, font, xlate("pneumaticcraft.armor.gui.misc.settings").withStyle(TextFormatting.DARK_AQUA), 100, optionPage.settingsYposition(), 0xFFFFFFFF);
        }
        super.render(matrixStack, x, y, partialTicks);
        optionPage.renderPost(matrixStack, x, y, partialTicks);
    }

    @Override
    public void tick() {
        super.tick();

        IOptionPage optionPage = getCurrentOptionsPage().page;
        optionPage.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return getCurrentOptionsPage().page.keyPressed(keyCode, scanCode, modifiers)
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return getCurrentOptionsPage().page.keyReleased(keyCode, scanCode, modifiers)
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return getCurrentOptionsPage().page.mouseClicked(mouseX, mouseY, mouseButton)
                || super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double dir) {
        return getCurrentOptionsPage().page.mouseScrolled(mouseX, mouseY, dir)
                || super.mouseScrolled(mouseX, mouseY, dir);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return getCurrentOptionsPage().page.mouseDragged(mouseX, mouseY, button, dragX, dragY)
                || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public <T extends Widget> T addWidget(T w) {
        return addButton(w);
    }

    @Override
    public List<Widget> getWidgetList() {
        return ImmutableList.copyOf(buttons);
    }

    @Override
    public FontRenderer getFontRenderer() {
        return font;
    }

    @Override
    public void setFocusedWidget(Widget w) {
        setFocused(w);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class UpgradeOption {
        private final IOptionPage page;
        private final ResourceLocation upgradeID;
        private final ItemStack[] icons;

        UpgradeOption(IOptionPage page, ResourceLocation upgradeID, ItemStack... icons) {
            this.page = page;
            this.upgradeID = upgradeID;
            this.icons = icons;
        }
    }
}
