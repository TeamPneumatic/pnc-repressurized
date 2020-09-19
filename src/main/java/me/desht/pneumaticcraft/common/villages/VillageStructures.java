package me.desht.pneumaticcraft.common.villages;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.*;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class VillageStructures {
    public static void init() {
        if (PNCConfig.Common.Villagers.addMechanicHouse) {
            PlainsVillagePools.init();
            SavannaVillagePools.init();
            TaigaVillagePools.init();
            DesertVillagePools.init();
            SnowyVillagePools.init();

            for (String biome : new String[]{"plains", "desert", "savanna", "taiga", "snowy"}) {
                addToPool(new ResourceLocation("village/" + biome + "/houses"),
                        RL("villages/mechanic_house_" + biome), 8);
            }
        }
    }

    private static void addToPool(ResourceLocation pool, ResourceLocation toAdd, int weight) {
        JigsawPattern old = JigsawManager.REGISTRY.get(pool);
        List<JigsawPiece> shuffled = old.getShuffledPieces(new Random());
        List<Pair<JigsawPiece, Integer>> newPieces = shuffled.stream()
                .map(p -> new Pair<>(p, 1))
                .collect(Collectors.toList());
        newPieces.add(new Pair<>(new SingleJigsawPiece(toAdd.toString(), ImmutableList.of(), JigsawPattern.PlacementBehaviour.RIGID), weight));
        JigsawManager.REGISTRY.register(new JigsawPattern(pool, old.getFallback(), newPieces, JigsawPattern.PlacementBehaviour.RIGID));
    }
}
