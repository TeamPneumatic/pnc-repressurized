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

package me.desht.pneumaticcraft.common.villages;

import com.mojang.datafixers.util.Pair;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.mixin.accessors.StructureTemplatePoolAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.ArrayList;
import java.util.List;

public class VillageStructures {
    /**
     * Adds the building to the targeted pool.
     * We will call this in addNewVillageBuilding method further down to add to every village.
     *
     * Note: This is an additive operation which means multiple mods can do this and they stack with each other safely.
     *
     * With thanks to TelepathicGrunt: https://gist.github.com/TelepathicGrunt/4fdbc445ebcbcbeb43ac748f4b18f342
     */
    private static void addPieceToPool(Registry<StructureTemplatePool> templatePoolRegistry, Holder<StructureProcessorList> emptyProcessor, ResourceLocation poolRL, String nbtPieceRL, StructureTemplatePool.Projection projection, int weight) {
        // Grab the pool we want to add to
        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        // Grabs the nbt piece and creates a SingleJigsawPiece of it that we can add to a structure's pool.
        // Note: street pieces are a legacy_single_pool_piece type, houses are single_pool_piece
        SinglePoolElement piece = poolRL.getPath().endsWith("streets") ?
                SinglePoolElement.legacy(nbtPieceRL, emptyProcessor).apply(projection) :
                SinglePoolElement.single(nbtPieceRL, emptyProcessor).apply(projection);

        StructureTemplatePoolAccess access = (StructureTemplatePoolAccess) pool;

        // Mixin to make JigsawPattern's templates field public for us to see.
        // Weight is handled by how many times the entry appears in this list.
        // We do not need to worry about immutability as this field is created using Lists.newArrayList(); which makes a mutable list.
        for (int i = 0; i < weight; i++) {
            access.getTemplates().add(piece);
        }

        // Mixin to make JigsawPattern's rawTemplates field public for us to see.
        // This list of pairs of pieces and weights is not used by vanilla by default but another mod may need it for efficiency.
        // So lets add to this list for completeness. We need to make a copy of the array as it can be an immutable list.
        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(access.getRawTemplates());
        listOfPieceEntries.add(new Pair<>(piece, weight));
        access.setRawTemplates(listOfPieceEntries);
    }

    public static void addMechanicHouse(final ServerAboutToStartEvent event) {
        int weight = ConfigHelper.common().villagers.mechanicHouseWeight.get();
        if (weight > 0) {
            Holder<StructureProcessorList> emptyProcessor = event.getServer().registryAccess().registryOrThrow(Registries.PROCESSOR_LIST)
                    .getHolderOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, new ResourceLocation("minecraft:empty")));

            Registry<StructureTemplatePool> templatePoolRegistry = event.getServer().registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);

            for (VillageBiome v : VillageBiome.values()) {
                // desert & snowy villages don't have street pieces large enough to support a PNC house
                // in this case, we add a custom street with extra reserved space big enough for the house
                // - the jigsaw pieces in that street use a custom pool in PNC's namespace which has only our house in it
                if (v.needsCustomStreet()) {
                    // note: in this case, our mechanic house is in the custom pneumaticcraft:village/<biome>/houses
                    //   template pool JSON, so doesn't need to be added in code
                    addPieceToPool(templatePoolRegistry, emptyProcessor,
                            new ResourceLocation("village/" + v.getBiomeName() + "/streets"),
                            Names.MOD_ID + ":villages/custom_street_" + v.getBiomeName(),
                            StructureTemplatePool.Projection.TERRAIN_MATCHING, Math.max(1, weight / 4));
                } else {
                    // add the house to the vanilla minecraft:village/<biome>/houses pool
                    addPieceToPool(templatePoolRegistry, emptyProcessor,
                            new ResourceLocation("village/" + v.getBiomeName() + "/houses"),
                            Names.MOD_ID + ":villages/mechanic_house_" + v.getBiomeName(),
                            StructureTemplatePool.Projection.RIGID, weight
                    );
                }
            }
        }
    }

    enum VillageBiome {
        PLAINS("plains", false),
        DESERT("desert", true),
        SAVANNA("savanna", false),
        TAIGA("taiga", false),
        SNOWY("snowy", true);

        private final String biomeName;
        private final boolean needsCustomStreet;

        VillageBiome(String biomeName, boolean needsCustomStreet) {
            this.biomeName = biomeName;
            this.needsCustomStreet = needsCustomStreet;
        }

        public String getBiomeName() {
            return biomeName;
        }

        public boolean needsCustomStreet() {
            return needsCustomStreet;
        }
    }
}
