package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.ItemSemiBlockBase;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.bullet;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public abstract class ItemLogisticsFrame extends ItemSemiBlockBase implements IColorableItem {

    ItemLogisticsFrame(ResourceLocation registryName) {
        super(registryName.toString());
    }

    @SuppressWarnings("unused")
    ItemLogisticsFrame(String registryName) {
        super(registryName);
    }

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
                    return new ContainerLogistics(getContainerType(), i, playerInventory, BlockPos.ZERO);
                }
            }, BlockPos.ZERO);
        }
        return ActionResult.newResult(ActionResultType.SUCCESS, stack);
    }

    protected abstract ContainerType<?> getContainerType();

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> curInfo, ITooltipFlag extraInfo) {
        super.addInformation(stack, worldIn, curInfo, extraInfo);
        addTooltip(stack, worldIn, curInfo, PneumaticCraftRepressurized.proxy.isSneakingInGui());
    }

    public static void addTooltip(ItemStack stack, World world, List<ITextComponent> curInfo, boolean sneaking) {
        if (stack.getTag() != null && stack.getItem() instanceof ItemSemiBlockBase) {
            SemiBlockLogistics logistics = ContainerLogistics.getLogistics(world, stack);
            if (logistics == null) return;
            if (sneaking) {
                if (logistics.isInvisible()) {
                    curInfo.add(bullet().appendSibling(xlate("gui.logistics_frame.invisible")));
                }
                if (logistics.isFuzzyDamage()) curInfo.add(bullet().appendSibling(xlate("gui.logistics_frame.fuzzyDamage")));
                if (logistics.isFuzzyNBT()) curInfo.add(bullet().appendSibling(xlate("gui.logistics_frame.fuzzyNBT")));
                ItemStack[] stacks = new ItemStack[logistics.getFilters().getSlots()];
                for (int i = 0; i < logistics.getFilters().getSlots(); i++) {
                    stacks[i] = logistics.getFilters().getStackInSlot(i);
                }
                curInfo.add(xlate("gui.logistics_frame." + (logistics.isWhitelist() ? "whitelist" : "blacklist")).appendText(":").applyTextStyle(TextFormatting.WHITE));
                int l = curInfo.size();
                PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, stacks);
                if (curInfo.size() == l) curInfo.add(xlate("gui.misc.no_items"));
                l = curInfo.size();
                for (int i = 0; i < 9; i++) {
                    FluidStack fluid = logistics.getTankFilter(i).getFluid();
                    if (!fluid.isEmpty()) {
                        curInfo.add(bullet().appendText(fluid.getAmount() + "mB ").appendSibling(fluid.getDisplayName()));
                    }
                }
                if (curInfo.size() == l) curInfo.add(xlate("gui.misc.no_fluids"));
            } else {
                String key = stack.getItem().getRegistryName().getPath();
                curInfo.add(xlate(String.format("gui.%s.hasFilters", key)));
            }
        }
    }

}
