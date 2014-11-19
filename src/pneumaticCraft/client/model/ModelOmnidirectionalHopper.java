package pneumaticCraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.tileentity.TileEntityOmnidirectionalHopper;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class ModelOmnidirectionalHopper extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Wall1;
    ModelRenderer Wall2;
    ModelRenderer Wall3;
    ModelRenderer Wall4;
    ModelRenderer Funnel;
    ModelRenderer Funnel2;
    ModelRenderer InserterBottom;
    private final ResourceLocation texture;

    public ModelOmnidirectionalHopper(ResourceLocation texture){
        this.texture = texture;
        textureWidth = 64;
        textureHeight = 64;

        Wall1 = new ModelRenderer(this, 48, 0);
        Wall1.addBox(0F, 0F, 0F, 2, 16, 5);
        Wall1.setRotationPoint(-8F, 8F, -8F);
        Wall1.setTextureSize(64, 64);
        Wall1.mirror = true;
        setRotation(Wall1, 0F, 0F, 0F);
        Wall2 = new ModelRenderer(this, 34, 0);
        Wall2.addBox(0F, 0F, 0F, 2, 16, 5);
        Wall2.setRotationPoint(6F, 8F, -8F);
        Wall2.setTextureSize(64, 64);
        Wall2.mirror = true;
        setRotation(Wall2, 0F, 0F, 0F);
        Wall3 = new ModelRenderer(this, 0, 24);
        Wall3.addBox(0F, 0F, 0F, 12, 2, 5);
        Wall3.setRotationPoint(-6F, 8F, -8F);
        Wall3.setTextureSize(64, 64);
        Wall3.mirror = true;
        setRotation(Wall3, 0F, 0F, 0F);
        Wall4 = new ModelRenderer(this, 0, 17);
        Wall4.addBox(0F, 0F, 0F, 12, 2, 5);
        Wall4.setRotationPoint(-6F, 22F, -8F);
        Wall4.setTextureSize(64, 64);
        Wall4.mirror = true;
        setRotation(Wall4, 0F, 0F, 0F);
        Funnel = new ModelRenderer(this, 0, 0);
        Funnel.addBox(0F, 0F, 0F, 16, 16, 1);
        Funnel.setRotationPoint(-8F, 8F, -3F);
        Funnel.setTextureSize(64, 64);
        Funnel.mirror = true;
        setRotation(Funnel, 0F, 0F, 0F);
        Funnel2 = new ModelRenderer(this, 0, 31);
        Funnel2.addBox(0F, 0F, 0F, 8, 8, 6);
        Funnel2.setRotationPoint(-4F, 12F, -2F);
        Funnel2.setTextureSize(64, 64);
        Funnel2.mirror = true;
        setRotation(Funnel2, 0F, 0F, 0F);
        InserterBottom = new ModelRenderer(this, 34, 29);
        InserterBottom.addBox(0F, 0F, 0F, 4, 4, 4);
        InserterBottom.setRotationPoint(-2F, 14F, 4F);
        InserterBottom.setTextureSize(64, 64);
        InserterBottom.mirror = true;
        setRotation(InserterBottom, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Wall1.render(f5);
        Wall2.render(f5);
        Wall3.render(f5);
        Wall4.render(f5);
        Funnel.render(f5);
        Funnel2.render(f5);
        InserterBottom.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity tile){
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4d(1, 1, 1, 1);
        TileEntityOmnidirectionalHopper te = null;

        if(tile instanceof TileEntityOmnidirectionalHopper) {
            te = (TileEntityOmnidirectionalHopper)tile;
            PneumaticCraftUtils.rotateMatrixByMetadata(te.getDirection().getOpposite().ordinal());
        } else {
            PneumaticCraftUtils.rotateMatrixByMetadata(ForgeDirection.DOWN.ordinal());
        }

        Wall1.render(size);
        Wall2.render(size);
        Wall3.render(size);
        Wall4.render(size);
        Funnel.render(size);
        Funnel2.render(size);
        renderMain(te);

        GL11.glPopMatrix();

        if(te != null) {
            PneumaticCraftUtils.rotateMatrixByMetadata(te.getBlockMetadata());
        } else {
            PneumaticCraftUtils.rotateMatrixByMetadata(ForgeDirection.DOWN.ordinal());
        }
        InserterBottom.render(size);
        renderBottom(te);
        GL11.glDisable(GL11.GL_BLEND);
    }

    protected void renderMain(TileEntityOmnidirectionalHopper hopper){}

    protected void renderBottom(TileEntityOmnidirectionalHopper hopper){}

    @Override
    public void renderDynamic(float size, TileEntity te, float partialTicks){

    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return texture;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

}
