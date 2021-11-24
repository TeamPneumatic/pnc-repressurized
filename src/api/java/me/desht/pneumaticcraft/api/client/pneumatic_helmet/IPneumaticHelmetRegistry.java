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

import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Retrieve an instance of this via {@link me.desht.pneumaticcraft.api.PneumaticRegistry.IPneumaticCraftInterface#getHelmetRegistry()}
 */
public interface IPneumaticHelmetRegistry {
    /**
     * Register an entity tracker for the Pneumatic Helmet.
     * @param entry the entity tracker
     */
    void registerEntityTrackEntry(Supplier<? extends IEntityTrackEntry> entry);

    /**
     * Register a block tracker for the Pneumatic Helmet
     * @param entry the block tracker
     */
    void registerBlockTrackEntry(IBlockTrackEntry entry);

    /**
     * Register a "foreign" entity with your hackable. This should be used for entities you didn't create, i.e.
     * vanilla or from a different mod.  For your own entities, just have your entity implement {@link IHackableEntity}
     *
     * @param entityClazz entity class; subclasses of this entity will also be affected
     * @param iHackable the hack to register
     */
    void addHackable(Class<? extends Entity> entityClazz, Supplier<? extends IHackableEntity> iHackable);

    /**
     * Register a "foreign" block with your hackable. This should be used for blocks you didn't create, i.e.
     * vanilla or from a different mod.  For your own blocks, just have your block implement {@link IHackableBlock}
     *
     * @param block the block class; subclasses of this block will also be affected
     * @param iHackable the hack to register
     */
    void addHackable(@Nonnull Block block, @Nonnull Supplier<? extends IHackableBlock> iHackable);

    void addHackable(@Nonnull ITag.INamedTag<Block> blockTag, @Nonnull Supplier<? extends IHackableBlock> iHackable);

    /**
     * Get a list of all current successful hacks on a given entity. This is used for example in Enderman hacking, so
     * the user can only hack an enderman once (more times wouldn't have any effect). This is mostly used for display
     * purposes.
     *
     * @param entity the entity to check
     * @return empty list if no hacks.
     */
    List<IHackableEntity> getCurrentEntityHacks(Entity entity);

    /**
     * Registers the client handler for a Pneumatic Armor upgrade. This must be called from a {@link FMLClientSetupEvent}
     * handler, using {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent#enqueueWork(Runnable)}. This
     * also registers any keybindings referenced by the render handler
     * (see {@link IArmorUpgradeClientHandler#getInitialKeyBinding()} and {@link IArmorUpgradeClientHandler#getSubKeybinds()}.
     *
     * @param clientHandler the handler to register
     */
    <T extends IArmorUpgradeHandler<?>> void registerRenderHandler(T handler, IArmorUpgradeClientHandler<T> clientHandler);

    /**
     * Create a new keybinding button for an {@link IOptionPage} armor GUI screen.  This is intended to be called from
     * {@link IOptionPage#populateGui(IGuiScreen)} to set up a button which can be used to change a particular key
     * binding.
     *
     * @param yPos y position of the button
     * @param keyBinding the keybinding modified by the button
     * @return the new button
     */
    IKeybindingButton makeKeybindingButton(int yPos, KeyBinding keyBinding);

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
}
