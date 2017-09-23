package me.desht.pneumaticcraft.client.model.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import me.desht.pneumaticcraft.common.item.ItemProgrammingPuzzle;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetStart;
import me.desht.pneumaticcraft.common.progwidgets.WidgetRegistrator;
import me.desht.pneumaticcraft.lib.Names;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class ModelProgrammingPuzzle implements IModel {
    public static final ModelResourceLocation LOCATION = new ModelResourceLocation(new ResourceLocation("forge", "dynbucket"), "inventory");

    // minimal Z offset to prevent depth-fighting
    private static final float NORTH_Z_BASE = 7.496f / 16f;
    private static final float SOUTH_Z_BASE = 8.504f / 16f;
    private static final float NORTH_Z_FLUID = 7.498f / 16f;
    private static final float SOUTH_Z_FLUID = 8.502f / 16f;

    public static final IModel MODEL = new ModelProgrammingPuzzle();

    private final IProgWidget widget;

    public ModelProgrammingPuzzle() {
        this(new ProgWidgetStart());
    }

    public ModelProgrammingPuzzle(IProgWidget widget) {
        this.widget = widget;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableList.of();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();
        for (IProgWidget widget : WidgetRegistrator.registeredWidgets) {
            builder.add(getWidgetTexture(widget));
        }
        return builder.build();
    }

    private static ResourceLocation getWidgetTexture(IProgWidget widget) {
        String resourcePath = widget.getTexture().toString();
        resourcePath = resourcePath.replace("textures/", "").replace(".png", "");
        return new ResourceLocation(resourcePath);
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

        ImmutableMap<TransformType, TRSRTransformation> transformMap = PerspectiveMapWrapper.getTransforms(state);

        TRSRTransformation transform = state.apply(Optional.empty()).orElse(TRSRTransformation.identity());
        TextureAtlasSprite widgetSprite = bakedTextureGetter.apply(getWidgetTexture(widget));
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        int width = widget.getWidth() + (widget.getParameters() != null && widget.getParameters().length > 0 ? 10 : 0);
        int height = widget.getHeight() + (widget.hasStepOutput() ? 5 : 0);

        Pair<Double, Double> maxUV = widget.getMaxUV();
        int textureSize = widget.getTextureSize();
        float scale = 1F / (float) Math.max(maxUV.getLeft(), maxUV.getRight());
        float transX = 0;//maxUV.getLeft().floatValue();
        float transY = -1 + maxUV.getRight().floatValue();
        transform = transform.compose(new TRSRTransformation(new Vector3f(0, 0, 0), null, new Vector3f(scale, scale, 1), null));
        transform = transform.compose(new TRSRTransformation(new Vector3f(transX, transY, 0), null, new Vector3f(1, 1, 1), null));

        builder.add(ItemTextureQuadConverter.genQuad(format, transform, 0, 0, 16 * maxUV.getLeft().floatValue(), 16 * maxUV.getRight().floatValue(), NORTH_Z_BASE, widgetSprite, EnumFacing.NORTH, 0xffffffff));
        builder.add(ItemTextureQuadConverter.genQuad(format, transform, 0, 0, 16 * maxUV.getLeft().floatValue(), 16 * maxUV.getRight().floatValue(), SOUTH_Z_BASE, widgetSprite, EnumFacing.SOUTH, 0xffffffff));

        return new BakedProgrammingPuzzle(this, builder.build(), widgetSprite, format, Maps.immutableEnumMap(transformMap), Maps.newHashMap());
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }

    @Override
    public IModel process(ImmutableMap<String, String> customData) {
        String progWidgetName = customData.get("progWidget");
        IProgWidget widget = WidgetRegistrator.getWidgetFromName(progWidgetName);

        if (widget == null) throw new IllegalStateException("Invalid widget: " + progWidgetName);

        // create new model with correct widget
        return new ModelProgrammingPuzzle(widget);
    }

    public enum LoaderProgrammingPuzzle implements ICustomModelLoader {
        instance;

        @Override
        public boolean accepts(ResourceLocation modelLocation) {
            return modelLocation.getResourceDomain().equals(Names.MOD_ID) && modelLocation.getResourcePath().contains("programming_puzzle");
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) throws IOException {
            return MODEL;
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {
            // no need to clear cache since we create a new model instance
        }
    }

    // the dynamic bucket is based on the empty bucket
    protected static class BakedProgrammingPuzzle implements IBakedModel {

        private final ModelProgrammingPuzzle parent;
        private final Map<String, IBakedModel> cache; // contains all the baked models since they'll never change
        private final ImmutableMap<TransformType, TRSRTransformation> transforms;
        private final ImmutableList<BakedQuad> quads;
        private final TextureAtlasSprite particle;
        private final VertexFormat format;
        private final PuzzleOverrideList overridesList;

        public BakedProgrammingPuzzle(ModelProgrammingPuzzle parent, ImmutableList<BakedQuad> quads,
                                      TextureAtlasSprite particle, VertexFormat format,
                                      ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms,
                                      Map<String, IBakedModel> cache) {
            this.quads = quads;
            this.particle = particle;
            this.format = format;
            this.parent = parent;
            this.transforms = transforms;
            this.cache = cache;
            this.overridesList = new PuzzleOverrideList(Collections.emptyList(), this);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return quads;
        }

        @Override
        public boolean isAmbientOcclusion() {
            return true;
        }

        @Override
        public boolean isGui3d() {
            return false;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return particle;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return overridesList;
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
            return PerspectiveMapWrapper.handlePerspective(this, transforms, cameraTransformType);
        }
    }

    private static class PuzzleOverrideList extends ItemOverrideList {

        private final BakedProgrammingPuzzle puzzle;

        PuzzleOverrideList(List<ItemOverride> overridesIn, BakedProgrammingPuzzle puzzle) {
            super(overridesIn);
            this.puzzle = puzzle;
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            IProgWidget widget = ItemProgrammingPuzzle.getWidgetForPiece(stack);
            if (widget == null) return originalModel;
            String name = widget.getWidgetString();

            if (!puzzle.cache.containsKey(name)) {
                IModel model = puzzle.parent.process(ImmutableMap.of("progWidget", name));
                Function<ResourceLocation, TextureAtlasSprite> textureGetter;
                textureGetter = location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());

                IBakedModel bakedModel = model.bake(new SimpleModelState(puzzle.transforms), puzzle.format, textureGetter);
                puzzle.cache.put(name, bakedModel);
                return bakedModel;
            }

            return puzzle.cache.get(name);
        }
    }
}
