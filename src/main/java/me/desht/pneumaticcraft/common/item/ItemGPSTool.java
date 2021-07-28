package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.IPositionProvider;
import me.desht.pneumaticcraft.client.gui.GuiGPSTool;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemGPSTool extends Item implements IPositionProvider, IGPSToolSync {
    public ItemGPSTool() {
        super(ModItems.defaultProps());
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext ctx) {
        BlockPos pos = ctx.getPos();
        if (ctx.getPlayer() == null) return ActionResultType.PASS;
        setGPSLocation(ctx.getPlayer().getUniqueID(), ctx.getPlayer().getHeldItem(ctx.getHand()), pos);
        if (!ctx.getWorld().isRemote)
            ctx.getPlayer().sendStatusMessage(new TranslationTextComponent("pneumaticcraft.message.gps_tool.targetSet" ,pos.getX(), pos.getY(), pos.getZ()).mergeStyle(TextFormatting.GREEN), false);
        ctx.getPlayer().playSound(ModSounds.CHIRP.get(), 1.0f, 1.5f);
        return ActionResultType.SUCCESS; // we don't want to use the item.
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (worldIn.isRemote) {
            GuiGPSTool.showGUI(stack, handIn, getGPSLocation(playerIn.getUniqueID(), stack));
        }
        return ActionResult.resultSuccess(stack);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> infoList, ITooltipFlag par4) {
        super.addInformation(stack, worldIn, infoList, par4);

        if (worldIn != null) {
            ClientUtils.addGuiContextSensitiveTooltip(stack, infoList);
            BlockPos pos = getGPSLocation(ClientUtils.getClientPlayer().getUniqueID(), stack);
            if (pos != null) {
                ITextComponent translated = new TranslationTextComponent(worldIn.getBlockState(pos).getBlock().getTranslationKey());
                IFormattableTextComponent blockName = worldIn.getChunkProvider().isChunkLoaded(new ChunkPos(pos)) ?
                        new StringTextComponent(" (").append(translated).appendString(")") :
                        StringTextComponent.EMPTY.copyRaw();
                String str = String.format("[%d, %d, %d]", pos.getX(), pos.getY(), pos.getZ());
                infoList.add(new StringTextComponent(str).mergeStyle(TextFormatting.YELLOW).append(blockName.mergeStyle(TextFormatting.GREEN)));
            }
            String varName = getVariable(stack);
            if (!varName.isEmpty()) {
                infoList.add(xlate("pneumaticcraft.gui.tooltip.gpsTool.variable", varName));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean heldItem) {
        String var = getVariable(stack);
        if (!var.isEmpty() && !world.isRemote && entity instanceof PlayerEntity) {
            BlockPos pos = GlobalVariableHelper.getPos(entity.getUniqueID(), var, PneumaticCraftUtils.invalidPos());
            BlockPos curPos = getGPSLocation(entity.getUniqueID(), stack);
            if (!pos.equals(curPos)) {
                setGPSLocation(entity.getUniqueID(), stack, pos, false);
            }
        }
    }

    public static BlockPos getGPSLocation(ItemStack stack) {
        return getGPSLocation(null, stack);
    }

    public static BlockPos getGPSLocation(UUID playerId, ItemStack gpsTool) {
        CompoundNBT compound = gpsTool.getTag();
        if (compound != null) {
            BlockPos curPos = compound.contains("Pos") ? NBTUtil.readBlockPos(compound.getCompound("Pos")) : PneumaticCraftUtils.invalidPos();
            String var = getVariable(gpsTool);
            if (!var.isEmpty()) {
                BlockPos pos = GlobalVariableHelper.getPos(playerId, var);
                if (pos != null && !curPos.equals(pos)) setGPSLocation(playerId, gpsTool, pos, false);
            }
            return PneumaticCraftUtils.isValidPos(curPos) ? curPos : null;
        } else {
            return null;
        }
    }

    public static void setGPSLocation(UUID playerId, ItemStack gpsTool, BlockPos pos) {
        setGPSLocation(playerId, gpsTool, pos, true);
    }

    public static void setGPSLocation(UUID playerId, ItemStack gpsTool, BlockPos pos, boolean updateVarManager) {
        NBTUtils.setCompoundTag(gpsTool, "Pos", NBTUtil.writeBlockPos(pos));
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
        if (!gpsTool.hasTag()) return "";
        String varName = gpsTool.getTag().getString("variable");
        if (!varName.isEmpty() && !GlobalVariableHelper.hasPrefix(varName)) {
            // TODO remove in 1.17 - migrate old unprefixed varnames
            varName = "#" + varName;
            setVariable(gpsTool, varName);
        }
        return varName;
    }

    @Override
    public List<BlockPos> getStoredPositions(UUID playerId, @Nonnull ItemStack stack) {
        return Collections.singletonList(getGPSLocation(playerId, stack));
    }

    @Override
    public int getRenderColor(int index) {
        return 0x90FFFF00;
    }

    @Override
    public void syncVariables(ServerPlayerEntity player, ItemStack stack) {
        String varName = getVariable(stack);
        if (!varName.isEmpty()) PneumaticRegistry.getInstance().syncGlobalVariable(player, varName);
    }

    @Override
    public void syncFromClient(PlayerEntity player, ItemStack stack, int index, BlockPos pos, String varName) {
        ItemGPSTool.setVariable(stack, varName);
        ItemGPSTool.setGPSLocation(player.getUniqueID(), stack, pos);
        if (!varName.isEmpty()) {
            GlobalVariableHelper.setPos(player.getUniqueID(), varName, pos);
        }
    }
}
