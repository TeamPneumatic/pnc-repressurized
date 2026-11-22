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

import com.mojang.math.Quadrant;
import me.desht.pneumaticcraft.client.model.custom.pressure_tube.PressureTubeModel;
import me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.blockstate.CustomBlockStateModelBuilder;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static me.desht.pneumaticcraft.common.registry.ModBlocks.*;
import static net.minecraft.client.data.models.BlockModelGenerators.*;

/**
 * Blockstates for all blocks, and models for the blocks which use template models (cubes, slabs, stairs, walls
 * and child models which only set textures). Models with their own elements, OBJ models and custom loader models
 * are hand-made and stay in src/main/resources.
 */
class ModBlockModelGenerators {
    private final BlockModelGenerators gen;

    // rotations which turn a model pointing up to point in the given direction
    private static final Map<Direction, VariantMutator> FROM_UP = rotations(
            NOP, X_ROT_180, X_ROT_90, X_ROT_90.then(Y_ROT_180), X_ROT_90.then(Y_ROT_270), X_ROT_90.then(Y_ROT_90));

    ModBlockModelGenerators(BlockModelGenerators gen) {
        this.gen = gen;
    }

    void run() {
        blockStates();
        blockModels();
    }

    /* ----------------------------------- blockstates ----------------------------------- */

    private void blockStates() {
        for (Supplier<? extends Block> b : new Supplier[]{
                ASSEMBLY_PLATFORM, COMPRESSED_BRICK_TILE, COMPRESSED_BRICKS, COMPRESSED_IRON_BLOCK, COMPRESSED_STONE,
                CREATIVE_COMPRESSED_IRON_BLOCK, CREATIVE_COMPRESSOR, DRILL_PIPE, ELECTROSTATIC_COMPRESSOR, EMPTY_SPAWNER,
                ETCHING_TANK, PRESSURE_CHAMBER_GLASS, PRESSURIZED_SPAWNER, REINFORCED_BRICK_TILE, REINFORCED_BRICKS,
                REINFORCED_STONE, SENTRY_TURRET, THERMAL_COMPRESSOR,
                // fluid blocks
                BIODIESEL, DIESEL, ETHANOL, GASOLINE, KEROSENE, LPG, LUBRICANT, MEMORY_ESSENCE, OIL, PLASTIC,
                VEGETABLE_OIL, YEAST_CULTURE
        }) {
            simple(b.get(), ModelLocationUtils.getModelLocation(b.get()));
        }
        for (Supplier<? extends Block> b : new Supplier[]{ASSEMBLY_DRILL, ASSEMBLY_IO_UNIT_EXPORT, ASSEMBLY_IO_UNIT_IMPORT, ASSEMBLY_LASER}) {
            simple(b.get(), block("assembly_base"));
        }
        simple(DRONE_REDSTONE_EMITTER.get(), Identifier.withDefaultNamespace("block/air"));
        simple(KEROSENE_LAMP_LIGHT.get(), Identifier.withDefaultNamespace("block/air"));
        simple(ETCHING_ACID.get(), Identifier.withDefaultNamespace("block/water"));

        pressureTube(PRESSURE_TUBE.get(), "");
        pressureTube(REINFORCED_PRESSURE_TUBE.get(), "reinforced");
        pressureTube(ADVANCED_PRESSURE_TUBE.get(), "advanced");

        // the direction each model faces when it isn't rotated depends on how the model was made
        for (Supplier<? extends Block> b : new Supplier[]{ADVANCED_LIQUID_COMPRESSOR, AIR_CANNON, LIQUID_COMPRESSOR, VACUUM_PUMP}) {
            horizontal(b.get(), Direction.EAST);
        }
        for (Supplier<? extends Block> b : new Supplier[]{AERIAL_INTERFACE, PROGRAMMABLE_CONTROLLER, REINFORCED_CHEST, SMART_CHEST, THERMOPNEUMATIC_PROCESSING_PLANT}) {
            horizontal(b.get(), Direction.NORTH);
        }
        for (Supplier<? extends Block> b : new Supplier[]{ELEVATOR_CALLER, PNEUMATIC_DOOR_BASE, PROGRAMMER, REFINERY, REFINERY_OUTPUT, SECURITY_STATION}) {
            horizontal(b.get(), Direction.SOUTH);
        }
        horizontal(MANUAL_COMPRESSOR.get(), ModelLocationUtils.getModelLocation(MANUAL_COMPRESSOR.get(), "_base"), Direction.SOUTH);

        horizontalOnOff(AIR_COMPRESSOR.get(), "on", Direction.EAST);
        horizontalOnOff(ADVANCED_AIR_COMPRESSOR.get(), "on", Direction.EAST);
        horizontalOnOff(FLUX_COMPRESSOR.get(), "active", Direction.SOUTH);
        horizontalOnOff(DRONE_INTERFACE.get(), "connected", Direction.NORTH);
        pneumaticDoor();
        solarCompressor();

        facing(APHORISM_TILE.get(), Direction.SOUTH, Quadrant.R90, Quadrant.R270);
        facing(THERMAL_LAGGING.get(), Direction.SOUTH, Quadrant.R90, Quadrant.R270);
        pressureChamberInterface();
        pressureChamberValve(PRESSURE_CHAMBER_VALVE.get());
        pressureChamberValve(REINFORCED_PRESSURE_CHAMBER_VALVE.get());
        heatSink();
        pneumaticDynamo();
        for (DyeColor color : DyeColor.values()) {
            wallLampState(wallLamp(color, false).get());
            wallLampState(wallLamp(color, true).get());
            plasticBrickState(plasticBrick(color).get(), true);
            plasticBrickState(smoothPlasticBrick(color).get(), false);
        }
        pressureChamberWall();

        axis(COMPRESSED_BRICK_PILLAR.get(), Quadrant.R270);
        axis(REINFORCED_BRICK_PILLAR.get(), Quadrant.R270);
        axis(TUBE_JUNCTION.get(), Quadrant.R90);

        stoneBlocks();

        tubeConnected(ELEVATOR_BASE.get(), false);
        tubeConnected(SPAWNER_EXTRACTOR.get(), false);
        tubeConnected(GAS_LIFT.get(), true);
        assemblyConnected(ASSEMBLY_CONTROLLER.get());
        assemblyConnected(UNIVERSAL_SENSOR.get());
        tank(TANK_SMALL.get(), "iron");
        tank(TANK_MEDIUM.get(), "gold");
        tank(TANK_LARGE.get(), "diamond");
        tank(TANK_HUGE.get(), "netherite");
        displayTable(DISPLAY_TABLE.get());
        displayTable(DISPLAY_SHELF.get());
        displayTable(TAG_WORKBENCH.get());
        elevatorFrame();
        chargingStation();
        fluidMixer();
        heatPipe();
        keroseneLamp();
        hopper(OMNIDIRECTIONAL_HOPPER.get(), "omnidirectional_hopper_base");
        hopper(LIQUID_HOPPER.get(), "liquid_hopper_base");
        uvLightBox();
        vacuumTrap();
        vortexTube();
    }

