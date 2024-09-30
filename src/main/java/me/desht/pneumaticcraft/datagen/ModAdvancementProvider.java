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

package me.desht.pneumaticcraft.datagen;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.advancements.CustomTrigger;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModCriterionTriggers;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.advancements.AdvancementRewards.Builder.experience;

public class ModAdvancementProvider extends AdvancementProvider {
    private static final ResourceLocation BACKGROUND_TEXTURE = RL("textures/gui/advancement_bg.png");

    public ModAdvancementProvider(DataGenerator generatorIn, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(generatorIn.getPackOutput(), lookupProvider, existingFileHelper, List.of(new PNCAdvancements()));
    }

    private static class PNCAdvancements implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            AdvancementHolder root = itemAdvancement("root", AdvancementType.TASK, ModItems.COMPRESSED_IRON_INGOT.get())
                    .rewards(experience(10))
                    .save(saver, id("root"));

            // oil tree
            AdvancementHolder oilBucket = itemAdvancement("oil_bucket", AdvancementType.TASK, ModItems.OIL_BUCKET.get())
                    .parent(root)
                    .save(saver, id("oil_bucket"));
            AdvancementHolder refinery = itemAdvancement("refinery", AdvancementType.GOAL, ModBlocks.REFINERY.get(),
                    new ItemPredicate[] {
                            itemPredicate(ModBlocks.REFINERY.get(), 1),
                            itemPredicate(ModBlocks.REFINERY_OUTPUT.get(), 2),
                    })
                    .parent(oilBucket)
                    .rewards(experience(20))
                    .save(saver, id("refinery"));
            itemAdvancement("liquid_compressor", AdvancementType.TASK, ModBlocks.LIQUID_COMPRESSOR.get())
                    .parent(refinery)
                    .rewards(experience(10))
                    .save(saver, id("liquid_compressor"));
            AdvancementHolder vortexTube = itemAdvancement("vortex_tube", AdvancementType.TASK, ModBlocks.VORTEX_TUBE.get())
                    .parent(refinery)
                    .save(saver, id("vortex_tube"));
            AdvancementHolder lpgBucket = itemAdvancement("lpg_bucket", AdvancementType.TASK, ModItems.LPG_BUCKET.get())
                    .parent(vortexTube)
                    .rewards(experience(20))
                    .save(saver, id("lpg_bucket"));
            AdvancementHolder tpp = itemAdvancement("tp_plant", AdvancementType.TASK, ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get())
                    .parent(lpgBucket)
                    .rewards(experience(10))
                    .save(saver, id("tp_plant"));
            AdvancementHolder plastic = itemAdvancement("plastic", AdvancementType.GOAL, ModItems.PLASTIC.get())
                    .parent(tpp)
                    .rewards(experience(10))
                    .save(saver, id("plastic"));
            AdvancementHolder jackhammer = itemAdvancement("jackhammer", AdvancementType.GOAL, ModItems.JACKHAMMER.get())
                    .parent(plastic)
                    .rewards(experience(20))
                    .save(saver, id("jackhammer"));
            itemAdvancement("drill_bit_netherite", AdvancementType.CHALLENGE, ModItems.NETHERITE_DRILL_BIT.get())
                    .parent(jackhammer)
                    .rewards(experience(50))
                    .save(saver, id("drill_bit_netherite"));
            AdvancementHolder amadronTablet = itemAdvancement("amadron_tablet", AdvancementType.TASK, ModItems.AMADRON_TABLET.get())
                    .parent(plastic)
                    .save(saver, id("amadron_tablet"));
            AdvancementHolder pcbBlueprint = itemAdvancement("pcb_blueprint", AdvancementType.TASK, ModItems.PCB_BLUEPRINT.get())
                    .parent(amadronTablet)
                    .rewards(experience(20))
                    .save(saver, id("pcb_blueprint"));
            itemAdvancement("uv_light_box", AdvancementType.TASK, ModBlocks.UV_LIGHT_BOX.get())
                    .parent(pcbBlueprint)
                    .save(saver, id("uv_light_box"));
            AdvancementHolder lubricant = itemAdvancement("lubricant_bucket", AdvancementType.TASK, ModItems.LUBRICANT_BUCKET.get())
                    .parent(tpp)
                    .save(saver, id("lubricant_bucket"));
            itemAdvancement("speed_upgrade", AdvancementType.TASK, ModUpgrades.SPEED.get().getItem())
                    .parent(lubricant)
                    .rewards(experience(15))
                    .save(saver, id("speed_upgrade"));
            AdvancementHolder yeast = itemAdvancement("yeast_culture", AdvancementType.TASK, ModItems.YEAST_CULTURE_BUCKET.get())
                    .parent(tpp)
                    .rewards(experience(10))
                    .save(saver, id("yeast_culture"));
            AdvancementHolder ethanol = itemAdvancement("ethanol", AdvancementType.TASK, ModItems.ETHANOL_BUCKET.get())
                    .parent(yeast)
                    .rewards(experience(10))
                    .save(saver, id("ethanol"));
            itemAdvancement("biodiesel", AdvancementType.GOAL, ModItems.BIODIESEL_BUCKET.get())
                    .parent(ethanol)
                    .rewards(experience(25))
                    .save(saver, id("biodiesel"));

