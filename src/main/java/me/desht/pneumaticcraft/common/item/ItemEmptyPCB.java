package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemEmptyPCB extends ItemNonDespawning {
    public ItemEmptyPCB() {
        super(defaultProps().maxDamage(100).setNoRepair(), "empty_pcb");
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<ITextComponent> infoList, ITooltipFlag par4) {
        super.addInformation(stack, player, infoList, par4);
        if (stack.getDamage() < 100) {
            infoList.add(xlate("gui.tooltip.item.uvLightBox.successChance", 100 - stack.getDamage()));
        } else {
            infoList.add(xlate("gui.tooltip.item.uvLightBox.putInLightBox"));
        }
        if (stack.hasTag()) {
            infoList.add(xlate("gui.tooltip.item.uvLightBox.etchProgress",stack.getTag().getInt("etchProgress")));
        } else if (stack.getDamage() < 100) {
            infoList.add(xlate("gui.tooltip.item.uvLightBox.putInAcid"));
        }
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
        super.onEntityItemUpdate(stack, entityItem);

        // todo 1.14 fluids : replace WATER with ETCHING_ACID
        if (entityItem.world.getFluidState(new BlockPos(entityItem)).isTagged(FluidTags.WATER)) {
            if (!stack.hasTag()) {
                stack.setTag(new CompoundNBT());
            }
            int etchProgress = stack.getTag().getInt("etchProgress");
            if (etchProgress < 100) {
                if (entityItem.ticksExisted % (TileEntityConstants.PCB_ETCH_TIME / 5) == 0) {
                    stack.getTag().putInt("etchProgress", etchProgress + 1);
                }
                World world = entityItem.getEntityWorld();
                if (world.rand.nextInt(15) == 0) {
                    double x = entityItem.posX + world.rand.nextDouble() * 0.3 - 0.15;
                    double y = entityItem.posY - 0.15;
                    double z = entityItem.posZ + world.rand.nextDouble() * 0.3 - 0.15;
                    world.addParticle(ParticleTypes.BUBBLE, x, y, z, 0.0, 0.05, 0.0);
                }
            } else if (!entityItem.world.isRemote) {
                int successCount = 0;
                int failedCount = 0;
                for (int i = 0; i < stack.getCount(); i++) {
                    if (entityItem.world.rand.nextInt(100) >= stack.getDamage()) {
                        successCount++;
                    } else {
                        failedCount++;
                    }
                }

                ItemStack successStack = new ItemStack(successCount == 0 ? ModItems.FAILED_PCB : ModItems.UNASSEMBLED_PCB,
                        successCount == 0 ? failedCount : successCount);
                entityItem.setItem(successStack);

                // Only when we have failed items and the existing item entity wasn't reused already for the failed items.
                if (successCount > 0 && failedCount > 0) {
                    ItemStack failedStack = new ItemStack(ModItems.FAILED_PCB, failedCount);
                    entityItem.world.addEntity(new ItemEntity(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ, failedStack));
                }
            }
        }
        return false;
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        ItemStack stack = new ItemStack(this);
        stack.setDamage(stack.getMaxDamage());
        items.add(stack);
    }
}
