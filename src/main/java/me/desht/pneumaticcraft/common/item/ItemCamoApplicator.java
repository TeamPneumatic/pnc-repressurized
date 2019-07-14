package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.core.Sounds;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ItemCamoApplicator extends ItemPressurizable {
    public ItemCamoApplicator() {
        super("camo_applicator", PneumaticValues.PNEUMATIC_WRENCH_MAX_AIR, PneumaticValues.PNEUMATIC_WRENCH_VOLUME);
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        BlockState camoState = getCamoState(stack);
        ITextComponent disp = super.getDisplayName(stack);
        if (camoState != null) {
            return disp.appendText(": ").appendSibling(getCamoStateDisplayName(camoState)).applyTextStyle(TextFormatting.YELLOW);
        } else {
            return disp;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        // right-click air: clear any camo
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote && playerIn.isSneaking() && getCamoState(stack) != null) {
            setCamoState(stack, null);
        }
        return ActionResult.newResult(ActionResultType.SUCCESS, stack);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getPos();
        PlayerEntity player = ctx.getPlayer();

        if (!world.isRemote) {
            if (player.isSneaking()) {
                // copy blockstate of clicked block
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof BlockPneumaticCraftCamo) {
                    NetworkHandler.sendToAllAround(new PacketPlaySound(SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.PLAYERS, pos, 1.0F, 2.0F, false), world);
                    player.sendStatusMessage(new TranslationTextComponent("message.camo.invalidBlock", getCamoStateDisplayName(state)), true);
                } else {
                    setCamoState(stack, state);
                }
            } else{
                // either apply saved camo, or remove current camo from block
                TileEntity te = world.getTileEntity(pos);
                if (!(te instanceof ICamouflageableTE)) {
                    return ActionResultType.PASS;
                }

                BlockState camoState = getCamoState(stack);

                float pressure = getPressure(stack);
                if (pressure < 0.1 && !player.isCreative()) {
                    // not enough pressure
                    return ActionResultType.FAIL;
                }

                // return any existing camouflage on the block/TE
                BlockState existingCamo = ((ICamouflageableTE) te).getCamouflage();

                if (existingCamo == camoState) {
                    NetworkHandler.sendToAllAround(new PacketPlaySound(SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.PLAYERS, pos, 1.0F, 2.0F, false), world);
                    return ActionResultType.SUCCESS;
                }

                // make sure player has enough of the camo item
                if (camoState != null && !player.isCreative()) {
                    ItemStack camoStack = ICamouflageableTE.getStackForState(camoState);
                    if (!PneumaticCraftUtils.consumeInventoryItem(player.inventory, camoStack)) {
                        player.sendStatusMessage(new TranslationTextComponent("message.camo.notEnoughBlocks").appendSibling(camoStack.getDisplayName()).applyTextStyles(TextFormatting.RED), true);
                        NetworkHandler.sendToAllAround(new PacketPlaySound(SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.PLAYERS, pos, 1.0F, 2.0F, false), world);
                        return ActionResultType.FAIL;
                    }
                }

                // return existing camo block, if any
                if (existingCamo != null && !player.isCreative()) {
                    ItemStack camoStack = ICamouflageableTE.getStackForState(existingCamo);
                    ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, camoStack);
                    world.addEntity(entity);
                    entity.onCollideWithPlayer(player);
                }

                // and apply the new camouflage
                addAir(stack, -PneumaticValues.USAGE_CAMO_APPLICATOR);
                ((ICamouflageableTE) te).setCamouflage(camoState);
                BlockState particleState = camoState == null ? existingCamo : camoState;
                if (particleState != null) {
                    player.getEntityWorld().playEvent(2001, pos, Block.getStateId(particleState));
                }
                NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.SHORT_HISS, SoundCategory.PLAYERS, pos, 1.0F, 1.0F, false), world);
                return ActionResultType.SUCCESS;
            }
        } else {
            return ActionResultType.SUCCESS;
        }

        return super.onItemUseFirst(stack, ctx);
    }

    private static void setCamoState(ItemStack stack, BlockState state) {
        CompoundNBT tag = stack.getTag();
        if (tag == null) tag = new CompoundNBT();
        if (state == null) {
            tag.remove("CamoState");
        } else {
            tag.put("CamoState", NBTUtil.writeBlockState(state));
        }
        stack.setTag(tag);
    }

    private static BlockState getCamoState(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null && tag.contains("CamoState")) {
            return NBTUtil.readBlockState(tag.getCompound("CamoState"));
        }
        return null;
    }

    public static ITextComponent getCamoStateDisplayName(BlockState state) {
        if (state != null) {
            return new ItemStack(state.getBlock().asItem()).getDisplayName();
        }
        return new StringTextComponent("<?>");
    }

}
