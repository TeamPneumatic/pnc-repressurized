package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.util.GlobalPosUtils;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemAmadronTablet extends ItemPressurizable implements IPositionProvider {
    public ItemAmadronTablet() {
        super(PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isRemote) {
//            NetworkHandler.sendToPlayer(new PacketSyncAmadronOffers(playerIn), (ServerPlayerEntity) playerIn);
            NetworkHooks.openGui((ServerPlayerEntity) playerIn, new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return playerIn.getHeldItem(handIn).getDisplayName();
                }

                @Nullable
                @Override
                public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                    return new ContainerAmadron(windowId, playerInventory, handIn);
                }
            }, buf -> buf.writeBoolean(handIn == Hand.MAIN_HAND));
        }
        return ActionResult.newResult(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        Direction facing = ctx.getFace();
        PlayerEntity player = ctx.getPlayer();
        World worldIn = ctx.getWorld();
        BlockPos pos = ctx.getPos();

        TileEntity te = worldIn.getTileEntity(pos);
        if (te == null) return ActionResultType.PASS;

        if (te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing).isPresent()) {
            if (!worldIn.isRemote) {
                setFluidProvidingLocation(player.getHeldItem(ctx.getHand()), GlobalPos.of(worldIn.getDimension().getType(), pos));
            } else {
                ctx.getPlayer().playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
            }
        } else if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing).isPresent()) {
            if (!worldIn.isRemote) {
                setItemProvidingLocation(player.getHeldItem(ctx.getHand()), GlobalPos.of(worldIn.getDimension().getType(), pos));
            } else {
                ctx.getPlayer().playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
            }
        } else {
            return ActionResultType.PASS;
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> infoList, ITooltipFlag flag) {
        super.addInformation(stack, worldIn, infoList, flag);
        GlobalPos gPos = getItemProvidingLocation(stack);
        if (gPos != null) {
            infoList.add(xlate("gui.tooltip.amadronTablet.itemLocation", gPos.toString()));
        } else {
            infoList.add(xlate("gui.tooltip.amadronTablet.selectItemLocation"));
        }

        gPos = getFluidProvidingLocation(stack);
        if (gPos != null) {
            infoList.add(xlate("gui.tooltip.amadronTablet.fluidLocation", gPos));
        } else {
            infoList.add(xlate("gui.tooltip.amadronTablet.selectFluidLocation"));
        }
    }

    public static LazyOptional<IItemHandler> getItemCapability(ItemStack tablet) {
        GlobalPos pos = getItemProvidingLocation(tablet);
        if (pos != null) {
            TileEntity te = GlobalPosUtils.getTileEntity(pos);
            return IOHelper.getInventoryForTE(te);
        }
        return LazyOptional.empty();
    }

    public static GlobalPos getItemProvidingLocation(ItemStack tablet) {
        return tablet.hasTag() && tablet.getTag().contains("itemPos") ?
                GlobalPosUtils.deserializeGlobalPos(tablet.getTag().getCompound("itemPos")) :
                null;
    }

    private static void setItemProvidingLocation(ItemStack tablet, GlobalPos globalPos) {
        NBTUtil.setCompoundTag(tablet, "itemPos", GlobalPosUtils.serializeGlobalPos(globalPos));
    }

    public static LazyOptional<IFluidHandler> getFluidCapability(ItemStack tablet) {
        GlobalPos pos = getFluidProvidingLocation(tablet);
        if (pos != null) {
            World world = DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), pos.getDimension(), false, false);
            return world == null ? LazyOptional.empty() : FluidUtil.getFluidHandler(world, pos.getPos(), null);
        }
        return LazyOptional.empty();
    }

    public static GlobalPos getFluidProvidingLocation(ItemStack tablet) {
        return tablet.hasTag() && tablet.getTag().contains("liquidPos") ?
                GlobalPosUtils.deserializeGlobalPos(tablet.getTag().getCompound("liquidPos")) :
                null;
    }

    private static void setFluidProvidingLocation(ItemStack tablet, GlobalPos globalPos) {
        NBTUtil.setCompoundTag(tablet, "liquidPos", GlobalPosUtils.serializeGlobalPos(globalPos));
    }

    public static Map<AmadronOffer, Integer> getShoppingCart(ItemStack tablet) {
        Map<AmadronOffer, Integer> offers = new HashMap<>();

        if (tablet.hasTag() && tablet.getTag().contains("shoppingCart")) {
            ListNBT list = tablet.getTag().getList("shoppingCart", 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundNBT tag = list.getCompound(i);
                offers.put(tag.contains("inStock") ? AmadronOfferCustom.loadFromNBT(tag) : AmadronOffer.loadFromNBT(tag), tag.getInt("amount"));
            }
        }
        return offers;
    }

    public static void setShoppingCart(ItemStack tablet, Map<AmadronOffer, Integer> cart) {
        ListNBT list = new ListNBT();
        for (Map.Entry<AmadronOffer, Integer> entry : cart.entrySet()) {
            CompoundNBT tag = new CompoundNBT();
            entry.getKey().writeToNBT(tag);
            tag.putInt("amount", entry.getValue());
            list.add(tag);
        }
        NBTUtil.setCompoundTag(tablet, "shoppingCart", list);
    }

    @Override
    public List<BlockPos> getStoredPositions(@Nonnull ItemStack stack) {
        GlobalPos gp1 = getItemProvidingLocation(stack);
        GlobalPos gp2 = getFluidProvidingLocation(stack);
        return Arrays.asList(gp1 == null ? null : gp1.getPos(), gp2 == null ? null : gp2.getPos());
    }

    @Override
    public int getRenderColor(int index) {
        switch (index) {
            case 0: return 0x90A0490E;  // item
            case 1: return 0x9000C0C0;  // liquid
            default: return -1;
        }
    }
}
