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

package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Names.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> AIR_CANNON = register("air_cannon");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREAK = register("creak");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEAKING_GAS = register("leaking_gas");
    public static final DeferredHolder<SoundEvent, SoundEvent> PNEUMATIC_CRUSHER = register("pneumatic_crusher");
    public static final DeferredHolder<SoundEvent, SoundEvent> INTERFACE_DOOR = register("interface_door");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEVATOR_RISING_START = register("elevator_rising_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEVATOR_RISING_STOP = register("elevator_rising_stop");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEVATOR_RISING = register("elevator_rising");
    public static final DeferredHolder<SoundEvent, SoundEvent> HELMET_HACK_FINISH = register("helmet_hack_finish");
    public static final DeferredHolder<SoundEvent, SoundEvent> HUD_INIT = register("hud_init");
    public static final DeferredHolder<SoundEvent, SoundEvent> HUD_INIT_COMPLETE = register("hud_init_complete");
    public static final DeferredHolder<SoundEvent, SoundEvent> HUD_ENTITY_LOCK = register("hud_entity_lock");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCI_FI = register("sci_fi");
    public static final DeferredHolder<SoundEvent, SoundEvent> PNEUMATIC_WRENCH = register("pneumatic_wrench");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINIGUN = register("minigun");
    public static final DeferredHolder<SoundEvent, SoundEvent> MINIGUN_STOP = register("minigun_stop");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRONE_HURT = register("drone_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRONE_DEATH = register("drone_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHORT_HISS = register("short_hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUNCH = register("punch");
    public static final DeferredHolder<SoundEvent, SoundEvent> PNEUMATIC_DOOR = register("pneumatic_door");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHIRP = register("chirp");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCUBA = register("scuba");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEAKING_GAS_LOW = register("leaking_gas_low");
    public static final DeferredHolder<SoundEvent, SoundEvent> JACKHAMMER_LOOP = register("jackhammer_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> JACKHAMMER_STOP = register("jackhammer_stop");

    private static DeferredHolder<SoundEvent,SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(RL(name)));
    }
}
