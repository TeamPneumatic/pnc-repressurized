package me.desht.pneumaticcraft.api.client.pneumaticHelmet;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/**
 * The Option Page is the page you see when you press 'F' (by default) with a Pneumatic Helmet equipped. You can
 * register this class by returning a new instance of this class at {@link IUpgradeRenderHandler#getGuiOptionsPage()}
 */
public interface IOptionPage {

    /**
     * This string is used in the text of the button of this page.
     *
     * @return the page name
     */
    String getPageName();

    /**
     * Here you can initialize your buttons and stuff like with a GuiScreen. For buttons, don't use button id 100 and
     * up, as they will be used as selection buttons for other option pages in the main GuiScreen.
     *
     * @param gui the holding GUI
     */
    void initGui(IGuiScreen gui);

    /**
     * Same as GuiScreen#actionPerformed(GuiButton).
     *
     * @param button a vanilla GuiButton
     */
    void actionPerformed(GuiButton button);

    /**
     * Called immediately before {@link GuiScreen#drawScreen(int, int, float)}
     *
     * @param x mouse X
     * @param y mouse Y
     * @param partialTicks partial ticks since last world ticks
     */
    void drawPreButtons(int x, int y, float partialTicks);

    /**
     * Called immediately after {@link GuiScreen#drawScreen(int, int, float)}
     * Here you can render additional things like text.
     *
     * @param x mouse X
     * @param y mouse Y
     * @param partialTicks partial ticks since last world ticks
     */
    void drawScreen(int x, int y, float partialTicks);

    /**
     * Called immediately after GuiScreen#keyTyped(char, int).
     *
     * @param ch typed character
     * @param key typed keycode
     */
    void keyTyped(char ch, int key);

    /**
     * Called when mouse is clicked via {@link GuiScreen#mouseClicked(int, int, int)}
     * @param x mouse X
     * @param y mouse Y
     * @param button mouse button
     */
    void mouseClicked(int x, int y, int button);

    /**
     * Called immediately after {@link GuiScreen#handleMouseInput()}
     */
    void handleMouseInput();

    /**
     * Can this upgrade be toggled off & on?
     *
     * @return true if the upgrade is toggleable, false otherwise
     */
    boolean canBeTurnedOff();

    /**
     * Should the "Settings" header be displayed?
     *
     * @return true if the header should be displayed, false otherwise
     */
    boolean displaySettingsText();

    /**
     * Y position from the "Setting" header.  The default is fine in most cases, but if your options page has
     * many buttons (e.g. like the Block Tracker), you may wish to adjust this.
     *
     * @return Y position, default 115
     */
    default int settingsYposition() { return 115; }

    /**
     * Called immediately after {@link GuiScreen#updateScreen()}
     */
    default void updateScreen() { }

    /**
     * Convenience class for simple toggleable armor features with no additional settings.
     */
    class SimpleToggleableOptions implements IOptionPage {
        private final String name;
        private final IUpgradeRenderHandler renderHandler;

        public SimpleToggleableOptions(IUpgradeRenderHandler renderHandler) {
            this.name = I18n.format("pneumaticHelmet.upgrade." + renderHandler.getUpgradeName());
            this.renderHandler = renderHandler;
        }

        protected IUpgradeRenderHandler getRenderHandler() {
            return renderHandler;
        }

        @Override
        public String getPageName() {
            return name;
        }

        @Override
        public void initGui(IGuiScreen gui) {

        }

        @Override
        public void actionPerformed(GuiButton button) {

        }

        @Override
        public void drawPreButtons(int x, int y, float partialTicks) {

        }

        @Override
        public void drawScreen(int x, int y, float partialTicks) {

        }

        @Override
        public void keyTyped(char ch, int key) {

        }

        @Override
        public void mouseClicked(int x, int y, int button) {

        }

        @Override
        public void handleMouseInput() {

        }

        @Override
        public boolean canBeTurnedOff() {
            return true;
        }

        @Override
        public boolean displaySettingsText() {
            return false;
        }
    }
}
