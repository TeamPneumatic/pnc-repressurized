/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import com.mojang.blaze3d.platform.InputConstants;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Optional;

/**
 * An Option Page is the GUI object which holds the control widgets for a particular Pneumatic Armor upgrade. Create
 * and return an instance of this class in {@link IArmorUpgradeClientHandler#getGuiOptionsPage(IGuiScreen)}.
 * <p>
 * It is strongly recommended to extend the {@link SimpleOptionPage} class rather than implement this interface directly.
 */
public interface IOptionPage {
    /**
     * Get a reference to the IGuiScreen object.  You can use this to get the font renderer, for example.
     *
     * @return the screen
     */
    IGuiScreen getGuiScreen();

    /**
     * This text is used in the GUI button for this page.
     *
     * @return the page name
     */
    MutableComponent getPageName();

    /**
     * Here you can initialize your buttons and stuff like with a {@link net.minecraft.client.gui.screens.Screen}.
     *
     * @param gui the holding GUI
     */
    void populateGui(IGuiScreen gui);

    /**
     * Called immediately before {@link net.minecraft.client.gui.screens.Screen#render(GuiGraphics, int, int, float)}
     *
     * @param graphics     the GUI draw context
     * @param x            mouse X
     * @param y            mouse Y
     * @param partialTicks partial ticks since last world ticks
     */
    void renderPre(GuiGraphics graphics, int x, int y, float partialTicks);

    /**
     * Called immediately after {@link net.minecraft.client.gui.screens.Screen#render(GuiGraphics, int, int, float)}
     * Here you can render additional things like text.
     *
     * @param graphics     the GUI draw context
     * @param x            mouse X
     * @param y            mouse Y
     * @param partialTicks partial ticks since last world ticks
     */
    void renderPost(GuiGraphics graphics, int x, int y, float partialTicks);

    /**
     * Called by {@link net.minecraft.client.gui.screens.Screen#keyPressed(int, int, int)} when a key is pressed.
     *
     * @param keyCode typed keycode
     * @param scanCode the scan code (rarely useful)
     * @param modifiers key modifiers
     * @return true if the event has been handled, false otherwise
     */
    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    /**
     * Called by {@link net.minecraft.client.gui.screens.Screen#keyReleased(int, int, int)} when a key is released.
     *
     * @param keyCode typed keycode
     * @param scanCode the scan code (rarely useful)
     * @param modifiers key modifiers
     * @return true if the event has been handled, false otherwise
     */
    boolean keyReleased(int keyCode, int scanCode, int modifiers);

    /**
     * Called when mouse is clicked via {@link net.minecraft.client.gui.screens.Screen#mouseClicked(double, double, int)}
     * @param x mouse X
     * @param y mouse Y
     * @param button mouse button
     * @return true if the event has been handled, false otherwise
     */
    boolean mouseClicked(double x, double y, int button);

    /**
     * Called when the mouse wheel is rolled.
     *
     * @param x mouse X
     * @param y mouse Y
     * @param dirX X scroll direction
     * @param dirY Y scroll direction
     * @return true if the event has been handled, false otherwise
     */
    boolean mouseScrolled(double x, double y, double dirX, double dirY);

    /**
     * Called when the mouse is dragged across the GUI
     * @param mouseX mouse X
     * @param mouseY mouse Y
     * @param button mouse button
     * @param dragX drag X
     * @param dragY drag Y
     * @return true if the event has been handled, false otherwise
     */
    boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY);

    /**
     * Can this upgrade be toggled on &amp; off?  If true, a checkbox (with the ability to bind a key) will be
     * automatically displayed in this upgrade's GUI.
     *
     * @return true if the upgrade is toggleable, false otherwise
     */
    boolean isToggleable();

    /**
     * Should the "Settings" header be displayed?
     *
     * @return true if the header should be displayed, false otherwise
     */
    boolean displaySettingsHeader();

    /**
     * Y position from the "Setting" header.  The default is fine in most cases, but if your options page has
     * many buttons (e.g. like the Block Tracker), you may wish to adjust this.
     *
     * @return Y position, default 115
     */
    default int settingsYposition() { return 115; }

    /**
     * Called immediately after {@link net.minecraft.client.gui.screens.Screen#tick()}
     */
    default void tick() { }

    /**
     * Get the keybinding button for this page, if any.  You can create a keybinding button with
     * {@link IClientArmorRegistry#makeKeybindingButton(int, KeyMapping)}.
     *
     * @return the keybinding button, or {@code Optional.empty()} if there isn't one
     */
    default Optional<IKeybindingButton> getKeybindingButton() { return Optional.empty(); }

    /**
     * Get the client upgrade handler that this screen is for.
     *
     * @return the client upgrade handler
     */
    IArmorUpgradeClientHandler<?> getClientUpgradeHandler();

    /**
     * Convenience class for simple armor features with no additional settings.
     */
    class SimpleOptionPage<T extends IArmorUpgradeClientHandler<?>> implements IOptionPage {
        private final IGuiScreen screen;
        private final MutableComponent name;
        private final T clientUpgradeHandler;

        public SimpleOptionPage(IGuiScreen screen, T clientUpgradeHandler) {
            this.screen = screen;
            this.name = Component.translatable(IArmorUpgradeHandler.getStringKey(clientUpgradeHandler.getID()));
            this.clientUpgradeHandler = clientUpgradeHandler;
        }

        @Override
        public T getClientUpgradeHandler() {
            return clientUpgradeHandler;
        }

        @Override
        public IGuiScreen getGuiScreen() {
            return screen;
        }

        @Override
        public MutableComponent getPageName() {
            return name;
        }

        @Override
        public void populateGui(IGuiScreen gui) {
        }

        @Override
        public void renderPre(GuiGraphics graphics, int x, int y, float partialTicks) {
        }

        @Override
        public void renderPost(GuiGraphics graphics, int x, int y, float partialTicks) {
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return getKeybindingButton().map(b -> b.receiveKey(InputConstants.Type.KEYSYM, keyCode)).orElse(false);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            return getKeybindingButton().map(b -> { b.receiveKeyReleased(); return true; }).orElse(false);
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            return getKeybindingButton().map(b -> b.receiveKey(InputConstants.Type.MOUSE, button)).orElse(false);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double x, double y, double dirX, double dirY) {
            return false;
        }


        @Override
        public boolean isToggleable() {
            return getClientUpgradeHandler().isToggleable();
        }

        @Override
        public boolean displaySettingsHeader() {
            return false;
        }
    }
}
