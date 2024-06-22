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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.item.ItemRegistry;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;

import java.util.List;

public class RenderSearchItemBlock {

    private final BlockPos pos;
    private final Level world;
    private long lastCheck = 0;
    private int cachedAmount;

    public RenderSearchItemBlock(Level world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public int getSearchedItemCount() {
        // this gets called every frame from the render methods, so some caching is desirable...
        if (world.getGameTime() - lastCheck >= 20) {
            cachedAmount = 0;
            IOHelper.getInventoryForBlock(world.getBlockEntity(pos)).ifPresent(handler -> {
                int itemCount = 0;
                Item searchedItem = PneumaticArmorItem.getSearchedItem(ClientUtils.getWornArmor(EquipmentSlot.HEAD));
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

    public void renderSearchBlock(PoseStack matrixStack, VertexConsumer builder, int totalCount, float partialTicks) {
        renderSearch(matrixStack, builder,pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, getSearchedItemCount(), totalCount, partialTicks);
    }

    public static void renderSearch(PoseStack matrixStack, VertexConsumer builder, double x, double y, double z, int itemCount, int totalCount, float partialTicks) {
        matrixStack.pushPose();

        matrixStack.translate(x, y, z);
        RenderUtils.rotateToPlayerFacing(matrixStack);
        float ratio = (float) itemCount / totalCount;
        float diff = (1 - ratio) / 1.5F;
        float size = 1 - diff;
        float f = ((ClientUtils.getClientLevel().getGameTime() & 0x1f) + partialTicks) / 5.092f;  // 0 .. 2*pi every 32 ticks
        float alpha = 0.65F + Mth.sin(f) * 0.15f;
        Matrix4f posMat = matrixStack.last().pose();
        builder.addVertex(posMat, -size, size, 0)
                .setColor(0, 1, 0, alpha)
                .setUv(0, 1)
                .setLight(LightTexture.FULL_BRIGHT);
        builder.addVertex(posMat, size, size, 0)
                .setColor(0, 1, 0, alpha)
                .setUv(1, 1)
                .setLight(LightTexture.FULL_BRIGHT);
        builder.addVertex(posMat, size, -size, 0)
                .setColor(0, 1, 0, alpha)
                .setUv(1, 0)
                .setLight(LightTexture.FULL_BRIGHT);
        builder.addVertex(posMat, -size, -size, 0)
                .setColor(0, 1, 0, alpha)
                .setUv(0, 0)
                .setLight(LightTexture.FULL_BRIGHT);

        matrixStack.popPose();
    }
}
