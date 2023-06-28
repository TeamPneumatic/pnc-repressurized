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
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.advancements.CustomTrigger;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.advancements.AdvancementRewards.Builder.experience;

public class ModAdvancementProvider extends ForgeAdvancementProvider {
    private static final ResourceLocation BACKGROUND_TEXTURE = RL("textures/gui/advancement_bg.png");

    public ModAdvancementProvider(DataGenerator generatorIn, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(generatorIn.getPackOutput(), lookupProvider, existingFileHelper, List.of(new PNCAdvancements()));
    }

    private static class PNCAdvancements implements AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<Advancement> saver, ExistingFileHelper existingFileHelper) {
            Advancement root = itemAdvancement("root", FrameType.TASK, ModItems.COMPRESSED_IRON_INGOT.get())
                    .rewards(experience(10))
                    .save(saver, id("root"));

            // oil tree
            Advancement oilBucket = itemAdvancement("oil_bucket", FrameType.TASK, ModItems.OIL_BUCKET.get())
                    .parent(root)
                    .save(saver, id("oil_bucket"));
            Advancement refinery = itemAdvancement("refinery", FrameType.GOAL, ModBlocks.REFINERY.get(),
                    new ItemPredicate[] {
                            itemPredicate(ModBlocks.REFINERY.get(), 1),
                            itemPredicate(ModBlocks.REFINERY_OUTPUT.get(), 2),
                    })
                    .parent(oilBucket)
                    .rewards(experience(20))
                    .save(saver, id("refinery"));
            itemAdvancement("liquid_compressor", FrameType.TASK, ModBlocks.LIQUID_COMPRESSOR.get())
                    .parent(refinery)
                    .rewards(experience(10))
                    .save(saver, id("liquid_compressor"));
            Advancement vortexTube = itemAdvancement("vortex_tube", FrameType.TASK, ModBlocks.VORTEX_TUBE.get())
                    .parent(refinery)
                    .save(saver, id("vortex_tube"));
            Advancement lpgBucket = itemAdvancement("lpg_bucket", FrameType.TASK, ModItems.LPG_BUCKET.get())
                    .parent(vortexTube)
                    .rewards(experience(20))
                    .save(saver, id("lpg_bucket"));
            Advancement tpp = itemAdvancement("tp_plant", FrameType.TASK, ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get())
                    .parent(lpgBucket)
                    .rewards(experience(10))
                    .save(saver, id("tp_plant"));
            Advancement plastic = itemAdvancement("plastic", FrameType.GOAL, ModItems.PLASTIC.get())
                    .parent(tpp)
                    .rewards(experience(10))
                    .save(saver, id("plastic"));
            Advancement jackhammer = itemAdvancement("jackhammer", FrameType.GOAL, ModItems.JACKHAMMER.get())
                    .parent(plastic)
                    .rewards(experience(20))
                    .save(saver, id("jackhammer"));
            itemAdvancement("drill_bit_netherite", FrameType.CHALLENGE, ModItems.NETHERITE_DRILL_BIT.get())
                    .parent(jackhammer)
                    .rewards(experience(50))
                    .save(saver, id("drill_bit_netherite"));
            Advancement amadronTablet = itemAdvancement("amadron_tablet", FrameType.TASK, ModItems.AMADRON_TABLET.get())
                    .parent(plastic)
                    .save(saver, id("amadron_tablet"));
            Advancement pcbBlueprint = itemAdvancement("pcb_blueprint", FrameType.TASK, ModItems.PCB_BLUEPRINT.get())
                    .parent(amadronTablet)
                    .rewards(experience(20))
                    .save(saver, id("pcb_blueprint"));
            itemAdvancement("uv_light_box", FrameType.TASK, ModBlocks.UV_LIGHT_BOX.get())
                    .parent(pcbBlueprint)
                    .save(saver, id("uv_light_box"));
            Advancement lubricant = itemAdvancement("lubricant_bucket", FrameType.TASK, ModItems.LUBRICANT_BUCKET.get())
                    .parent(tpp)
                    .save(saver, id("lubricant_bucket"));
            itemAdvancement("speed_upgrade", FrameType.TASK, ModUpgrades.SPEED.get().getItem())
                    .parent(lubricant)
                    .rewards(experience(15))
                    .save(saver, id("speed_upgrade"));
            Advancement yeast = itemAdvancement("yeast_culture", FrameType.TASK, ModItems.YEAST_CULTURE_BUCKET.get())
                    .parent(tpp)
                    .rewards(experience(10))
                    .save(saver, id("yeast_culture"));
            Advancement ethanol = itemAdvancement("ethanol", FrameType.TASK, ModItems.ETHANOL_BUCKET.get())
                    .parent(yeast)
                    .rewards(experience(10))
                    .save(saver, id("ethanol"));
            itemAdvancement("biodiesel", FrameType.GOAL, ModItems.BIODIESEL_BUCKET.get())
                    .parent(ethanol)
                    .rewards(experience(25))
                    .save(saver, id("biodiesel"));

            // pressure tube tree
            Advancement pressureTube = itemAdvancement("pressure_tube", FrameType.TASK, ModBlocks.PRESSURE_TUBE.get())
                    .parent(root)
                    .save(saver, id("pressure_tube"));
            itemAdvancement("air_compressor", FrameType.TASK, ModBlocks.AIR_COMPRESSOR.get())
                    .parent(pressureTube)
                    .rewards(experience(10))
                    .save(saver, id("air_compressor"));
            itemAdvancement("minigun", FrameType.TASK, ModItems.MINIGUN.get())
                    .parent(pressureTube)
                    .rewards(experience(10))
                    .save(saver, id("minigun"));
            Advancement wrench = itemAdvancement("pneumatic_wrench", FrameType.TASK, ModItems.PNEUMATIC_WRENCH.get(),
                    new ItemPredicate[] {
                            itemPredicateNoNBT(ModItems.PNEUMATIC_WRENCH.get(), 1)
                    })
                    .parent(pressureTube)
                    .rewards(experience(10))
                    .save(saver, id("pneumatic_wrench"));
            customAdvancement(AdvancementTriggers.CHARGED_WRENCH, "pneumatic_wrench_charged", FrameType.TASK, ModItems.PNEUMATIC_WRENCH.get())
                    .parent(wrench)
                    .rewards(experience(10))
                    .save(saver, id("pneumatic_wrench_charged"));
            customAdvancement(AdvancementTriggers.MACHINE_VANDAL, "machine_vandal", FrameType.TASK, Items.IRON_PICKAXE)
                    .parent(wrench)
                    .save(saver, id("machine_vandal"));

            // logistics tree
            Advancement frames = itemAdvancement("logistics_frame", FrameType.TASK, ModItems.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(),
                    new ItemPredicate[] {
                            itemPredicate(ModItems.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(), 1),
                            itemPredicate(ModItems.LOGISTICS_FRAME_REQUESTER.get(), 1),
                    })
                    .parent(root)
                    .rewards(experience(20))
                    .save(saver, id("logistics_frame"));
            Advancement configurator = itemAdvancement("logistics_configurator", FrameType.TASK, ModItems.LOGISTICS_CONFIGURATOR.get())
                    .parent(frames)
                    .rewards(experience(10))
                    .save(saver, id("logistics_configurator"));
            customAdvancement(AdvancementTriggers.LOGISTICS_DRONE_DEPLOYED, "logistics_drone", FrameType.GOAL, ModItems.LOGISTICS_DRONE.get())
                    .parent(configurator)
                    .rewards(experience(10))
                    .save(saver, id("logistics_drone"));

            // pressure chamber tree
            Advancement pressureChamber = customAdvancement(AdvancementTriggers.PRESSURE_CHAMBER, "pressure_chamber", FrameType.GOAL, ModBlocks.PRESSURE_CHAMBER_WALL.get())
                    .parent(root)
                    .rewards(experience(20))
                    .save(saver, id("pressure_chamber"));
            Advancement etchingAcid = itemAdvancement("etchacid_bucket", FrameType.TASK, ModItems.ETCHING_ACID_BUCKET.get())
                    .parent(pressureChamber)
                    .save(saver, id("etchacid_bucket"));
            Advancement emptyPCB = itemAdvancement("empty_pcb", FrameType.TASK, ModItems.EMPTY_PCB.get())
                    .parent(etchingAcid)
                    .save(saver, id("empty_pcb"));
            Advancement unassembledPCB = itemAdvancement("unassembled_pcb", FrameType.TASK, ModItems.UNASSEMBLED_PCB.get())
                    .parent(emptyPCB)
                    .save(saver, id("unassembled_pcb"));
            Advancement pcb = itemAdvancement("printed_circuit_board", FrameType.GOAL, ModItems.PRINTED_CIRCUIT_BOARD.get())
                    .parent(unassembledPCB)
                    .rewards(experience(20))
                    .save(saver, id("printed_circuit_board"));
            // armor subtree
            Advancement armor = customAdvancement(AdvancementTriggers.PNEUMATIC_ARMOR, "pneumatic_armor", FrameType.TASK, ModItems.PNEUMATIC_HELMET.get())
                    .parent(pcb)
                    .rewards(experience(20))
                    .save(saver, id("pneumatic_armor"));
            Advancement jetBoots = customAdvancement(AdvancementTriggers.FLIGHT, "flight", FrameType.CHALLENGE, ModUpgrades.JET_BOOTS.get().getItem())
                    .parent(armor)
                    .rewards(experience(50))
                    .save(saver, id("flight"));
            customAdvancement(AdvancementTriggers.FLY_INTO_WALL, "fly_into_wall", FrameType.TASK, Blocks.BRICKS)
                    .parent(jetBoots)
                    .save(saver, id("fly_into_wall"));
            customAdvancement(AdvancementTriggers.BLOCK_HACK, "block_hack", FrameType.TASK, ModUpgrades.BLOCK_TRACKER.get().getItem())
                    .parent(armor)
                    .rewards(experience(10))
                    .save(saver, id("block_hack"));
            customAdvancement(AdvancementTriggers.ENTITY_HACK, "entity_hack", FrameType.TASK, ModUpgrades.ENTITY_TRACKER.get().getItem())
                    .parent(armor)
                    .rewards(experience(10))
                    .save(saver, id("entity_hack"));
            // assembly line subtree
            Advancement assembly = itemAdvancement("assembly_controller", FrameType.GOAL, ModBlocks.ASSEMBLY_CONTROLLER.get())
                    .parent(pcb)
                    .rewards(experience(30))
                    .save(saver, id("assembly_controller"));
            Advancement advancedTube = itemAdvancement("advanced_pressure_tube", FrameType.TASK, ModBlocks.ADVANCED_PRESSURE_TUBE.get())
                    .parent(assembly)
                    .rewards(experience(10))
                    .save(saver, id("advanced_pressure_tube"));
            itemAdvancement("aerial_interface", FrameType.TASK, ModBlocks.AERIAL_INTERFACE.get())
                    .parent(advancedTube)
                    .rewards(experience(10))
                    .save(saver, id("aerial_interface"));
            itemAdvancement("programmable_controller", FrameType.TASK, ModBlocks.PROGRAMMABLE_CONTROLLER.get())
                    .parent(advancedTube)
                    .rewards(experience(10))
                    .save(saver, id("programmable_controller"));
            itemAdvancement("flux_compressor", FrameType.TASK, ModBlocks.FLUX_COMPRESSOR.get())
                    .parent(advancedTube)
                    .rewards(experience(10))
                    .save(saver, id("flux_compressor"));
            itemAdvancement("aphorism_tile", FrameType.TASK, ModBlocks.APHORISM_TILE.get())
                    .parent(assembly)
                    .save(saver, id("aphorism_tile"));
            // programmer subtree
            Advancement programmer = itemAdvancement("programmer", FrameType.TASK, ModBlocks.PROGRAMMER.get())
                    .parent(pcb)
                    .save(saver, id("programmer"));
            Advancement puzzle = itemAdvancement("programming_puzzle", FrameType.TASK, ModItems.PROGRAMMING_PUZZLE.get())
                    .parent(programmer)
                    .save(saver, id("programming_puzzle"));
            customAdvancement(AdvancementTriggers.PROGRAM_DRONE, "program_drone", FrameType.CHALLENGE, ModItems.DRONE.get())
                    .parent(puzzle)
                    .rewards(experience(50))
                    .save(saver, id("program_drone"));
        }

        private static String id(String s) {
            return Names.MOD_ID + ":" + s;
        }

        private Advancement.Builder customAdvancement(CustomTrigger trigger, String name, FrameType type, ItemLike itemDisp) {
            return Advancement.Builder.advancement()
                    .display(itemDisp,
                            xlate("pneumaticcraft.advancement." + name),
                            xlate("pneumaticcraft.advancement." + name + ".desc"),
                            BACKGROUND_TEXTURE, type, true, true, false)
                    .addCriterion("0", trigger.getInstance());
        }
        private Advancement.Builder itemAdvancement(String name, FrameType type, ItemLike... items) {
            Validate.isTrue(items.length > 0);
            return Advancement.Builder.advancement()
                    .display(items[0],
                            xlate("pneumaticcraft.advancement." + name),
                            xlate("pneumaticcraft.advancement." + name + ".desc"),
                            BACKGROUND_TEXTURE, type, true, true, false)
                    .addCriterion("0", InventoryChangeTrigger.TriggerInstance.hasItems(items));
        }

        private Advancement.Builder itemAdvancement(String name, FrameType type, ItemLike item, ItemPredicate[] predicates) {
            return Advancement.Builder.advancement()
                    .display(item,
                            xlate("pneumaticcraft.advancement." + name),
                            xlate("pneumaticcraft.advancement." + name + ".desc"),
                            BACKGROUND_TEXTURE, type, true, true, false)
                    .addCriterion("0", InventoryChangeTrigger.TriggerInstance.hasItems(predicates));
        }

        private ItemPredicate itemPredicate(ItemLike item, int minCount) {
            return new ItemPredicate(null, Collections.singleton(item.asItem()), MinMaxBounds.Ints.atLeast(minCount), MinMaxBounds.Ints.ANY,
                    new EnchantmentPredicate[0], new EnchantmentPredicate[0], null, NbtPredicate.ANY);
        }

        private ItemPredicate itemPredicateNoNBT(ItemLike item, int minCount) {
            return new ItemPredicate(null, Collections.singleton(item.asItem()), MinMaxBounds.Ints.atLeast(minCount), MinMaxBounds.Ints.ANY,
                    new EnchantmentPredicate[0], new EnchantmentPredicate[0], null, new NbtPredicate(null));
        }
    }
}
