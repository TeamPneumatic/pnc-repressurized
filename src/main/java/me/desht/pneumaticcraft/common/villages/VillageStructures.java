package me.desht.pneumaticcraft.common.villages;

import com.mojang.datafixers.util.Pair;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

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
    private static void addBuildingToPool(MutableRegistry<JigsawPattern> templatePoolRegistry, ResourceLocation poolRL, String nbtPieceRL, int weight) {
        // Grab the pool we want to add to
        JigsawPattern pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        // Grabs the nbt piece and creates a SingleJigsawPiece of it that we can add to a structure's pool.
        SingleJigsawPiece piece = SingleJigsawPiece.single(nbtPieceRL).apply(JigsawPattern.PlacementBehaviour.RIGID);

        // AccessTransformer to make JigsawPattern's templates field public for us to see.
        // public net.minecraft.world.gen.feature.jigsaw.JigsawPattern templates #templates
        // Weight is handled by how many times the entry appears in this list.
        // We do not need to worry about immutability as this field is created using Lists.newArrayList(); which makes a mutable list.
        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        // AccessTransformer to make JigsawPattern's rawTemplates field public for us to see.
        // net.minecraft.world.gen.feature.jigsaw.JigsawPattern rawTemplates #rawTemplates
        // This list of pairs of pieces and weights is not used by vanilla by default but another mod may need it for efficiency.
        // So lets add to this list for completeness. We need to make a copy of the array as it can be an immutable list.
        List<Pair<JigsawPiece, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }

    public static void addMechanicHouse(final FMLServerAboutToStartEvent event) {
        MutableRegistry<JigsawPattern> templatePoolRegistry = event.getServer().registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);

        for (String biome : new String[]{"plains", "desert", "savanna", "taiga", "snowy"}) {
            addBuildingToPool(templatePoolRegistry,
                    new ResourceLocation("village/" + biome + "/houses"),
                    Names.MOD_ID + ":villages/mechanic_house_" + biome,
                    8
            );
        }
    }
}
