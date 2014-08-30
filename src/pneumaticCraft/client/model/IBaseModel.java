package pneumaticCraft.client.model;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public interface IBaseModel{

    /**
     * Main render method
     * @param size
     * @param tile TileEntity that is being rendered. Warning: This can be null (in itemrendering for example)
     * @param partialTicks is 0 when item rendering.
     */
    public void renderModel(float size, TileEntity tile, float partialTicks);

    public ResourceLocation getModelTexture();

    public boolean rotateModelBasedOnBlockMeta();
}
