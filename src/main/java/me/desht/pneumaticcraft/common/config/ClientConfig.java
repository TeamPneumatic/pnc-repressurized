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

package me.desht.pneumaticcraft.common.config;

import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.misc.ITranslatableEnum;
import me.desht.pneumaticcraft.client.pneumatic_armor.ComponentInit;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Locale;

public class ClientConfig {
    public static class General {
        public ModConfigSpec.BooleanValue aphorismDrama;
        public ModConfigSpec.EnumValue<IProgWidget.WidgetDifficulty> programmerDifficulty;
        public ModConfigSpec.BooleanValue logisticsGuiTint;
        public ModConfigSpec.BooleanValue semiBlockLighting;
        public ModConfigSpec.BooleanValue guiBevel;
        public ModConfigSpec.BooleanValue alwaysShowPressureDurabilityBar;
        public ModConfigSpec.BooleanValue tubeModuleRedstoneParticles;
        public ModConfigSpec.BooleanValue guiRemoteGridSnap;
        public ModConfigSpec.BooleanValue programmerGuiPauses;
        public ModConfigSpec.BooleanValue notifyAmadronOfferUpdates;
        public ModConfigSpec.BooleanValue pressureChamberParticles;
        public ModConfigSpec.BooleanValue jackHammerHud;
    }

    public static class Armor {
        public ModConfigSpec.IntValue blockTrackerMaxTimePerTick;
        public ModConfigSpec.DoubleValue leggingsFOVFactor;
        public ModConfigSpec.BooleanValue fancyArmorModels;
        public ModConfigSpec.BooleanValue pathEnabled;
        public ModConfigSpec.BooleanValue wirePath;
        public ModConfigSpec.BooleanValue xRayEnabled;
        public ModConfigSpec.EnumValue<PathUpdateSetting> pathUpdateSetting;
        public ModConfigSpec.BooleanValue showPressureNumerically;
        public ModConfigSpec.BooleanValue showEnchantGlint;
        public ModConfigSpec.IntValue maxJetBootsFlightRoll;
        public ModConfigSpec.EnumValue<ComponentInit> componentInitMessages;
    }

    public static class Sound {
        public ModConfigSpec.DoubleValue elevatorVolumeRunning;
        public ModConfigSpec.DoubleValue elevatorVolumeStartStop;
        public ModConfigSpec.DoubleValue airLeakVolume;
        public ModConfigSpec.DoubleValue minigunVolumeHeld;
        public ModConfigSpec.DoubleValue minigunVolumeDrone;
        public ModConfigSpec.DoubleValue minigunVolumeSentryTurret;
        public ModConfigSpec.DoubleValue jetbootsVolume;
        public ModConfigSpec.DoubleValue jetbootsVolumeBuilderMode;
        public ModConfigSpec.DoubleValue jackhammerVolume;
    }

    public final ClientConfig.General general = new General();
    public final ClientConfig.Armor armor = new Armor();
    public final ClientConfig.Sound sound = new Sound();

