package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IOptionPage;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            MainWindow mw = Minecraft.getInstance().getMainWindow();
            instance.init(Minecraft.getInstance(), mw.getScaledWidth(), mw.getScaledHeight());  // causes init() to be called

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
            WidgetButtonExtended button = new WidgetButtonExtended(210, 20 + i * 22, 120, 20,
                    upgradeOptions.get(i).page.getPageName(), b -> setPage(idx));
            button.setRenderStacks(upgradeOptions.get(i).icons);
            button.setIconPosition(WidgetButtonExtended.IconPosition.RIGHT);
            if (pageNumber == i) button.active = false;
            addButton(button);
        }
        if (pageNumber > upgradeOptions.size() - 1) {
            pageNumber = upgradeOptions.size() - 1;
        }
        WidgetKeybindCheckBox checkBox = WidgetKeybindCheckBox.getOrCreate(upgradeOptions.get(pageNumber).upgradeID, 40, 25, 0xFFFFFFFF, null);
        if (upgradeOptions.get(pageNumber).page.isToggleable()) {
            addButton(checkBox);
        }
        upgradeOptions.get(pageNumber).page.populateGui(this);
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
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            List<IArmorUpgradeHandler> renderHandlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
            for (int i = 0; i < renderHandlers.size(); i++) {
                if (inInitPhase || CommonArmorHandler.getHandlerForPlayer().isUpgradeInserted(slot, i)) {
                    IArmorUpgradeHandler handler = renderHandlers.get(i);
                    if (inInitPhase
                            || ItemPneumaticArmor.isPneumaticArmorPiece(Minecraft.getInstance().player, slot)
                            || handler instanceof CoreComponentsHandler) {
                        IArmorUpgradeClientHandler clientHandler = ArmorUpgradeClientRegistry.getInstance().getClientHandler(handler);
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
        IOptionPage optionPage = upgradeOptions.get(pageNumber).page;
        optionPage.renderPre(matrixStack, x, y, partialTicks);
        drawCenteredString(matrixStack, font, upgradeOptions.get(pageNumber).page.getPageName().deepCopy().mergeStyle(TITLE_PREFIX), 100, 12, 0xFFFFFFFF);
        if (optionPage.displaySettingsHeader()) {
            drawCenteredString(matrixStack, font, xlate("pneumaticcraft.armor.gui.misc.settings"), 100, optionPage.settingsYposition(), 0xFFFFFFFF);
        }
        super.render(matrixStack, x, y, partialTicks);
        optionPage.renderPost(matrixStack, x, y, partialTicks);
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
    public void setFocusedWidget(Widget w) {
        setListener(w);
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
