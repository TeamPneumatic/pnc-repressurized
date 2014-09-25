package pneumaticCraft.client.model;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.tileentity.TileEntityPneumaticDoorBase;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Textures;

public class ModelPneumaticDoorBase extends ModelBase implements IBaseModel{
    //fields
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape4;
    ModelRenderer Shape5;
    ModelRenderer Cilinder1;
    ModelRenderer Cilinder2;
    ModelRenderer Cilinder3;

    public ModelPneumaticDoorBase(){
        textureWidth = 64;
        textureHeight = 64;

        Shape1 = new ModelRenderer(this, 0, 0);
        Shape1.addBox(0F, 0F, 0F, 16, 12, 16);
        Shape1.setRotationPoint(-8F, 12F, -8F);
        Shape1.setTextureSize(64, 32);
        Shape1.mirror = true;
        setRotation(Shape1, 0F, 0F, 0F);
        Shape2 = new ModelRenderer(this, 0, 0);
        Shape2.addBox(0F, 0F, 0F, 3, 4, 16);
        Shape2.setRotationPoint(-8F, 8F, -8F);
        Shape2.setTextureSize(64, 32);
        Shape2.mirror = true;
        setRotation(Shape2, 0F, 0F, 0F);
        Shape4 = new ModelRenderer(this, 0, 0);
        Shape4.addBox(0F, 0F, 0F, 13, 4, 2);
        Shape4.setRotationPoint(-5F, 8F, -8F);
        Shape4.setTextureSize(64, 32);
        Shape4.mirror = true;
        setRotation(Shape4, 0F, 0F, 0F);
        Shape5 = new ModelRenderer(this, 0, 0);
        Shape5.addBox(0F, 0F, 0F, 2, 4, 14);
        Shape5.setRotationPoint(6F, 8F, -6F);
        Shape5.setTextureSize(64, 32);
        Shape5.mirror = true;
        setRotation(Shape5, 0F, 0F, 0F);
        Cilinder1 = new ModelRenderer(this, 0, 28);
        Cilinder1.addBox(0F, 0F, 0F, 3, 3, 10);
        Cilinder1.setRotationPoint(2.5F, 8.5F, -6F);
        Cilinder1.setTextureSize(64, 32);
        Cilinder1.mirror = true;
        setRotation(Cilinder1, 0F, 0F, 0F);
        Cilinder2 = new ModelRenderer(this, 0, 28);
        Cilinder2.addBox(0F, 0F, 0F, 2, 2, 10);
        Cilinder2.setRotationPoint(3F, 9F, -6F);
        Cilinder2.setTextureSize(64, 32);
        Cilinder2.mirror = true;
        setRotation(Cilinder2, 0F, 0F, 0F);
        Cilinder3 = new ModelRenderer(this, 0, 28);
        Cilinder3.addBox(0F, 0F, 0F, 1, 1, 10);
        Cilinder3.setRotationPoint(3.5F, 9.5F, -6F);
        Cilinder3.setTextureSize(64, 32);
        Cilinder3.mirror = true;
        setRotation(Cilinder3, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Shape1.render(f5);
        Shape2.render(f5);
        Shape4.render(f5);
        Shape5.render(f5);
        Cilinder1.render(f5);
        Cilinder2.render(f5);
        Cilinder3.render(f5);
    }

    public void renderModel(float size, float progress, boolean renderBase, boolean rightGoing){
        if(renderBase) {
            Shape1.render(size);
            Shape2.render(size);
            Shape4.render(size);
            Shape5.render(size);
        }
        float cosinus = /*12 / 16F -*/(float)Math.sin(Math.toRadians((1 - progress) * 90)) * 12 / 16F;
        float sinus = 9 / 16F - (float)Math.cos(Math.toRadians((1 - progress) * 90)) * 9 / 16F;
        double extension = Math.sqrt(Math.pow(sinus, 2) + Math.pow(cosinus + 4 / 16F, 2));
        //System.out.println("sinus: " + sinus);
        GL11.glTranslated(((rightGoing ? -4 : 0) + 2.5) / 16F, 0, -6 / 16F);
        double cilinderAngle = Math.toDegrees(Math.atan(sinus / (cosinus + 14 / 16F)));
        GL11.glRotated(cilinderAngle, 0, rightGoing ? 1 : -1, 0);
        GL11.glTranslated(((rightGoing ? -3 : 0) - 2.5) / 16F, 0, 6 / 16F);
        double extensionPart = extension * 0.5D;
        Cilinder1.render(size);
        GL11.glTranslated(0, 0, extensionPart);
        Cilinder2.render(size);
        GL11.glTranslated(0, 0, extensionPart);
        Cilinder3.render(size);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z){
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void renderStatic(float size, TileEntity tile){

    }

    @Override
    public ResourceLocation getModelTexture(TileEntity tile){
        return Textures.MODEL_PNEUMATIC_DOOR_BASE;
    }

    @Override
    public boolean rotateModelBasedOnBlockMeta(){
        return false;
    }

    @Override
    public void renderDynamic(float size, TileEntity tile, float partialTicks){
        if(tile instanceof TileEntityPneumaticDoorBase) {
            TileEntityPneumaticDoorBase door = (TileEntityPneumaticDoorBase)tile;
            ItemStack camoStack = door.getStackInSlot(TileEntityPneumaticDoorBase.CAMO_SLOT);
            boolean renderBase = true;
            if(camoStack != null && camoStack.getItem() instanceof ItemBlock) {
                Block block = Block.getBlockFromItem(camoStack.getItem());
                renderBase = !PneumaticCraftUtils.isRenderIDCamo(block.getRenderType());
            }
            PneumaticCraftUtils.rotateMatrixByMetadata(door.orientation.ordinal());
            renderModel(size, door.oldProgress + (door.progress - door.oldProgress) * partialTicks, renderBase, ((TileEntityPneumaticDoorBase)tile).rightGoing);
        } else {
            renderModel(size, 1, true, false);
        }
    }

}
