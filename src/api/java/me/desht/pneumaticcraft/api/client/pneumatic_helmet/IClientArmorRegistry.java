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

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Retrieve an instance of this via {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getClientArmorRegistry()}
 */
public interface IClientArmorRegistry {
    int DEFAULT_MESSAGE_BGCOLOR = 0x7000AA00;

    /**
     * Register an entity tracker for the Pneumatic Helmet. Call this from {@link FMLCommonSetupEvent} listener (do not
     * use {@link FMLCommonSetupEvent#enqueueWork(Runnable)}).
     * @param entry the entity tracker
     */
    void registerEntityTrackEntry(Supplier<? extends IEntityTrackEntry> entry);

    /**
     * Register an block track entry (i.e. a subcategory of the Block Tracker) for the Pneumatic Helmet.
     * Call this from a {@link FMLClientSetupEvent} listener (do not use {@link FMLClientSetupEvent#enqueueWork(Runnable)}).
     * @param id the block entry ID
     * @param entry the block track entry ({@link IBlockTrackEntry#getEntryID()} must return the same ID as the {@code id} parameter
     */
    void registerBlockTrackEntry(ResourceLocation id, Supplier<? extends IBlockTrackEntry> entry);

    /**
     * Add a message for display in the Pneumatic Helmet HUD display
     * @param title the message title
     * @param message the message text (can be empty if the title suffices for a one-line message)
     * @param duration the duration in ticks for the message to be displayed
     * @param backColor the message background color, including alpha (DEFAULT_MESSAGE_BGCOLOR is the default green color used by most messages)
     */
    void addHUDMessage(Component title, List<Component> message, int duration, int backColor);

    /**
     * Convenience version of {@link #addHUDMessage(Component, List, int, int)} which displays a one-line message for
     * 2.5 seconds, using the default green background colour
     * @param title the message
     */
    default void addHUDMessage(Component title) {
        addHUDMessage(title, Collections.emptyList(), 50, DEFAULT_MESSAGE_BGCOLOR);
    }

    /**
     * Registers the client handler for a Pneumatic Armor upgrade. This must be called from a {@link FMLClientSetupEvent}
     * handler; do <strong>not</strong> use {@link net.neoforged.fml.event.lifecycle.FMLClientSetupEvent#enqueueWork(Runnable)}.
     * This also registers any keybindings referenced by the render handler
     * (see {@link IArmorUpgradeClientHandler#getInitialKeyBinding()} and {@link IArmorUpgradeClientHandler#getSubKeybinds()}.
     *
     * @param handler the common upgrade handler, previously registered with
     * @param clientHandler the client handler to register with the common upgrade handler
     */
    <T extends IArmorUpgradeHandler<?>> void registerUpgradeHandler(T handler, IArmorUpgradeClientHandler<T> clientHandler);

    /**
     * Create a new keybinding button for an {@link IOptionPage} armor GUI screen.  This is intended to be called from
     * {@link IOptionPage#populateGui(IGuiScreen)} to set up a button which can be used to change a particular key
     * binding.
     *
     * @param yPos y position of the button
     * @param keyBinding the keybinding modified by the button
     * @return the new button
     */
    IKeybindingButton makeKeybindingButton(int yPos, KeyMapping keyBinding);

    /**
     * Create or retrieve the toggle checkbox for the given upgrade. If the checkbox doesn't already exist, it will be
     * created; if it exists, the existing checkbox will be returned. There is only ever one toggle checkbox in
     * existence for any given upgrade ID / clientside upgrade handler.
     * <p>
     * This is intended to be called from {@link IOptionPage#populateGui(IGuiScreen)} when creating the GUI for
     * an upgrade handler.
     *
     * @param upgradeId the upgrade ID
     * @param xPos X position of the widget
     * @param yPos Y position of the widget
     * @param color widget's text color in ARGB format
     * @param onPressed called when the checkbox is toggled
     */
    ICheckboxWidget makeKeybindingCheckBox(ResourceLocation upgradeId, int xPos, int yPos, int color, Consumer<ICheckboxWidget> onPressed);

    /**
     * Create a stat panel for display on the Pneumatic Armor HUD.
     * <p>
     * This panel is automatically coloured according to the player's armor eyepiece color setting, and is
     * re-positionable by the player. See {@link IArmorUpgradeClientHandler#getDefaultStatLayout()} to define the
     * default positioning for the panel.
     *
     * @param title title text to display on the stat
     * @param icon an icon to draw next to the title
     * @param clientHandler the client upgrade handler this panel is associated with
     * @return the stat panel
     */
    IGuiAnimatedStat makeHUDStatPanel(Component title, ItemStack icon, IArmorUpgradeClientHandler<?> clientHandler);

    /**
     * Just like {@link #makeHUDStatPanel(Component, ItemStack, IArmorUpgradeClientHandler)} but you can pass an
     * arbitrary texture to use as the icon. The texture should be 16x16.
     *
     * @param title title text to display on the stat
     * @param icon an icon to draw next to the title
     * @param clientHandler the client upgrade handler this panel is associated with
     * @return the stat panel
     */
    IGuiAnimatedStat makeHUDStatPanel(Component title, ResourceLocation icon, IArmorUpgradeClientHandler<?> clientHandler);

    /**
     * Create a "Move Stat Screen..." button to allow the stat panel for an upgrade to be moved. Clicking this button
     * will automatically open the stat panel configuration GUI and allow the panel to be moved.
     * <p>
     * Call this from {@link IOptionPage#populateGui(IGuiScreen)}, and pass the return value to
     * {@link IGuiScreen#addWidget(AbstractWidget)} to add this button to your upgrade GUI.
     *
     * @param x button X position
     * @param y button Y position
     * @param handler the client upgrade handler (can be obtained via {@link IOptionPage.SimpleOptionPage#getClientUpgradeHandler()}
     * @return the button
     */
    AbstractWidget makeStatMoveButton(int x, int y, IArmorUpgradeClientHandler<?> handler);

    /**
     * Get the block position and face that is currently focused on by the player via the Block Tracker upgrade. If the
     * Block Tracker isn't currently active or the player isn't currently looking at a block which is of interest to the
     * block tracker, this will return {@code Optional.empty()}.
     *
     * @return the block and face the player is currently looking at, or Optional.empty() if not focused on a block
     */
    Optional<BlockTrackerFocus> getBlockTrackerFocus();

    record BlockTrackerFocus(BlockPos pos, Direction face) { }
}
