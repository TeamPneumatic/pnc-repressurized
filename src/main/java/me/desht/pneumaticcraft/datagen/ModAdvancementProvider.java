package me.desht.pneumaticcraft.datagen;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.advancements.CustomTrigger;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.criterion.*;
import net.minecraft.block.Blocks;
import net.minecraft.data.AdvancementProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static net.minecraft.advancements.AdvancementRewards.Builder.experience;

public class ModAdvancementProvider extends AdvancementProvider {
    private static final ResourceLocation BACKGROUND_TEXTURE = RL("textures/gui/advancement_bg.png");
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private final DataGenerator generator;

    public ModAdvancementProvider(DataGenerator generatorIn) {
        super(generatorIn);
        this.generator = generatorIn;
    }

    @Override
    public void act(DirectoryCache cache) throws IOException {
        Path path = this.generator.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();
        Consumer<Advancement> consumer = (advancement) -> {
            if (!set.add(advancement.getId())) {
                throw new IllegalStateException("Duplicate advancement " + advancement.getId());
            } else {
                Path path1 = getPath(path, advancement);
                try {
                    IDataProvider.save(GSON, cache, advancement.copy().serialize(), path1);
                } catch (IOException e) {
                    Log.error("Couldn't save advancement {}", path1, e);
                }
            }
        };
        this.register(consumer);
    }

    private static Path getPath(Path pathIn, Advancement advancementIn) {
        return pathIn.resolve("data/" + advancementIn.getId().getNamespace() + "/advancements/" + advancementIn.getId().getPath() + ".json");
    }

    private static String id(String s) {
        return Names.MOD_ID + ":" + s;
    }

