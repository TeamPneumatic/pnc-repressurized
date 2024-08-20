package me.desht.pneumaticcraft.client.model.custom.pressure_tube;

import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PressureTubeModel extends BakedModelWrapper<BakedModel> {
    private static final Map<CacheKey, List<BakedQuad>> MODEL_CACHE = new ConcurrentHashMap<>();

    private final int tubeTypeHash;  // identifies the variant of pressure tube
    private final BakedModel[] disconnectedFaces;
    private final BakedModel[] connectedArms;
    private final BakedModel[] closedArms;

    public PressureTubeModel(String tubeType, BakedModel core, BakedModel[] disconnectedFaces, BakedModel[] connectedArms, BakedModel[] closedArms) {
        super(core);
        this.tubeTypeHash = tubeType.hashCode();
        this.disconnectedFaces = disconnectedFaces;
        this.connectedArms = connectedArms;
        this.closedArms = closedArms;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        Short beProps = extraData.get(PressureTubeBlockEntity.CONNECTION_PROPERTY);
        if (side == null && beProps != null) {
            CacheKey key = new CacheKey(tubeTypeHash, beProps);
            List<BakedQuad> cachedQuads = MODEL_CACHE.get(key);
            if (cachedQuads == null) {
                cachedQuads = new ArrayList<>();
                byte packedClosed = (byte) (beProps & 0xFF);
                byte packedConnected = (byte) ((beProps & 0xFF00) >> 8);
                for (Direction dir : DirectionUtil.VALUES) {
                    if (DirectionUtil.getDirectionBit(packedClosed, dir)) {
                        cachedQuads.addAll(closedArms[dir.get3DDataValue()].getQuads(state, null, rand, extraData, renderType));
                    } else if (DirectionUtil.getDirectionBit(packedConnected, dir)) {
                        cachedQuads.addAll(connectedArms[dir.get3DDataValue()].getQuads(state, null, rand, extraData, renderType));
                    } else {
                        cachedQuads.addAll(disconnectedFaces[dir.get3DDataValue()].getQuads(state, null, rand, extraData, renderType));
                    }
                }
                MODEL_CACHE.put(key, cachedQuads);
            }
            return cachedQuads;
        } else {
            return List.of();
        }
    }

    private record CacheKey(int tubeTypeHash, short beProps) {
    }
}
