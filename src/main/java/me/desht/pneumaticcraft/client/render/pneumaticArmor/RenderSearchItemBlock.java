package me.desht.pneumaticcraft.client.render.pneumaticArmor;

import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class RenderSearchItemBlock {

    private final BlockPos pos;
    private final World world;

    public RenderSearchItemBlock(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    private int getSearchedItemCount() {
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
//        if (world.getTileEntity(pos) instanceof IInventory) {
            IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            int itemCount = 0;
//            IInventory inventory = (IInventory) world.getTileEntity(pos);
            ItemStack searchStack = ItemPneumaticArmor.getSearchedStack(FMLClientHandler.instance().getClient().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
            if (searchStack.isEmpty()) return 0;
            for (int l = 0; l < handler.getSlots(); l++) {
                if (!handler.getStackInSlot(l).isEmpty()) {
                    itemCount += getSearchedItemCount(handler.getStackInSlot(l), searchStack);
                }
            }
            return itemCount;
        }
        return 0;
    }

    public static int getSearchedItemCount(ItemStack stack, ItemStack searchStack) {
        int itemCount = 0;
        if (stack.isItemEqual(searchStack)) {
            itemCount += stack.getCount();
        }
        List<ItemStack> inventoryItems = PneumaticCraftUtils.getStacksInItem(stack);
        for (ItemStack s : inventoryItems) {
            itemCount += getSearchedItemCount(s, searchStack);
        }
        return itemCount;
    }

    public boolean isAlreadyTrackingCoord(BlockPos pos) {
        return pos.equals(this.pos);
    }

    public boolean renderSearchBlock(int totalCount) {
        int itemCount = getSearchedItemCount();
        renderSearch(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemCount, totalCount);
        return itemCount > 0;
    }

    public static void renderSearch(double x, double y, double z, int itemCount, int totalCount) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glColor4d(0, 1, 0, 0.5D);
        GL11.glRotatef(180.0F - Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(180.0F - Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        // GL11.glLineWidth(1.0F);
        double ratio = (double) itemCount / totalCount;
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
        BufferBuilder wr = Tessellator.getInstance().getBuffer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(-size, size, 0).tex(0, 1).endVertex();
        wr.pos(-size, -size, 0).tex(0, 0).endVertex();
        wr.pos(size, -size, 0).tex(1, 0).endVertex();
        wr.pos(size, size, 0).tex(1, 1).endVertex();

        Tessellator.getInstance().draw();

        GL11.glPopMatrix();
    }
    /*
        private static void renderCircle(){

            BufferBuilder wr = Tessellator.getInstance()getBuffer();
            tess.startDrawing(GL11.GL_POLYGON);
            for(int i = 0; i < PneumaticCraftUtils.circlePoints; i++) {
                wr.pos(PneumaticCraftUtils.sin[i], PneumaticCraftUtils.cos[i], 0);
            }

            Tessellator.getInstance().draw();

        }
        */
}
