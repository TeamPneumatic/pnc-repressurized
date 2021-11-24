/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.render.pneumatic_armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
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
            IOHelper.getInventoryForTE(world.getBlockEntity(pos)).ifPresent(handler -> {
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
        List<ItemStack> inventoryItems = ItemRegistry.getInstance().getStacksInItem(stack);
        for (ItemStack s : inventoryItems) {
            itemCount += getSearchedItemCount(s, item);
        }
        return itemCount;
    }

    public void renderSearchBlock(MatrixStack matrixStack, IVertexBuilder builder, int totalCount, float partialTicks) {
        renderSearch(matrixStack, builder,pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, getSearchedItemCount(), totalCount, partialTicks);
    }

    public static void renderSearch(MatrixStack matrixStack, IVertexBuilder builder, double x, double y, double z, int itemCount, int totalCount, float partialTicks) {
        matrixStack.pushPose();

        matrixStack.translate(x, y, z);
        RenderUtils.rotateToPlayerFacing(matrixStack);
        float ratio = (float) itemCount / totalCount;
        float diff = (1 - ratio) / 1.5F;
        float size = 1 - diff;
        float f = ((Minecraft.getInstance().level.getGameTime() & 0x1f) + partialTicks) / 5.092f;  // 0 .. 2*pi every 32 ticks
        float alpha = 0.65F + MathHelper.sin(f) * 0.15f;
        Matrix4f posMat = matrixStack.last().pose();
        builder.vertex(posMat, -size, size, 0).color(0, 1, 0, alpha).uv(0, 1).uv2(FULL_BRIGHT).endVertex();
        builder.vertex(posMat, size, size, 0).color(0, 1, 0, alpha).uv(1, 1).uv2(FULL_BRIGHT).endVertex();
        builder.vertex(posMat, size, -size, 0).color(0, 1, 0, alpha).uv(1, 0).uv2(FULL_BRIGHT).endVertex();
        builder.vertex(posMat, -size, -size, 0).color(0, 1, 0, alpha).uv(0, 0).uv2(FULL_BRIGHT).endVertex();

        matrixStack.popPose();
    }
}
