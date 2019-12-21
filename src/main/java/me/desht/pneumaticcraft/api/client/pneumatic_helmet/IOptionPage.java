package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;

/**
 * The Option Page is the page you see when you press 'F' (by default) with a Pneumatic Helmet equipped. You can
 * register this class by returning a new instance of this class at {@link IUpgradeRenderHandler#getGuiOptionsPage(IGuiScreen)}
 */
public interface IOptionPage {

    /**
     * Get a reference to the IGuiScreen object.  You can use this to get the font renderer, for example.
     *
     * @return the screen
     */
    IGuiScreen getGuiScreen();

    /**
     * This string is used in the text of the button of this page.
     *
     * @return the page name
     */
    String getPageName();

    /**
     * Here you can initialize your buttons and stuff like with a {@link Screen}.
     *
     * @param gui the holding GUI
     */
    void populateGui(IGuiScreen gui);

    /**
     * Called immediately before {@link Screen#render(int, int, float)}
     *
     * @param x mouse X
     * @param y mouse Y
     * @param partialTicks partial ticks since last world ticks
     */
    void renderPre(int x, int y, float partialTicks);

    /**
     * Called immediately after {@link Screen#render(int, int, float)}
     * Here you can render additional things like text.
     *
     * @param x mouse X
     * @param y mouse Y
     * @param partialTicks partial ticks since last world ticks
     */
    void renderPost(int x, int y, float partialTicks);

    /**
     * Called immediately after Screen#keyPressed(int, int, int).
     *
     * @param keyCode typed keycode
     * @param scanCode the scan code (rarely useful)
     * @param modifiers key modifiers
     * @return true if the event has been handled, false otherwise
     */
    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    /**
     * Called when mouse is clicked via {@link Screen#mouseClicked(double, double, int)}
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
     * @param dir scroll direction
     * @return true if the event has been handled, false otherwise
     */
    boolean mouseScrolled(double x, double y, double dir);

    /**
     * Can this upgrade be toggled off & on?
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
     * Called immediately after {@link Screen#tick()}
     */
    default void tick() { }

    /**
     * Convenience class for simple toggleable armor features with no additional settings.
     */
    class SimpleToggleableOptions<T extends IUpgradeRenderHandler> implements IOptionPage {
        private final IGuiScreen screen;
        private final String name;
        private final T upgradeHandler;

        public SimpleToggleableOptions(IGuiScreen screen, T upgradeHandler) {
            this.screen = screen;
            this.name = I18n.format("pneumaticHelmet.upgrade." + upgradeHandler.getUpgradeID());
            this.upgradeHandler = upgradeHandler;
        }

        protected T getUpgradeHandler() {
            return upgradeHandler;
        }

        @Override
        public IGuiScreen getGuiScreen() {
            return screen;
        }

        @Override
        public String getPageName() {
            return name;
        }

        @Override
        public void populateGui(IGuiScreen gui) {

        }

        @Override
        public void renderPre(int x, int y, float partialTicks) {

        }

        @Override
        public void renderPost(int x, int y, float partialTicks) {

        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            return false;
        }

        @Override
        public boolean mouseScrolled(double x, double y, double dir) {
            return false;
        }


        @Override
        public boolean isToggleable() {
            return true;
        }

        @Override
        public boolean displaySettingsHeader() {
            return false;
        }
    }
}