    ClientConfig(ModConfigSpec.Builder builder) {
        builder.push("general");
        general.aphorismDrama = builder
                .comment("Enable Aphorism Tile Drama!  http://mc-drama.herokuapp.com/")
                .translation("pneumaticcraft.config.client.general.aphorism_drama")
                .define("aphorism_drama", true);
        general.programmerDifficulty = builder
                .comment("Defines which widgets are shown in the Programmer GUI: easy, medium, or advanced")
                .translation("pneumaticcraft.config.client.general.fancy_armor_models")
                .defineEnum("programmer_difficulty", IProgWidget.WidgetDifficulty.EASY);
        general.logisticsGuiTint = builder
                .comment("Tint Logistics configuration GUI backgrounds according to the colour of the logistics frame you are configuring.")
                .translation("pneumaticcraft.config.client.general.logistics_gui_tint")
                .define("logistics_gui_tint", true);
        general.guiBevel = builder
                .comment("Should GUI side tabs be shown with a beveled edge? Setting to false uses a plain black edge, as in earlier versions of the mod.")
                .translation("pneumaticcraft.config.client.general.gui_bevel")
                .define("gui_bevel", true);
        general.alwaysShowPressureDurabilityBar = builder
                .comment("Always show the pressure durability bar for pressurizable items, even when full?")
                .translation("pneumaticcraft.config.client.general.always_show_pressure_durability_bar")
                .define("always_show_pressure_durability_bar", true);
        general.tubeModuleRedstoneParticles = builder
                .comment("Should tube modules emit redstone play redstone particle effects when active?")
                .translation("pneumaticcraft.config.client.general.tube_module_redstone_particles")
                .define("tube_module_redstone_particles", true);
        general.guiRemoteGridSnap = builder
                .comment("Should widgets in the GUI Remote Editor be snapped to a 4x4 grid?")
                .translation("pneumaticcraft.config.client.general.gui_remote_grid_snap")
                .define("gui_remote_grid_snap", true);
        general.programmerGuiPauses = builder
                .comment("Should the SSP game pause when the Programmer GUI is open (does not apply in SMP)?")
                .translation("pneumaticcraft.config.client.general.programmer_gui_pauses")
                .define("programmer_gui_pauses", false);
        general.notifyAmadronOfferUpdates = builder
                .comment("Should players holding an Amadron Tablet get a notification message when offers are shuffled periodically?")
                .translation("pneumaticcraft.config.client.general.notify_amadron_offer_updates")
                .define("notify_amadron_offer_updates", true);
        general.pressureChamberParticles = builder
                .comment("Should Pressure Chambers show air particle effects inside themselves when pressurized?")
                .translation("pneumaticcraft.config.client.general.pressure_chamber_particles")
                .define("pressure_chamber_particles", true);
        general.jackHammerHud = builder
                .comment("Should the Jackhammer continually show a HUD indicating break modes other than basic single-block? If false, the mode HUD will show only when switching modes")
                .translation("pneumaticcraft.config.client.general.jackhammer_hud")
                .define("jackhammer_hud", true);
        builder.pop();

        builder.push("armor");
        armor.leggingsFOVFactor = builder
                .comment("Intensity of the FOV modification when using Pneumatic Leggings speed boost: 0.0 for no FOV modification, higher values zoom out more.  Note: non-zero values may cause FOV clashes with other mods.")
                .translation("pneumaticcraft.config.client.armor.leggings_fov_factor")
                .defineInRange("leggings_fov_factor", 0.0, 0.0, 1.0);
        armor.blockTrackerMaxTimePerTick = builder
                .comment("Maximum time, as a percentage of the tick, that the Pneumatic Helmet Block Tracker may take when active and scanning blocks. Larger values mean more rapid update of block information, but potentially greater impact on client FPS.")
                .translation("pneumaticcraft.config.client.armor.block_tracker_max_time_per_tick")
                .defineInRange("block_tracker_max_time_per_tick", 10, 1, 100);
        armor.pathEnabled = builder
                .comment("Enable the Pneumatic Helmet Coordinate Tracker pathfinder")
                .translation("pneumaticcraft.config.client.armor.path_enabled")
                .define("path_enabled", false);
        armor.wirePath = builder
                .comment("True if the Pneumatic Helmet Coordinate Tracker pathfinder should draw lines, false for tiles.")
                .translation("pneumaticcraft.config.client.armor.wire_path")
                .define("wire_path", false);
        armor.xRayEnabled = builder
                .comment("Should the Pneumatic Helmet Coordinate Tracker pathfinder path be visible through blocks?")
                .translation("pneumaticcraft.config.client.armor.xray_enabled")
                .define("xray_enabled", false);
        armor.pathUpdateSetting = builder
                .comment("How frequently should the Pneumatic Helmet Coordinate Tracker pathfinder path be recalculated?")
                .translation("pneumaticcraft.config.client.armor.xray_enabled")
                .defineEnum("path_update_setting", PathUpdateSetting.NORMAL);
        armor.showPressureNumerically = builder
                .comment("True: show pressure as numbers.  False: show pressure as horizontal bar.")
                .translation("pneumaticcraft.config.client.armor.show_pressure_numerically")
                .define("show_pressure_numerically", true);
        armor.showEnchantGlint = builder
                .comment("Should enchantment glint be shown on Pneumatic Armor pieces? Disable if you don't like the enchantment glint messing up your carefully chosen colour scheme...")
                .translation("pneumaticcraft.config.client.armor.show_enchant_glint")
                .define("show_enchant_glint", true);
        armor.maxJetBootsFlightRoll = builder
                .comment("Maximum screen roll in degrees when banking left or right during Jet Boots flight - cosmetic only")
                .translation("pneumaticcraft.config.client.armor.max_jet_boots_roll")
                .defineInRange("max_jet_boots_roll", 35, 0, 90);
        armor.componentInitMessages = builder
                .comment("Which component initialisation messages to display when armor is booting up")
                .translation("pneumaticcraft.config.client.armor.component_init_messages")
                .defineEnum("component_init_messages", ComponentInit.ALL);
        builder.pop();

        builder.push("sound");
        sound.elevatorVolumeRunning = builder
                .comment("Volume level of the Elevator while running")
                .translation("pneumaticcraft.config.client.sound.elevator_volume_running")
                .defineInRange("elevator_volume_running", 1.0d, 0d, 2d);
        sound.elevatorVolumeStartStop = builder
                .comment("Volume level of the Elevator *clunk* while starting/stopping")
                .translation("pneumaticcraft.config.client.sound.elevator_volume_start_stop")
                .defineInRange("elevator_volume_start_stop", 1.0d, 0d, 2d);
        sound.airLeakVolume = builder
                .comment("Volume level of air leaks from unconnected tubes/machines. Beware: turning this off could lead to undetected leaks wasting pressure forever!")
                .translation("pneumaticcraft.config.client.sound.air_leak_volume")
                .defineInRange("air_leak_volume", 1.0d, 0d, 2d);
        sound.minigunVolumeHeld = builder
                .comment("Volume level of the hand-held Minigun")
                .translation("pneumaticcraft.config.client.sound.minigun_volume_held")
                .defineInRange("minigun_volume_held", 0.75d, 0d, 2d);
        sound.minigunVolumeDrone = builder
                .comment("Volume level of drone-mounted Miniguns")
                .translation("pneumaticcraft.config.client.sound.minigun_volume_drone")
                .defineInRange("minigun_volume_drone", 1.0d, 0d, 2d);
        sound.minigunVolumeSentryTurret = builder
                .comment("Volume level of the Sentry Turret's Minigun")
                .translation("pneumaticcraft.config.client.sound.minigun_volume_sentry_turret")
                .defineInRange("minigun_volume_sentry_turret", 1.0d, 0d, 2d);
        sound.jetbootsVolume = builder
                .comment("Volume level of the Jet Boots in normal flight mode")
                .translation("pneumaticcraft.config.client.sound.jetboots_volume")
                .defineInRange("jetboots_volume", 1.0d, 0d, 2d);
        sound.jetbootsVolumeBuilderMode = builder
                .comment("Volume level of the Jet Boots when in Builder Mode")
                .translation("pneumaticcraft.config.client.sound.jetboots_volume_builder_mode")
                .defineInRange("jetboots_volume_builder_mode", 0.3d, 0d, 2d);
        sound.jackhammerVolume = builder
                .comment("Volume level of the Jackhammer")
                .translation("pneumaticcraft.config.client.sound.jackhammer_volume")
                .defineInRange("jackhammer_volume", 0.7d, 0d, 2d);
    }

    /**
     * Used by the Pneumatic Helmet coordinate tracker to control path update frequency.
     */
    public enum PathUpdateSetting implements ITranslatableEnum {
        SLOW(100),
        NORMAL(20),
        FAST(1);

        private final int ticks;

        PathUpdateSetting(int ticks) {
            this.ticks = ticks;
        }

        public int getTicks() {
            return ticks;
        }

        public PathUpdateSetting cycle() {
            return PathUpdateSetting.values()[(ordinal() + 1) % values().length];
        }

        @Override
        public String getTranslationKey() {
            return "pneumaticcraft.armor.gui.coordinateTracker.pathUpdate." + toString().toLowerCase(Locale.ROOT);
        }
    }
}