            // pressure tube tree
            AdvancementHolder pressureTube = itemAdvancement("pressure_tube", AdvancementType.TASK, ModBlocks.PRESSURE_TUBE.get())
                    .parent(root)
                    .save(saver, id("pressure_tube"));
            itemAdvancement("air_compressor", AdvancementType.TASK, ModBlocks.AIR_COMPRESSOR.get())
                    .parent(pressureTube)
                    .rewards(experience(10))
                    .save(saver, id("air_compressor"));
            itemAdvancement("minigun", AdvancementType.TASK, ModItems.MINIGUN.get())
                    .parent(pressureTube)
                    .rewards(experience(10))
                    .save(saver, id("minigun"));
            AdvancementHolder wrench = itemAdvancement("pneumatic_wrench", AdvancementType.TASK, ModItems.PNEUMATIC_WRENCH.get(),
                    new ItemPredicate[] {
                            itemPredicate(ModItems.PNEUMATIC_WRENCH.get(), 1)
                    })
                    .parent(pressureTube)
                    .rewards(experience(10))
                    .save(saver, id("pneumatic_wrench"));
            customAdvancement(ModCriterionTriggers.CHARGED_WRENCH, AdvancementType.TASK, ModItems.PNEUMATIC_WRENCH.get())
                    .parent(wrench)
                    .rewards(experience(10))
                    .save(saver, id("pneumatic_wrench_charged"));
            customAdvancement(ModCriterionTriggers.MACHINE_VANDAL, AdvancementType.TASK, Items.IRON_PICKAXE)
                    .parent(wrench)
                    .save(saver, id("machine_vandal"));

            // logistics tree
            AdvancementHolder frames = itemAdvancement("logistics_frame", AdvancementType.TASK, ModItems.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(),
                    new ItemPredicate[] {
                            itemPredicate(ModItems.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(), 1),
                            itemPredicate(ModItems.LOGISTICS_FRAME_REQUESTER.get(), 1),
                    })
                    .parent(root)
                    .rewards(experience(20))
                    .save(saver, id("logistics_frame"));
            AdvancementHolder configurator = itemAdvancement("logistics_configurator", AdvancementType.TASK, ModItems.LOGISTICS_CONFIGURATOR.get())
                    .parent(frames)
                    .rewards(experience(10))
                    .save(saver, id("logistics_configurator"));
            customAdvancement(ModCriterionTriggers.LOGISTICS_DRONE_DEPLOYED, AdvancementType.GOAL, ModItems.LOGISTICS_DRONE.get())
                    .parent(configurator)
                    .rewards(experience(10))
                    .save(saver, id("logistics_drone"));

