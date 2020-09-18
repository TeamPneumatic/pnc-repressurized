/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package me.desht.pneumaticcraft.common.worldgen;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * This will go once Forge has proper support for adding features to biomes.
 */
public class BiomeModifier
{
//    private final Biome biome;
//
//    public BiomeModifier(Biome biome)
//    {
//        this.biome = biome;
//    }
//
//    public void addFeature(GenerationStage.Decoration stage, ConfiguredFeature<?, ?> newFeature)
//    {
//        final int index = stage.ordinal();
//        List<List<Supplier<ConfiguredFeature<?, ?>>>> allFeatures = new ArrayList<>(biome.func_242440_e().field_242484_f);
//        while(allFeatures.size() <= index)
//            allFeatures.add(new ArrayList<>());
//        List<Supplier<ConfiguredFeature<?, ?>>> oreGen = new ArrayList<>(allFeatures.get(index));
//        oreGen.add(() -> newFeature);
//        allFeatures.set(index, oreGen);
//        biome.getGenerationSettings().field_242484_f = allFeatures;
//    }
}

