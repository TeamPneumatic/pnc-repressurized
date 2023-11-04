/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.Window;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.*;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftScreen;
import me.desht.pneumaticcraft.client.gui.pneumatic_armor.options.NullOptions;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorFeatureStatus;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.CoreComponentsHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ArmorMainScreen extends AbstractPneumaticCraftScreen implements IGuiScreen {
    private static final ChatFormatting[] TITLE_PREFIX = { ChatFormatting.AQUA, ChatFormatting.UNDERLINE };

    public static final ItemStack[] ARMOR_STACKS = new ItemStack[]{
            new ItemStack(ModItems.PNEUMATIC_BOOTS.get()),
            new ItemStack(ModItems.PNEUMATIC_LEGGINGS.get()),
            new ItemStack(ModItems.PNEUMATIC_CHESTPLATE.get()),
            new ItemStack(ModItems.PNEUMATIC_HELMET.get())
    };

    // A static instance which can handle keybinds when the GUI is closed.
    private static ArmorMainScreen instance;
    private static int pageNumber;

    private final List<UpgradeOption> upgradeOptions = new ArrayList<>();
    private boolean inInitPhase = true;
    private final UpgradeOption nullOptionsPage = new UpgradeOption(new NullOptions(this), RL("null"), new ItemStack(Items.BARRIER));

    private ArmorMainScreen() {
        super(Component.literal("Main Screen"));
    }

    public static ArmorMainScreen getInstance() {
        return instance;
    }

    public static void initHelmetCoreComponents() {
        if (instance == null) {
            instance = new ArmorMainScreen();
            Window mw = Minecraft.getInstance().getWindow();
            instance.init(Minecraft.getInstance(), mw.getGuiScaledWidth(), mw.getGuiScaledHeight());  // causes init() to be called

            for (int i = 1; i < instance.upgradeOptions.size(); i++) {
                pageNumber = i;
                instance.init();
            }
            pageNumber = 0;
            instance.inInitPhase = false;
            ArmorFeatureStatus.INSTANCE.saveIfChanged();
        }
    }

    @Override
    public void init() {
        super.init();

        xSize = width;
        ySize = height;

        clearWidgets();
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
            button.setHighlightWhenInactive(true);
            button.setRenderStacks(upgradeOptions.get(i).icons).setIconPosition(WidgetButtonExtended.IconPosition.RIGHT).setIconSpacing(12);
            if (pageNumber == i) button.active = false;
            addRenderableWidget(button);
            yPos += 22;
            if (yPos > ySize - 22) {
                yPos = 5;
                xPos += buttonWidth + 55;
            }
        }
        pageNumber = Math.min(pageNumber, upgradeOptions.size() - 1);
        if (pageNumber < 0 && !upgradeOptions.isEmpty()) pageNumber = 0;

        maybeAddEnableCheckbox();
        maybeAddStatHiddenCheckbox();
        getCurrentOptionsPage().page.populateGui(this);
    }

    private void maybeAddEnableCheckbox() {
        ICheckboxWidget checkBox = PneumaticRegistry.getInstance().getClientArmorRegistry()
                .makeKeybindingCheckBox(getCurrentOptionsPage().upgradeID, 40, 25, 0xFFFFFFFF, null);
        if (getCurrentOptionsPage().page.isToggleable()) {
            addRenderableWidget(checkBox.asWidget());
        }
    }

    private void maybeAddStatHiddenCheckbox() {
        var handler = getCurrentOptionsPage().page.getClientUpgradeHandler();
        StatPanelLayout layout = ArmorHUDLayout.INSTANCE.getLayoutFor(getCurrentOptionsPage().upgradeID, handler.getDefaultStatLayout());
        WidgetCheckBox statToggle = new WidgetCheckBox(40, 37, 0xFFFFFFFF, xlate("pneumaticcraft.armor.gui.misc.hideStat"), b -> {
            ArmorHUDLayout.INSTANCE.updateLayout(getCurrentOptionsPage().upgradeID, layout.x(), layout.y(), layout.expandsLeft(), b.checked);
        });
        if (handler.getAnimatedStat() != null) {
            statToggle.checked = layout.hidden();
            addRenderableWidget(statToggle);
        }
    }

    private void setPage(int newPage) {
        pageNumber = newPage;
        init();
    }

    public UpgradeOption getCurrentOptionsPage() {
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
        for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            List<IArmorUpgradeHandler<?>> upgradeHandlers = ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot);
            for (int i = 0; i < upgradeHandlers.size(); i++) {
                if (inInitPhase || CommonArmorHandler.getHandlerForPlayer().isUpgradeInserted(slot, i) || slot == EquipmentSlot.HEAD && i == 0) {
                    IArmorUpgradeHandler<?> handler = upgradeHandlers.get(i);
                    if (inInitPhase
                            || PneumaticArmorItem.isPneumaticArmorPiece(ClientUtils.getClientPlayer(), slot)
                            || handler instanceof CoreComponentsHandler) {
                        IArmorUpgradeClientHandler<?> clientHandler = ClientArmorRegistry.getInstance().getClientHandler(handler.getID());
                        IOptionPage optionPage = clientHandler.getGuiOptionsPage(this);
                        if (optionPage != null) {
                            List<ItemStack> stacks = new ArrayList<>();
                            stacks.add(ARMOR_STACKS[handler.getEquipmentSlot().getIndex()]);
                            Arrays.stream(handler.getRequiredUpgrades()).map(PNCUpgrade::getItemStack).forEach(stacks::add);
                            upgradeOptions.add(new UpgradeOption(optionPage, handler.getID(), stacks.toArray(new ItemStack[0])));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTicks) {
        renderBackground(graphics);
        IOptionPage optionPage = getCurrentOptionsPage().page;
        optionPage.renderPre(graphics, x, y, partialTicks);
        graphics.drawCenteredString(font, getCurrentOptionsPage().page.getPageName().copy().withStyle(TITLE_PREFIX), 100, 12, 0xFFFFFFFF);
        if (optionPage.displaySettingsHeader()) {
            graphics.drawCenteredString(font, xlate("pneumaticcraft.armor.gui.misc.settings").withStyle(ChatFormatting.DARK_AQUA), 100, optionPage.settingsYposition(), 0xFFFFFFFF);
        }
        super.render(graphics, x, y, partialTicks);
        optionPage.renderPost(graphics, x, y, partialTicks);
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
                || super.keyReleased(keyCode, scanCode, modifiers);
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
    public <T extends AbstractWidget> T addWidget(T w) {
        return addRenderableWidget(w);
    }

    @Override
    public List<Renderable> getWidgetList() {
        return ImmutableList.copyOf(renderables);
    }

    @Override
    public Font getFontRenderer() {
        return font;
    }

    @Override
    public void setFocusedWidget(AbstractWidget w) {
        setFocused(w);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public record UpgradeOption(IOptionPage page,
                                 ResourceLocation upgradeID,
                                 ItemStack... icons) {
    }
}
