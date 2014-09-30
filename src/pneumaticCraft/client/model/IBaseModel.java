package pneumaticCraft.client.model;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public interface IBaseModel{

    /**
     * Main render method
     * @param size
     * @param tile TileEntity that is being rendered. Warning: This can be null (in itemrendering for example)
     */
    public void renderStatic(float size, TileEntity te);

    public void renderDynamic(float size, TileEntity te, float partialTicks);

    public ResourceLocation getModelTexture(TileEntity tile);

    public boolean rotateModelBasedOnBlockMeta();
}
