package me.desht.pneumaticcraft.client.model.module;

import me.desht.pneumaticcraft.client.gui.GuiUtils;
import me.desht.pneumaticcraft.common.block.tubes.ModulePressureGauge;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

public class ModelGauge extends ModelModuleBase {
    private final ModelRenderer shape1;
    private final ModelRenderer shape2;
    private final ModulePressureGauge gaugeModule;

    public ModelGauge(ModulePressureGauge gaugeModule) {
        textureWidth = 64;
        textureHeight = 32;

        shape1 = new ModelRenderer(this, 0, 0);
        shape1.addBox(0F, 0F, 0F, 3, 3, 3);
        shape1.setRotationPoint(-1.5F, 14.5F, 2F);
        shape1.setTextureSize(64, 32);
        shape1.mirror = true;
        setRotation(shape1, 0F, 0F, 0F);
        shape2 = new ModelRenderer(this, 0, 6);
        shape2.addBox(0F, 0F, 0F, 8, 8, 1);
        shape2.setRotationPoint(-4F, 12F, 5F);
        shape2.setTextureSize(64, 32);
        shape2.mirror = true;
        setRotation(shape2, 0F, 0F, 0F);
        this.gaugeModule = gaugeModule;
    }

    @Override
    protected void renderDynamic(float scale, float partialTicks) {
        shape1.render(scale);
        shape2.render(scale);

        float pressure = 0f;
        float dangerPressure = 5f;
        float critPressure = 7f;
        if (gaugeModule != null && gaugeModule.getTube() instanceof TileEntityPneumaticBase) {
            TileEntityPneumaticBase base = (TileEntityPneumaticBase) gaugeModule.getTube();
            pressure = base.getPressure();
            critPressure = base.criticalPressure;
            dangerPressure = base.dangerPressure;
        }
        GL11.glTranslated(0, 1, 0.378);
        double widgetScale = 0.007D;
        GL11.glScaled(widgetScale, widgetScale, widgetScale);
        GL11.glRotated(180, 0, 1, 0);
        GL11.glDisable(GL11.GL_LIGHTING);
        GuiUtils.drawPressureGauge(FMLClientHandler.instance().getClient().fontRenderer, -1, critPressure, dangerPressure, -1, pressure, 0, 0, 0);
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.MODEL_GAUGE;
    }
}