    private void simple(Block block, Identifier model) {
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, plainVariant(model)));
    }

    private void pressureTube(Block block, String prefix) {
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block,
                MultiVariant.of(new CustomBlockStateModelBuilder.Simple(new PressureTubeModel.Unbaked(prefix)))));
    }

    private void horizontal(Block block, Direction unrotated) {
        horizontal(block, ModelLocationUtils.getModelLocation(block), unrotated);
    }

    private void horizontal(Block block, Identifier model, Direction unrotated) {
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, plainVariant(model))
                .with(horizontalRotations(unrotated)));
    }

    /**
     * Horizontally rotated block with a boolean property choosing between the "_off" and "_on" model.
     */
    private void horizontalOnOff(Block block, String propName, Direction unrotated) {
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(this.<Boolean>prop(block, propName))
                        .select(false, plainVariant(ModelLocationUtils.getModelLocation(block, "_off")))
                        .select(true, plainVariant(ModelLocationUtils.getModelLocation(block, "_on"))))
                .with(horizontalRotations(unrotated)));
    }

    private void pneumaticDoor() {
        Block block = PNEUMATIC_DOOR.get();
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(this.<Boolean>prop(block, "top_door"))
                        .select(false, plainVariant(ModelLocationUtils.getModelLocation(block)))
                        .select(true, plainVariant(ModelLocationUtils.getModelLocation(block, "_top"))))
                .with(PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).generate(dir -> yRotationReversed(Direction.SOUTH, dir))));
    }

    private void solarCompressor() {
        Block block = SOLAR_COMPRESSOR.get();
        MultiVariant base = plainVariant(ModelLocationUtils.getModelLocation(block, "_base"));
        MultiVariant bounding = plainVariant(ModelLocationUtils.getModelLocation(block, "_bounding"));
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(this.<Boolean>prop(block, "bounding"), BlockStateProperties.HORIZONTAL_FACING)
                        .generate((isBounding, dir) -> isBounding ? bounding : base.with(yRotation(Direction.NORTH, dir)))));
    }

    /**
     * Block which can face in all six directions; horizontal directions are rotated around the Y axis, and up/down
     * are rotated around the X axis from the unrotated horizontal direction.
     */
    private void facing(Block block, Direction unrotated, Quadrant upRot, Quadrant downRot) {
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, plainVariant(ModelLocationUtils.getModelLocation(block)))
                .with(facingRotations(unrotated, upRot, downRot)));
    }

    private void pressureChamberInterface() {
        Block block = PRESSURE_CHAMBER_INTERFACE.get();
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, plainVariant(ModelLocationUtils.getModelLocation(block)))
                .with(PropertyDispatch.modify(BlockStateProperties.FACING).generate(dir -> switch (dir) {
                    case UP -> X_ROT_270;
                    case DOWN -> X_ROT_90;
                    default -> yRotationReversed(Direction.SOUTH, dir);
                })));
    }

    private void pressureChamberValve(Block block) {
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(this.<Boolean>prop(block, "formed"))
                        .select(false, plainVariant(ModelLocationUtils.getModelLocation(block)))
                        .select(true, plainVariant(ModelLocationUtils.getModelLocation(block, "_formed"))))
                .with(facingRotations(Direction.NORTH, Quadrant.R90, Quadrant.R270)));
    }

    private void heatSink() {
        Block block = HEAT_SINK.get();
        Map<Direction, VariantMutator> rots = rotations(X_ROT_180, NOP, X_ROT_270, X_ROT_90, X_ROT_90.then(Y_ROT_90), X_ROT_270.then(Y_ROT_90));
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, plainVariant(ModelLocationUtils.getModelLocation(block)))
                .with(PropertyDispatch.modify(BlockStateProperties.FACING).generate(rots::get)));
    }

    private void pneumaticDynamo() {
        Block block = PNEUMATIC_DYNAMO.get();
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(this.<Boolean>prop(block, "active"))
                        .select(false, plainVariant(ModelLocationUtils.getModelLocation(block, "_off")))
                        .select(true, plainVariant(ModelLocationUtils.getModelLocation(block, "_on"))))
                .with(PropertyDispatch.modify(BlockStateProperties.FACING).generate(FROM_UP::get)));
    }

    private void wallLampState(Block block) {
        Map<Direction, VariantMutator> rots = rotations(NOP, X_ROT_180, X_ROT_90, X_ROT_270, X_ROT_90.then(Y_ROT_270), X_ROT_90.then(Y_ROT_90));
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, plainVariant(block("wall_lamp")))
                .with(PropertyDispatch.modify(BlockStateProperties.FACING).generate(rots::get)));
    }

    private <T extends Comparable<T>> void plasticBrickState(Block block, boolean pips) {
        Property<T> xPart = prop(block, "x_part");
        Property<T> zPart = prop(block, "z_part");
        MultiPartGenerator mp = MultiPartGenerator.multiPart(block);
        if (pips) {
            mp.with(plainVariant(block("plastic_brick_pips")));
        }
        for (T x : xPart.getPossibleValues()) {
            for (T z : zPart.getPossibleValues()) {
                mp.with(condition().term(xPart, x).term(zPart, z),
                        plainVariant(block("plastic_brick_" + xPart.getName(x) + "_" + zPart.getName(z))));
            }
        }
        gen.blockStateOutput.accept(mp);
    }

    private <T extends Comparable<T>> void pressureChamberWall() {
        Block block = PRESSURE_CHAMBER_WALL.get();
        Property<T> wallState = prop(block, "wall_state");
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block)
                .with(PropertyDispatch.initial(wallState)
                        .generate(state -> plainVariant(ModelLocationUtils.getModelLocation(block, "_" + wallState.getName(state))))));
    }

    /**
     * Pillar-style block; the X rotation used for the horizontal axes depends on the model.
     */
    private void axis(Block block, Quadrant horizontalRot) {
        VariantMutator xRot = VariantMutator.X_ROT.withValue(horizontalRot);
        gen.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, plainVariant(ModelLocationUtils.getModelLocation(block)))
                .with(PropertyDispatch.modify(BlockStateProperties.AXIS)
                        .select(Direction.Axis.X, xRot.then(Y_ROT_90))
                        .select(Direction.Axis.Y, NOP)
                        .select(Direction.Axis.Z, xRot)));
    }

    private void stoneBlocks() {
        gen.blockStateOutput.accept(createStairs(COMPRESSED_BRICK_STAIRS.get(),
                plainVariant(ModelLocationUtils.getModelLocation(COMPRESSED_BRICK_STAIRS.get(), "_inner")),
                plainVariant(ModelLocationUtils.getModelLocation(COMPRESSED_BRICK_STAIRS.get())),
                plainVariant(ModelLocationUtils.getModelLocation(COMPRESSED_BRICK_STAIRS.get(), "_outer"))));
        gen.blockStateOutput.accept(createStairs(REINFORCED_BRICK_STAIRS.get(),
                plainVariant(ModelLocationUtils.getModelLocation(REINFORCED_BRICK_STAIRS.get(), "_inner")),
                plainVariant(ModelLocationUtils.getModelLocation(REINFORCED_BRICK_STAIRS.get())),
                plainVariant(ModelLocationUtils.getModelLocation(REINFORCED_BRICK_STAIRS.get(), "_outer"))));
        slab(COMPRESSED_BRICK_SLAB.get(), ModelLocationUtils.getModelLocation(COMPRESSED_BRICKS.get()));
        slab(REINFORCED_BRICK_SLAB.get(), ModelLocationUtils.getModelLocation(REINFORCED_BRICKS.get()));
        slab(COMPRESSED_STONE_SLAB.get(), ModelLocationUtils.getModelLocation(COMPRESSED_STONE_SLAB.get(), "_double"));
        slab(REINFORCED_STONE_SLAB.get(), ModelLocationUtils.getModelLocation(REINFORCED_STONE_SLAB.get(), "_double"));
        wall(COMPRESSED_BRICK_WALL.get());
        wall(REINFORCED_BRICK_WALL.get());
    }

    private void slab(Block block, Identifier full) {
        gen.blockStateOutput.accept(createSlab(block,
                plainVariant(ModelLocationUtils.getModelLocation(block)),
                plainVariant(ModelLocationUtils.getModelLocation(block, "_top")),
                plainVariant(full)));
    }

    private void wall(Block block) {
        gen.blockStateOutput.accept(createWall(block,
                plainVariant(ModelLocationUtils.getModelLocation(block, "_post")),
                plainVariant(ModelLocationUtils.getModelLocation(block, "_side")),
                plainVariant(ModelLocationUtils.getModelLocation(block, "_side_tall"))));
    }

    /**
     * Block with pressure tube connections on its sides (and optionally its top), plus the assembly connector for
     * the gas lift.
     */
    private void tubeConnected(Block block, boolean gasLift) {
        MultiPartGenerator mp = MultiPartGenerator.multiPart(block)
                .with(plainVariant(ModelLocationUtils.getModelLocation(block)));
        if (gasLift) {
            mp.with(plainVariant(block("assembly_connector")));
            mp.with(when(AbstractPneumaticCraftBlock.UP, true), plainVariant(block("pressure_tube_connected")));
        }
        tubeConnections(mp, block("pressure_tube_connected"), false);
        gen.blockStateOutput.accept(mp);
    }

    private void assemblyConnected(Block block) {
        MultiVariant edge = plainVariant(block("assembly_connector_edge"));
        gen.blockStateOutput.accept(MultiPartGenerator.multiPart(block)
                .with(plainVariant(ModelLocationUtils.getModelLocation(block)))
                .with(when(AbstractPneumaticCraftBlock.NORTH, true), edge.with(Y_ROT_270))
                .with(when(AbstractPneumaticCraftBlock.SOUTH, true), edge.with(Y_ROT_90))
                .with(when(AbstractPneumaticCraftBlock.WEST, true), edge.with(Y_ROT_180))
                .with(when(AbstractPneumaticCraftBlock.EAST, true), edge));
    }

    private void tank(Block block, String tier) {
        gen.blockStateOutput.accept(MultiPartGenerator.multiPart(block)
                .with(plainVariant(block("fluid_tank_body_" + tier)))
                .with(when(AbstractPneumaticCraftBlock.UP, false), plainVariant(block("fluid_tank_top_" + tier)))
                .with(when(AbstractPneumaticCraftBlock.DOWN, false), plainVariant(block("fluid_tank_bottom_" + tier))));
    }

    private void displayTable(Block block) {
        MultiVariant top = plainVariant(ModelLocationUtils.getModelLocation(block, "_top"));
        MultiPartGenerator mp = MultiPartGenerator.multiPart(block);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            mp.with(condition().term(BlockStateProperties.HORIZONTAL_FACING, dir), top.with(yRotationReversed(Direction.NORTH, dir)));
        }
        cornerLegs(mp, block, ModelLocationUtils.getModelLocation(block, "_leg"));
        gen.blockStateOutput.accept(mp);
    }

    private void elevatorFrame() {
        Block block = ELEVATOR_FRAME.get();
        MultiPartGenerator mp = MultiPartGenerator.multiPart(block);
        cornerLegs(mp, block, ModelLocationUtils.getModelLocation(block, "_single"));
        gen.blockStateOutput.accept(mp);
    }

    /**
     * A model in each corner which doesn't have a neighbouring block, starting from the north-west corner.
     */
    private void cornerLegs(MultiPartGenerator mp, Block block, Identifier model) {
        MultiVariant leg = plainVariant(model);
        mp.with(when(this.<Boolean>prop(block, "ne"), false), leg.with(Y_ROT_90))
                .with(when(this.<Boolean>prop(block, "nw"), false), leg)
                .with(when(this.<Boolean>prop(block, "sw"), false), leg.with(Y_ROT_270))
                .with(when(this.<Boolean>prop(block, "se"), false), leg.with(Y_ROT_180));
    }

    private void chargingStation() {
        Block block = CHARGING_STATION.get();
        MultiVariant station = plainVariant(ModelLocationUtils.getModelLocation(block));
        MultiVariant pad = plainVariant(block("charge_pad"));
        MultiPartGenerator mp = MultiPartGenerator.multiPart(block);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            mp.with(condition().term(BlockStateProperties.HORIZONTAL_FACING, dir), station.with(yRotation(Direction.SOUTH, dir)));
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            mp.with(condition().term(this.<Boolean>prop(block, "charge_pad"), true).term(BlockStateProperties.HORIZONTAL_FACING, dir),
                    pad.with(yRotation(Direction.NORTH, dir)));
        }
        gen.blockStateOutput.accept(mp);
    }

    private void fluidMixer() {
        Block block = FLUID_MIXER.get();
        MultiPartGenerator mp = MultiPartGenerator.multiPart(block);
        horizontalParts(mp, ModelLocationUtils.getModelLocation(block), Direction.NORTH);
        tubeConnections(mp, block("pressure_tube_connected"), false);
        gen.blockStateOutput.accept(mp);
    }

    private void heatPipe() {
        Block block = HEAT_PIPE.get();
        MultiPartGenerator mp = MultiPartGenerator.multiPart(block)
                .with(plainVariant(ModelLocationUtils.getModelLocation(block, "_center")));
        tubeConnections(mp, ModelLocationUtils.getModelLocation(block, "_side"), true);
        gen.blockStateOutput.accept(mp);
    }

    private void keroseneLamp() {
        Block block = KEROSENE_LAMP.get();
        Property<Direction> connected = prop(block, "connected");
        MultiVariant supportH = plainVariant(ModelLocationUtils.getModelLocation(block, "_support_h"));
        MultiPartGenerator mp = MultiPartGenerator.multiPart(block);
        horizontalParts(mp, ModelLocationUtils.getModelLocation(block), Direction.SOUTH);
        mp.with(condition().term(connected, Direction.UP), plainVariant(ModelLocationUtils.getModelLocation(block, "_support_v")));
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            mp.with(condition().term(connected, dir), supportH.with(yRotation(Direction.SOUTH, dir)));
        }
        gen.blockStateOutput.accept(mp);
    }

    private void hopper(Block block, String baseModel) {
        Property<Direction> input = prop(block, "input");
        MultiVariant output = plainVariant(block("omnidirectional_hopper_output"));
        MultiVariant base = plainVariant(block(baseModel));
        MultiPartGenerator mp = MultiPartGenerator.multiPart(block);
        for (Direction dir : Direction.values()) {
            mp.with(condition().term(BlockStateProperties.FACING, dir), output.with(FROM_UP.get(dir)));
        }
        for (Direction dir : Direction.values()) {
            mp.with(condition().term(input, dir), base.with(FROM_UP.get(dir)));
        }
        gen.blockStateOutput.accept(mp);
    }

    private void uvLightBox() {
        Block block = UV_LIGHT_BOX.get();
        MultiVariant pcb = plainVariant(block("pcb"));
        MultiPartGenerator mp = MultiPartGenerator.multiPart(block);
        horizontalParts(mp, ModelLocationUtils.getModelLocation(block), Direction.EAST);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            mp.with(condition().term(this.<Boolean>prop(block, "loaded"), true).term(BlockStateProperties.HORIZONTAL_FACING, dir),
                    pcb.with(yRotation(Direction.EAST, dir)));
        }
        gen.blockStateOutput.accept(mp);
    }

    private void vacuumTrap() {
        Block block = VACUUM_TRAP.get();
        MultiPartGenerator mp = MultiPartGenerator.multiPart(block);
        for (boolean open : new boolean[]{false, true}) {
            MultiVariant model = plainVariant(ModelLocationUtils.getModelLocation(block, open ? "_open" : "_closed"));
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                mp.with(condition().term(BlockStateProperties.HORIZONTAL_FACING, dir).term(this.<Boolean>prop(block, "open"), open),
                        model.with(yRotation(Direction.EAST, dir)));
            }
        }
        gen.blockStateOutput.accept(mp);
    }

    private void vortexTube() {
        Block block = VORTEX_TUBE.get();
        MultiVariant tube = plainVariant(ModelLocationUtils.getModelLocation(block));
        MultiPartGenerator mp = MultiPartGenerator.multiPart(block);
        for (Direction dir : Direction.values()) {
            mp.with(condition().term(BlockStateProperties.FACING, dir), tube.with(facingRotation(Direction.SOUTH, Quadrant.R90, Quadrant.R270, dir)));
        }
        tubeConnections(mp, block("pressure_tube_connected"), true);
        gen.blockStateOutput.accept(mp);
    }

    /**
     * A part for each horizontal facing of the block.
     */
    private void horizontalParts(MultiPartGenerator mp, Identifier model, Direction unrotated) {
        MultiVariant variant = plainVariant(model);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            mp.with(condition().term(BlockStateProperties.HORIZONTAL_FACING, dir), variant.with(yRotation(unrotated, dir)));
        }
    }

    /**
     * A connection model (pointing up) for each side with a connection.
     */
    private void tubeConnections(MultiPartGenerator mp, Identifier model, boolean vertical) {
        MultiVariant variant = plainVariant(model);
        for (Direction dir : Direction.values()) {
            if (vertical || dir.getAxis().isHorizontal()) {
                mp.with(when(AbstractPneumaticCraftBlock.CONNECTION_PROPERTIES[dir.get3DDataValue()], true), variant.with(FROM_UP.get(dir)));
            }
        }
    }

    /* ----------------------------------- models ----------------------------------- */

    private void blockModels() {
        cubeAll(COMPRESSED_STONE.get());
        cubeAll(COMPRESSED_BRICKS.get());
        cubeAll(COMPRESSED_BRICK_TILE.get());
        cubeAll(EMPTY_SPAWNER.get());
        cubeAll(PRESSURIZED_SPAWNER.get());
        column(REINFORCED_STONE.get(), texture("reinforced_stone"), texture("reinforced_stone_top"));
        column(REINFORCED_BRICKS.get(), texture("reinforced_bricks"), texture("reinforced_bricks_top"));
        column(REINFORCED_BRICK_TILE.get(), texture("reinforced_brick_tile"), texture("reinforced_brick_tile_top"));
        column(COMPRESSED_BRICK_PILLAR.get(), texture("compressed_brick_pillar"), texture("compressed_brick_pillar_top"));
        column(REINFORCED_BRICK_PILLAR.get(), texture("reinforced_brick_pillar"), texture("reinforced_brick_pillar_top"));

        slabModels(COMPRESSED_BRICK_SLAB.get(), texture("compressed_bricks"), texture("compressed_bricks"));
        slabModels(REINFORCED_BRICK_SLAB.get(), texture("reinforced_bricks"), texture("reinforced_bricks"));
        slabModels(COMPRESSED_STONE_SLAB.get(), texture("compressed_brick_tile"), texture("compressed_stone_slab_side"));
        slabModels(REINFORCED_STONE_SLAB.get(), texture("reinforced_brick_tile"), texture("reinforced_stone_slab_side"));
        ModelTemplates.CUBE_COLUMN.createWithSuffix(COMPRESSED_STONE_SLAB.get(), "_double",
                TextureMapping.column(texture("compressed_stone_slab_side"), texture("compressed_brick_tile")), gen.modelOutput);
        ModelTemplates.CUBE_COLUMN.createWithSuffix(REINFORCED_STONE_SLAB.get(), "_double",
                TextureMapping.column(texture("reinforced_stone_slab_side"), texture("reinforced_stone_slab_top")), gen.modelOutput);
        stairsModels(COMPRESSED_BRICK_STAIRS.get(), texture("compressed_bricks"));
        stairsModels(REINFORCED_BRICK_STAIRS.get(), texture("reinforced_bricks"));
        wallModels(COMPRESSED_BRICK_WALL.get(), texture("compressed_bricks"));
        wallModels(REINFORCED_BRICK_WALL.get(), texture("reinforced_bricks"));

        // machines with a different texture on each side
        Material sidePanel = texture("generic/side_panel");
        Material topPanel = texture("generic/top_panel");
        Material bottomPanel = texture("generic/bottom_panel");
        cube("aerial_interface", texture("aerial_interface_front"), texture("aerial_interface_front"), sidePanel,
                texture("aerial_interface_top"), bottomPanel, sidePanel, sidePanel);
        cube("programmable_controller", texture("programmable_controller_front"), texture("programmable_controller_front"), sidePanel,
                texture("programmable_controller_top"), bottomPanel, sidePanel, sidePanel);
        cube("drone_interface_off", texture("drone_interface_front"), texture("drone_interface_front"), sidePanel,
                topPanel, bottomPanel, sidePanel, sidePanel);
        cube("drone_interface_on", texture("drone_interface_front_on"), texture("drone_interface_front_on"), sidePanel,
                topPanel, bottomPanel, sidePanel, sidePanel);

        // pressure chamber
        Material valveSide = texture("pressure_chamber/valve_side");
        cube("pressure_chamber_valve", texture("pressure_chamber/valve"), texture("pressure_chamber/valve"), texture("pressure_chamber/valve"),
                valveSide, valveSide, valveSide, valveSide);
        cube("reinforced_pressure_chamber_valve", texture("pressure_chamber/valve"), texture("pressure_chamber/reinforced_valve"), texture("pressure_chamber/reinforced_valve"),
                valveSide, valveSide, valveSide, valveSide);
        child("pressure_chamber_valve_formed", block("pressure_chamber_valve"),
                "north", texture("pressure_chamber/valve_formed"), "south", texture("pressure_chamber/valve_formed"));
        child("reinforced_pressure_chamber_valve_formed", block("pressure_chamber_valve"),
                "north", texture("pressure_chamber/reinforced_valve_formed"), "south", texture("pressure_chamber/reinforced_valve_formed"));
        ModelTemplates.CUBE_ALL.create(PRESSURE_CHAMBER_GLASS.get(), TextureMapping.cube(texture("pressure_chamber/window")), gen.modelOutput);
        child("pressure_chamber_glass_inv", ModelTemplates.CUBE_ALL.model.orElseThrow(),
                "all", texture("pressure_chamber/window"), "particle", texture("pressure_chamber/window"));
        pressureChamberWallModels();

        // child models which only change textures
        child("creative_compressed_iron_block", block("compressed_iron_block"),
                "texture", texture("creative_compressed_iron_block"), "particle", texture("creative_compressed_iron_block"));
        child("flux_compressor_on", block("flux_compressor_off"), "0", modelTexture("flux_compressor_on"));
        child("pneumatic_dynamo_on", block("pneumatic_dynamo_off"), "0", modelTexture("pneumatic_dynamo_on"));
        child("pneumatic_door_top", block("pneumatic_door"));
        child("plastic_brick", Identifier.fromNamespaceAndPath("neoforge", "block/default"),
                "top", texture("plastic_brick_top"), "bottom", texture("plastic_brick_bottom"),
                "left", texture("plastic_brick_left"), "right", texture("plastic_brick_right"),
                "none", texture("plastic_brick_none"), "particle", texture("plastic_brick_none"));
        for (String part : new String[]{"inv", "leg", "top"}) {
            child("tag_workbench_" + part, block("display_table_" + part),
                    "side", modelTexture("tag_workbench_side"), "top", modelTexture("tag_workbench_top"),
                    "particle", modelTexture("tag_workbench_side"));
        }
    }

    private void pressureChamberWallModels() {
        String[] names = {"pressure_chamber_wall", "center", "middle_horizontal", "middle_vertical",
                "top_left", "top_right", "bottom_left", "bottom_right"};
        Material[] t = new Material[names.length];
        for (int i = 0; i < names.length; i++) {
            t[i] = texture("pressure_chamber/" + names[i]);
        }
        Material wall = t[0], center = t[1], midH = t[2], midV = t[3], tl = t[4], tr = t[5], bl = t[6], br = t[7];

        ModelTemplates.CUBE_ALL.createWithSuffix(PRESSURE_CHAMBER_WALL.get(), "_none", TextureMapping.cube(wall), gen.modelOutput);
        ModelTemplates.CUBE_ALL.createWithSuffix(PRESSURE_CHAMBER_WALL.get(), "_center", TextureMapping.cube(center), gen.modelOutput);
        child("pressure_chamber_wall_default", ModelTemplates.CUBE.model.orElseThrow(), "particle", wall);
        // edges and corners: textures in the order down, up, north, south, east, west
        wallVariant("xedge", midV, midV, midH, midH, midH, midH);
        wallVariant("yedge", midV, midV, midV, midV, midV, midV);
        wallVariant("zedge", midH, midH, midH, midH, midV, midV);
        wallVariant("xmin_ymax_zmax", br, bl, bl, tl, br, tr);
        wallVariant("xmin_ymax_zmin", tr, tl, tr, br, bl, tl);
        wallVariant("xmin_ymin_zmax", tl, tr, tl, bl, tr, br);
        wallVariant("xmin_ymin_zmin", bl, br, br, tr, tl, bl);
    }

    private void wallVariant(String name, Material down, Material up, Material north, Material south, Material east, Material west) {
        child("pressure_chamber_wall_" + name, block("pressure_chamber_wall_default"),
                "down", down, "up", up, "north", north, "south", south, "east", east, "west", west);
    }

    private void cubeAll(Block block) {
        ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(block), gen.modelOutput);
    }

    private void column(Block block, Material side, Material end) {
        ModelTemplates.CUBE_COLUMN.create(block, TextureMapping.column(side, end), gen.modelOutput);
    }

    private void slabModels(Block block, Material topAndBottom, Material side) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.BOTTOM, topAndBottom).put(TextureSlot.TOP, topAndBottom).put(TextureSlot.SIDE, side);
        ModelTemplates.SLAB_BOTTOM.create(block, mapping, gen.modelOutput);
        ModelTemplates.SLAB_TOP.create(block, mapping, gen.modelOutput);
    }

    private void stairsModels(Block block, Material texture) {
        TextureMapping mapping = TextureMapping.cube(texture)
                .put(TextureSlot.BOTTOM, texture).put(TextureSlot.TOP, texture).put(TextureSlot.SIDE, texture);
        ModelTemplates.STAIRS_STRAIGHT.create(block, mapping, gen.modelOutput);
        ModelTemplates.STAIRS_INNER.create(block, mapping, gen.modelOutput);
        ModelTemplates.STAIRS_OUTER.create(block, mapping, gen.modelOutput);
    }

    private void wallModels(Block block, Material texture) {
        TextureMapping mapping = new TextureMapping().put(TextureSlot.WALL, texture);
        ModelTemplates.WALL_POST.create(block, mapping, gen.modelOutput);
        ModelTemplates.WALL_LOW_SIDE.create(block, mapping, gen.modelOutput);
        ModelTemplates.WALL_TALL_SIDE.create(block, mapping, gen.modelOutput);
        ModelTemplates.WALL_INVENTORY.create(block, mapping, gen.modelOutput);
    }

    private void cube(String name, Material particle, Material north, Material south, Material up, Material down, Material east, Material west) {
        ModelTemplates.CUBE.create(block(name), new TextureMapping()
                .put(TextureSlot.PARTICLE, particle).put(TextureSlot.NORTH, north).put(TextureSlot.SOUTH, south)
                .put(TextureSlot.UP, up).put(TextureSlot.DOWN, down).put(TextureSlot.EAST, east).put(TextureSlot.WEST, west), gen.modelOutput);
    }

    /**
     * A model which inherits from another model, only setting some textures.
     *
     * @param slotsAndTextures alternating texture slot names and {@link Material}s
     */
    private void child(String name, Identifier parent, Object... slotsAndTextures) {
        TextureSlot[] slots = new TextureSlot[slotsAndTextures.length / 2];
        TextureMapping mapping = new TextureMapping();
        for (int i = 0; i < slots.length; i++) {
            slots[i] = TextureSlot.create((String) slotsAndTextures[i * 2]);
            mapping.put(slots[i], (Material) slotsAndTextures[i * 2 + 1]);
        }
        new ModelTemplate(Optional.of(parent), Optional.empty(), slots).create(block(name), mapping, gen.modelOutput);
    }

    /* ----------------------------------- utilities ----------------------------------- */

    private static Identifier block(String name) {
        return RL("block/" + name);
    }

    private static Material texture(String name) {
        return new Material(RL("block/" + name));
    }

    private static Material modelTexture(String name) {
        return new Material(RL("pnc_model/" + name));
    }

    /**
     * Get a block's property by name. Many of PNC's block properties are defined in the block classes and aren't
     * public, so this is simpler than referring to them directly.
     */
    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> Property<T> prop(Block block, String name) {
        return (Property<T>) Objects.requireNonNull(block.getStateDefinition().getProperty(name),
                () -> "no property '" + name + "' in " + block);
    }

    private static <T extends Comparable<T>> ConditionBuilder when(Property<T> property, T value) {
        return condition().term(property, value);
    }

    private static Map<Direction, VariantMutator> rotations(VariantMutator up, VariantMutator down, VariantMutator north,
                                                            VariantMutator south, VariantMutator west, VariantMutator east) {
        Map<Direction, VariantMutator> map = new EnumMap<>(Direction.class);
        map.put(Direction.UP, up);
        map.put(Direction.DOWN, down);
        map.put(Direction.NORTH, north);
        map.put(Direction.SOUTH, south);
        map.put(Direction.WEST, west);
        map.put(Direction.EAST, east);
        return map;
    }

    /**
     * {@return the Y rotation which turns a model facing the {@code unrotated} direction to face {@code dir}}
     */
    private static VariantMutator yRotation(Direction unrotated, Direction dir) {
        return yRotation(dir.toYRot() - unrotated.toYRot());
    }

    /**
     * {@return like {@link #yRotation(Direction, Direction)}, but turning the other way; some models were made to be
     * rotated like this}
     */
    private static VariantMutator yRotationReversed(Direction unrotated, Direction dir) {
        return yRotation(unrotated.toYRot() - dir.toYRot());
    }

    private static VariantMutator yRotation(float degrees) {
        int deg = Math.floorMod((int) degrees, 360);
        return deg == 0 ? NOP : VariantMutator.Y_ROT.withValue(Quadrant.parseJson(deg));
    }

    private static VariantMutator facingRotation(Direction unrotated, Quadrant upRot, Quadrant downRot, Direction dir) {
        return switch (dir) {
            case UP -> VariantMutator.X_ROT.withValue(upRot);
            case DOWN -> VariantMutator.X_ROT.withValue(downRot);
            default -> yRotation(unrotated, dir);
        };
    }

    private static PropertyDispatch<VariantMutator> horizontalRotations(Direction unrotated) {
        return PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING).generate(dir -> yRotation(unrotated, dir));
    }

    private static PropertyDispatch<VariantMutator> facingRotations(Direction unrotated, Quadrant upRot, Quadrant downRot) {
        return PropertyDispatch.modify(BlockStateProperties.FACING).generate(dir -> facingRotation(unrotated, upRot, downRot, dir));
    }
}
