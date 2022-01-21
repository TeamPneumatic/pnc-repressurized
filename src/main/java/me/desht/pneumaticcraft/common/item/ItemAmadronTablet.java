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

import me.desht.pneumaticcraft.api.item.EnumUpgrade;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.api.item.IUpgradeAcceptor;
import me.desht.pneumaticcraft.common.amadron.ShoppingBasket;
import me.desht.pneumaticcraft.common.core.ModContainers;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.upgrade.ApplicableUpgradesDB;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemAmadronTablet extends ItemPressurizable
        implements IPositionProvider, IChargeableContainerProvider, IUpgradeAcceptor {
    public ItemAmadronTablet() {
        super(ModItems.toolProps(), PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (!worldIn.isClientSide) {
            openGui(playerIn, handIn);
        }
        return InteractionResultHolder.success(playerIn.getItemInHand(handIn));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Direction facing = ctx.getClickedFace();
        Player player = ctx.getPlayer();
        Level worldIn = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te == null) return InteractionResult.PASS;

        if (te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing).isPresent()) {
            if (!worldIn.isClientSide) {
                setFluidProvidingLocation(player.getItemInHand(ctx.getHand()), GlobalPosHelper.makeGlobalPos(worldIn, pos));
            } else {
                ctx.getPlayer().playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
            }
        } else if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing).isPresent()) {
            if (!worldIn.isClientSide) {
                setItemProvidingLocation(player.getItemInHand(ctx.getHand()), GlobalPosHelper.makeGlobalPos(worldIn, pos));
            } else {
                ctx.getPlayer().playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
            }
        } else {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> infoList, TooltipFlag flag) {
        super.appendHoverText(stack, worldIn, infoList, flag);
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

    public static LazyOptional<IItemHandler> getItemCapability(ItemStack tablet) {
        GlobalPos pos = getItemProvidingLocation(tablet);
        if (pos != null) {
            BlockEntity te = GlobalPosHelper.getTileEntity(pos);
            for (Direction dir : DirectionUtil.VALUES) {
                LazyOptional<IItemHandler> lazy = IOHelper.getInventoryForTE(te, dir);
                if (lazy.isPresent()) return lazy;
            }
        }
        return LazyOptional.empty();
    }

    public static GlobalPos getItemProvidingLocation(ItemStack tablet) {
        return tablet.hasTag() && tablet.getTag().contains("itemPos") ?
                GlobalPosHelper.fromNBT(tablet.getTag().getCompound("itemPos")) :
                null;
    }

    private static void setItemProvidingLocation(ItemStack tablet, GlobalPos globalPos) {
        NBTUtils.setCompoundTag(tablet, "itemPos", GlobalPosHelper.toNBT(globalPos));
    }

    public static LazyOptional<IFluidHandler> getFluidCapability(ItemStack tablet) {
        GlobalPos pos = getFluidProvidingLocation(tablet);
        if (pos != null) {
            BlockEntity te = GlobalPosHelper.getTileEntity(pos);
            for (Direction dir : DirectionUtil.VALUES) {
                LazyOptional<IFluidHandler> lazy = IOHelper.getFluidHandlerForTE(te, dir);
                if (lazy.isPresent()) return lazy;
            }
        }
        return LazyOptional.empty();
    }

    public static GlobalPos getFluidProvidingLocation(ItemStack tablet) {
        return tablet.hasTag() && tablet.getTag().contains("liquidPos") ?
                GlobalPosHelper.fromNBT(tablet.getTag().getCompound("liquidPos")) :
                null;
    }

    private static void setFluidProvidingLocation(ItemStack tablet, GlobalPos globalPos) {
        NBTUtils.setCompoundTag(tablet, "liquidPos", GlobalPosHelper.toNBT(globalPos));
    }

    @Nonnull
    public static ShoppingBasket loadShoppingCart(ItemStack tablet) {
        if (!(tablet.getItem() instanceof ItemAmadronTablet)) return new ShoppingBasket();
        return ShoppingBasket.fromNBT(tablet.getTagElement("shoppingCart"));
    }

    public static void saveShoppingCart(ItemStack tablet, ShoppingBasket cart) {
        if (tablet.getItem() instanceof ItemAmadronTablet) {
            NBTUtils.setCompoundTag(tablet, "shoppingCart", cart.toNBT());
        }
    }

    @Override
    public List<BlockPos> getStoredPositions(Level world, @Nonnull ItemStack stack) {
        GlobalPos gp1 = getItemProvidingLocation(stack);
        GlobalPos gp2 = getFluidProvidingLocation(stack);
        return Arrays.asList(gp1 == null ? null : gp1.pos(), gp2 == null ? null : gp2.pos());
    }

    @Override
    public int getRenderColor(int index) {
        switch (index) {
            case 0: return 0x90A0490E;  // item
            case 1: return 0x9000C0C0;  // liquid
            default: return -1;
        }
    }

    public static void openGui(Player playerIn, InteractionHand handIn) {
        NetworkHooks.openGui((ServerPlayer) playerIn, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return playerIn.getItemInHand(handIn).getHoverName();
            }

            @Override
            public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                return new ContainerAmadron(windowId, playerInventory, handIn);
            }
        }, buf -> buf.writeBoolean(handIn == InteractionHand.MAIN_HAND));
    }

    @Override
    public Map<EnumUpgrade, Integer> getApplicableUpgrades() {
        return ApplicableUpgradesDB.getInstance().getApplicableUpgrades(this);
    }

    @Override
    public String getUpgradeAcceptorTranslationKey() {
        return getDescriptionId();
    }

    @Override
    public MenuProvider getContainerProvider(TileEntityChargingStation te) {
        return new IChargeableContainerProvider.Provider(te, ModContainers.CHARGING_AMADRON.get());
    }
}
