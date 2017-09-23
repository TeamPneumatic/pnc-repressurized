package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.client.gui.GuiGPSTool;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.remote.GlobalVariableManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

public class ItemGPSTool extends ItemPneumatic {
    public ItemGPSTool() {
        super("gps_tool");
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND) return EnumActionResult.PASS;
        setGPSLocation(player.getHeldItemMainhand(), pos);
        if (!worldIn.isRemote)
            player.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + "[GPS Tool] Set Coordinates to " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "."), false);
        return EnumActionResult.SUCCESS; // we don't want to use the item.
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (handIn != EnumHand.MAIN_HAND) return ActionResult.newResult(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
        ItemStack stack = playerIn.getHeldItemMainhand();
        if (worldIn.isRemote) {
            BlockPos pos = getGPSLocation(stack);
            FMLCommonHandler.instance().showGuiScreen(new GuiGPSTool(pos != null ? pos : new BlockPos(0, 0, 0), getVariable(stack)));
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> infoList, ITooltipFlag par4) {
        super.addInformation(stack, worldIn, infoList, par4);
        NBTTagCompound compound = stack.getTagCompound();
        if (compound != null) {
            int x = compound.getInteger("x");
            int y = compound.getInteger("y");
            int z = compound.getInteger("z");
            if (x != 0 || y != 0 || z != 0) {
                infoList.add("\u00a72Set to " + x + ", " + y + ", " + z);
            }
            String varName = getVariable(stack);
            if (!varName.equals("")) {
                infoList.add(I18n.format("gui.tooltip.gpsTool.variable", varName));
            }
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean heldItem) {
        String var = getVariable(stack);
        if (!var.equals("") && !world.isRemote) {
            BlockPos pos = GlobalVariableManager.getInstance().getPos(var);
            setGPSLocation(stack, pos);
        }
    }

    public static BlockPos getGPSLocation(ItemStack gpsTool) {
        NBTTagCompound compound = gpsTool.getTagCompound();
        if (compound != null) {
            String var = getVariable(gpsTool);
            if (!var.equals("") && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
                BlockPos pos = GlobalVariableManager.getInstance().getPos(var);
                setGPSLocation(gpsTool, pos);
            }
            int x = compound.getInteger("x");
            int y = compound.getInteger("y");
            int z = compound.getInteger("z");
            if (x != 0 || y != 0 || z != 0) {
                return new BlockPos(x, y, z);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static void setGPSLocation(ItemStack gpsTool, BlockPos pos) {
        NBTUtil.setPos(gpsTool, pos);
        String var = getVariable(gpsTool);
        if (!var.equals("")) GlobalVariableManager.getInstance().set(var, pos);
    }

    public static void setVariable(ItemStack gpsTool, String variable) {
        NBTUtil.setString(gpsTool, "variable", variable);
    }

    public static String getVariable(ItemStack gpsTool) {
        return gpsTool.hasTagCompound() ? gpsTool.getTagCompound().getString("variable") : "";
    }
}
