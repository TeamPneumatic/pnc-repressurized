/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.item;

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.GPSAreaToolScreen;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.drone.progwidgets.SavedDroneProgram;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GPSAreaToolItem extends Item implements IPositionProvider, IGPSToolSync {
    public GPSAreaToolItem() {
        super(ModItems.defaultProps()
                .component(ModDataComponents.SAVED_DRONE_PROGRAM, SavedDroneProgram.EMPTY)
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getPlayer() != null) {
            Optional<BlockPos> optPos = getGPSLocation(ctx.getPlayer(), ctx.getItemInHand(), 0);
            if (!ctx.getClickedPos().equals(optPos.orElse(null))) {
                setGPSPosAndNotify(ctx.getPlayer(), ctx.getHand(), ctx.getClickedPos(), 0);
                ctx.getPlayer().playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
            }
        }
        return InteractionResult.SUCCESS; // we don't want to use the item.
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (worldIn.isClientSide) {
            GPSAreaToolScreen.showGUI(stack, handIn, 0);
        }
        return InteractionResultHolder.success(stack);
    }

    public static void setGPSPosAndNotify(Player player, ItemStack stack, BlockPos pos, int index) {
        setGPSLocation(player, stack, pos, null, index, true);
        if (player instanceof ServerPlayer) {
            player.displayClientMessage(Component.literal(ChatFormatting.AQUA + String.format("[%s] ", stack.getDisplayName().getString()))
                    .append(getMessageText(player.level(), pos, index)), false);
        }
    }

    public static void setGPSPosAndNotify(Player player, InteractionHand hand, BlockPos pos, int index) {
        setGPSPosAndNotify(player, player.getItemInHand(hand), pos, index);
    }

    private static Component getMessageText(Level worldIn, BlockPos pos, int index) {
        Component translated = PneumaticCraftUtils.getBlockNameAt(worldIn, pos);
        MutableComponent blockName = worldIn.isLoaded(pos) ?
                Component.literal(" (").append(translated).append(")") :
                Component.empty().plainCopy();
        String str = String.format("P%d%s: [%d, %d, %d]", index + 1, ChatFormatting.YELLOW, pos.getX(), pos.getY(), pos.getZ());
        return Component.literal(str).withStyle(index == 0 ? ChatFormatting.RED : ChatFormatting.GREEN).append(blockName.withStyle(ChatFormatting.GREEN));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> infoList, TooltipFlag par4) {
        super.appendHoverText(stack, context, infoList, par4);

        ClientUtils.getOptionalClientLevel().ifPresent(level ->  {
            ClientUtils.addGuiContextSensitiveTooltip(stack, infoList);
            int n = infoList.size();
            ProgWidgetArea area = getArea(ClientUtils.getClientPlayer(), stack);
            for (int index = 0; index < 2; index++) {
                final int i = index;
                getGPSLocation(ClientUtils.getClientPlayer(), stack, index)
                        .ifPresent(pos -> infoList.add(getMessageText(level, pos, i)));
                String varName = area.getVarName(index);
                if (!varName.isEmpty()) {
                    infoList.add(xlate("pneumaticcraft.gui.tooltip.gpsTool.variable", varName));
                }
            }
            if (infoList.size() - n >= 2) area.addAreaTypeTooltip(infoList);
        });
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean heldItem) {
        if (!world.isClientSide && entity instanceof Player p) {
            ProgWidgetArea area = getArea(p, stack);
            for (int index = 0; index < 2; index++) {
                String varName = area.getVarName(index);
                if (!varName.isEmpty()) {
                    BlockPos curPos = area.getPos(index).orElse(PneumaticCraftUtils.invalidPos());
                    BlockPos pos = GlobalVariableHelper.getInstance().getPos(entity.getUUID(), varName, PneumaticCraftUtils.invalidPos());
                    if (!curPos.equals(pos)) {
                        setGPSLocation(p, stack, pos, area, index, false);
                    }
                }
            }
        }
    }

    @Nonnull
    public static ProgWidgetArea getArea(UUID playerId, ItemStack stack) {
        Validate.isTrue(stack.getItem() instanceof GPSAreaToolItem);

        List<IProgWidget> widgets = SavedDroneProgram.loadProgWidgets(stack);
        if (!widgets.isEmpty() && widgets.getFirst() instanceof ProgWidgetArea area) {
            area.setVariableProvider(GlobalVariableHelper.getInstance().getVariableProvider(), playerId);  // allows client to read vars for rendering purposes
            return area;
        }
        return new ProgWidgetArea();
    }

    public static ProgWidgetArea getArea(Player player, ItemStack stack) {
        return getArea(player.getUUID(), stack);
    }

    public static Optional<BlockPos> getGPSLocation(Player player, ItemStack gpsTool, int index) {
        Validate.isTrue(index == 0 || index == 1, "index must be 0 or 1!");
        ProgWidgetArea area = getArea(player, gpsTool);
        Optional<BlockPos> pos = area.getPos(index);

        // if there's a variable set for this index, use its value instead (and update the stored position)
        String var = area.getVarName(index);
        if (!var.isEmpty() && !player.level().isClientSide) {
            BlockPos newPos = GlobalVariableHelper.getInstance().getPos(player.getUUID(), var);
            if (pos.isEmpty() || !pos.get().equals(newPos)) {
                area.setPos(index, newPos);
                setArea(gpsTool, area);
            }
            return Optional.of(newPos);
        }

        return pos;
    }

    private static void setGPSLocation(Player player, ItemStack gpsTool, BlockPos pos, ProgWidgetArea area, int index, boolean updateVar) {
        if (area == null) area = getArea(player, gpsTool);
        area.setPos(index, pos);
        setArea(gpsTool, area);

        if (updateVar) {
            String varName = area.getVarName(index);
            if (!varName.isEmpty()) {
                GlobalVariableHelper.getInstance().setPos(player.getUUID(), varName, pos);
            }
        }
    }

    public static void setVariable(Player player, ItemStack gpsTool, String variable, int index) {
        ProgWidgetArea area = getArea(player, gpsTool);
        area.setVarName(index, variable);
        setArea(gpsTool, area);
    }

    public static void setArea(ItemStack gpsTool, ProgWidgetArea area) {
        SavedDroneProgram.writeToItem(gpsTool, List.of(area));
    }

    public static String getVariable(Player player, ItemStack gpsTool, int index) {
        return getArea(player, gpsTool).getVarName(index);
    }

    @Override
    public void syncVariables(ServerPlayer player, ItemStack stack) {
        ProgWidgetArea area = getArea(player, stack);
        String v1 = area.getVarName(0);
        String v2 = area.getVarName(1);
        if (GlobalVariableHelper.getInstance().hasPrefix(v1)) PneumaticRegistry.getInstance().getMiscHelpers().syncGlobalVariable(player, v1);
        if (GlobalVariableHelper.getInstance().hasPrefix(v2)) PneumaticRegistry.getInstance().getMiscHelpers().syncGlobalVariable(player, v2);
    }

    @Override
    public List<BlockPos> getStoredPositions(UUID playerId, @Nonnull ItemStack stack) {
        ProgWidgetArea widgetArea = getArea(playerId, stack);
        return List.copyOf(widgetArea.getArea(new HashSet<>()));
    }

    @Override
    public List<BlockPos> getRawStoredPositions(Player player, ItemStack stack) {
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
    public void syncFromClient(Player player, ItemStack stack, int index, BlockPos pos, String varName) {
        GPSAreaToolItem.setVariable(player, stack, varName, index);
        GPSAreaToolItem.setGPSPosAndNotify(player, stack, pos, index);
        if (!varName.isEmpty()) {
            GlobalVariableHelper.getInstance().setPos(player.getUUID(), varName, pos);
        }
    }

    public static class EventHandler {
        @SubscribeEvent
        public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
            if (event.getItemStack().getItem() == ModItems.GPS_AREA_TOOL.get()) {
                Optional<BlockPos> optPos = getGPSLocation(event.getEntity(), event.getItemStack(), 1);
                if (!event.getPos().equals(optPos.orElse(null))) {
                    event.getEntity().playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
                    setGPSPosAndNotify(event.getEntity(), event.getHand(), event.getPos(), 1);
                }
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onLeftClickAir(PlayerInteractEvent.LeftClickEmpty event) {
            if (event.getItemStack().getItem() == ModItems.GPS_AREA_TOOL.get()) {
                GPSAreaToolScreen.showGUI(event.getItemStack(), event.getHand(), 1);
            }
        }
    }
}
