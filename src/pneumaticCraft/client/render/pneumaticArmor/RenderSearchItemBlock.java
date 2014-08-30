package pneumaticCraft.client.render.pneumaticArmor;

import java.util.List;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.common.item.ItemPneumaticArmor;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import cpw.mods.fml.client.FMLClientHandler;

public class RenderSearchItemBlock{

    int blockX;
    int blockY;
    int blockZ;
    World world;

    public RenderSearchItemBlock(World world, int x, int y, int z){
        this.world = world;
        blockX = x;
        blockY = y;
        blockZ = z;
    }

    private int getSearchedItemCount(){
        if(world.getTileEntity(blockX, blockY, blockZ) instanceof IInventory) {
            int itemCount = 0;
            IInventory inventory = (IInventory)world.getTileEntity(blockX, blockY, blockZ);
            ItemStack searchStack = ItemPneumaticArmor.getSearchedStack(FMLClientHandler.instance().getClient().thePlayer.getCurrentArmor(3));
            if(searchStack == null) return 0;
            for(int l = 0; l < inventory.getSizeInventory(); l++) {
                if(inventory.getStackInSlot(l) != null) {
                    itemCount += getSearchedItemCount(inventory.getStackInSlot(l), searchStack);
                }
            }
            return itemCount;
        }
        return 0;
    }

    public static int getSearchedItemCount(ItemStack stack, ItemStack searchStack){
        int itemCount = 0;
        if(stack.isItemEqual(searchStack)) {
            itemCount += stack.stackSize;
        }
        List<ItemStack> inventoryItems = PneumaticCraftUtils.getStacksInItem(stack);
        for(ItemStack s : inventoryItems) {
            itemCount += getSearchedItemCount(s, searchStack);
        }
        return itemCount;
    }

    public boolean isAlreadyTrackingCoord(int x, int y, int z){
        return blockX == x && blockY == y && blockZ == z;
    }

    public boolean renderSearchBlock(int totalCount){
        int itemCount = getSearchedItemCount();
        renderSearch(blockX + 0.5D, blockY + 0.5D, blockZ + 0.5D, itemCount, totalCount);
        return itemCount > 0;
    }

    public static void renderSearch(double x, double y, double z, int itemCount, int totalCount){
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glColor4d(0, 1, 0, 0.5D);
        GL11.glRotatef(180.0F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(180.0F - RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
        // GL11.glLineWidth(1.0F);
        double ratio = (double)itemCount / totalCount;
        double diff = (1 - ratio) / 1.5D;
        double size = 1 - diff;
        /*
        for(double i = size; i > 0; i -= 0.06D) {
            GL11.glPushMatrix();
            GL11.glScaled(i, i, i);
            renderCircle();
            GL11.glPopMatrix();
        }
        */
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_QUADS);
        tess.addVertexWithUV(-size, size, 0, 0, 1);
        tess.addVertexWithUV(-size, -size, 0, 0, 0);
        tess.addVertexWithUV(size, -size, 0, 1, 0);
        tess.addVertexWithUV(size, size, 0, 1, 1);

        tess.draw();

        GL11.glPopMatrix();
    }
    /*
        private static void renderCircle(){

            Tessellator tess = Tessellator.instance;
            tess.startDrawing(GL11.GL_POLYGON);
            for(int i = 0; i < PneumaticCraftUtils.circlePoints; i++) {
                tess.addVertex(PneumaticCraftUtils.sin[i], PneumaticCraftUtils.cos[i], 0);
            }

            tess.draw();

        }
        */
}
