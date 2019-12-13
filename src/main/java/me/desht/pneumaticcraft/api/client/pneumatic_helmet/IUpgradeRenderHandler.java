package me.desht.pneumaticcraft.api.client.pneumatic_helmet;

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;

/**
 * To add upgrades to a Pneumatic armor piece, implement this interface. You can add members to this class, but these
 * can only be client sided members as this class will be used as a singleton. Therefore, only one of these instances
 * exist at the server side, so any member that is used server side will affect every player.
 */
public interface IUpgradeRenderHandler {

    /**
     * Return here a unique ID for the upgrade. This is displayed in the formatting [upgradeName] + " " + "found"/
     * "not found" on initialization of the helmet.
     *
     * @return a unique ID
     */
    String getUpgradeID();

    /**
     * This is called from PneumaticCraft's config handler in the pre-init phase. You can use this method to read
     * settings such as stat positions.
     */
    default void initConfig() {}

    /**
     * When called this should save the settings to the config file. Called when changed a setting. When you want to use
     * PneumaticCraft's config file, save a reference of it somewhere in this class when the config gets passed in the
     * initConfig() method (this always will be called first).
     */
    default void saveToConfig() {}

    /**
     * This method will be called every client tick, and should be used to update logic like the tracking and velocities
     * of stuff.
     *
     * @param player the player wearing the pneumatic helmet
     * @param rangeUpgrades number of range upgrades installed in the armor piece
     */
    void update(PlayerEntity player, int rangeUpgrades);

    /**
     * Called in the 3D render stage (called from {@link net.minecraftforge.client.event.RenderWorldLastEvent})
     *
     * @param partialTicks partial ticks since last world tick
     */
    void render3D(float partialTicks);

    /**
     * Called in the 2D render stage (called from {@link net.minecraftforge.event.TickEvent.RenderTickEvent})
     *
     * @param partialTicks partial ticks since last world tick
     * @param helmetEnabled true when isEnabled() returned true earlier. Can be used to close AnimatedStats for instance.
     *                      However this is already handled if you return an AnimatedStat in getAnimatedStat().
     */
    void render2D(float partialTicks, boolean helmetEnabled);

    /**
     * You can return a {@link IGuiAnimatedStat} here, that the HUD Handler will pick up and render. It also
     * automatically opens and closes the stat as necessary. The GuiMoveStat uses this method to retrieve the to be
     * moved stat.
     *
     * @return the animated stat, or null if no stat used.
     */
    IGuiAnimatedStat getAnimatedStat();

    /**
     * Should return the upgrades that are required to be in the helmet to enable this module.
     *
     * @return an array of required items; these do not need be registered PneumaticCraft upgrades
     */
    Item[] getRequiredUpgrades();

    /**
     * Returns the usage in mL/tick when this upgrade handler is enabled.
     *
     * @param rangeUpgrades number of range upgrades installed in the armor piece
     * @param player the player wearing the armor
     * @return usage in mL/tick
     */
    float getEnergyUsage(int rangeUpgrades, PlayerEntity player);

    /**
     * Called when (re-)equipped the helmet this method should be used to clear information like current tracked entities.
     * So clearing lists and other references as this handler should re-acquire when reinstalled.
     */
    void reset();

    /**
     * When you have some options for your upgrade handler you could return a new instance of an IOptionsPage.
     * When you do so, it will automatically get picked up by the options handler, and it will be added to the
     * options GUI when this upgrade returns true when calling isEnabled(). Returning null is valid.
     *
     * @return an options page
     */
    IOptionPage getGuiOptionsPage();

    /**
     * Get the minimum helmet pressure for this renderer to operate; the armor piece pressure must be <i>greater</i>
     * than this.  Most components require any pressure >0.0 bar.  Return any negative value for the component to
     * always render.
     *
     * @return the minimum required pressure
     */
    default float getMinimumPressure() {
        return 0.0f;
    }

    /**
     * Get the armor slot that this upgrade handler is attached to.
     *
     * @return the armor slot
     */
    EquipmentSlotType getEquipmentSlot();

    /**
     * Called when the screen resolution has changed.  This can be used to reset IGuiAnimatedState positions,
     * for example.
     */
    default void onResolutionChanged() {
    }

    /**
     * Convenience class for simple toggleable armor features with no additional settings.
     */
    abstract class SimpleToggleableRenderHandler implements IUpgradeRenderHandler {
        @Override
        public void update(PlayerEntity player, int rangeUpgrades) {

        }

        @Override
        public void render3D(float partialTicks) {

        }

        @Override
        public void render2D(float partialTicks, boolean helmetEnabled) {

        }

        @Override
        public IGuiAnimatedStat getAnimatedStat() {
            return null;
        }

        @Override
        public float getEnergyUsage(int rangeUpgrades, PlayerEntity player) {
            return 0;
        }

        @Override
        public void reset() {

        }

        @Override
        public IOptionPage getGuiOptionsPage() {
            return new IOptionPage.SimpleToggleableOptions(this);
        }

        @Override
        public float getMinimumPressure() {
            return 0;
        }
    }
}
