package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftCamo;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketPlaySound;
import me.desht.pneumaticcraft.common.tileentity.ICamouflageableTE;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ItemCamoApplicator extends ItemPressurizable {
    public ItemCamoApplicator() {
        super("camo_applicator", PneumaticValues.PNEUMATIC_WRENCH_MAX_AIR, PneumaticValues.PNEUMATIC_WRENCH_VOLUME);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        IBlockState camoState = getCamoState(stack);
        String disp = super.getItemStackDisplayName(stack);
        if (camoState != null) {
            return disp + ": " + TextFormatting.YELLOW + getCamoStateDisplayName(camoState);
        } else {
            return disp;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        // right-click air: clear any camo
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote && playerIn.isSneaking() && getCamoState(stack) != null) {
            setCamoState(stack, null);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                // copy blockstate of clicked block
                IBlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof BlockPneumaticCraftCamo) {
                    NetworkHandler.sendToAllAround(new PacketPlaySound(SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.PLAYERS, pos, 1.0F, 2.0F, false), world);
                    player.sendStatusMessage(new TextComponentTranslation("message.camo.invalidBlock", getCamoStateDisplayName(state)), true);
                } else {
                    setCamoState(stack, state);
                }
            } else{
                // either apply saved camo, or remove current camo from block
                TileEntity te = world.getTileEntity(pos);
                if (!(te instanceof ICamouflageableTE)) {
                    return EnumActionResult.PASS;
                }

                IBlockState camoState = getCamoState(stack);

                float pressure = getPressure(stack);
                if (pressure < 0.1 && !player.capabilities.isCreativeMode) {
                    // not enough pressure
                    return EnumActionResult.FAIL;
                }

                // make sure player has enough of the camo item
                if (camoState != null && !player.capabilities.isCreativeMode) {
                    ItemStack camoStack = ICamouflageableTE.getStackForState(camoState);
                    if (!PneumaticCraftUtils.consumeInventoryItem(player.inventory, camoStack)) {
                        String name = camoStack.getDisplayName();
                        player.sendStatusMessage(new TextComponentTranslation("message.camo.notEnoughBlocks", name), true);
                        NetworkHandler.sendToAllAround(new PacketPlaySound(SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.PLAYERS, pos, 1.0F, 2.0F, false), world);
                        return EnumActionResult.FAIL;
                    }
                }

                // return any existing camouflage on the block/TE
                IBlockState existingCamo = ((ICamouflageableTE) te).getCamouflage();

                if (existingCamo == camoState) {
                    NetworkHandler.sendToAllAround(new PacketPlaySound(SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.PLAYERS, pos, 1.0F, 2.0F, false), world);
                    return EnumActionResult.SUCCESS;
                }

                if (existingCamo != null && !player.capabilities.isCreativeMode) {
                    ItemStack camoStack = ICamouflageableTE.getStackForState(existingCamo);
                    EntityItem entity = new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, camoStack);
                    world.spawnEntity(entity);
                    entity.onCollideWithPlayer(player);
                }

                // and apply the new camouflage
                addAir(stack, -PneumaticValues.USAGE_CAMO_APPLICATOR);
                ((ICamouflageableTE) te).setCamouflage(camoState);
                NetworkHandler.sendToAllAround(new PacketPlaySound(Sounds.SHORT_HISS, SoundCategory.PLAYERS, pos, 1.0F, 1.0F, false), world);
                return EnumActionResult.SUCCESS;
            }
        } else {
            return EnumActionResult.SUCCESS;
        }

        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    private static void setCamoState(ItemStack stack, IBlockState state) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) tag = new NBTTagCompound();
        if (state == null) {
            tag.removeTag("CamoState");
        } else {
            Block block = state.getBlock();
            NBTTagCompound stateTag = new NBTTagCompound();
            stateTag.setString("block", block.getRegistryName().toString());
            stateTag.setInteger("meta", block.getMetaFromState(state));
            stack.setTagCompound(stateTag);
            tag.setTag("CamoState", stateTag);
        }
        stack.setTagCompound(tag);
    }

    private static IBlockState getCamoState(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("CamoState")) {
            NBTTagCompound stateTag = stack.getTagCompound().getCompoundTag("CamoState");
            Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(stateTag.getString("block")));
            return b != null ? b.getStateFromMeta(stateTag.getInteger("meta")) : null;
        }
        return null;
    }

    public static String getCamoStateDisplayName(IBlockState state) {
        if (state != null) {
            Block b = state.getBlock();
            Item item = Item.getItemFromBlock(b);
            if (item != null) {
                return new ItemStack(item, 1, b.getMetaFromState(state)).getDisplayName();
            }
        }
        return "<?>";
    }

    private static String getCamoStateDisplayName(ItemStack stack) {
        return getCamoStateDisplayName(getCamoState(stack));
    }
}
