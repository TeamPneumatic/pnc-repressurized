package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.areatool.GuiGPSAreaTool;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemGPSAreaTool extends Item implements IPositionProvider, IGPSToolSync {
    public ItemGPSAreaTool() {
        super(ModItems.defaultProps());
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        if (ctx.getPlayer() != null) {
            setGPSPosAndNotify(ctx.getPlayer(), ctx.getHand(), ctx.getPos(), 0);
            ctx.getPlayer().playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (worldIn.isRemote) {
            GuiGPSAreaTool.showGUI(stack, handIn, 0);
        }
        return ActionResult.resultSuccess(stack);
    }


    public static void setGPSPosAndNotify(PlayerEntity player, ItemStack stack, BlockPos pos, int index) {
        setGPSLocation(player, stack, pos, null, index, true);
        if (player instanceof ServerPlayerEntity) {
            player.sendStatusMessage(new StringTextComponent(TextFormatting.AQUA + String.format("[%s] ", stack.getDisplayName().getString()))
                    .append(getMessageText(player.world, pos, index)), false);
            ((ServerPlayerEntity) player).connection.sendPacket(new SHeldItemChangePacket(player.inventory.currentItem));
        }
    }

    public static void setGPSPosAndNotify(PlayerEntity player, Hand hand, BlockPos pos, int index) {
        setGPSPosAndNotify(player, player.getHeldItem(hand), pos, index);
    }

    private static ITextComponent getMessageText(World worldIn, BlockPos pos, int index) {
        ITextComponent translated = new TranslationTextComponent(worldIn.getBlockState(pos).getBlock().getTranslationKey());
        IFormattableTextComponent blockName = worldIn.getChunkProvider().isChunkLoaded(new ChunkPos(pos)) ?
                new StringTextComponent(" (").append(translated).appendString(")") :
                StringTextComponent.EMPTY.copyRaw();
        String str = String.format("P%d%s: [%d, %d, %d]", index + 1, TextFormatting.YELLOW, pos.getX(), pos.getY(), pos.getZ());
        return new StringTextComponent(str).mergeStyle(index == 0 ? TextFormatting.RED : TextFormatting.GREEN).append(blockName.mergeStyle(TextFormatting.GREEN));
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> infoList, ITooltipFlag tooltipFlag) {
        super.addInformation(stack, worldIn, infoList, tooltipFlag);

        if (worldIn != null) {
            ClientUtils.addGuiContextSensitiveTooltip(stack, infoList);
            int n = infoList.size();
            ProgWidgetArea area = getArea(ClientUtils.getClientPlayer(), stack);
            for (int index = 0; index < 2; index++) {
                final int i = index;
                getGPSLocation(ClientUtils.getClientPlayer(), stack, index).ifPresent(pos -> infoList.add(getMessageText(worldIn, pos, i)));
                String varName = area.getVarName(index);
                if (!varName.isEmpty()) {
                    infoList.add(xlate("pneumaticcraft.gui.tooltip.gpsTool.variable", varName));
                }
            }
            if (infoList.size() - n >= 2) area.addAreaTypeTooltip(infoList);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean heldItem) {
        if (!world.isRemote && entity instanceof PlayerEntity) {
            ProgWidgetArea area = getArea((PlayerEntity) entity, stack);
            for (int index = 0; index < 2; index++) {
                String varName = area.getVarName(index);
                if (!varName.isEmpty()) {
                    BlockPos curPos = area.getPos(index).orElse(PneumaticCraftUtils.invalidPos());
                    BlockPos pos = GlobalVariableHelper.getPos(entity.getUniqueID(), varName, PneumaticCraftUtils.invalidPos());
                    if (!curPos.equals(pos)) {
                        setGPSLocation((PlayerEntity) entity, stack, pos, area, index, false);
                    }
                }
            }
        }
    }

    @Nonnull
    public static ProgWidgetArea getArea(UUID playerId, ItemStack stack) {
        Validate.isTrue(stack.getItem() instanceof ItemGPSAreaTool);
        ProgWidgetArea area = new ProgWidgetArea();
        if (stack.hasTag()) {
            area.setVariableProvider(GlobalVariableHelper.getVariableProvider(), playerId);  // allows client to read vars for rendering purposes
            area.readFromNBT(stack.getTag());
        }
        return area;
    }

    public static ProgWidgetArea getArea(PlayerEntity player, ItemStack stack) {
        return getArea(player.getUniqueID(), stack);
    }

    @Nonnull
    public static Optional<BlockPos> getGPSLocation(PlayerEntity player, ItemStack gpsTool, int index) {
        Validate.isTrue(index == 0 || index == 1, "index must be 0 or 1!");
        ProgWidgetArea area = getArea(player, gpsTool);
        Optional<BlockPos> pos = area.getPos(index);

        // if there's a variable set for this index, use its value instead (and update the stored position)
        String var = area.getVarName(index);
        if (!var.isEmpty() && !player.getEntityWorld().isRemote) {
            BlockPos newPos = GlobalVariableHelper.getPos(player.getUniqueID(), var);
            if (!pos.isPresent() || !pos.get().equals(newPos)) {
                area.setPos(index, newPos);
                area.writeToNBT(gpsTool.getOrCreateTag());
            }
            return Optional.of(newPos);
        }

        return pos;
    }

    private static void setGPSLocation(PlayerEntity player, ItemStack gpsTool, BlockPos pos, ProgWidgetArea area, int index, boolean updateVar) {
        if (area == null) area = getArea(player, gpsTool);
        area.setPos(index, pos);
        area.writeToNBT(gpsTool.getOrCreateTag());

        if (updateVar) {
            String varName = area.getVarName(index);
            if (!varName.isEmpty()) {
                GlobalVariableHelper.setPos(player.getUniqueID(), varName, pos);
            }
        }
    }

    public static void setVariable(PlayerEntity player, ItemStack gpsTool, String variable, int index) {
        ProgWidgetArea area = getArea(player, gpsTool);
        area.setVarName(index, variable);
        area.writeToNBT(gpsTool.getOrCreateTag());
    }

    public static String getVariable(PlayerEntity player, ItemStack gpsTool, int index) {
        return getArea(player, gpsTool).getVarName(index);
    }

    @Override
    public void syncVariables(ServerPlayerEntity player, ItemStack stack) {
        ProgWidgetArea area = getArea(player, stack);
        String v1 = area.getVarName(0);
        String v2 = area.getVarName(1);
        if (!v1.isEmpty()) PneumaticRegistry.getInstance().syncGlobalVariable(player, v1);
        if (!v2.isEmpty()) PneumaticRegistry.getInstance().syncGlobalVariable(player, v2);
    }

    @Override
    public List<BlockPos> getStoredPositions(UUID playerId, @Nonnull ItemStack stack) {
        Set<BlockPos> posSet = new HashSet<>();
        getArea(playerId, stack).getArea(posSet);
        return new ArrayList<>(posSet);
    }

    @Override
    public List<BlockPos> getRawStoredPositions(PlayerEntity player, ItemStack stack) {
        ProgWidgetArea area = getArea(player, stack);
        return ImmutableList.of(area.getPos(0).orElse(BlockPos.ZERO), area.getPos(1).orElse(BlockPos.ZERO));
    }

    @Override
    public int getRenderColor(int index) {
        return 0x60FFFF00;
    }

    @Override
    public boolean disableDepthTest() {
        return false;
    }

    @Override
    public void syncFromClient(PlayerEntity player, ItemStack stack, int index, BlockPos pos, String varName) {
        ItemGPSAreaTool.setVariable(player, stack, varName, index);
        ItemGPSAreaTool.setGPSPosAndNotify(player, stack, pos, index);
        if (!varName.isEmpty()) {
            GlobalVariableHelper.setPos(player.getUniqueID(), varName, pos);
        }
    }

    public static class EventHandler {
        @SubscribeEvent
        public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
            if (event.getItemStack().getItem() == ModItems.GPS_AREA_TOOL.get()) {
                Optional<BlockPos> optPos = getGPSLocation(event.getPlayer(), event.getItemStack(), 1);
                if (!event.getPos().equals(optPos.orElse(null))) {
                    event.getPlayer().playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
                    setGPSPosAndNotify(event.getPlayer(), event.getHand(), event.getPos(), 1);
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
