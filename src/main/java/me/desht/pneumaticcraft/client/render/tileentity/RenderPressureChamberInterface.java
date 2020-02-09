package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.block.ModelPressureChamberInterface;
import me.desht.pneumaticcraft.client.render.StaticItemRenderer;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.ResourceLocation;

public class RenderPressureChamberInterface extends AbstractTileModelRenderer<TileEntityPressureChamberInterface> {
    private final ModelPressureChamberInterface model;
    private StaticItemRenderer customRenderItem = null;

    public RenderPressureChamberInterface() {
        model = new ModelPressureChamberInterface();
    }

    @Override
    ResourceLocation getTexture(TileEntityPressureChamberInterface te) {
        return Textures.MODEL_PRESSURE_CHAMBER_INTERFACE;
    }

    @Override
    void renderModel(TileEntityPressureChamberInterface te, float partialTicks) {
        if (te != null) {
            ItemEntity ghostEntityItem = null;
            if (!te.getStackInInterface().isEmpty()) {
                ghostEntityItem = new ItemEntity(EntityType.ITEM, te.getWorld());
                ghostEntityItem.setItem(te.getStackInInterface());
            }
            RenderUtils.rotateMatrixForDirection(te.getRotation());
            model.renderModel(0.0625f, te, partialTicks);


            if (ghostEntityItem != null) {
                if (customRenderItem == null) {
                    customRenderItem = new StaticItemRenderer();
                }

//            float zOff = 0f;
//            if (te.interfaceMode == TileEntityPressureChamberInterface.InterfaceDirection.IMPORT && renderOutputProgress >= MAX_PROGRESS - 5) {
//                // render item moving out of the interface into the chamber (always in +Z direction due to matrix rotation)
//                zOff = (1.0f - (MAX_PROGRESS - renderOutputProgress) + partialTicks) / 3f;
//            }
//
//            GlStateManager.translated(0, 1.25f, zOff);
                GlStateManager.scaled(1.0F, -1F, -1F);

                EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
                boolean fancySetting = renderManager.options.fancyGraphics;
                renderManager.options.fancyGraphics = true;
                customRenderItem.doRender(ghostEntityItem, 0, 0, 0, 0, 0);
                renderManager.options.fancyGraphics = fancySetting;
            }
        }
    }
}
