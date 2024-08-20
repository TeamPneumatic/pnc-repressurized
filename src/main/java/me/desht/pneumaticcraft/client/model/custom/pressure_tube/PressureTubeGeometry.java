package me.desht.pneumaticcraft.client.model.custom.pressure_tube;

import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.joml.Vector3f;

import java.util.function.Function;

public record PressureTubeGeometry(String tubeType, BlockModel core, BlockModel connected, BlockModel closed) implements IUnbakedGeometry<PressureTubeGeometry> {
    private static final Vector3f BLOCK_CENTER = new Vector3f(0.5f, 0.5f, 0.5f);

    // JSON models for the connected and closed parts are in the UP orientation
    // rotate them as appropriate to get a rotated model for each direction (DUNSWE order)
    private static final BlockModelRotation[] ROTATIONS = new BlockModelRotation[] {
            BlockModelRotation.X180_Y0,
            BlockModelRotation.X0_Y0,
            BlockModelRotation.X90_Y0,
            BlockModelRotation.X90_Y180,
            BlockModelRotation.X90_Y270,
            BlockModelRotation.X90_Y90
    };

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        BakedModel[] rotatedCore = new BakedModel[6];
        BakedModel[] rotatedConnected = new BakedModel[6];
        BakedModel[] rotatedClosed = new BakedModel[6];

        for (Direction dir : DirectionUtil.VALUES) {
            int d = dir.get3DDataValue();
            ModelState rotatedState = UnbakedGeometryHelper.composeRootTransformIntoModelState(
                    modelState,
                    ROTATIONS[d].getRotation().applyOrigin(BLOCK_CENTER)
            );
            rotatedCore[d] = core.bake(baker, core, spriteGetter, rotatedState, true);
            rotatedConnected[d] = connected.bake(baker, connected, spriteGetter, rotatedState, true);
            rotatedClosed[d] = closed.bake(baker, closed, spriteGetter, rotatedState, true);
        }

        return new PressureTubeModel(tubeType, core.bake(baker, core, spriteGetter, modelState, true), rotatedCore, rotatedConnected, rotatedClosed);
    }
}
