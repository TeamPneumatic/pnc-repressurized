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

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.lib.NBTKeys;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.ItemSemiBlock;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

import static me.desht.pneumaticcraft.api.misc.Symbols.bullet;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class ItemLogisticsFrame extends ItemSemiBlock {
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn) {
        ItemStack stack = player.getItemInHand(handIn);
        if (!world.isClientSide) {
            NetworkHooks.openGui((ServerPlayer) player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return stack.getHoverName();
                }

                @Override
                public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                    return new ContainerLogistics(getContainerType(), i, playerInventory, -1);
                }
            }, (buffer) -> buffer.writeVarInt(-1));
        }
        return InteractionResultHolder.success(stack);
    }

    protected abstract MenuType<?> getContainerType();

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> curInfo, TooltipFlag extraInfo) {
        super.appendHoverText(stack, worldIn, curInfo, extraInfo);

        addLogisticsTooltip(stack, worldIn, curInfo, ClientUtils.hasShiftDown());
    }

    public static List<Component> addLogisticsTooltip(ItemStack stack, Level world, List<Component> curInfo, boolean sneaking) {
        if (stack.getTag() != null && stack.getTag().contains(NBTKeys.ENTITY_TAG) && stack.getItem() instanceof ItemSemiBlock) {
            if (sneaking) {
                CompoundTag tag = stack.getTag().getCompound(NBTKeys.ENTITY_TAG);
                if (tag.getBoolean(EntityLogisticsFrame.NBT_INVISIBLE)) {
                    curInfo.add(bullet().append(xlate("pneumaticcraft.gui.logistics_frame.invisible")).withStyle(ChatFormatting.YELLOW));
                }
                if (tag.getBoolean(EntityLogisticsFrame.NBT_MATCH_DURABILITY)) {
                    curInfo.add(bullet().append(xlate("pneumaticcraft.gui.logistics_frame.matchDurability")).withStyle(ChatFormatting.YELLOW));
                }
                if (tag.getBoolean(EntityLogisticsFrame.NBT_MATCH_NBT)) {
                    curInfo.add(bullet().append(xlate("pneumaticcraft.gui.logistics_frame.matchNBT")).withStyle(ChatFormatting.YELLOW));
                }
                if (tag.getBoolean(EntityLogisticsFrame.NBT_MATCH_MODID)) {
                    curInfo.add(bullet().append(xlate("pneumaticcraft.gui.logistics_frame.matchModId")).withStyle(ChatFormatting.YELLOW));
                }

                boolean whitelist = tag.getBoolean(EntityLogisticsFrame.NBT_ITEM_WHITELIST);
                curInfo.add(xlate("pneumaticcraft.gui.logistics_frame." + (whitelist ? "itemWhitelist" : "itemBlacklist"))
                        .append(":").withStyle(ChatFormatting.YELLOW));

                ItemStackHandler handler = new ItemStackHandler();
                handler.deserializeNBT(tag.getCompound(EntityLogisticsFrame.NBT_ITEM_FILTERS));
                ItemStack[] stacks = new ItemStack[handler.getSlots()];
                for (int i = 0; i < handler.getSlots(); i++) {
                    stacks[i] = handler.getStackInSlot(i);
                }
                int l = curInfo.size();
                PneumaticCraftUtils.summariseItemStacks(curInfo, stacks, ChatFormatting.GOLD + Symbols.BULLET + " ");
                if (curInfo.size() == l) curInfo.add(bullet().withStyle(ChatFormatting.GOLD).append(xlate("pneumaticcraft.gui.misc.no_items").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC)));
                l = curInfo.size();


                whitelist = tag.getBoolean(EntityLogisticsFrame.NBT_FLUID_WHITELIST);
                curInfo.add(xlate("pneumaticcraft.gui.logistics_frame." + (whitelist ? "fluidWhitelist" : "fluidBlacklist"))
                        .append(":").withStyle(ChatFormatting.YELLOW));

                EntityLogisticsFrame.FluidFilter fluidFilter = new EntityLogisticsFrame.FluidFilter();
                fluidFilter.deserializeNBT(tag.getCompound(EntityLogisticsFrame.NBT_FLUID_FILTERS));
                for (int i = 0; i < fluidFilter.size(); i++) {
                    FluidStack fluid = fluidFilter.get(i);
                    if (!fluid.isEmpty()) {
                        curInfo.add(bullet().append(fluid.getAmount() + "mB ").append(fluid.getDisplayName()).withStyle(ChatFormatting.GOLD));
                    }
                }
                if (curInfo.size() == l) curInfo.add(bullet().withStyle(ChatFormatting.GOLD).append(xlate("pneumaticcraft.gui.misc.no_fluids").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC)));
            } else {
                curInfo.add(xlate("pneumaticcraft.gui.logistics_frame.hasFilters"));
            }
        }
        return curInfo;
    }

}
