package me.desht.pneumaticcraft.common.villages;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern.PlacementBehaviour;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.LegacySingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.ProcessorLists;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class VillageStructures {
    public static void init() {
        PlainsVillagePools.init();
        SavannaVillagePools.init();
        TaigaVillagePools.init();
        DesertVillagePools.init();
        SnowyVillagePools.init();

        for (String biome : new String[] { "plains", "desert", "savanna", "taiga", "snowy" }) {
            addToPool(new ResourceLocation("village/" + biome + "/houses"),
                    RL("villages/mechanic_house_" + biome), 8);
        }
    }

    private static void addToPool(ResourceLocation pool, ResourceLocation toAdd, int weight) {
        JigsawPattern old = WorldGenRegistries.field_243656_h.getOrDefault(pool);
        List<JigsawPiece> shuffled = old != null ? old.getShuffledPieces(new Random()) : ImmutableList.of();
        List<Pair<JigsawPiece, Integer>> newPieces = shuffled.stream().map(p -> new Pair<>(p, 1)).collect(Collectors.toList());
        newPieces.add(new Pair<>(new LegacySingleJigsawPiece(Either.left(toAdd), () -> ProcessorLists.field_244101_a, PlacementBehaviour.RIGID), weight));
        ResourceLocation name = old.getName();
        Registry.register(WorldGenRegistries.field_243656_h, pool, new JigsawPattern(pool, name, newPieces));

//        JigsawPattern old = JigsawManager.REGISTRY.get(pool);
//        List<JigsawPiece> shuffled = old.getShuffledPieces(new Random());
//        List<Pair<JigsawPiece, Integer>> newPieces = shuffled.stream()
//                .map(p -> new Pair<>(p, 1))
//                .collect(Collectors.toList());
//        newPieces.add(new Pair<>(new SingleJigsawPiece(toAdd.toString()), weight)); // ImmutableList.of(), JigsawPattern.PlacementBehaviour.RIGID), weight));
//        JigsawManager.REGISTRY.register(new JigsawPattern(pool, old.getFallback(), newPieces, PlacementBehaviour.RIGID));
    }
}
