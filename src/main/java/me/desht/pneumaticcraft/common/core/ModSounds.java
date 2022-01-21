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

package me.desht.pneumaticcraft.common.core;

import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Names.MOD_ID);

    public static final RegistryObject<SoundEvent> AIR_CANNON = register("air_cannon");
    public static final RegistryObject<SoundEvent> CREAK = register("creak");
    public static final RegistryObject<SoundEvent> LEAKING_GAS = register("leaking_gas");
    public static final RegistryObject<SoundEvent> PNEUMATIC_CRUSHER = register("pneumatic_crusher");
    public static final RegistryObject<SoundEvent> INTERFACE_DOOR = register("interface_door");
    public static final RegistryObject<SoundEvent> ELEVATOR_RISING_START = register("elevator_rising_start");
    public static final RegistryObject<SoundEvent> ELEVATOR_RISING_STOP = register("elevator_rising_stop");
    public static final RegistryObject<SoundEvent> ELEVATOR_RISING = register("elevator_rising");
    public static final RegistryObject<SoundEvent> HELMET_HACK_FINISH = register("helmet_hack_finish");
    public static final RegistryObject<SoundEvent> HUD_INIT = register("hud_init");
    public static final RegistryObject<SoundEvent> HUD_INIT_COMPLETE = register("hud_init_complete");
    public static final RegistryObject<SoundEvent> HUD_ENTITY_LOCK = register("hud_entity_lock");
    public static final RegistryObject<SoundEvent> SCI_FI = register("sci_fi");
    public static final RegistryObject<SoundEvent> PNEUMATIC_WRENCH = register("pneumatic_wrench");
    public static final RegistryObject<SoundEvent> MINIGUN = register("minigun");
    public static final RegistryObject<SoundEvent> MINIGUN_STOP = register("minigun_stop");
    public static final RegistryObject<SoundEvent> DRONE_HURT = register("drone_hurt");
    public static final RegistryObject<SoundEvent> DRONE_DEATH = register("drone_death");
    public static final RegistryObject<SoundEvent> SHORT_HISS = register("short_hiss");
    public static final RegistryObject<SoundEvent> PUNCH = register("punch");
    public static final RegistryObject<SoundEvent> PNEUMATIC_DOOR = register("pneumatic_door");
    public static final RegistryObject<SoundEvent> CHIRP = register("chirp");
    public static final RegistryObject<SoundEvent> SCUBA = register("scuba");
    public static final RegistryObject<SoundEvent> LEAKING_GAS_LOW = register("leaking_gas_low");
    public static final RegistryObject<SoundEvent> JACKHAMMER_LOOP = register("jackhammer_loop");
    public static final RegistryObject<SoundEvent> JACKHAMMER_STOP = register("jackhammer_stop");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(RL(name)));
    }
}
