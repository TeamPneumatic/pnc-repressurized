package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.client.ColorHandlers;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityLogisticsFrame;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.ItemSemiBlock;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.NBTKeys;
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
        if (stack.getTag() != null && stack.getTag().contains(NBTKeys.ENTITY_TAG) && stack.getItem() instanceof ItemSemiBlock) {
            if (sneaking) {
                CompoundNBT tag = stack.getTag().getCompound(NBTKeys.ENTITY_TAG);
                if (tag.getBoolean(EntityLogisticsFrame.NBT_INVISIBLE)) {
                    curInfo.add(bullet().append(xlate("pneumaticcraft.gui.logistics_frame.invisible")).mergeStyle(TextFormatting.YELLOW));
                }
                if (tag.getBoolean(EntityLogisticsFrame.NBT_MATCH_DURABILITY)) {
                    curInfo.add(bullet().append(xlate("pneumaticcraft.gui.logistics_frame.matchDurability")).mergeStyle(TextFormatting.YELLOW));
                }
                if (tag.getBoolean(EntityLogisticsFrame.NBT_MATCH_NBT)) {
                    curInfo.add(bullet().append(xlate("pneumaticcraft.gui.logistics_frame.matchNBT")).mergeStyle(TextFormatting.YELLOW));
                }
                if (tag.getBoolean(EntityLogisticsFrame.NBT_MATCH_MODID)) {
                    curInfo.add(bullet().append(xlate("pneumaticcraft.gui.logistics_frame.matchModId")).mergeStyle(TextFormatting.YELLOW));
                }

                boolean whitelist = tag.getBoolean(EntityLogisticsFrame.NBT_ITEM_WHITELIST);
                curInfo.add(xlate("pneumaticcraft.gui.logistics_frame." + (whitelist ? "itemWhitelist" : "itemBlacklist"))
                        .appendString(":").mergeStyle(TextFormatting.YELLOW));

                ItemStackHandler handler = new ItemStackHandler();
                handler.deserializeNBT(tag.getCompound(EntityLogisticsFrame.NBT_ITEM_FILTERS));
                ItemStack[] stacks = new ItemStack[handler.getSlots()];
                for (int i = 0; i < handler.getSlots(); i++) {
                    stacks[i] = handler.getStackInSlot(i);
                }
                int l = curInfo.size();
                PneumaticCraftUtils.summariseItemStacks(curInfo, stacks, TextFormatting.GOLD.toString() + GuiConstants.BULLET + " ");
                if (curInfo.size() == l) curInfo.add(bullet().mergeStyle(TextFormatting.GOLD).append(xlate("pneumaticcraft.gui.misc.no_items").mergeStyle(TextFormatting.GOLD, TextFormatting.ITALIC)));
                l = curInfo.size();


                whitelist = tag.getBoolean(EntityLogisticsFrame.NBT_FLUID_WHITELIST);
                curInfo.add(xlate("pneumaticcraft.gui.logistics_frame." + (whitelist ? "fluidWhitelist" : "fluidBlacklist"))
                        .appendString(":").mergeStyle(TextFormatting.YELLOW));

                EntityLogisticsFrame.FluidFilter fluidFilter = new EntityLogisticsFrame.FluidFilter();
                fluidFilter.deserializeNBT(tag.getCompound(EntityLogisticsFrame.NBT_FLUID_FILTERS));
                for (int i = 0; i < fluidFilter.size(); i++) {
                    FluidStack fluid = fluidFilter.get(i);
                    if (!fluid.isEmpty()) {
                        curInfo.add(bullet().appendString(fluid.getAmount() + "mB ").append(fluid.getDisplayName()).mergeStyle(TextFormatting.GOLD));
                    }
                }
                if (curInfo.size() == l) curInfo.add(bullet().mergeStyle(TextFormatting.GOLD).append(xlate("pneumaticcraft.gui.misc.no_fluids").mergeStyle(TextFormatting.GOLD, TextFormatting.ITALIC)));
            } else {
                curInfo.add(xlate("pneumaticcraft.gui.logistics_frame.hasFilters"));
            }
        }
    }

}