            // pressure chamber tree
            AdvancementHolder pressureChamber = customAdvancement(ModCriterionTriggers.PRESSURE_CHAMBER, AdvancementType.GOAL, ModBlocks.PRESSURE_CHAMBER_WALL.get())
                    .parent(root)
                    .rewards(experience(20))
                    .save(saver, id("pressure_chamber"));
            AdvancementHolder etchingAcid = itemAdvancement("etchacid_bucket", AdvancementType.TASK, ModItems.ETCHING_ACID_BUCKET.get())
                    .parent(pressureChamber)
                    .save(saver, id("etchacid_bucket"));
            AdvancementHolder emptyPCB = itemAdvancement("empty_pcb", AdvancementType.TASK, ModItems.EMPTY_PCB.get())
                    .parent(etchingAcid)
                    .save(saver, id("empty_pcb"));
            AdvancementHolder unassembledPCB = itemAdvancement("unassembled_pcb", AdvancementType.TASK, ModItems.UNASSEMBLED_PCB.get())
                    .parent(emptyPCB)
                    .save(saver, id("unassembled_pcb"));
            AdvancementHolder pcb = itemAdvancement("printed_circuit_board", AdvancementType.GOAL, ModItems.PRINTED_CIRCUIT_BOARD.get())
                    .parent(unassembledPCB)
                    .rewards(experience(20))
                    .save(saver, id("printed_circuit_board"));
            // armor subtree
            AdvancementHolder armor = customAdvancement(ModCriterionTriggers.PNEUMATIC_ARMOR, AdvancementType.TASK, ModItems.PNEUMATIC_HELMET.get())
                    .parent(pcb)
                    .rewards(experience(20))
                    .save(saver, id("pneumatic_armor"));
            AdvancementHolder jetBoots = customAdvancement(ModCriterionTriggers.FLIGHT, AdvancementType.CHALLENGE, ModUpgrades.JET_BOOTS.get().getItem())
                    .parent(armor)
                    .rewards(experience(50))
                    .save(saver, id("flight"));
            customAdvancement(ModCriterionTriggers.FLY_INTO_WALL, AdvancementType.TASK, Blocks.BRICKS)
                    .parent(jetBoots)
                    .save(saver, id("fly_into_wall"));
            customAdvancement(ModCriterionTriggers.BLOCK_HACK, AdvancementType.TASK, ModUpgrades.BLOCK_TRACKER.get().getItem())
                    .parent(armor)
                    .rewards(experience(10))
                    .save(saver, id("block_hack"));
            customAdvancement(ModCriterionTriggers.ENTITY_HACK, AdvancementType.TASK, ModUpgrades.ENTITY_TRACKER.get().getItem())
                    .parent(armor)
                    .rewards(experience(10))
                    .save(saver, id("entity_hack"));
            // assembly line subtree
            AdvancementHolder assembly = itemAdvancement("assembly_controller", AdvancementType.GOAL, ModBlocks.ASSEMBLY_CONTROLLER.get())
                    .parent(pcb)
                    .rewards(experience(30))
                    .save(saver, id("assembly_controller"));
            AdvancementHolder advancedTube = itemAdvancement("advanced_pressure_tube", AdvancementType.TASK, ModBlocks.ADVANCED_PRESSURE_TUBE.get())
                    .parent(assembly)
                    .rewards(experience(10))
                    .save(saver, id("advanced_pressure_tube"));
            itemAdvancement("aerial_interface", AdvancementType.TASK, ModBlocks.AERIAL_INTERFACE.get())
                    .parent(advancedTube)
                    .rewards(experience(10))
                    .save(saver, id("aerial_interface"));
            itemAdvancement("programmable_controller", AdvancementType.TASK, ModBlocks.PROGRAMMABLE_CONTROLLER.get())
                    .parent(advancedTube)
                    .rewards(experience(10))
                    .save(saver, id("programmable_controller"));
            itemAdvancement("flux_compressor", AdvancementType.TASK, ModBlocks.FLUX_COMPRESSOR.get())
                    .parent(advancedTube)
                    .rewards(experience(10))
                    .save(saver, id("flux_compressor"));
            itemAdvancement("aphorism_tile", AdvancementType.TASK, ModBlocks.APHORISM_TILE.get())
                    .parent(assembly)
                    .save(saver, id("aphorism_tile"));
            // programmer subtree
            AdvancementHolder programmer = itemAdvancement("programmer", AdvancementType.TASK, ModBlocks.PROGRAMMER.get())
                    .parent(pcb)
                    .save(saver, id("programmer"));
            AdvancementHolder puzzle = itemAdvancement("programming_puzzle", AdvancementType.TASK, ModItems.PROGRAMMING_PUZZLE.get())
                    .parent(programmer)
                    .save(saver, id("programming_puzzle"));
            customAdvancement(ModCriterionTriggers.PROGRAM_DRONE, AdvancementType.CHALLENGE, ModItems.DRONE.get())
                    .parent(puzzle)
                    .rewards(experience(50))
                    .save(saver, id("program_drone"));
        }

        private static String id(String s) {
            return Names.MOD_ID + ":" + s;
        }

        private Advancement.Builder customAdvancement(Supplier<CustomTrigger> triggerSupplier, AdvancementType type, ItemLike itemDisp) {
            CustomTrigger trigger = triggerSupplier.get();
            String namespace = trigger.getInstance().id().getNamespace();
            String path = trigger.getInstance().id().getPath();
            return Advancement.Builder.advancement()
                    .display(itemDisp,
                            xlate(namespace + ".advancement." + path),
                            xlate(namespace + ".advancement." + path + ".desc"),
                            BACKGROUND_TEXTURE, type, true, true, false)
                    .addCriterion("0", new Criterion<>(trigger, trigger.getInstance()));
        }

        private Advancement.Builder itemAdvancement(String name, AdvancementType type, ItemLike... items) {
            Validate.isTrue(items.length > 0);
            return Advancement.Builder.advancement()
                    .display(items[0],
                            xlate("pneumaticcraft.advancement." + name),
                            xlate("pneumaticcraft.advancement." + name + ".desc"),
                            BACKGROUND_TEXTURE, type, true, true, false)
                    .addCriterion("0", InventoryChangeTrigger.TriggerInstance.hasItems(items));
        }

        private Advancement.Builder itemAdvancement(String name, AdvancementType type, ItemLike item, ItemPredicate[] predicates) {
            return Advancement.Builder.advancement()
                    .display(item,
                            xlate("pneumaticcraft.advancement." + name),
                            xlate("pneumaticcraft.advancement." + name + ".desc"),
                            BACKGROUND_TEXTURE, type, true, true, false)
                    .addCriterion("0", InventoryChangeTrigger.TriggerInstance.hasItems(predicates));
        }

        private ItemPredicate itemPredicate(ItemLike item, int minCount) {
            return ItemPredicate.Builder.item()
                    .of(item.asItem())
                    .withCount(MinMaxBounds.Ints.atLeast(minCount))
                    .build();
        }

        private ItemPredicate itemPredicateWithDurability(ItemLike item, int minCount) {
            return ItemPredicate.Builder.item()
                    .of(item.asItem())
                    .withCount(MinMaxBounds.Ints.atLeast(minCount))
                    .withSubPredicate(ItemSubPredicates.DAMAGE, ItemDamagePredicate.durability(MinMaxBounds.Ints.ANY))
                    .build();
        }

        private ItemPredicate itemPredicateNoNBT(ItemLike item, int minCount) {
            return ItemPredicate.Builder.item()
                    .of(item.asItem())
                    .withCount(MinMaxBounds.Ints.atLeast(minCount))
                    .withSubPredicate(ItemSubPredicates.DAMAGE, ItemDamagePredicate.durability(MinMaxBounds.Ints.ANY))
//                    .hasNbt(null)
                    .build();
        }

    }
}
