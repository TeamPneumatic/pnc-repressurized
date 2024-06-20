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
import me.desht.pneumaticcraft.api.upgrade.IUpgradeItem;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.stream.Stream;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class UpgradeItem extends Item implements IUpgradeItem, CreativeTabStackProvider {
    private final PNCUpgrade upgrade;
    private final int tier;

    public UpgradeItem(PNCUpgrade upgrade, int tier, Rarity rarity) {
        this(upgrade, tier, ModItems.defaultProps().rarity(rarity));
    }

    public UpgradeItem(PNCUpgrade upgrade, int tier, Properties properties) {
        super(properties);
        this.upgrade = upgrade;
        this.tier = tier;
    }

    @Override
    public PNCUpgrade getUpgradeType() {
        return upgrade;
    }

    @Override
    public int getUpgradeTier() {
        return tier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> infoList, TooltipFlag par4) {
        if (ClientUtils.hasShiftDown()) {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.item.upgrade.usedIn").withStyle(ChatFormatting.AQUA));
            PneumaticRegistry.getInstance().getUpgradeRegistry().addUpgradeTooltip(upgrade, infoList);
        } else {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.item.upgrade.shiftMessage").withStyle(ChatFormatting.AQUA));
        }
        // FIXME code smell
        if (getUpgradeType() == ModUpgrades.DISPENSER.get()) {
            Direction dir = stack.get(ModDataComponents.EJECT_DIR);
            infoList.add(xlate("pneumaticcraft.message.dispenser.direction", dir == null ?
                    xlate("pneumaticcraft.gui.misc.any") :
                    xlate("pneumaticcraft.gui.tooltip.direction." + dir.getSerializedName()))
            );
            infoList.add(xlate("pneumaticcraft.message.dispenser.clickToSet"));
        }
        super.appendHoverText(stack, context, infoList, par4);
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
            stack.remove(ModDataComponents.EJECT_DIR);
            player.displayClientMessage(Component.translatable("pneumaticcraft.message.dispenser.direction", "*"), true);
        } else {
            stack.set(ModDataComponents.EJECT_DIR, facing);
            player.displayClientMessage(Component.translatable("pneumaticcraft.message.dispenser.direction", facing.getSerializedName()), true);
        }
    }

    public static UpgradeItem of(ItemStack stack) {
        return (UpgradeItem) stack.getItem();
    }

    @Override
    public Stream<ItemStack> getStacksForItem() {
        return Stream.of(new ItemStack(this));
    }
}
