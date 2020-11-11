package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipeTypes;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ModCategoryUid {
    static final ResourceLocation FLUID_MIXER = RL(PneumaticCraftRecipeTypes.FLUID_MIXER);
    static final ResourceLocation ASSEMBLY_CONTROLLER = RL("assembly_controller");
    static final ResourceLocation EXPLOSION_CRAFTING = RL("compressed_iron_explosion");
    static final ResourceLocation PRESSURE_CHAMBER = RL("pressure_chamber");
    static final ResourceLocation REFINERY = RL("refinery");
    static final ResourceLocation THERMO_PLANT = RL("thermo_plant");
    static final ResourceLocation HEAT_FRAME_COOLING = RL("heat_frame_cooling");
    static final ResourceLocation AMADRON_TRADE = RL("amadron_trade");
    static final ResourceLocation YEAST_CRAFTING = RL("yeast_crafting");
    static final ResourceLocation SPAWNER_EXTRACTION = RL("spawner_extraction");

    // pseudo-recipes
    static final ResourceLocation UV_LIGHT_BOX = RL("uv_light_box");
    static final ResourceLocation PLASTIC_SOLIDIFYING = RL("plastic_solidifying");
    static final ResourceLocation ETCHING_TANK = RL("etching_tank");
}
