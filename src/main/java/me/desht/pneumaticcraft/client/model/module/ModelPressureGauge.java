package me.desht.pneumaticcraft.client.model.module;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModulePressureGauge;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.util.ResourceLocation;

public class ModelPressureGauge extends AbstractModelRenderer<ModulePressureGauge> {
    private final RendererModel shape1;
    private final RendererModel shape2;

    public ModelPressureGauge() {
        textureWidth = 64;
        textureHeight = 32;

        shape1 = new RendererModel(this, 0, 0);
        shape1.addBox(0F, 0F, 0F, 3, 3, 3);
        shape1.setRotationPoint(-1.5F, 14.5F, 2F);
        shape1.setTextureSize(64, 32);
        shape1.mirror = true;
        setRotation(shape1, 0F, 0F, 0F);
        shape2 = new RendererModel(this, 0, 6);
        shape2.addBox(0F, 0F, 0F, 8, 8, 1);
        shape2.setRotationPoint(-4F, 12F, 5F);
        shape2.setTextureSize(64, 32);
        shape2.mirror = true;
        setRotation(shape2, 0F, 0F, 0F);
    }

    @Override
    protected void renderDynamic(ModulePressureGauge module, float scale, float partialTicks) {
        if (module != null && module.isUpgraded()) RenderUtils.glColorHex(0xFFC0FF70);
        shape1.render(scale);
        shape2.render(scale);

        float pressure = 0f;
        float dangerPressure = 5f;
        float critPressure = 7f;
        if (module != null && module.getTube() instanceof TileEntityPneumaticBase) {
            TileEntityPneumaticBase base = (TileEntityPneumaticBase) module.getTube();
            pressure = base.getPressure();
            critPressure = base.criticalPressure;
            dangerPressure = base.dangerPressure;
        }
        GlStateManager.translated(0, 1, 0.378);
        double widgetScale = 0.007D;
        GlStateManager.scaled(widgetScale, widgetScale, widgetScale);
        GlStateManager.rotated(180, 0, 1, 0);
        GlStateManager.disableLighting();
        GuiUtils.drawPressureGauge(Minecraft.getInstance().fontRenderer, -1, critPressure, dangerPressure, -1.001F, pressure, 0, 0, 0);
        GlStateManager.enableLighting();

    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_GAUGE;
    }
}
