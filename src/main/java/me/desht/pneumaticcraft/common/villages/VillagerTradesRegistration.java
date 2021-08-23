package me.desht.pneumaticcraft.common.villages;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.config.PNCConfig;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModVillagers;
import me.desht.pneumaticcraft.common.util.RandomTradeBuilder;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerTradesRegistration {
    public enum WhichTrades {
        NONE,
        PCB_BLUEPRINT,
        ALL;

        boolean shouldAddBlueprint() {
            return this != NONE;
        }
    }

    @SubscribeEvent
    public static void registerTrades(VillagerTradesEvent event) {
        Int2ObjectMap<List<VillagerTrades.ITrade>> trades = event.getTrades();
        if (event.getType() == ModVillagers.MECHANIC.get()) {
            if (PNCConfig.Common.Villagers.whichTrades.shouldAddBlueprint()) {
                trades.get(1).add(new RandomTradeBuilder(4, 10, 0.05F)
                        .setEmeraldPriceFor(10, 19, ModItems.PCB_BLUEPRINT.get(), 1)
                        .build()
                );
            }
            if (PNCConfig.Common.Villagers.whichTrades == WhichTrades.ALL) {
                trades.get(1).add(new RandomTradeBuilder(16, 4, 0.05F)
                        .setEmeraldPrice(7, 11)
                        .setForSale((rand) -> new ItemStack(ModBlocks.COMPRESSED_IRON_BLOCK.get()))
                        .build()
                );
                trades.get(1).add(new RandomTradeBuilder(16, 2, 0.05F)
                        .setEmeraldPrice(1, 2)
                        .setForSale((rand) -> new ItemStack(ModBlocks.PRESSURE_TUBE.get(), 8))
                        .build()
                );
                trades.get(1).add(new RandomTradeBuilder(16, 4, 0.05F)
                        .setEmeraldPrice(3, 12)
                        .setForSale((rand) -> new ItemStack(ModItems.AIR_CANISTER.get(), 1))
                        .build()
                );
                trades.get(2).add(new RandomTradeBuilder(16, 4, 0.05F)
                        .setEmeraldPrice(2, 6)
                        .setForSale((rand) -> new ItemStack(ModItems.LOGISTICS_CORE.get(), 4))
                        .build()
                );
                trades.get(2).add(new RandomTradeBuilder(16, 6, 0.05f)
                        .setEmeraldPriceFor(3, 4, ModItems.TRANSISTOR.get(), 2)
                        .build()
                );
                trades.get(2).add(new RandomTradeBuilder(16, 6, 0.05f)
                        .setEmeraldPriceFor(3, 4, ModItems.CAPACITOR.get(), 2)
                        .build()
                );
                trades.get(2).add(new RandomTradeBuilder(16, 4, 0.05f)
                        .setEmeraldPriceFor(3, 4, ModItems.TURBINE_BLADE.get(), 3)
                        .build()
                );
                trades.get(3).add(new RandomTradeBuilder(4, 15, 0.05F)
                        .setEmeraldPriceFor(10, 20, ModItems.LOGISTICS_DRONE.get(), 1)
                        .build()
                );
                trades.get(3).add(new RandomTradeBuilder(4, 15, 0.05F)
                        .setEmeraldPriceFor(10, 20, ModItems.HARVESTING_DRONE.get(), 1)
                        .build()
                );
                trades.get(3).add(new RandomTradeBuilder(4, 15, 0.05F)
                        .setEmeraldPriceFor(10, 20, ModItems.COLLECTOR_DRONE.get(), 1)
                        .build()
                );
                trades.get(3).add(new RandomTradeBuilder(4, 15, 0.05F)
                        .setEmeraldPriceFor(10, 20, ModItems.GUARD_DRONE.get(), 1)
                        .build()
                );
                trades.get(3).add(new RandomTradeBuilder(16, 10, 0.05F)
                        .setEmeraldPriceFor(6, 12, ModItems.PNEUMATIC_CYLINDER.get(), 1)
                        .build()
                );
                trades.get(3).add(new RandomTradeBuilder(12, 10, 0.05F)
                        .setEmeraldPriceFor(8, 16, ModItems.GUN_AMMO.get(), 1)
                        .build()
                );
                trades.get(4).add(new RandomTradeBuilder(6, 12, 0.05F)
                        .setEmeraldPriceFor(10, 20, ModItems.GUN_AMMO_FREEZING.get(), 1)
                        .build()
                );
                trades.get(4).add(new RandomTradeBuilder(6, 12, 0.05F)
                        .setEmeraldPriceFor(10, 20, ModItems.GUN_AMMO_WEIGHTED.get(), 1)
                        .build()
                );
                trades.get(4).add(new RandomTradeBuilder(6, 12, 0.05F)
                        .setEmeraldPriceFor(10, 20, ModItems.GUN_AMMO_AP.get(), 1)
                        .build()
                );
                trades.get(4).add(new RandomTradeBuilder(6, 12, 0.05F)
                        .setEmeraldPriceFor(10, 20, ModItems.GUN_AMMO_EXPLOSIVE.get(), 1)
                        .build()
                );
                trades.get(4).add(new RandomTradeBuilder(6, 12, 0.05F)
                        .setEmeraldPriceFor(10, 20, ModItems.GUN_AMMO_INCENDIARY.get(), 1)
                        .build()
                );
                trades.get(4).add(new RandomTradeBuilder(8, 15, 0.05F)
                        .setEmeraldPriceFor(25, 40, ModItems.PROGRAMMING_PUZZLE.get(), 16)
                        .build()
                );
                trades.get(5).add(new RandomTradeBuilder(2, 10, 0.05F)
                        .setEmeraldPriceFor(11, 25, ModItems.NUKE_VIRUS.get(), 1)
                        .build()
                );
                trades.get(5).add(new RandomTradeBuilder(2, 10, 0.05F)
                        .setEmeraldPriceFor(11, 25, ModItems.STOP_WORM.get(), 1)
                        .build()
                );
                trades.get(5).add(new RandomTradeBuilder(6, 15, 0.05F)
                        .setEmeraldPriceFor(25, 40, ModItems.PRINTED_CIRCUIT_BOARD.get(), 1)
                        .build()
                );
                trades.get(5).add(new RandomTradeBuilder(3, 15, 0.05F)
                        .setEmeraldPriceFor(15, 35, ModItems.MICROMISSILES.get(), 1)
                        .build()
                );
                trades.get(5).add(new RandomTradeBuilder(4, 15, 0.05F)
                        .setEmeraldPriceFor(30, 40, ModItems.DRONE.get(), 1)
                        .build()
                );
            }
        }
    }
}
