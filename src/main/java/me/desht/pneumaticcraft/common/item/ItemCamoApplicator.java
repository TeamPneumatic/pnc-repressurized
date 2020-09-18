package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerItem;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
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
import net.minecraftforge.common.util.Constants;

public class ItemCamoApplicator extends ItemPressurizable {
    public ItemCamoApplicator() {
        super(ModItems.toolProps(), PneumaticValues.PNEUMATIC_WRENCH_MAX_AIR, PneumaticValues.PNEUMATIC_WRENCH_VOLUME);
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
        if (playerIn.isSneaking()) {
            if (!worldIn.isRemote) {
                setCamoState(playerIn.getHeldItem(handIn), null);
            } else {
                if (getCamoState(playerIn.getHeldItem(handIn)) != null) {
                    playerIn.playSound(ModSounds.CHIRP.get(), 1.0f, 1.0f);
                }
            }
        }
        return new ActionResult<>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getPos();
        PlayerEntity player = ctx.getPlayer();

        if (!world.isRemote) {
            if (player.isSneaking()) {
                // sneak-right-click: clear camo
                setCamoState(stack, null);
            } else {
                TileEntity te = world.getTileEntity(pos);
                BlockState state = world.getBlockState(pos);
                if (!(te instanceof ICamouflageableTE)) {
                    // right-click non-camo block: copy its state
                    setCamoState(stack, state);
                    NetworkHandler.sendToAllAround(new PacketPlaySound(ModSounds.CHIRP.get(), SoundCategory.PLAYERS,
                            pos, 1.0F, 2.0F, false), world);
                } else {
                    // right-click camo block: try to apply (or remove) camo

                    IAirHandlerItem airHandler = stack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).orElseThrow(RuntimeException::new);
                    if (!player.isCreative() && airHandler.getPressure() < 0.1F) {
                        // not enough pressure
                        return ActionResultType.FAIL;
                    }

                    BlockState newCamo = getCamoState(stack);
                    BlockState existingCamo = ((ICamouflageableTE) te).getCamouflage();

                    if (existingCamo == newCamo) {
                        NetworkHandler.sendToAllAround(new PacketPlaySound(SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.PLAYERS,
                                pos, 1.0F, 2.0F, false), world);
                        return ActionResultType.SUCCESS;
                    }

                    // make sure player has enough of the camo item
                    if (newCamo != null && !player.isCreative()) {
                        ItemStack camoStack = ICamouflageableTE.getStackForState(newCamo);
                        if (!PneumaticCraftUtils.consumeInventoryItem(player.inventory, camoStack)) {
                            player.sendStatusMessage(new TranslationTextComponent("pneumaticcraft.message.camo.notEnoughBlocks")
                                    .appendSibling(camoStack.getDisplayName())
                                    .applyTextStyles(TextFormatting.RED), true);
                            NetworkHandler.sendToAllAround(new PacketPlaySound(ModSounds.MINIGUN_STOP.get(), SoundCategory.PLAYERS,
                                    pos, 1.0F, 2.0F, false), world);
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
                    airHandler.addAir(-PneumaticValues.USAGE_CAMO_APPLICATOR);
                    ((ICamouflageableTE) te).setCamouflage(newCamo);
                    BlockState particleState = newCamo == null ? existingCamo : newCamo;
                    if (particleState != null) {
                        player.getEntityWorld().playEvent(Constants.WorldEvents.BREAK_BLOCK_EFFECTS, pos, Block.getStateId(particleState));
                    }
                    NetworkHandler.sendToAllAround(new PacketPlaySound(ModSounds.SHORT_HISS.get(), SoundCategory.PLAYERS, pos, 1.0F, 1.0F, false), world);
                }
            }
        }

        return ActionResultType.SUCCESS;
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
