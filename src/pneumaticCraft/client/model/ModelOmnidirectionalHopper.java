package pneumaticCraft.client.model;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.tileentity.TileEntityOmnidirectionalHopper;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class ModelOmnidirectionalHopper extends BaseModel{

    private static final String[] parts = new String[]{"Wall1", "Wall2", "Wall3", "Wall4", "Funnel", "Funnel2"};

    public ModelOmnidirectionalHopper(){
        super("omnidirectionalHopper.tcn");
    }

    @Override
    public void renderStatic(float size, TileEntity tile){
        GL11.glPushMatrix();
        TileEntityOmnidirectionalHopper te = null;
        GL11.glScaled(16, 16, 16);
        GL11.glTranslated(0, -24 / 16D, 0);
        if(tile instanceof TileEntityOmnidirectionalHopper) {
            te = (TileEntityOmnidirectionalHopper)tile;
            PneumaticCraftUtils.rotateMatrixByMetadata(te.getDirection().getOpposite().ordinal());
        } else {
            PneumaticCraftUtils.rotateMatrixByMetadata(ForgeDirection.DOWN.ordinal());
        }
        GL11.glTranslated(0, 24 / 16D, 0);
        GL11.glScaled(1 / 16D, 1 / 16D, 1 / 16D);
        for(String part : parts)
            model.renderPart(part);
        GL11.glPopMatrix();

        GL11.glScaled(16, 16, 16);
        GL11.glTranslated(0, -24 / 16D, 0);
        if(te != null) {
            PneumaticCraftUtils.rotateMatrixByMetadata(te.getBlockMetadata());
        } else {
            PneumaticCraftUtils.rotateMatrixByMetadata(ForgeDirection.DOWN.ordinal());
        }
        GL11.glTranslated(0, 24 / 16D, 0);
        GL11.glScaled(1 / 16D, 1 / 16D, 1 / 16D);
        model.renderPart("InserterBottom");

    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

}
