package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.common.amadron.ShoppingBasket;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemAmadronTablet extends ItemPressurizable implements IPositionProvider {
    public ItemAmadronTablet() {
        super(ModItems.toolProps(), PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isClientSide) {
            openGui(playerIn, handIn);
        }
        return ActionResult.success(playerIn.getItemInHand(handIn));
    }

    @Override
    public ActionResultType useOn(ItemUseContext ctx) {
        Direction facing = ctx.getClickedFace();
        PlayerEntity player = ctx.getPlayer();
        World worldIn = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        TileEntity te = worldIn.getBlockEntity(pos);
        if (te == null) return ActionResultType.PASS;

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
            return ActionResultType.PASS;
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> infoList, ITooltipFlag flag) {
        super.appendHoverText(stack, worldIn, infoList, flag);
        GlobalPos gPos = getItemProvidingLocation(stack);
        if (gPos != null) {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.amadronTablet.itemLocation", GlobalPosHelper.prettyPrint(gPos)).withStyle(TextFormatting.YELLOW));
        } else {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.amadronTablet.selectItemLocation"));
        }

        gPos = getFluidProvidingLocation(stack);
        if (gPos != null) {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.amadronTablet.fluidLocation", GlobalPosHelper.prettyPrint(gPos)).withStyle(TextFormatting.YELLOW));
        } else {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.amadronTablet.selectFluidLocation"));
        }
    }

    public static LazyOptional<IItemHandler> getItemCapability(ItemStack tablet) {
        GlobalPos pos = getItemProvidingLocation(tablet);
        if (pos != null) {
            TileEntity te = GlobalPosHelper.getTileEntity(pos);
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
            TileEntity te = GlobalPosHelper.getTileEntity(pos);
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
    public List<BlockPos> getStoredPositions(World world, @Nonnull ItemStack stack) {
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

    public static void openGui(PlayerEntity playerIn, Hand handIn) {
        NetworkHooks.openGui((ServerPlayerEntity) playerIn, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return playerIn.getItemInHand(handIn).getHoverName();
            }

            @Override
            public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                return new ContainerAmadron(windowId, playerInventory, handIn);
            }
        }, buf -> buf.writeBoolean(handIn == Hand.MAIN_HAND));
    }
}
