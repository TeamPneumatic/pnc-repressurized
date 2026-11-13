package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.lib.Names;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModGameEvents {
    public static final DeferredRegister<GameEvent> GAME_EVENTS
            = DeferredRegister.create(Registries.GAME_EVENT, Names.MOD_ID);

    public static final DeferredHolder<GameEvent, GameEvent> JET_BOOTS_FLY
            = GAME_EVENTS.register("jet_boots_fly", () -> new GameEvent(16));
    public static final DeferredHolder<GameEvent, GameEvent> JET_BOOTS_FLY_BUILDER
            = GAME_EVENTS.register("jet_boots_fly_builder", () -> new GameEvent(8));
}
