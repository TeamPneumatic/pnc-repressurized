package me.desht.pneumaticcraft.lib;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

@Mod.EventBusSubscriber
@GameRegistry.ObjectHolder(Names.MOD_ID)
public class Sounds {
    @GameRegistry.ObjectHolder("air_cannon")
    public static final SoundEvent CANNON_SOUND = null;
    @GameRegistry.ObjectHolder("leaking_gas")
    public static final SoundEvent LEAKING_GAS_SOUND = null;
    @GameRegistry.ObjectHolder("pneumatic_crusher")
    public static final SoundEvent PNEUMATIC_CRUSHER_SOUND = null;
    @GameRegistry.ObjectHolder("interface_door")
    public static final SoundEvent INTERFACE_DOOR = null;
    @GameRegistry.ObjectHolder("elevator_rising_start")
    public static final SoundEvent ELEVATOR_START = null;
    @GameRegistry.ObjectHolder("elevator_rising_stop")
    public static final SoundEvent ELEVATOR_STOP = null;
    @GameRegistry.ObjectHolder("elevator_moving")
    public static final SoundEvent ELEVATOR_MOVING = null;
    @GameRegistry.ObjectHolder("helmet_hack_finish")
    public static final SoundEvent HELMET_HACK_FINISH = null;
    @GameRegistry.ObjectHolder("hud_init")
    public static final SoundEvent HUD_INIT = null;
    @GameRegistry.ObjectHolder("hud_init_complete")
    public static final SoundEvent HUD_INIT_COMPLETE = null;
    @GameRegistry.ObjectHolder("hud_entity_lock")
    public static final SoundEvent HUD_ENTITY_LOCK = null;
    @GameRegistry.ObjectHolder("sci_fi")
    public static final SoundEvent SCIFI = null;
    @GameRegistry.ObjectHolder("pneumatic_wrench")
    public static final SoundEvent PNEUMATIC_WRENCH = null;
    @GameRegistry.ObjectHolder("minigun")
    public static final SoundEvent MINIGUN = null;
    @GameRegistry.ObjectHolder("minigun_stop")
    public static final SoundEvent MINIGUN_STOP = null;
    @GameRegistry.ObjectHolder("drone_hurt")
    public static final SoundEvent DRONE_HURT = null;
    @GameRegistry.ObjectHolder("drone_death")
    public static final SoundEvent DRONE_DEATH = null;

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
                buildSound("air_cannon"),
                buildSound("leaking_gas"),
                buildSound("pneumatic_crusher"),
                buildSound("interface_door"),
                buildSound("elevator_rising_start"),
                buildSound("elevator_rising_stop"),
                buildSound("elevator_moving"),
                buildSound("helmet_hack_finish"),
                buildSound("hud_init"),
                buildSound("hud_init_complete"),
                buildSound("hud_entity_lock"),
                buildSound("sci_fi"),
                buildSound("pneumatic_wrench"),
                buildSound("minigun"),
                buildSound("minigun_stop"),
                buildSound("drone_hurt"),
                buildSound("drone_death")
        );
    }

    private static SoundEvent buildSound(String key) {
        return new SoundEvent(RL(key)).setRegistryName(key);
    }
}
