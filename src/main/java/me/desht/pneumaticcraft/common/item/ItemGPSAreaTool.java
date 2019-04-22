package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.areatool.GuiGPSAreaTool;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.remote.GlobalVariableManager;
import me.desht.pneumaticcraft.common.util.NBTUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemGPSAreaTool extends ItemPneumatic implements IPositionProvider {
    public ItemGPSAreaTool() {
        super("gps_area_tool");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND) return EnumActionResult.PASS;
        setGPSPosAndNotify(player, pos, 0);
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
                setGPSPosAndNotify(event.getEntityPlayer(), event.getPos(), 1);
            }
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onLeftClickAir(PlayerInteractEvent.LeftClickEmpty event){
        if(event.getItemStack().getItem() == this){
            showGUI(event.getItemStack(), 1);
        }
    }

    public static void setGPSPosAndNotify(EntityPlayer player, BlockPos pos, int index){
        setGPSLocation(player.getHeldItemMainhand(), pos, index);
        if (!player.world.isRemote) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + String.format("[GPS Area Tool] Set P%d to %d, %d, %d.", index + 1, pos.getX(), pos.getY(), pos.getZ())), false);
            if (player instanceof EntityPlayerMP)
                ((EntityPlayerMP) player).connection.sendPacket(new SPacketHeldItemChange(player.inventory.currentItem));
        }
    }
    
    private void showGUI(ItemStack stack, int index){
        FMLCommonHandler.instance().showGuiScreen(new GuiGPSAreaTool(stack, index));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> infoList, ITooltipFlag par4) {
        super.addInformation(stack, worldIn, infoList, par4);
        for(int index = 0; index < 2; index++){
            BlockPos pos = getGPSLocation(stack, index);
            infoList.add(String.format("\u00a72P%d: %d, %d, %d", index + 1, pos.getX(), pos.getY(), pos.getZ()));
            String varName = getVariable(stack, index);
            if (!varName.isEmpty()) {
                infoList.add(I18n.format("gui.tooltip.gpsTool.variable", varName));
            }
        }
        getArea(stack).addAreaTypeTooltip(infoList);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean heldItem) {
        if (!world.isRemote) {
            for (int index = 0; index < 2; index++) {
                String var = getVariable(stack, index);
                if (!var.isEmpty()) {
                    BlockPos pos = GlobalVariableManager.getInstance().getPos(var);
                    setGPSLocation(stack, pos, index);
                }
            }
        }
    }

    @Nonnull
    public static ProgWidgetArea getArea(ItemStack stack) {
        Validate.isTrue(stack.getItem() instanceof ItemGPSAreaTool);
        ProgWidgetArea area = new ProgWidgetArea();
        if (stack.hasTagCompound()) {
            area.readFromNBT(stack.getTagCompound());
        }
        return area;
    }

    public static BlockPos getGPSLocation(ItemStack gpsTool, int index) {
        ProgWidgetArea area = getArea(gpsTool);

        String var = getVariable(gpsTool, index);
        if (!var.equals("") && PneumaticCraftRepressurized.proxy.getClientWorld() == null) {
            BlockPos pos = GlobalVariableManager.getInstance().getPos(var);
            setGPSLocation(gpsTool, pos, index);
        }

        if (index == 0) {
            return new BlockPos(area.x1, area.y1, area.z1);
        } else if (index == 1) {
            return new BlockPos(area.x2, area.y2, area.z2);
        } else {
            throw new IllegalArgumentException("index must be 0 or 1!");
        }
    }

    private static void setGPSLocation(ItemStack gpsTool, BlockPos pos, int index) {
        ProgWidgetArea area = getArea(gpsTool);
        if (index == 0) {
            area.setP1(pos);
        } else if (index == 1) {
            area.setP2(pos);
        }
        NBTUtil.initNBTTagCompound(gpsTool);
        area.writeToNBT(gpsTool.getTagCompound());
    }

    public static void setVariable(ItemStack gpsTool, String variable, int index) {
        ProgWidgetArea area = getArea(gpsTool);
        if (index == 0) {
            area.setCoord1Variable(variable);
        } else if (index == 1) {
            area.setCoord2Variable(variable);
        }
        NBTUtil.initNBTTagCompound(gpsTool);
        area.writeToNBT(gpsTool.getTagCompound());
    }

    public static String getVariable(ItemStack gpsTool, int index) {
        ProgWidgetArea area = getArea(gpsTool);
        return index == 0 ? area.getCoord1Variable() : area.getCoord2Variable();
    }

    @Override
    public List<BlockPos> getStoredPositions(@Nonnull ItemStack stack) {
        Set<BlockPos> posSet = new HashSet<>();
        getArea(stack).getArea(posSet);
        return new ArrayList<>(posSet);
    }

    @Override
    public int getRenderColor(int index) {
        return 0x90FFFF00;
    }
    
    @Override
    public boolean disableDepthTest(){
        return false;
    }
}
