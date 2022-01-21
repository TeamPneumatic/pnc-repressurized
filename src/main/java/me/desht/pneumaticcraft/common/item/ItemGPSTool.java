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
import me.desht.pneumaticcraft.client.gui.GuiGPSTool;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemGPSTool extends Item implements IPositionProvider {
    public ItemGPSTool() {
        super(ModItems.defaultProps());
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        setGPSLocation(ctx.getPlayer().getItemInHand(ctx.getHand()), pos);
        if (!ctx.getLevel().isClientSide)
            ctx.getPlayer().displayClientMessage(new TranslatableComponent("pneumaticcraft.message.gps_tool.targetSet" ,pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GREEN), false);
        ctx.getPlayer().playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
        return InteractionResult.SUCCESS; // we don't want to use the item.
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (worldIn.isClientSide) {
            GuiGPSTool.showGUI(stack, handIn, getGPSLocation(worldIn, stack));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> infoList, TooltipFlag par4) {
        super.appendHoverText(stack, worldIn, infoList, par4);

        ClientUtils.addGuiContextSensitiveTooltip(stack, infoList);
        BlockPos pos = getGPSLocation(stack);
        if (pos != null) {
            Component translated = PneumaticCraftUtils.getBlockNameAt(worldIn, pos);
            MutableComponent blockName = new TextComponent(" (").append(translated).append(")");
            String str = String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());
            infoList.add(new TextComponent(str).withStyle(ChatFormatting.YELLOW).append(blockName.withStyle(ChatFormatting.GREEN)));
        }
        String varName = getVariable(stack);
        if (!varName.isEmpty()) {
            infoList.add(xlate("pneumaticcraft.gui.tooltip.gpsTool.variable", varName));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean heldItem) {
        String var = getVariable(stack);
        if (!var.equals("") && !world.isClientSide) {
            BlockPos pos = GlobalVariableManager.getInstance().getPos(var);
            setGPSLocation(stack, pos);
        }
    }

    public static BlockPos getGPSLocation(ItemStack stack) {
        return getGPSLocation(null, stack);
    }

    public static BlockPos getGPSLocation(Level world, ItemStack gpsTool) {
        CompoundTag compound = gpsTool.getTag();
        if (compound != null) {
            String var = getVariable(gpsTool);
            if (!var.isEmpty() && world != null && !world.isClientSide) {
                BlockPos pos = GlobalVariableManager.getInstance().getPos(var);
                setGPSLocation(gpsTool, pos);
            }
            BlockPos pos = net.minecraft.nbt.NbtUtils.readBlockPos(compound.getCompound("Pos"));
            return pos.equals(BlockPos.ZERO) ? null : pos;
        } else {
            return null;
        }
    }

    public static void setGPSLocation(ItemStack gpsTool, BlockPos pos) {
        gpsTool.getOrCreateTag().put("Pos", net.minecraft.nbt.NbtUtils.writeBlockPos(pos));
        String var = getVariable(gpsTool);
        if (!var.isEmpty()) GlobalVariableManager.getInstance().set(var, pos);
    }

    public static void setVariable(ItemStack gpsTool, String variable) {
        NBTUtils.setString(gpsTool, "variable", variable);
    }

    public static String getVariable(ItemStack gpsTool) {
        return gpsTool.hasTag() ? gpsTool.getTag().getString("variable") : "";
    }

    @Override
    public List<BlockPos> getStoredPositions(Level world, @Nonnull ItemStack stack) {
        return Collections.singletonList(getGPSLocation(world, stack));
    }

    @Override
    public int getRenderColor(int index) {
        return 0x90FFFF00;
    }

    @Override
    public void syncVariables(ServerPlayer player, ItemStack stack) {
        String varName = getVariable(stack);
        if (!varName.isEmpty()) PneumaticRegistry.getInstance().syncGlobalVariable(player, varName);
    }
}
