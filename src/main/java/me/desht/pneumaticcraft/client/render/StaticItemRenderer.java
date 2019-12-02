package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

/**
 * Pretty much ItemRenderer but renders item entities completely static (no rotation, no bobbing)
 */
public class StaticItemRenderer extends EntityRenderer<ItemEntity> {
    private final net.minecraft.client.renderer.ItemRenderer itemRenderer;
    private final Random random = new Random();

    public StaticItemRenderer() {
        this(Minecraft.getInstance().getRenderManager(), Minecraft.getInstance().getItemRenderer());
    }

    private StaticItemRenderer(EntityRendererManager renderManagerIn, net.minecraft.client.renderer.ItemRenderer itemRendererIn) {
        super(renderManagerIn);
        this.itemRenderer = itemRendererIn;
        this.shadowSize = 0.15F;
        this.shadowOpaque = 0.75F;
    }

    private int transformModelCount(ItemEntity itemIn, double x, double y, double z, IBakedModel model) {
        ItemStack itemstack = itemIn.getItem();
        Item item = itemstack.getItem();
        if (item == null) {
            return 0;
        } else {
            int i = this.getModelCount(itemstack);
            float f2 = model.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.getY();
            GlStateManager.translatef((float)x, (float)y + 0.25F * f2, (float)z);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            return i;
        }
    }

    private int getModelCount(ItemStack stack) {
        int i = 1;
        if (stack.getCount() > 48) {
            i = 5;
        } else if (stack.getCount() > 32) {
            i = 4;
        } else if (stack.getCount() > 16) {
            i = 3;
        } else if (stack.getCount() > 1) {
            i = 2;
        }

        return i;
    }

    public void doRender(ItemEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        ItemStack itemstack = entity.getItem();
        int i = itemstack.isEmpty() ? 187 : Item.getIdFromItem(itemstack.getItem()) + itemstack.getDamage();
        this.random.setSeed((long)i);
        boolean flag = false;
        if (this.bindEntityTexture(entity)) {
            this.renderManager.textureManager.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false);
            flag = true;
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        IBakedModel ibakedmodel = this.itemRenderer.getItemModelWithOverrides(itemstack, entity.world, null);
        int j = this.transformModelCount(entity, x, y, z, ibakedmodel);
        boolean flag1 = ibakedmodel.isGui3d();
        if (!flag1) {
            float f3 = -0.0F * (float)(j - 1) * 0.5F;
            float f4 = -0.0F * (float)(j - 1) * 0.5F;
            float f5 = -0.09375F * (float)(j - 1) * 0.5F;
            GlStateManager.translatef(f3, f4, f5);
        }

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
        }

        for(int k = 0; k < j; ++k) {
            if (flag1) {
                GlStateManager.pushMatrix();
                if (k > 0) {
                    float f7 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f9 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f6 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    GlStateManager.translatef(shouldSpreadItems() ? f7 : 0, shouldSpreadItems() ? f9 : 0, f6);
                }

                IBakedModel transformedModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false);
                this.itemRenderer.renderItem(itemstack, transformedModel);
                GlStateManager.popMatrix();
            } else {
                GlStateManager.pushMatrix();
                if (k > 0) {
                    float f8 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    GlStateManager.translatef(f8, f10, 0.0F);
                }

                IBakedModel transformedModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false);
                this.itemRenderer.renderItem(itemstack, transformedModel);
                GlStateManager.popMatrix();
                GlStateManager.translatef(0.0F, 0.0F, 0.09375F);
            }
        }

        if (this.renderOutlines) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        this.bindEntityTexture(entity);
        if (flag) {
            this.renderManager.textureManager.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(ItemEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }

    /*==================================== FORGE START ===========================================*/

    /**
     * @return If items should spread out when rendered in 3D
     */
    public boolean shouldSpreadItems() {
        return true;
    }

    /**
     * @return If items should have a bob effect
     */
    public boolean shouldBob() {
        return false;
    }
    /*==================================== FORGE END =============================================*/
}
