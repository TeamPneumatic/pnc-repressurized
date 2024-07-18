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

package me.desht.pneumaticcraft.common.item.logistics;

import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.entity.semiblock.AbstractLogisticsFrameEntity;
import me.desht.pneumaticcraft.common.inventory.LogisticsMenu;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.semiblock.SemiblockItem;
import me.desht.pneumaticcraft.common.util.FluidFilter;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.api.misc.Symbols.bullet;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class AbstractLogisticsFrameItem extends SemiblockItem {
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn) {
        ItemStack stack = player.getItemInHand(handIn);
        if (player instanceof ServerPlayer sp) {
            sp.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return stack.getHoverName();
                }

                @Override
                public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
                    return new LogisticsMenu(getContainerType(), i, playerInventory, -1);
                }
            }, (buffer) -> buffer.writeVarInt(-1));
        }
        return InteractionResultHolder.success(stack);
    }

    protected abstract MenuType<?> getContainerType();

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> curInfo, TooltipFlag extraInfo) {
        super.appendHoverText(stack, context, curInfo, extraInfo);

        addLogisticsTooltip(stack, context, curInfo, ClientUtils.hasShiftDown());
    }

    public static List<Component> addLogisticsTooltip(ItemStack stack, TooltipContext context, List<Component> curInfo, boolean sneaking) {
        HolderLookup.Provider provider = context.registries();
        if (stack.has(ModDataComponents.SEMIBLOCK_DATA) && stack.getItem() instanceof SemiblockItem && provider != null) {
            if (sneaking) {
                CompoundTag tag = stack.getOrDefault(ModDataComponents.SEMIBLOCK_DATA, CustomData.EMPTY).copyTag();
                if (tag.getBoolean(AbstractLogisticsFrameEntity.NBT_INVISIBLE)) {
                    curInfo.add(bullet().append(xlate("pneumaticcraft.gui.logistics_frame.invisible")).withStyle(ChatFormatting.YELLOW));
                }
                if (tag.getBoolean(AbstractLogisticsFrameEntity.NBT_MATCH_DURABILITY)) {
                    curInfo.add(bullet().append(xlate("pneumaticcraft.gui.logistics_frame.matchDurability")).withStyle(ChatFormatting.YELLOW));
                }
                if (tag.getBoolean(AbstractLogisticsFrameEntity.NBT_MATCH_COMPONENTS)) {
                    curInfo.add(bullet().append(xlate("pneumaticcraft.gui.logistics_frame.matchNBT")).withStyle(ChatFormatting.YELLOW));
                }
                if (tag.getBoolean(AbstractLogisticsFrameEntity.NBT_MATCH_MODID)) {
                    curInfo.add(bullet().append(xlate("pneumaticcraft.gui.logistics_frame.matchModId")).withStyle(ChatFormatting.YELLOW));
                }

                boolean whitelist = tag.getBoolean(AbstractLogisticsFrameEntity.NBT_ITEM_WHITELIST);
                curInfo.add(xlate("pneumaticcraft.gui.logistics_frame." + (whitelist ? "itemWhitelist" : "itemBlacklist"))
                        .append(":").withStyle(ChatFormatting.YELLOW));

                ItemStackHandler handler = new ItemStackHandler();
                handler.deserializeNBT(provider, tag.getCompound(AbstractLogisticsFrameEntity.NBT_ITEM_FILTERS));
                List<ItemStack> stacks = new ArrayList<>();
                for (int i = 0; i < handler.getSlots(); i++) {
                    if (!handler.getStackInSlot(i).isEmpty()) stacks.add(handler.getStackInSlot(i));
                }
                int tooltipSize = curInfo.size();
                PneumaticCraftUtils.summariseItemStacks(curInfo, stacks, Component.literal(Symbols.BULLET + " ").withStyle(ChatFormatting.GOLD));
                if (curInfo.size() == tooltipSize) {
                    curInfo.add(bullet().withStyle(ChatFormatting.GOLD)
                            .append(xlate("pneumaticcraft.gui.misc.no_items").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC)));
                }
                tooltipSize = curInfo.size();

                whitelist = tag.getBoolean(AbstractLogisticsFrameEntity.NBT_FLUID_WHITELIST);
                curInfo.add(xlate("pneumaticcraft.gui.logistics_frame." + (whitelist ? "fluidWhitelist" : "fluidBlacklist"))
                        .append(":").withStyle(ChatFormatting.YELLOW));

                FluidFilter.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag.getCompound(AbstractLogisticsFrameEntity.NBT_FLUID_FILTERS))
                        .ifSuccess(fluidFilter -> {
                            for (int i = 0; i < fluidFilter.size(); i++) {
                                FluidStack fluid = fluidFilter.getFluid(i);
                                if (!fluid.isEmpty()) {
                                    curInfo.add(bullet().append(fluid.getAmount() + "mB ").append(fluid.getHoverName()).withStyle(ChatFormatting.GOLD));
                                }
                            }
                        });

                if (curInfo.size() == tooltipSize) {
                    curInfo.add(bullet().withStyle(ChatFormatting.GOLD)
                            .append(xlate("pneumaticcraft.gui.misc.no_fluids").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC)));
                }
            } else {
                curInfo.add(xlate("pneumaticcraft.gui.logistics_frame.hasFilters"));
            }
        }
        return curInfo;
    }

}
