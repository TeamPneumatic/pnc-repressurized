package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.areatool.GuiGPSAreaTool;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemGPSAreaTool extends Item implements IPositionProvider {
    public ItemGPSAreaTool() {
        super(ModItems.defaultProps());
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        setGPSPosAndNotify(ctx.getPlayer(), ctx.getPos(), ctx.getHand(), 0);
        ctx.getPlayer().playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
        return ActionResultType.SUCCESS; // we don't want to use the item.
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (worldIn.isRemote) {
            GuiGPSAreaTool.showGUI(stack, handIn, 0);
        }
        return ActionResult.resultSuccess(stack);
    }


    public static void setGPSPosAndNotify(PlayerEntity player, BlockPos pos, Hand hand, int index) {
        ItemStack stack = player.getHeldItem(hand);
        setGPSLocation(stack, pos, index);
        if (!player.world.isRemote) {
            player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + String.format("[%s] ", stack.getDisplayName().getString()))
                    .append(getMessageText(player.world, pos, index)), false);
            if (player instanceof ServerPlayerEntity)
                ((ServerPlayerEntity) player).connection.sendPacket(new SHeldItemChangePacket(player.inventory.currentItem));
        }
    }

    private static ITextComponent getMessageText(World worldIn, BlockPos pos, int index) {
        ITextComponent translated = new TranslationTextComponent(worldIn.getBlockState(pos).getBlock().getTranslationKey());
        IFormattableTextComponent blockName = worldIn.getChunkProvider().isChunkLoaded(new ChunkPos(pos)) ?
                new StringTextComponent(" (").append(translated).appendString(")") :
                StringTextComponent.EMPTY.copyRaw();
        String str = String.format("P%d%s: [%d, %d, %d]", index + 1, TextFormatting.YELLOW.toString(), pos.getX(), pos.getY(), pos.getZ());
        return new StringTextComponent(str).mergeStyle(index == 0 ? TextFormatting.RED : TextFormatting.GREEN).append(blockName.mergeStyle(TextFormatting.GREEN));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> infoList, ITooltipFlag par4) {
        super.addInformation(stack, worldIn, infoList, par4);

        if (worldIn != null) {
            ClientUtils.addGuiContextSensitiveTooltip(stack, infoList);
            int nPos = 0;
            for (int index = 0; index < 2; index++) {
                BlockPos pos = getGPSLocation(worldIn, stack, index);
                if (!pos.equals(BlockPos.ZERO)) {
                    infoList.add(getMessageText(worldIn, pos, index));//.mergeStyle(index == 0 ? TextFormatting.RED : TextFormatting.GREEN));
                    nPos++;
                }
                String varName = getVariable(stack, index);
                if (!varName.isEmpty()) {
                    infoList.add(xlate("pneumaticcraft.gui.tooltip.gpsTool.variable", varName));
                }
            }
            if (nPos > 0) getArea(stack).addAreaTypeTooltip(infoList);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean heldItem) {
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
        if (stack.hasTag()) {
            area.setVariableProvider(GlobalVariableManager.getInstance());  // allows client to read vars for rendering purposes
            area.readFromNBT(stack.getTag());
        }
        return area;
    }

    public static BlockPos getGPSLocation(World world, ItemStack gpsTool, int index) {
        ProgWidgetArea area = getArea(gpsTool);

        String var = getVariable(gpsTool, index);
        if (!var.isEmpty() && !world.isRemote) {
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
        NBTUtils.initNBTTagCompound(gpsTool);
        String var = getVariable(gpsTool, index);
        if (!var.equals("")) GlobalVariableManager.getInstance().set(var, pos);
        area.writeToNBT(gpsTool.getTag());
    }

    public static void setVariable(ItemStack gpsTool, String variable, int index) {
        ProgWidgetArea area = getArea(gpsTool);
        if (index == 0) {
            area.setCoord1Variable(variable);
        } else if (index == 1) {
            area.setCoord2Variable(variable);
        }
        NBTUtils.initNBTTagCompound(gpsTool);
        area.writeToNBT(gpsTool.getTag());
    }

    public static String getVariable(ItemStack gpsTool, int index) {
        ProgWidgetArea area = getArea(gpsTool);
        return index == 0 ? area.getCoord1Variable() : area.getCoord2Variable();
    }

    @Override
    public void syncVariables(ServerPlayerEntity player, ItemStack stack) {
        String v1 = getVariable(stack, 0);
        if (!v1.isEmpty()) PneumaticRegistry.getInstance().syncGlobalVariable(player, v1);
        String v2 = getVariable(stack, 1);
        if (!v1.isEmpty()) PneumaticRegistry.getInstance().syncGlobalVariable(player, v2);
    }

    @Override
    public List<BlockPos> getStoredPositions(World world, @Nonnull ItemStack stack) {
        Set<BlockPos> posSet = new HashSet<>();
        getArea(stack).getArea(posSet);
        return new ArrayList<>(posSet);
    }

    @Override
    public List<BlockPos> getRawStoredPositions(World world, ItemStack stack) {
        ProgWidgetArea area = getArea(stack);
        return ImmutableList.of(
                new BlockPos(area.x1, area.y1, area.z1),
                new BlockPos(area.x2, area.y2, area.z2)
        );
    }

    @Override
    public int getRenderColor(int index) {
        return 0x60FFFF00;
    }

    @Override
    public boolean disableDepthTest() {
        return false;
    }

    public static class EventHandler {
        @SubscribeEvent
        public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
            if (event.getItemStack().getItem() == ModItems.GPS_AREA_TOOL.get()) {
                if (!event.getPos().equals(getGPSLocation(event.getWorld(), event.getItemStack(), 1))) {
                    event.getPlayer().playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
                    setGPSPosAndNotify(event.getPlayer(), event.getPos(), event.getHand(), 1);
                }
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onLeftClickAir(PlayerInteractEvent.LeftClickEmpty event) {
            if (event.getItemStack().getItem() == ModItems.GPS_AREA_TOOL.get()) {
                GuiGPSAreaTool.showGUI(event.getItemStack(), event.getHand(), 1);
            }
        }
    }
}
