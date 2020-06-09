package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.ItemSemiBlock;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;
import static me.desht.pneumaticcraft.lib.GuiConstants.bullet;

public abstract class ItemLogisticsFrame extends ItemSemiBlock implements ColorHandlers.ITintableItem {
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand handIn) {
        ItemStack stack = player.getHeldItem(handIn);
        if (!world.isRemote) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return stack.getDisplayName();
                }

                @Nullable
                @Override
                public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                    return new ContainerLogistics(getContainerType(), i, playerInventory, -1);
                }
            }, (buffer) -> buffer.writeVarInt(-1));
        }
        return ActionResult.resultSuccess(stack);
    }

    protected abstract ContainerType<?> getContainerType();

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> curInfo, ITooltipFlag extraInfo) {
        super.addInformation(stack, worldIn, curInfo, extraInfo);

        addLogisticsTooltip(stack, worldIn, curInfo, ClientUtils.hasShiftDown());
    }

    public static void addLogisticsTooltip(ItemStack stack, World world, List<ITextComponent> curInfo, boolean sneaking) {
        if (stack.getTag() != null && stack.getTag().contains("EntityTag") && stack.getItem() instanceof ItemSemiBlock) {
            if (sneaking) {
                CompoundNBT tag = stack.getTag().getCompound("EntityTag");
                if (tag.getBoolean(EntityLogisticsFrame.NBT_INVISIBLE)) {
                    curInfo.add(bullet().appendSibling(xlate("pneumaticcraft.gui.logistics_frame.invisible")).applyTextStyle(TextFormatting.YELLOW));
                }
                if (tag.getBoolean(EntityLogisticsFrame.NBT_MATCH_DURABILITY)) {
                    curInfo.add(bullet().appendSibling(xlate("pneumaticcraft.gui.logistics_frame.matchDurability")).applyTextStyle(TextFormatting.YELLOW));
                }
                if (tag.getBoolean(EntityLogisticsFrame.NBT_MATCH_NBT)) {
                    curInfo.add(bullet().appendSibling(xlate("pneumaticcraft.gui.logistics_frame.matchNBT")).applyTextStyle(TextFormatting.YELLOW));
                }
                if (tag.getBoolean(EntityLogisticsFrame.NBT_MATCH_MODID)) {
                    curInfo.add(bullet().appendSibling(xlate("pneumaticcraft.gui.logistics_frame.matchModId")).applyTextStyle(TextFormatting.YELLOW));
                }

                boolean whitelist = tag.getBoolean(EntityLogisticsFrame.NBT_WHITELIST);
                curInfo.add(xlate("pneumaticcraft.gui.logistics_frame." + (whitelist ? "whitelist" : "blacklist"))
                        .appendText(":").applyTextStyle(TextFormatting.YELLOW));

                ItemStackHandler handler = new ItemStackHandler();
                handler.deserializeNBT(tag.getCompound(EntityLogisticsFrame.NBT_ITEM_FILTERS));
                ItemStack[] stacks = new ItemStack[handler.getSlots()];
                for (int i = 0; i < handler.getSlots(); i++) {
                    stacks[i] = handler.getStackInSlot(i);
                }
                int l = curInfo.size();
                PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, stacks, TextFormatting.YELLOW.toString() + GuiConstants.BULLET + " ");
                if (curInfo.size() == l) curInfo.add(bullet().applyTextStyle(TextFormatting.YELLOW).appendSibling(xlate("pneumaticcraft.gui.misc.no_items").applyTextStyles(TextFormatting.GOLD, TextFormatting.ITALIC)));
                l = curInfo.size();

                EntityLogisticsFrame.FluidFilter fluidFilter = new EntityLogisticsFrame.FluidFilter();
                fluidFilter.deserializeNBT(tag.getCompound(EntityLogisticsFrame.NBT_FLUID_FILTERS));
                for (int i = 0; i < fluidFilter.size(); i++) {
                    FluidStack fluid = fluidFilter.get(i);
                    if (!fluid.isEmpty()) {
                        curInfo.add(bullet().appendText(fluid.getAmount() + "mB ").appendSibling(fluid.getDisplayName()).applyTextStyle(TextFormatting.YELLOW));
                    }
                }
                if (curInfo.size() == l) curInfo.add(bullet().applyTextStyle(TextFormatting.YELLOW).appendSibling(xlate("pneumaticcraft.gui.misc.no_fluids").applyTextStyles(TextFormatting.GOLD, TextFormatting.ITALIC)));
            } else {
                curInfo.add(xlate("pneumaticcraft.gui.logistics_frame.hasFilters"));
            }
        }
    }

}
