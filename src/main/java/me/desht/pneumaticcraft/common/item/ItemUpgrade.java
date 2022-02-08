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

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IUpgradeItem;
import me.desht.pneumaticcraft.api.item.PNCUpgrade;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModUpgrades;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemUpgrade extends Item implements IUpgradeItem {
    public static final String NBT_DIRECTION = "Facing";

    private final Supplier<PNCUpgrade> upgrade;
    private final int tier;

    public ItemUpgrade(Supplier<PNCUpgrade> upgrade, int tier) {
        super(ModItems.defaultProps());
        this.upgrade = upgrade;
        this.tier = tier;
    }

    @Override
    public PNCUpgrade getUpgradeType() {
        return upgrade.get();
    }

    @Override
    public int getUpgradeTier() {
        return tier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> infoList, TooltipFlag par4) {
        if (ClientUtils.hasShiftDown()) {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.item.upgrade.usedIn").withStyle(ChatFormatting.AQUA));
            PneumaticRegistry.getInstance().getItemRegistry().addTooltip(upgrade.get(), infoList);
        } else {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.item.upgrade.shiftMessage").withStyle(ChatFormatting.AQUA));
        }
        if (getUpgradeType() == ModUpgrades.DISPENSER.get()) {
            Direction dir = stack.hasTag() ? Direction.byName(NBTUtils.getString(stack, NBT_DIRECTION)) : null;
            infoList.add(xlate("pneumaticcraft.message.dispenser.direction", dir == null ?
                    xlate("pneumaticcraft.gui.misc.any") :
                    xlate("pneumaticcraft.gui.tooltip.direction." + dir.getSerializedName()))
            );
            infoList.add(xlate("pneumaticcraft.message.dispenser.clickToSet"));
        }
        super.appendHoverText(stack, world, infoList, par4);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (getUpgradeType() == ModUpgrades.DISPENSER.get()) {
            if (context.getPlayer() instanceof ServerPlayer sp) {
                setDirection(sp, context.getHand(), context.getClickedFace());
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (getUpgradeType() == ModUpgrades.DISPENSER.get()) {
            if (playerIn instanceof ServerPlayer sp) {
                setDirection(sp, handIn, null);
            }
            return InteractionResultHolder.success(playerIn.getItemInHand(handIn));
        }
        return super.use(worldIn, playerIn, handIn);
    }

    private void setDirection(ServerPlayer player, InteractionHand hand, Direction facing) {
        ItemStack stack = player.getItemInHand(hand);
        if (facing == null) {
            stack.setTag(null);
            player.displayClientMessage(new TranslatableComponent("pneumaticcraft.message.dispenser.direction", "*"), true);
        } else {
            NBTUtils.setString(stack, NBT_DIRECTION, facing.getSerializedName());
            player.displayClientMessage(new TranslatableComponent("pneumaticcraft.message.dispenser.direction", facing.getSerializedName()), true);
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return getUpgradeType() == ModUpgrades.CREATIVE.get() ? Rarity.EPIC : Rarity.COMMON;
    }

    public static ItemUpgrade of(ItemStack stack) {
        return (ItemUpgrade) stack.getItem();
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            if (upgrade.get().isDependencyLoaded()) {
                items.add(new ItemStack(this));
            }
        }
    }
}
