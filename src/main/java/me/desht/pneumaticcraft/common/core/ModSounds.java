package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@ObjectHolder(Names.MOD_ID)
public class ModSounds {
    public static final SoundEvent AIR_CANNON = null;
    public static final SoundEvent LEAKING_GAS = null;
    public static final SoundEvent PNEUMATIC_CRUSHER = null;
    public static final SoundEvent INTERFACE_DOOR = null;
    public static final SoundEvent ELEVATOR_RISING_START = null;
    public static final SoundEvent ELEVATOR_RISING_STOP = null;
    public static final SoundEvent ELEVATOR_RISING = null;
    public static final SoundEvent HELMET_HACK_FINISH = null;
    public static final SoundEvent HUD_INIT = null;
    public static final SoundEvent HUD_INIT_COMPLETE = null;
    public static final SoundEvent HUD_ENTITY_LOCK = null;
    public static final SoundEvent SCI_FI = null;
    public static final SoundEvent PNEUMATIC_WRENCH = null;
    public static final SoundEvent MINIGUN = null;
    public static final SoundEvent MINIGUN_STOP = null;
    public static final SoundEvent DRONE_HURT = null;
    public static final SoundEvent DRONE_DEATH = null;
    public static final SoundEvent SHORT_HISS = null;
    public static final SoundEvent PUNCH = null;
    public static final SoundEvent PNEUMATIC_DOOR = null;
    public static final SoundEvent CHIRP = null;
    public static final SoundEvent SCUBA = null;
    public static final SoundEvent LEAKING_GAS_LOW = null;

    @Mod.EventBusSubscriber(modid=Names.MOD_ID, bus= Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
            event.getRegistry().registerAll(
                    buildSound("air_cannon"),
                    buildSound("leaking_gas"),
                    buildSound("pneumatic_crusher"),
                    buildSound("interface_door"),
                    buildSound("elevator_rising_start"),
                    buildSound("elevator_rising_stop"),
                    buildSound("elevator_rising"),
                    buildSound("helmet_hack_finish"),
                    buildSound("hud_init"),
                    buildSound("hud_init_complete"),
                    buildSound("hud_entity_lock"),
                    buildSound("sci_fi"),
                    buildSound("pneumatic_wrench"),
                    buildSound("minigun"),
                    buildSound("minigun_stop"),
                    buildSound("drone_hurt"),
                    buildSound("drone_death"),
                    buildSound("short_hiss"),
                    buildSound("punch"),
                    buildSound("pneumatic_door"),
                    buildSound("chirp"),
                    buildSound("scuba"),
                    buildSound("leaking_gas_low")
            );
        }

        private static SoundEvent buildSound(String key) {
            return new SoundEvent(RL(key)).setRegistryName(key);
        }
    }

}
