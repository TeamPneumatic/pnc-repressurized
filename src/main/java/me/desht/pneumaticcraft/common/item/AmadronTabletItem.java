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

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.common.amadron.ImmutableBasket;
import me.desht.pneumaticcraft.common.amadron.ShoppingBasket;
import me.desht.pneumaticcraft.common.block.entity.utility.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.inventory.AmadronMenu;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AmadronTabletItem extends PressurizableItem
        implements IPositionProvider, IChargeableContainerProvider {
    public AmadronTabletItem() {
        super(ModItems.toolProps().component(ModDataComponents.AMADRON_SHOPPING_BASKET, ShoppingBasket.empty()),
                PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (playerIn instanceof ServerPlayer sp) {
            openGui(sp, handIn);
        }
        return InteractionResultHolder.sidedSuccess(playerIn.getItemInHand(handIn), playerIn.level().isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Direction facing = ctx.getClickedFace();
        Player player = ctx.getPlayer();
        Level worldIn = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te == null || player == null) return InteractionResult.PASS;

        if (IOHelper.getFluidHandlerForBlock(te, facing).isPresent()) {
            if (!worldIn.isClientSide) {
                setFluidProvidingLocation(player.getItemInHand(ctx.getHand()), GlobalPosHelper.makeGlobalPos(worldIn, pos));
            } else {
                player.playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
            }
        } else if (IOHelper.getInventoryForBlock(te, facing).isPresent()) {
            if (!worldIn.isClientSide) {
                setItemProvidingLocation(player.getItemInHand(ctx.getHand()), GlobalPosHelper.makeGlobalPos(worldIn, pos));
            } else {
                player.playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
            }
        } else {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> infoList, TooltipFlag flag) {
        super.appendHoverText(stack, context, infoList, flag);
        GlobalPos gPos = getItemProvidingLocation(stack);
        if (gPos != null) {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.amadronTablet.itemLocation", GlobalPosHelper.prettyPrint(gPos)).withStyle(ChatFormatting.YELLOW));
        } else {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.amadronTablet.selectItemLocation"));
        }

        gPos = getFluidProvidingLocation(stack);
        if (gPos != null) {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.amadronTablet.fluidLocation", GlobalPosHelper.prettyPrint(gPos)).withStyle(ChatFormatting.YELLOW));
        } else {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.amadronTablet.selectFluidLocation"));
        }
    }

    public static Optional<IItemHandler> getItemCapability(ItemStack tablet) {
        GlobalPos pos = getItemProvidingLocation(tablet);
        if (pos != null) {
            BlockEntity te = GlobalPosHelper.getTileEntity(pos);
            for (Direction dir : DirectionUtil.VALUES) {
                Optional<IItemHandler> cap = IOHelper.getInventoryForBlock(te, dir);
                if (cap.isPresent()) return cap;
            }
        }
        return Optional.empty();
    }

    public static GlobalPos getItemProvidingLocation(ItemStack tablet) {
        return tablet.get(ModDataComponents.AMADRON_ITEM_POS);
    }

    private static void setItemProvidingLocation(ItemStack tablet, GlobalPos globalPos) {
        if (globalPos == null) {
            tablet.remove(ModDataComponents.AMADRON_ITEM_POS);
        } else {
            tablet.set(ModDataComponents.AMADRON_ITEM_POS, globalPos);
        }
    }

    public static Optional<IFluidHandler> getFluidCapability(ItemStack tablet) {
        GlobalPos pos = getFluidProvidingLocation(tablet);
        if (pos != null) {
            BlockEntity te = GlobalPosHelper.getTileEntity(pos);
            for (Direction dir : DirectionUtil.VALUES) {
                Optional<IFluidHandler> cap = IOHelper.getFluidHandlerForBlock(te, dir);
                if (cap.isPresent()) return cap;
            }
        }
        return Optional.empty();
    }

    public static GlobalPos getFluidProvidingLocation(ItemStack tablet) {
        return tablet.get(ModDataComponents.AMADRON_FLUID_POS);
    }

    private static void setFluidProvidingLocation(ItemStack tablet, GlobalPos globalPos) {
        if (globalPos == null) {
            tablet.remove(ModDataComponents.AMADRON_FLUID_POS);
        } else {
            tablet.set(ModDataComponents.AMADRON_FLUID_POS, globalPos);
        }
    }

    @Nonnull
    public static ImmutableBasket loadShoppingCart(ItemStack tablet) {
        return tablet.getOrDefault(ModDataComponents.AMADRON_SHOPPING_BASKET, ShoppingBasket.empty());
    }

    public static void saveShoppingCart(ItemStack tablet, ShoppingBasket basket) {
        tablet.set(ModDataComponents.AMADRON_SHOPPING_BASKET, basket.toImmutable());
    }

    @NotNull
    @Override
    public List<BlockPos> getStoredPositions(UUID playerId, @NotNull ItemStack stack) {
        GlobalPos gp1 = getItemProvidingLocation(stack);
        GlobalPos gp2 = getFluidProvidingLocation(stack);
        return Arrays.asList(gp1 == null ? null : gp1.pos(), gp2 == null ? null : gp2.pos());
    }

    @Override
    public int getRenderColor(int index) {
        return switch (index) {
            case 0 -> 0x90A0490E;  // item
            case 1 -> 0x9000C0C0;  // liquid
            default -> -1;
        };
    }

    @Override
    public Optional<TagKey<Item>> getUpgradeBlacklistTag() {
        return Optional.of(PneumaticCraftTags.Items.AMADRON_TABLET_UPGRADE_BLACKLIST);
    }

    public static void openGui(ServerPlayer playerIn, InteractionHand handIn) {
        playerIn.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return playerIn.getItemInHand(handIn).getHoverName();
            }

            @Override
            public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                return new AmadronMenu(windowId, playerInventory, handIn);
            }
        }, buf -> buf.writeBoolean(handIn == InteractionHand.MAIN_HAND));
    }

    @Override
    public MenuProvider getContainerProvider(ChargingStationBlockEntity te) {
        return new IChargeableContainerProvider.Provider(te, ModMenuTypes.CHARGING_AMADRON.get());
    }
}
