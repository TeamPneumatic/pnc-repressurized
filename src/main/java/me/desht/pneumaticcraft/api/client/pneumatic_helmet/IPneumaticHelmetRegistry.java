package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.tags.ITag;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

/**
 * Retrieve an instance of this via {@link PneumaticRegistry.IPneumaticCraftInterface#getHelmetRegistry()}
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
     * Registers a Pneumatic Helmet module. This must be called from a {@code FMLClientSetupEvent} handler.
     *
     * @param clientHandler the handler to register
     */
    void registerRenderHandler(IArmorUpgradeHandler handler, IArmorUpgradeClientHandler clientHandler);

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
}
