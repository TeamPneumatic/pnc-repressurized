package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Represents the client-specific part of an armor upgrade handler; provides methods for rendering, getting the
 * configuration GUI page, and read/writing client-side configuration. It's recommended to extend
 * {@link AbstractHandler} or {@link SimpleToggleableHandler} rather than implement this interface directly.
 */
public interface IArmorUpgradeClientHandler {
    /**
     * Get the common handler corresponding to this client handler. There is always a one-to-mapping between common
     * and client handlers.
     */
    IArmorUpgradeHandler getCommonHandler();

    /**
     * This is called when a {@link net.minecraftforge.fml.config.ModConfig.ModConfigEvent} is received for the mod.
     */
    default void initConfig() {}

    /**
     * When called this should save the settings to config.
     */
    default void saveToConfig() {}

    /**
     * This method is called every client tick, and should be used to update logic like the tracking and velocities
     * of stuff.
     *  @param armorHandler common armor handler for the player wearing this armor piece
     *
     */
    void tickClient(ICommonArmorHandler armorHandler);

    /**
     * Called in the 3D render stage (called from {@link net.minecraftforge.client.event.RenderWorldLastEvent})
     *
     * @param matrixStack the matrix stack
     * @param buffer the render type buffer
     * @param partialTicks partial ticks since last world tick
     */
    void render3D(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks);

    /**
     * Called in the 2D render stage (called from {@link net.minecraftforge.client.event.RenderGameOverlayEvent.Post})
     *
     * @param partialTicks partial ticks since last world tick
     * @param helmetEnabled true when isEnabled() returned true earlier. Can be used to close AnimatedStats for instance.
     *                      However this is already handled if you return an AnimatedStat in getAnimatedStat().
     */
    void render2D(MatrixStack matrixStack, float partialTicks, boolean helmetEnabled);

    /**
     * You can return a {@link IGuiAnimatedStat} here, that the HUD Handler will pick up and render. It also
     * automatically opens and closes the stat as necessary. The GuiMoveStat uses this method to retrieve the to be
     * moved stat.
     *
     * @return the animated stat, or null if no stat used.
     */
    default IGuiAnimatedStat getAnimatedStat() {
        return null;
    }

    /**
     * Called when (re-)equipping the armor piece.  Use this to clear any information held by the handler, e.g.
     * currently tracked entities.
     */
    void reset();

    /**
     * When you have some options for your upgrade handler, return a new instance of an IOptionsPage.
     * When you do so, it will automatically get picked up by the options handler, and it will be added to the
     * options GUI when this upgrade returns true when calling isEnabled(). Returning null here is valid.
     *
     * @param screen an instance of the gui Screen object
     * @return an options page, or null if the upgrade does not have an options page
     */
    IOptionPage getGuiOptionsPage(IGuiScreen screen);

    /**
     * Called when the screen resolution has changed. Primarily intended to allow render handlers to recalculate
     * stat positions.
     */
    default void onResolutionChanged() {
    }

    /**
     * Get the default keybinding for toggling this upgrade on/off. By default, an unbound key binding will be
     * registered for the upgrade, so it appears in the Config -> Controls screen with no binding. Note that only
     * toggles are added here; keybinds which trigger specific actions (e.g. the Chestplate Launcher or Drone Debugging
     * key) need to be registered explicitly.
     * <p>
     * If this upgrade is not a toggleable upgrade, return {@code Optional.empty()} here.
     *
     * @return the default key binding for this upgrade
     */
    default Optional<KeyBinding> getInitialKeyBinding() {
        return Optional.of(new KeyBinding(IArmorUpgradeHandler.getStringKey(getCommonHandler().getID()), KeyConflictContext.IN_GAME, KeyModifier.NONE,
                InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, getKeybindCategory()));
    }

    /**
     * Get all the sub-keybinds for this upgrade handler. Any checkboxes which toggle a sub-feature of this upgrade
     * (e.g. the various Block Tracker categories, or the Jet Boots builder mode) need to be returned here so a key
     * binding can be registered for them.
     *
     * @return a collection of ID's
     */
    default Collection<ResourceLocation> getSubKeybinds() {
        return Collections.emptyList();
    }

    /**
     * Get the keybind category for this upgrade.  By default, this is the same as the default category for all
     * PneumaticCraft keybinds.
     *
     * @return a keybind category ID
     */
    default String getKeybindCategory() {
        return Names.PNEUMATIC_KEYBINDING_CATEGORY_UPGRADE_TOGGLES;
    }

    /**
     * Get the keybind category for any sub-keybinds.  By default, this is the same as the default category for all
     * PneumaticCraft keybinds.
     * @return a keybind category ID
     */
    default String getSubKeybindCategory() {
        return Names.PNEUMATIC_KEYBINDING_CATEGORY_UPGRADE_TOGGLES;
    }

    /**
     * Called when the player alters their eyepiece color in the Pneumatic Armor GUI "Colors..." screen to re-color any
     * stat this client handler displays.  The default implementation works for most cases, but if your handler displays
     * extra stats (like the Entity or Block tracker does), override this method to re-color them too.
     *
     * @param color the new color for the stat display, as chosen by the player
     */
    default void setOverlayColor(int color) {
        IGuiAnimatedStat stat = getAnimatedStat();
        if (stat != null) stat.setBackgroundColor(color);
    }

    /**
     * Convenience class which allows a reference to the common upgrade handler to be passed in and retrieved.
     */
    abstract class AbstractHandler implements IArmorUpgradeClientHandler {
        private final IArmorUpgradeHandler commonHandler;

        public AbstractHandler(IArmorUpgradeHandler commonHandler) {
            this.commonHandler = commonHandler;
        }

        @Override
        public IArmorUpgradeHandler getCommonHandler() {
            return commonHandler;
        }
    }

    /**
     * Convenience class for simple toggleable armor features with no additional settings.
     */
    abstract class SimpleToggleableHandler extends AbstractHandler {
        public SimpleToggleableHandler(IArmorUpgradeHandler commonHandler) {
            super(commonHandler);
        }

        @Override
        public void tickClient(ICommonArmorHandler armorHandler) {
        }

        @Override
        public void render3D(MatrixStack matrixStack, IRenderTypeBuffer buffer, float partialTicks) {
        }

        @Override
        public void render2D(MatrixStack matrixStack, float partialTicks, boolean helmetEnabled) {
        }

        @Override
        public void reset() {
        }

        @Override
        public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
            return new IOptionPage.SimpleToggleableOptions<>(screen, this);
        }
    }
}
