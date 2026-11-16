package me.desht.pneumaticcraft.client.model.custom.pressure_tube;

import com.mojang.math.Quadrant;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Block state model for pressure tubes. The core, connected and closed part models are in the UP orientation and
 * are baked once per direction; which part is shown on each side comes from the block entity's model data.
 */
public class PressureTubeModel implements DynamicBlockStateModel {
    // rotations from the UP orientation to each direction (DUNSWE order)
    private static final BlockModelRotation[] ROTATIONS = new BlockModelRotation[] {
            rotation(Quadrant.R180, Quadrant.R0),
            rotation(Quadrant.R0, Quadrant.R0),
            rotation(Quadrant.R90, Quadrant.R0),
            rotation(Quadrant.R90, Quadrant.R180),
            rotation(Quadrant.R90, Quadrant.R270),
            rotation(Quadrant.R90, Quadrant.R90)
    };

    private final BlockStateModelPart[] disconnectedFaces;
    private final BlockStateModelPart[] connectedArms;
    private final BlockStateModelPart[] closedArms;
    private final Material.Baked particleMaterial;
    @BakedQuad.MaterialFlags
    private final int materialFlags;

    private PressureTubeModel(BlockStateModelPart[] disconnectedFaces, BlockStateModelPart[] connectedArms, BlockStateModelPart[] closedArms) {
        this.disconnectedFaces = disconnectedFaces;
        this.connectedArms = connectedArms;
        this.closedArms = closedArms;
        this.particleMaterial = disconnectedFaces[Direction.UP.get3DDataValue()].particleMaterial();
        int flags = 0;
        for (int i = 0; i < 6; i++) {
            flags |= disconnectedFaces[i].materialFlags() | connectedArms[i].materialFlags() | closedArms[i].materialFlags();
        }
        this.materialFlags = flags;
    }

    private static BlockModelRotation rotation(Quadrant x, Quadrant y) {
        return BlockModelRotation.get(Quadrant.fromXYAngles(x, y));
    }

    private static Short getConnections(BlockAndTintGetter level, BlockPos pos) {
        return level.getModelData(pos).get(PressureTubeBlockEntity.CONNECTION_PROPERTY);
    }

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        Short beProps = getConnections(level, pos);
        return new GeometryKey(this, beProps == null ? -1 : beProps);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        Short beProps = getConnections(level, pos);
        if (beProps == null) {
            return;
        }
        byte packedClosed = (byte) (beProps & 0xFF);
        byte packedConnected = (byte) ((beProps & 0xFF00) >> 8);
        for (Direction dir : DirectionUtil.VALUES) {
            int d = dir.get3DDataValue();
            if (DirectionUtil.getDirectionBit(packedClosed, dir)) {
                parts.add(closedArms[d]);
            } else if (DirectionUtil.getDirectionBit(packedConnected, dir)) {
                parts.add(connectedArms[d]);
            } else {
                parts.add(disconnectedFaces[d]);
            }
        }
    }

    @Override
    public Material.Baked particleMaterial() {
        return particleMaterial;
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return materialFlags;
    }

    private record GeometryKey(PressureTubeModel model, int beProps) {
    }

    /**
     * Blockstate JSON: {@code {"type": "pneumaticcraft:pressure_tube", "prefix": "advanced"}}; the prefix selects
     * the {@code block/<prefix>_pressure_tube_*} part models and may be omitted for the basic tube.
     */
    public record Unbaked(String prefix) implements CustomUnbakedBlockStateModel {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.STRING.optionalFieldOf("prefix", "").forGetter(Unbaked::prefix)
        ).apply(builder, Unbaked::new));

        private Identifier partModel(String part) {
            return RL("block/" + (prefix.isEmpty() ? "" : prefix + "_") + "pressure_tube_" + part);
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new PressureTubeModel(bakeRotated(baker, "disconnected"), bakeRotated(baker, "connected"), bakeRotated(baker, "closed"));
        }

        private BlockStateModelPart[] bakeRotated(ModelBaker baker, String part) {
            BlockStateModelPart[] res = new BlockStateModelPart[6];
            Identifier id = partModel(part);
            for (Direction dir : DirectionUtil.VALUES) {
                int d = dir.get3DDataValue();
                res[d] = SimpleModelWrapper.bake(baker, id, ROTATIONS[d]);
            }
            return res;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(partModel("disconnected"));
            resolver.markDependency(partModel("connected"));
            resolver.markDependency(partModel("closed"));
        }

        @Override
        public MapCodec<Unbaked> codec() {
            return MAP_CODEC;
        }
    }
}
