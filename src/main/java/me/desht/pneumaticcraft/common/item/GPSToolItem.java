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

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.GPSToolScreen;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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

import javax.annotation.Nonnull;
import java.util.*;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GPSToolItem extends Item implements IPositionProvider, IGPSToolSync {
    public GPSToolItem() {
        super(ModItems.defaultProps());
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        if (ctx.getPlayer() == null) return InteractionResult.PASS;
        setGPSLocation(ctx.getPlayer().getUUID(), ctx.getPlayer().getItemInHand(ctx.getHand()), pos);
        if (!ctx.getLevel().isClientSide)
            ctx.getPlayer().displayClientMessage(Component.translatable("pneumaticcraft.message.gps_tool.targetSet" ,pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GREEN), false);
        ctx.getPlayer().playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
        return InteractionResult.SUCCESS; // we don't want to use the item.
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (worldIn.isClientSide) {
            GPSToolScreen.showGUI(stack, handIn, getGPSLocation(playerIn.getUUID(), stack).orElse(playerIn.blockPosition()));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> infoList, TooltipFlag par4) {
        super.appendHoverText(stack, worldIn, infoList, par4);

        if (worldIn != null) {
            ClientUtils.addGuiContextSensitiveTooltip(stack, infoList);
            getGPSLocation(ClientUtils.getClientPlayer().getUUID(), stack).ifPresent(pos -> {
                Component translated = Component.translatable(worldIn.getBlockState(pos).getBlock().getDescriptionId());
                MutableComponent blockName = worldIn.isLoaded(pos) ?
                        Component.literal(" (").append(translated).append(")") :
                        Component.empty().plainCopy();
                String str = String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());
                infoList.add(Component.literal(str).withStyle(ChatFormatting.YELLOW).append(blockName.withStyle(ChatFormatting.GREEN)));
            });
            String varName = getVariable(stack);
            if (!varName.isEmpty()) {
                infoList.add(xlate("pneumaticcraft.gui.tooltip.gpsTool.variable", varName));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean heldItem) {
        String var = getVariable(stack);
        if (!var.isEmpty() && !world.isClientSide && entity instanceof Player) {
            getGPSLocation(entity.getUUID(), stack).ifPresent(curPos -> {
                BlockPos varPos = GlobalVariableHelper.getPos(entity.getUUID(), var, PneumaticCraftUtils.invalidPos());
                if (!varPos.equals(curPos)) {
                    setGPSLocation(entity.getUUID(), stack, varPos, false);
                }
            });
        }
    }

    @Nonnull
    public static Optional<BlockPos> getGPSLocation(ItemStack stack) {
        return getGPSLocation(null, stack);
    }

    @Nonnull
    public static Optional<BlockPos> getGPSLocation(UUID playerId, ItemStack gpsTool) {
        CompoundTag compound = gpsTool.getTag();
        if (gpsTool.getItem() == ModItems.GPS_TOOL.get() && compound != null) {
            BlockPos curPos = compound.contains("Pos") ? NbtUtils.readBlockPos(compound.getCompound("Pos")) : PneumaticCraftUtils.invalidPos();
            String var = getVariable(gpsTool);
            if (!var.isEmpty()) {
                BlockPos pos = GlobalVariableHelper.getPos(playerId, var);
                if (pos != null && !curPos.equals(pos)) setGPSLocation(playerId, gpsTool, pos, false);
            }
            return PneumaticCraftUtils.isValidPos(curPos) ? Optional.of(curPos) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    public static void setGPSLocation(UUID playerId, ItemStack gpsTool, BlockPos pos) {
        setGPSLocation(playerId, gpsTool, pos, true);
    }

    public static void setGPSLocation(UUID playerId, ItemStack gpsTool, BlockPos pos, boolean updateVarManager) {
        NBTUtils.setCompoundTag(gpsTool, "Pos", NbtUtils.writeBlockPos(pos));
        if (updateVarManager) {
            String var = getVariable(gpsTool);
            if (!var.isEmpty()) {
                GlobalVariableHelper.setPos(playerId, var, pos);
            }
        }
    }

    public static void setVariable(ItemStack gpsTool, String variable) {
        NBTUtils.setString(gpsTool, "variable", variable);
    }

    public static String getVariable(ItemStack gpsTool) {
        return gpsTool.hasTag() ? Objects.requireNonNull(gpsTool.getTag()).getString("variable") : "";
    }

    @Override
    public List<BlockPos> getStoredPositions(UUID playerId, @Nonnull ItemStack stack) {
        return getGPSLocation(playerId, stack).map(Collections::singletonList).orElse(Collections.emptyList());
    }

    @Override
    public int getRenderColor(int index) {
        return 0x90FFFF00;
    }

    @Override
    public void syncVariables(ServerPlayer player, ItemStack stack) {
        String varName = getVariable(stack);
        if (GlobalVariableHelper.hasPrefix(varName)) PneumaticRegistry.getInstance().getMiscHelpers().syncGlobalVariable(player, varName);
    }

    @Override
    public void syncFromClient(Player player, ItemStack stack, int index, BlockPos pos, String varName) {
        GPSToolItem.setVariable(stack, varName);
        GPSToolItem.setGPSLocation(player.getUUID(), stack, pos);
        if (!varName.isEmpty()) {
            GlobalVariableHelper.setPos(player.getUUID(), varName, pos);
        }
    }
}
