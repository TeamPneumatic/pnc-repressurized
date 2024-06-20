package me.desht.pneumaticcraft.common.worldgen;

import com.mojang.serialization.MapCodec;
import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.registry.ModPlacementModifierTypes;
import me.desht.pneumaticcraft.common.util.WildcardedRLMatcher;
import me.desht.pneumaticcraft.mixin.accessors.WorldGenRegionAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class OilLakeFilter extends PlacementFilter {
    private static final OilLakeFilter INSTANCE = new OilLakeFilter();
    public static final MapCodec<OilLakeFilter> CODEC = MapCodec.unit(() -> INSTANCE);

    public static OilLakeFilter oilLakeFilter() {
        return INSTANCE;
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource rand, BlockPos origin) {
        // config-driven dimension filtering
        if (!DimensionFilter.isDimensionOK(context.getLevel())) {
            return false;
        }

        // don't allow oil lakes to generate within any structure feature in pneumaticcraft:no_oil_lakes tag
        if (context.getLevel() instanceof WorldGenRegion region) {
            SectionPos sectionPos = SectionPos.of(origin);
            ChunkAccess chunkAccess = context.getLevel().getChunk(origin);

            Registry<Structure> reg = context.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
            StructureManager sfManager = ((WorldGenRegionAccess)region).getStructureManager();

            for (Holder<Structure> structureHolder : reg.getOrCreateTag(PneumaticCraftTags.Structures.NO_OIL_LAKES)) {
                StructureStart startForFeature = sfManager.getStartForStructure(sectionPos, structureHolder.value(), chunkAccess);
                if (startForFeature != null && startForFeature.isValid()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacementModifierTypes.OIL_LAKE_FILTER.get();
    }

    public static class DimensionFilter {
        private static WildcardedRLMatcher dimensionMatcherB = null;
        private static WildcardedRLMatcher dimensionMatcherW = null;

        private static boolean isDimensionOK(WorldGenLevel level) {
            // non-empty whitelist match OR no blacklist match
            ResourceLocation name = level.getLevel().dimension().location();
            return getDimensionWhitelist().isEmpty() ? !getDimensionBlacklist().test(name) : getDimensionWhitelist().test(name);
        }

        private static WildcardedRLMatcher getDimensionWhitelist() {
            if (dimensionMatcherW == null) {
                dimensionMatcherW = new WildcardedRLMatcher(ConfigHelper.common().worldgen.oilWorldGenDimensionWhitelist.get());
            }
            return dimensionMatcherW;
        }

        private static WildcardedRLMatcher getDimensionBlacklist() {
            if (dimensionMatcherB == null) {
                dimensionMatcherB = new WildcardedRLMatcher(ConfigHelper.common().worldgen.oilWorldGenDimensionBlacklist.get());
            }
            return dimensionMatcherB;
        }

        public static void clearMatcherCaches() {
            dimensionMatcherB = null;
            dimensionMatcherW = null;
        }
    }
}
