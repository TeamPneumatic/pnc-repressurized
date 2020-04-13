package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

import static me.desht.pneumaticcraft.client.util.RenderUtils.FULL_BRIGHT;

public class RenderSearchItemBlock {

    private final BlockPos pos;
    private final World world;
    private long lastCheck = 0;
    private int cachedAmount;

    public RenderSearchItemBlock(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public int getSearchedItemCount() {
        // this gets called every frame from the render methods, so some caching is desirable...
        if (world.getGameTime() - lastCheck >= 20) {
            cachedAmount = 0;
            IOHelper.getInventoryForTE(world.getTileEntity(pos)).ifPresent(handler -> {
                int itemCount = 0;
                Item searchedItem = ItemPneumaticArmor.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlotType.HEAD));
                if (searchedItem != null) {
                    for (int l = 0; l < handler.getSlots(); l++) {
                        if (!handler.getStackInSlot(l).isEmpty()) {
                            itemCount += getSearchedItemCount(handler.getStackInSlot(l), searchedItem);
                        }
                    }
                }
                cachedAmount = itemCount;
            });
            lastCheck = world.getGameTime();
        }
        return cachedAmount;
    }

    public static int getSearchedItemCount(ItemStack stack, Item item) {
        int itemCount = 0;
        if (stack.getItem() == item) {
            itemCount += stack.getCount();
        }
        List<ItemStack> inventoryItems = PneumaticCraftUtils.getStacksInItem(stack);
        for (ItemStack s : inventoryItems) {
            itemCount += getSearchedItemCount(s, item);
        }
        return itemCount;
    }

    public void renderSearchBlock(MatrixStack matrixStack, IVertexBuilder builder, int totalCount, float partialTicks) {
        renderSearch(matrixStack, builder,pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, getSearchedItemCount(), totalCount, partialTicks);
    }

    public static void renderSearch(MatrixStack matrixStack, IVertexBuilder builder, double x, double y, double z, int itemCount, int totalCount, float partialTicks) {
        matrixStack.push();

        matrixStack.translate(x, y, z);
        RenderUtils.rotateToPlayerFacing(matrixStack);
        float ratio = (float) itemCount / totalCount;
        float diff = (1 - ratio) / 1.5F;
        float size = 1 - diff;
        float f = ((Minecraft.getInstance().world.getGameTime() & 0x1f) + partialTicks) / 5.092f;  // 0 .. 2*pi every 32 ticks
        float alpha = 0.65F + MathHelper.sin(f) * 0.15f;
        Matrix4f posMat = matrixStack.getLast().getMatrix();
        builder.pos(posMat, -size, size, 0).color(0, 1, 0, alpha).tex(0, 1).lightmap(FULL_BRIGHT).endVertex();
        builder.pos(posMat, size, size, 0).color(0, 1, 0, alpha).tex(1, 1).lightmap(FULL_BRIGHT).endVertex();
        builder.pos(posMat, size, -size, 0).color(0, 1, 0, alpha).tex(1, 0).lightmap(FULL_BRIGHT).endVertex();
        builder.pos(posMat, -size, -size, 0).color(0, 1, 0, alpha).tex(0, 0).lightmap(FULL_BRIGHT).endVertex();

        matrixStack.pop();
    }
}
