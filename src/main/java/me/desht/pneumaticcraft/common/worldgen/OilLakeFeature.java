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

package me.desht.pneumaticcraft.common.worldgen;

/**
 * Extended vanilla lake feature to allow blacklisting by dimension ID and also to prevent generation within
 * structures in the "pneumaticcraft:no_oil_lakes" structure feature tag (which is "#villages" by default).
 */
public class OilLakeFeature /*extends LakeFeature*/ {
//    public OilLakeFeature() {
//        super(LakeFeature.Configuration.CODEC);
//    }
//
//    @Override
//    public boolean place(FeaturePlaceContext<Configuration> context) {
//        if (!WorldGenFiltering.isDimensionOK(context.level())) {
//            return false;
//        }
//
//        if (context.level() instanceof WorldGenRegion region) {
//            // don't allow oil lakes to generate within any structure feature in pneumaticcraft:no_oil_lakes tag
//            SectionPos sectionPos = SectionPos.of(context.origin());
//            ChunkAccess chunkAccess = context.level().getChunk(context.origin());
//
//            Registry<ConfiguredStructureFeature<?, ?>> reg = context.level().registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
//            StructureFeatureManager sfManager = region.structureFeatureManager;  // accesstransform
//
//            for (Holder<ConfiguredStructureFeature<?, ?>> csf : reg.getOrCreateTag(PneumaticCraftTags.ConfiguredStructures.NO_OIL_LAKES)) {
//                StructureStart startForFeature = sfManager.getStartForFeature(sectionPos, csf.value(), chunkAccess);
//                if (startForFeature != null && startForFeature.isValid()) {
//                    return false;
//                }
//            }
//        }
//
//        return super.place(context);
//    }
}
