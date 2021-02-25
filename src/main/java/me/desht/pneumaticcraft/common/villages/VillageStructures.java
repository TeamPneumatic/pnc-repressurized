package me.desht.pneumaticcraft.common.villages;

import com.mojang.datafixers.util.Pair;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern.PlacementBehaviour;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.structure.*;
import org.jline.utils.Log;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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
        JigsawPattern old = WorldGenRegistries.JIGSAW_POOL.getOrDefault(pool);
        if (old == null) {
            Log.warn("no jigsaw pool for " + pool + "? skipping pneumatic villager house generation for it");
            return;
        }
        List<JigsawPiece> shuffled = old.getShuffledPieces(ThreadLocalRandom.current());
        List<Pair<JigsawPiece, Integer>> newPieces = shuffled.stream().map(p -> new Pair<>(p, 1)).collect(Collectors.toList());
        JigsawPiece newPiece = JigsawPiece.func_242849_a(toAdd.toString()).apply(PlacementBehaviour.RIGID);
        newPieces.add(Pair.of(newPiece, weight));
        Registry.register(WorldGenRegistries.JIGSAW_POOL, pool, new JigsawPattern(pool, old.getName(), newPieces));
    }
}
