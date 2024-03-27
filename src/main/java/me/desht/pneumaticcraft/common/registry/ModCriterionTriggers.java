package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.advancements.CustomTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCriterionTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> CRITERION_TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, Names.MOD_ID);

    public static final Supplier<CustomTrigger> EXPLODE_IRON = register("root");
    public static final Supplier<CustomTrigger> OIL_BUCKET = register("oil_bucket");
    public static final Supplier<CustomTrigger> NINEBYNINE = register("9x9");
    public static final Supplier<CustomTrigger> PRESSURE_CHAMBER = register("pressure_chamber");
    public static final Supplier<CustomTrigger> PROGRAM_DRONE = register("program_drone");
    public static final Supplier<CustomTrigger> PNEUMATIC_ARMOR = register("pneumatic_armor");
    public static final Supplier<CustomTrigger> ENTITY_HACK = register("entity_hack");
    public static final Supplier<CustomTrigger> BLOCK_HACK = register("block_hack");
    public static final Supplier<CustomTrigger> FLIGHT = register("flight");
    public static final Supplier<CustomTrigger> FLY_INTO_WALL = register("fly_into_wall");
    public static final Supplier<CustomTrigger> LOGISTICS_DRONE_DEPLOYED = register("logistics_drone_deployed");
    public static final Supplier<CustomTrigger> CHARGED_WRENCH = register("charged_wrench");
    public static final Supplier<CustomTrigger> MACHINE_VANDAL = register("machine_vandal");

    private static Supplier<CustomTrigger> register(String name) {
        return CRITERION_TRIGGERS.register(name, () -> new CustomTrigger(name));
    }

}

