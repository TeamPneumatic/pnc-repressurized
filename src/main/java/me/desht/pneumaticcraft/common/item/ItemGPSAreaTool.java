package me.desht.pneumaticcraft.common.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.GuiGPSTool;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.capabilities.CapabilityGPSAreaTool;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemGPSAreaTool extends ItemPneumatic implements IPositionProvider {
    public ItemGPSAreaTool() {
        super("gps_area_tool");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND) return EnumActionResult.PASS;
        setPos(player, pos, 0);
        return EnumActionResult.SUCCESS; // we don't want to use the item.
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (handIn != EnumHand.MAIN_HAND) return ActionResult.newResult(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
        ItemStack stack = playerIn.getHeldItemMainhand();
        if (worldIn.isRemote) {
            showGUI(stack, 0);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }
    
    @SubscribeEvent
    public void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event){
        if(event.getItemStack().getItem() == this){
            if(!event.getPos().equals(getGPSLocation(event.getItemStack(), 1))){
                setPos(event.getEntityPlayer(), event.getPos(), 1);
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public void onLeftClickAir(PlayerInteractEvent.LeftClickEmpty event){
        if(event.getItemStack().getItem() == this){
            showGUI(event.getItemStack(), 1);
        }
    }
    
    private void setPos(EntityPlayer player, BlockPos pos, int index){
        setGPSLocation(player.getHeldItemMainhand(), pos, index);
        if (!player.world.isRemote)
            player.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + String.format("[GPS Area Tool] Set P%d to %d, %d, %d.", index + 1, pos.getX(), pos.getY(), pos.getZ())), false);
    }
    
    private void showGUI(ItemStack stack, int index){
        BlockPos pos = getGPSLocation(stack, index);
        FMLCommonHandler.instance().showGuiScreen(new GuiGPSTool(pos != null ? pos : new BlockPos(0, 0, 0), getVariable(stack, index)));
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> infoList, ITooltipFlag par4) {
        super.addInformation(stack, worldIn, infoList, par4);
        for(int index = 0; index < 2; index++){
            NBTTagCompound compound = getCoordTag(stack, index);
            if (compound != null) {
                int x = compound.getInteger("x");
                int y = compound.getInteger("y");
                int z = compound.getInteger("z");
                if (x != 0 || y != 0 || z != 0) {
                    infoList.add(String.format("\u00a72P%d: %d, %d, %d", index, x, y, z));
                }
                String varName = getVariable(stack, index);
                if (!varName.equals("")) {
                    infoList.add(I18n.format("gui.tooltip.gpsTool.variable", varName));
                }
            }
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean heldItem) {
        for(int index = 0; index < 2; index++){
            String var = getVariable(stack, index);
            if (!var.equals("") && !world.isRemote) {
                BlockPos pos = GlobalVariableManager.getInstance().getPos(var);
                setGPSLocation(stack, pos, index);
            }
        }
    }

    public static BlockPos getGPSLocation(ItemStack gpsTool, int index) {
        NBTTagCompound compound = getCoordTag(gpsTool, index);
        if (compound != null) {
            String var = getVariable(gpsTool, index);
            if (!var.equals("") && PneumaticCraftRepressurized.proxy.getClientWorld() == null) {
                BlockPos pos = GlobalVariableManager.getInstance().getPos(var);
                setGPSLocation(gpsTool, pos, index);
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

    public static void setGPSLocation(ItemStack gpsTool, BlockPos pos, int index) {
        getCap(gpsTool).setPos(pos, index);
        
        NBTTagCompound tag = getCoordTag(gpsTool, index);
        
        NBTUtil.setPos(tag, pos);
        String var = getVariable(gpsTool, index);
        if (!var.equals("")) GlobalVariableManager.getInstance().set(var, pos);
    }
    
    private static NBTTagCompound getCoordTag(ItemStack gpsTool, int index){
        return NBTUtil.getCompoundTag(gpsTool, "coord" + index);
    }

    public static void setVariable(ItemStack gpsTool, String variable, int index) {
        getCoordTag(gpsTool, index).setString("variable", variable);
    }

    public static String getVariable(ItemStack gpsTool, int index) {
        return getCoordTag(gpsTool, index).getString("variable");
    }

    @Override
    public List<BlockPos> getStoredPositions(@Nonnull ItemStack stack) {
        Set<BlockPos> area = getCap(stack).getArea();
        return new ArrayList<>(area);
        //return Arrays.asList(getGPSLocation(stack, 0), getGPSLocation(stack, 1));
    }

    @Override
    public int getRenderColor(int index) {
        return 0x90FFFF00;
    }
    
    private static CapabilityGPSAreaTool getCap(ItemStack stack){
        return stack.getCapability(CapabilityGPSAreaTool.INSTANCE, null);
    }
}
