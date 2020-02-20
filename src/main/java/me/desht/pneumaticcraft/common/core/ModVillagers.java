package me.desht.pneumaticcraft.common.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.desht.pneumaticcraft.common.util.RandomTradeBuilder;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern.PlacementBehaviour;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.PlainsVillagePools;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ModVillagers {
    public static final DeferredRegister<PointOfInterestType> POI = new DeferredRegister<>(ForgeRegistries.POI_TYPES, Names.MOD_ID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = new DeferredRegister<>(ForgeRegistries.PROFESSIONS, Names.MOD_ID);

    public static final RegistryObject<PointOfInterestType> MECHANIC_POI = POI.register("mechanic",
            () -> new PointOfInterestType("mechanic", getAllStates(ModBlocks.CHARGING_STATION.get()), 1, ModSounds.SHORT_HISS.get(), 1));
    public static final RegistryObject<VillagerProfession> MECHANIC = registerProfession("mechanic", ModVillagers.MECHANIC_POI);

    private static RegistryObject<VillagerProfession> registerProfession(String name, Supplier<PointOfInterestType> poiType) {
        return PROFESSIONS.register(name, () -> new VillagerProfession(Names.MOD_ID + ":" + name, poiType.get(), ImmutableSet.of(), ImmutableSet.of()));
    }

    private static Set<BlockState> getAllStates(Block block) {
        return ImmutableSet.copyOf(block.getStateContainer().getValidStates());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class VillagerTradesRegistration {
        @SubscribeEvent
        public static void registerTrades(VillagerTradesEvent event) {
            Int2ObjectMap<List<VillagerTrades.ITrade>> trades = event.getTrades();
            if (event.getType() == MECHANIC.get()) {
                trades.get(1).add(new RandomTradeBuilder(4, 10, 0.05F)
                        .setEmeraldPriceFor(10, 19, ModItems.PCB_BLUEPRINT.get(), 1)
                        .build()
                );
                trades.get(1).add(new RandomTradeBuilder(8, 2, 0.05F)
                        .setEmeraldPrice(3, 7)
                        .setForSale((rand) -> new ItemStack(ModBlocks.COMPRESSED_IRON_BLOCK.get()))
                        .build()
                );
                trades.get(2).add(new RandomTradeBuilder(1, 10, 0.05F)
                        .setEmeraldPriceFor(1, 5, ModItems.NUKE_VIRUS.get(), 1)
                        .build()
                );
                trades.get(2).add(new RandomTradeBuilder(1, 10, 0.05F)
                        .setEmeraldPriceFor(1, 5, ModItems.STOP_WORM.get(), 1)
                        .build()
                );
            }
        }
    }

    public static class Structures {
        public static void init() {
            PlainsVillagePools.init();

            addToPool(new ResourceLocation("village/plains/houses"),
                    RL("villages/mechanic_house_plains"), 5);
        }

        private static void addToPool(ResourceLocation pool, ResourceLocation toAdd, int weight) {
            JigsawPattern old = JigsawManager.REGISTRY.get(pool);
            List<JigsawPiece> shuffled = old.getShuffledPieces(new Random());
            List<Pair<JigsawPiece, Integer>> newPieces = shuffled.stream()
                    .map(p -> new Pair<>(p, 1))
                    .collect(Collectors.toList());
            newPieces.add(new Pair<>(new SingleJigsawPiece(toAdd.toString(), ImmutableList.of(), PlacementBehaviour.RIGID), weight));
            JigsawManager.REGISTRY.register(new JigsawPattern(pool, old.func_214948_a(), newPieces, PlacementBehaviour.RIGID));
        }
    }
}
