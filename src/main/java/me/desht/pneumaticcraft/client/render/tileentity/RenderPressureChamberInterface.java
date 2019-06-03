package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelPressureChamberInterface;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberInterface;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.ResourceLocation;

public class RenderPressureChamberInterface extends AbstractModelRenderer<TileEntityPressureChamberInterface> {
    private final ModelPressureChamberInterface model;

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
            EntityItem ghostEntityItem = null;
            if (!te.getStackInInterface().isEmpty()) {
                ghostEntityItem = new EntityItem(te.getWorld());
                ghostEntityItem.hoverStart = 0.0F;
                ghostEntityItem.setItem(te.getStackInInterface());
            }
            RenderUtils.rotateMatrixByMetadata(te.getRotation().ordinal());
            model.renderModel(0.0625f, te, partialTicks, ghostEntityItem);
        }
    }
}