    public void register(Consumer<Advancement> t) {
        Advancement root = itemAdvancement("root", FrameType.TASK, ModItems.COMPRESSED_IRON_INGOT.get())
                .withRewards(experience(10))
                .register(t, id("root"));

        // oil tree
        Advancement oilBucket = itemAdvancement("oil_bucket", FrameType.TASK, ModItems.OIL_BUCKET.get())
                .withParent(root)
                .register(t, id("oil_bucket"));
        Advancement refinery = itemAdvancement("refinery", FrameType.GOAL, ModBlocks.REFINERY.get(),
                new ItemPredicate[] {
                        itemPredicate(ModBlocks.REFINERY.get(), 1),
                        itemPredicate(ModBlocks.REFINERY_OUTPUT.get(), 2),
                })
                .withParent(oilBucket)
                .withRewards(experience(20))
                .register(t, id("refinery"));
        itemAdvancement("liquid_compressor", FrameType.TASK, ModBlocks.LIQUID_COMPRESSOR.get())
                .withParent(refinery)
                .withRewards(experience(10))
                .register(t, id("liquid_compressor"));
        Advancement vortexTube = itemAdvancement("vortex_tube", FrameType.TASK, ModBlocks.VORTEX_TUBE.get())
                .withParent(refinery)
                .register(t, id("vortex_tube"));
        Advancement lpgBucket = itemAdvancement("lpg_bucket", FrameType.TASK, ModItems.LPG_BUCKET.get())
                .withParent(vortexTube)
                .withRewards(experience(20))
                .register(t, id("lpg_bucket"));
        Advancement tpp = itemAdvancement("tp_plant", FrameType.TASK, ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get())
                .withParent(lpgBucket)
                .withRewards(experience(10))
                .register(t, id("tp_plant"));
        Advancement plastic = itemAdvancement("plastic", FrameType.GOAL, ModItems.PLASTIC.get())
                .withParent(tpp)
                .withRewards(experience(10))
                .register(t, id("plastic"));
        Advancement jackhammer = itemAdvancement("jackhammer", FrameType.GOAL, ModItems.JACKHAMMER.get())
                .withParent(plastic)
                .withRewards(experience(20))
                .register(t, id("jackhammer"));
        itemAdvancement("drill_bit_netherite", FrameType.CHALLENGE, ModItems.NETHERITE_DRILL_BIT.get())
                .withParent(jackhammer)
                .withRewards(experience(50))
                .register(t, id("drill_bit_netherite"));
        Advancement amadronTablet = itemAdvancement("amadron_tablet", FrameType.TASK, ModItems.AMADRON_TABLET.get())
                .withParent(plastic)
                .register(t, id("amadron_tablet"));
        Advancement pcbBlueprint = itemAdvancement("pcb_blueprint", FrameType.TASK, ModItems.PCB_BLUEPRINT.get())
                .withParent(amadronTablet)
                .withRewards(experience(20))
                .register(t, id("pcb_blueprint"));
        itemAdvancement("uv_light_box", FrameType.TASK, ModBlocks.UV_LIGHT_BOX.get())
                .withParent(pcbBlueprint)
                .register(t, id("uv_light_box"));
        Advancement lubricant = itemAdvancement("lubricant_bucket", FrameType.TASK, ModItems.LUBRICANT_BUCKET.get())
                .withParent(tpp)
                .register(t, id("lubricant_bucket"));
        itemAdvancement("speed_upgrade", FrameType.TASK, EnumUpgrade.SPEED.getItem())
                .withParent(lubricant)
                .withRewards(experience(15))
                .register(t, id("speed_upgrade"));
        Advancement yeast = itemAdvancement("yeast_culture", FrameType.TASK, ModItems.YEAST_CULTURE_BUCKET.get())
                .withParent(tpp)
                .withRewards(experience(10))
                .register(t, id("yeast_culture"));
        Advancement ethanol = itemAdvancement("ethanol", FrameType.TASK, ModItems.ETHANOL_BUCKET.get())
                .withParent(yeast)
                .withRewards(experience(10))
                .register(t, id("ethanol"));
        itemAdvancement("biodiesel", FrameType.GOAL, ModItems.BIODIESEL_BUCKET.get())
                .withParent(ethanol)
                .withRewards(experience(25))
                .register(t, id("biodiesel"));

        // pressure tube tree
        Advancement pressureTube = itemAdvancement("pressure_tube", FrameType.TASK, ModBlocks.PRESSURE_TUBE.get())
                .withParent(root)
                .register(t, id("pressure_tube"));
        itemAdvancement("air_compressor", FrameType.TASK, ModBlocks.AIR_COMPRESSOR.get())
                .withParent(pressureTube)
                .withRewards(experience(10))
                .register(t, id("air_compressor"));
        itemAdvancement("minigun", FrameType.TASK, ModItems.MINIGUN.get())
                .withParent(pressureTube)
                .withRewards(experience(10))
                .register(t, id("minigun"));
        Advancement wrench = itemAdvancement("pneumatic_wrench", FrameType.TASK, ModItems.PNEUMATIC_WRENCH.get(),
                new ItemPredicate[] {
                        itemPredicateNoNBT(ModItems.PNEUMATIC_WRENCH.get(), 1)
                })
                .withParent(pressureTube)
                .withRewards(experience(10))
                .register(t, id("pneumatic_wrench"));
        customAdvancement(AdvancementTriggers.CHARGED_WRENCH, "pneumatic_wrench_charged", FrameType.TASK, ModItems.PNEUMATIC_WRENCH.get())
                .withParent(wrench)
                .withRewards(experience(10))
                .register(t, id("pneumatic_wrench_charged"));
        customAdvancement(AdvancementTriggers.MACHINE_VANDAL, "machine_vandal", FrameType.TASK, Items.IRON_PICKAXE)
                .withParent(wrench)
                .register(t, id("machine_vandal"));

        // logistics tree
        Advancement frames = itemAdvancement("logistics_frame", FrameType.TASK, ModItems.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(),
                new ItemPredicate[] {
                        itemPredicate(ModItems.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(), 1),
                        itemPredicate(ModItems.LOGISTICS_FRAME_REQUESTER.get(), 1),
                })
                .withParent(root)
                .withRewards(experience(20))
                .register(t, id("logistics_frame"));
        Advancement configurator = itemAdvancement("logistics_configurator", FrameType.TASK, ModItems.LOGISTICS_CONFIGURATOR.get())
                .withParent(frames)
                .withRewards(experience(10))
                .register(t, id("logistics_configurator"));
        customAdvancement(AdvancementTriggers.LOGISTICS_DRONE_DEPLOYED, "logistics_drone", FrameType.GOAL, ModItems.LOGISTICS_DRONE.get())
                .withParent(configurator)
                .withRewards(experience(10))
                .register(t, id("logistics_drone"));

        // pressure chamber tree
        Advancement pressureChamber = customAdvancement(AdvancementTriggers.PRESSURE_CHAMBER, "pressure_chamber", FrameType.GOAL, ModBlocks.PRESSURE_CHAMBER_WALL.get())
                .withParent(root)
                .withRewards(experience(20))
                .register(t, id("pressure_chamber"));
        Advancement etchingAcid = itemAdvancement("etchacid_bucket", FrameType.TASK, ModItems.ETCHING_ACID_BUCKET.get())
                .withParent(pressureChamber)
                .register(t, id("etchacid_bucket"));
        Advancement emptyPCB = itemAdvancement("empty_pcb", FrameType.TASK, ModItems.EMPTY_PCB.get())
                .withParent(etchingAcid)
                .register(t, id("empty_pcb"));
        Advancement unassembledPCB = itemAdvancement("unassembled_pcb", FrameType.TASK, ModItems.UNASSEMBLED_PCB.get())
                .withParent(emptyPCB)
                .register(t, id("unassembled_pcb"));
        Advancement pcb = itemAdvancement("printed_circuit_board", FrameType.GOAL, ModItems.PRINTED_CIRCUIT_BOARD.get())
                .withParent(unassembledPCB)
                .withRewards(experience(20))
                .register(t, id("printed_circuit_board"));
        // armor subtree
        Advancement armor = customAdvancement(AdvancementTriggers.PNEUMATIC_ARMOR, "pneumatic_armor", FrameType.TASK, ModItems.PNEUMATIC_HELMET.get())
                .withParent(pcb)
                .withRewards(experience(20))
                .register(t, id("pneumatic_armor"));
        Advancement jetBoots = customAdvancement(AdvancementTriggers.FLIGHT, "flight", FrameType.CHALLENGE, EnumUpgrade.JET_BOOTS.getItem())
                .withParent(armor)
                .withRewards(experience(50))
                .register(t, id("flight"));
        customAdvancement(AdvancementTriggers.FLY_INTO_WALL, "fly_into_wall", FrameType.TASK, Blocks.BRICKS)
                .withParent(jetBoots)
                .register(t, id("fly_into_wall"));
        customAdvancement(AdvancementTriggers.BLOCK_HACK, "block_hack", FrameType.TASK, EnumUpgrade.BLOCK_TRACKER.getItem())
                .withParent(armor)
                .withRewards(experience(10))
                .register(t, id("block_hack"));
        customAdvancement(AdvancementTriggers.ENTITY_HACK, "entity_hack", FrameType.TASK, EnumUpgrade.ENTITY_TRACKER.getItem())
                .withParent(armor)
                .withRewards(experience(10))
                .register(t, id("entity_hack"));
        // assembly line subtree
        Advancement assembly = itemAdvancement("assembly_controller", FrameType.GOAL, ModBlocks.ASSEMBLY_CONTROLLER.get())
                .withParent(pcb)
                .withRewards(experience(30))
                .register(t, id("assembly_controller"));
        Advancement advancedTube = itemAdvancement("advanced_pressure_tube", FrameType.TASK, ModBlocks.ADVANCED_PRESSURE_TUBE.get())
                .withParent(assembly)
                .withRewards(experience(10))
                .register(t, id("advanced_pressure_tube"));
        itemAdvancement("aerial_interface", FrameType.TASK, ModBlocks.AERIAL_INTERFACE.get())
                .withParent(advancedTube)
                .withRewards(experience(10))
                .register(t, id("aerial_interface"));
        itemAdvancement("programmable_controller", FrameType.TASK, ModBlocks.PROGRAMMABLE_CONTROLLER.get())
                .withParent(advancedTube)
                .withRewards(experience(10))
                .register(t, id("programmable_controller"));
        itemAdvancement("flux_compressor", FrameType.TASK, ModBlocks.FLUX_COMPRESSOR.get())
                .withParent(advancedTube)
                .withRewards(experience(10))
                .register(t, id("flux_compressor"));
        itemAdvancement("aphorism_tile", FrameType.TASK, ModBlocks.APHORISM_TILE.get())
                .withParent(assembly)
                .register(t, id("aphorism_tile"));
        // programmer subtree
        Advancement programmer = itemAdvancement("programmer", FrameType.TASK, ModBlocks.PROGRAMMER.get())
                .withParent(pcb)
                .register(t, id("programmer"));
        Advancement puzzle = itemAdvancement("programming_puzzle", FrameType.TASK, ModItems.PROGRAMMING_PUZZLE.get())
                .withParent(programmer)
                .register(t, id("programming_puzzle"));
        customAdvancement(AdvancementTriggers.PROGRAM_DRONE, "program_drone", FrameType.CHALLENGE, ModItems.DRONE.get())
                .withParent(puzzle)
                .withRewards(experience(50))
                .register(t, id("program_drone"));
    }

