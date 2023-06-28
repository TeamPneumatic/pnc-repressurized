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
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.api.pneumatic_armor.ICommonArmorHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents the client-specific part of an armor upgrade handler; provides methods for rendering, getting the
 * configuration GUI page, reading/writing client-side configuration, and handling keybinds. It's recommended to extend
 * {@link AbstractHandler} or {@link SimpleToggleableHandler} rather than implement this interface directly.
 * <p>
 * Register an instance of this via {@link IClientArmorRegistry#registerUpgradeHandler(IArmorUpgradeHandler, IArmorUpgradeClientHandler)}.
 * You will need a corresponding {@link IArmorUpgradeHandler} object; there is a 1-1 relationship.
 */
public interface IArmorUpgradeClientHandler<T extends IArmorUpgradeHandler<?>> {
    /**
     * Get the common handler corresponding to this client handler. There is always a one-to-one mapping between common
     * and client handlers.
     */
    T getCommonHandler();

    /**
     * Convenience method to get this client handler's ID, which is always the same as the corresponding common
     * handler's ID. Do not override this method!
     *
     * @return the handler ID
     */
    default ResourceLocation getID() {
        return getCommonHandler().getID();
    }

    /**
     * This is called when a {@link net.minecraftforge.fml.event.config.ModConfigEvent} is received for the mod.
     */
    default void initConfig() {}

    /**
     * When called this should save the settings to config.
     */
    default void saveToConfig() {}

    /**
     * This method is called every client tick, and should be used to update clientside logic for armor upgrades.
     *
     * @param armorHandler common armor handler for the player wearing this armor piece
     * @param isEnabled true if the upgrade is currently enabled, false otherwise
     */
    void tickClient(ICommonArmorHandler armorHandler, boolean isEnabled);

    /**
     * Called in the 3D render stage (via {@link net.minecraftforge.client.event.RenderLevelStageEvent})
     *
     * @param matrixStack the matrix stack
     * @param buffer the render type buffer
     * @param partialTicks partial ticks since last world tick
     */
    void render3D(PoseStack matrixStack, MultiBufferSource buffer, float partialTicks);

    /**
     * Called in the 2D render stage (via Forge's {@link net.minecraftforge.client.gui.overlay.IGuiOverlay} system).
     *
     * @param graphics              the matrix stack
     * @param partialTicks          partial ticks since last world tick
     * @param armorPieceHasPressure true if the armor piece actually has any pressure
     */
    void render2D(GuiGraphics graphics, float partialTicks, boolean armorPieceHasPressure);

    /**
     * You can return a {@link IGuiAnimatedStat} here, which the HUD Handler will pick up and render. It also
     * automatically opens and closes the stat window as necessary.
     * <p>
     * {@link IClientArmorRegistry#makeHUDStatPanel(Component, ItemStack, IArmorUpgradeClientHandler)} is a useful
     * method for creating a panel.
     * <p>
     * The recommended way to handle this is to have a
     * {@link IGuiAnimatedStat} field in your client upgrade handler, and lazy-init that in
     * this method, also resetting the field to null in {@link #onResolutionChanged()}.
     *
     * @return the animated stat, or null if this upgrade doesn't use/require a stat window
     */
    default IGuiAnimatedStat getAnimatedStat() {
        return null;
    }

    /**
     * Return the default screen layout for this upgrade's stat panel, if it has one. Note that the position is
     * easily modifiable by the player using the "Move Screen..." button in the upgrade's GUI.
     * <p>
     * If your handler doesn't have a stat panel (i.e. {@link #getAnimatedStat()} returns null), you don't need to
     * override this. If it does have a panel, it's recommended to override this with a reasonable default position.
     *
     * @return the default position
     */
    default StatPanelLayout getDefaultStatLayout() {
        return StatPanelLayout.DEFAULT;
    }

    /**
     * Called when (re-)equipping the armor piece.  Use this to clear any client-side state information held by the
     * upgrade handler and initialise it to a known state.
     */
    void reset();

    /**
     * When you have some configurable options for your upgrade handler, return a new instance of an {@link IOptionPage}.
     * When you do so, it will automatically get picked up by the armor GUI handler, and a button for the upgrade
     * will be displayed in the main armor GUI.
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
     * Is this upgrade toggleable, i.e. can it be switched on &amp; off?  Toggleable upgrades will have a checkbox in their
     * GUI page with a possible associated keybinding. Non-toggleable upgrades generally have a bindable hotkey to
     * trigger a one-off action (e.g. hacking, chestplate launcher...).  The default return value for this method is
     * true, which is the most common case.  Override to return false for non-toggleable upgrades.
     *
     * @return true if the upgrade is toggleable, false otherwise
     */
    default boolean isToggleable() {
        return true;
    }

    /**
     * Get the default keybinding for toggling this upgrade on/off. By default, an unbound key binding will be
     * registered for the upgrade, so it appears in the vanilla Config -> Controls screen with no binding. Note that only
     * toggles are added here; keybinds for non-toggleable upgrade which trigger specific actions (e.g. the
     * Chestplate Launcher or Drone Debugging key) need to be registered explicitly.
     * <p>
     * Do not override this default implementation. Non-toggleable upgrades return {@code Optional.empty()}
     * by default.
     *
     * @return the default key binding for this upgrade
     */
    default Optional<KeyMapping> getInitialKeyBinding() {
        return isToggleable() ?
                Optional.of(new KeyMapping(IArmorUpgradeHandler.getStringKey(getID()),
                        KeyConflictContext.IN_GAME, KeyModifier.NONE,
                        InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, getKeybindCategory())) :
                Optional.empty();
    }

    /**
     * Get all the sub-keybinds for this upgrade handler. The ID's of any checkboxes which toggle a sub-feature of this
     * upgrade (e.g. the various Block Tracker categories, or the Jet Boots builder mode) need to be returned here so a
     * key binding can be registered for them.
     * <p>
     * The ID's returned here are the same as those passed to
     * {@link IClientArmorRegistry#makeKeybindingCheckBox(ResourceLocation, int, int, int, Consumer)}.
     *
     * @return a collection of ID's
     */
    default Collection<ResourceLocation> getSubKeybinds() {
        return Collections.emptyList();
    }

    /**
     * Get the keybind used to trigger this upgrade's action, if any. This is distinct from the toggle keybind (which
     * switches an upgrade on or off); the trigger keybind triggers an action, e.g. Hacking, Pneumatic Kick...
     *
     * @return an optional keybinding name
     */
    default Optional<KeyMapping> getTriggerKeyBinding() {
        return Optional.empty();
    }

    /**
     * Called when the registered triggered keybind (if any) is pressed.
     * @param armorHandler the client-side common armor handler object for the player
     */
    default void onTriggered(ICommonArmorHandler armorHandler) {
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
     *
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
     * Is this upgrade enabled by default, i.e. when the player first equips the armor and there's no value saved in
     * ArmorFeatureStatus.json?
     *
     * @return whether the upgrade should be enabled by default
     */
    default boolean isEnabledByDefault() {
        return false;
    }

    /**
     * Is the given sub-feature of this upgrade enabled by default? (e.g. the various Block Tracker categories,
     * or the Jet Boots builder mode)
     *
     * @param subModuleName name of the submodule name (by convention "{upgrade}.module.{subfeature}")
     * @return whether the sub-feature should be enabled by default
     */
    default boolean isEnabledByDefault(String subModuleName) {
        return false;
    }

    /**
     * Convenience class which allows a reference to the common upgrade handler to be passed in and retrieved.
     */
    abstract class AbstractHandler<T extends IArmorUpgradeHandler<?>> implements IArmorUpgradeClientHandler<T> {
        private final T commonHandler;

        public AbstractHandler(T commonHandler) {
            this.commonHandler = commonHandler;
        }

        @Override
        public T getCommonHandler() {
            return commonHandler;
        }
    }

    /**
     * Convenience class for simple toggleable armor features with no additional settings.
     */
    abstract class SimpleToggleableHandler<T extends IArmorUpgradeHandler<?>> extends AbstractHandler<T> {
        public SimpleToggleableHandler(T commonHandler) {
            super(commonHandler);
        }

        @Override
        public void tickClient(ICommonArmorHandler armorHandler, boolean isEnabled) {
        }

        @Override
        public void render3D(PoseStack matrixStack, MultiBufferSource buffer, float partialTicks) {
        }

        @Override
        public void render2D(GuiGraphics graphics, float partialTicks, boolean armorPieceHasPressure) {
        }

        @Override
        public void reset() {
        }

        @Override
        public IOptionPage getGuiOptionsPage(IGuiScreen screen) {
            return new IOptionPage.SimpleOptionPage<>(screen, this);
        }
    }
}