    /***************************************
     * Helpers
     */

    private Advancement.Builder customAdvancement(CustomTrigger trigger, String name, FrameType type, IItemProvider itemDisp) {
        return Advancement.Builder.builder()
                .withDisplay(itemDisp,
                        xlate("pneumaticcraft.advancement." + name),
                        xlate("pneumaticcraft.advancement." + name + ".desc"),
                        BACKGROUND_TEXTURE, type, true, true, false)
                .withCriterion("0", trigger.getInstance());
    }
    private Advancement.Builder itemAdvancement(String name, FrameType type, IItemProvider... items) {
        Validate.isTrue(items.length > 0);
        return Advancement.Builder.builder()
                .withDisplay(items[0],
                        xlate("pneumaticcraft.advancement." + name),
                        xlate("pneumaticcraft.advancement." + name + ".desc"),
                        BACKGROUND_TEXTURE, type, true, true, false)
                .withCriterion("0", InventoryChangeTrigger.Instance.forItems(items));
    }

    private Advancement.Builder itemAdvancement(String name, FrameType type, IItemProvider item, ItemPredicate[] predicates) {
        return Advancement.Builder.builder()
                .withDisplay(item,
                        xlate("pneumaticcraft.advancement." + name),
                        xlate("pneumaticcraft.advancement." + name + ".desc"),
                        BACKGROUND_TEXTURE, type, true, true, false)
                .withCriterion("0", InventoryChangeTrigger.Instance.forItems(predicates));
    }

    private ItemPredicate itemPredicate(IItemProvider item, int minCount) {
        return new ItemPredicate(null, item.asItem(), MinMaxBounds.IntBound.atLeast(minCount), MinMaxBounds.IntBound.UNBOUNDED,
                new EnchantmentPredicate[0], new EnchantmentPredicate[0], null, NBTPredicate.ANY);
    }

    private ItemPredicate itemPredicateNoNBT(IItemProvider item, int minCount) {
        return new ItemPredicate(null, item.asItem(), MinMaxBounds.IntBound.atLeast(minCount), MinMaxBounds.IntBound.UNBOUNDED,
                new EnchantmentPredicate[0], new EnchantmentPredicate[0], null, new NBTPredicate(null));
    }
}
