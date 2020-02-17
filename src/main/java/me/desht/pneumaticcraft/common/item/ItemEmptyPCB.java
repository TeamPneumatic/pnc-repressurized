package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModFluids;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityUVLightBox;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemEmptyPCB extends ItemNonDespawning {
    private static final String NBT_ETCH_PROGRESS = "pneumaticcraft:etch_progress";

    @Override
    public void addInformation(ItemStack stack, World player, List<ITextComponent> infoList, ITooltipFlag par4) {
        super.addInformation(stack, player, infoList, par4);
        int uvProgress = TileEntityUVLightBox.getExposureProgress(stack);
        int etchProgress = getEtchProgress(stack);

        infoList.add(xlate("gui.tooltip.item.uvLightBox.successChance", uvProgress));
        if (uvProgress < 100) {
            infoList.add(xlate("gui.tooltip.item.uvLightBox.putInLightBox"));
        }
        if (etchProgress > 0) {
            infoList.add(xlate("gui.tooltip.item.uvLightBox.etchProgress", etchProgress));
        }
        if (uvProgress > 0) {
            infoList.add(xlate("gui.tooltip.item.uvLightBox.putInAcid"));
        }
    }

    private static int getEtchProgress(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(NBT_ETCH_PROGRESS) : 0;
    }

    private static void setEtchProgress(ItemStack stack, int progress) {
        Validate.isTrue(progress >= 0 && progress <= 100);
        stack.getOrCreateTag().putInt(NBT_ETCH_PROGRESS, progress);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        int progress = TileEntityUVLightBox.getExposureProgress(stack);
        return 1 - progress / 100D;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        int progress = TileEntityUVLightBox.getExposureProgress(stack);
        return progress < 100;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        int progress = TileEntityUVLightBox.getExposureProgress(stack);
        return progress << 16 | 0xFF;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
        super.onEntityItemUpdate(stack, entityItem);

        if (entityItem.world.getFluidState(new BlockPos(entityItem)).getFluid() == ModFluids.ETCHING_ACID.get()) {
            if (!stack.hasTag()) {
                stack.setTag(new CompoundNBT());
            }
            int etchProgress = getEtchProgress(stack);
            if (etchProgress < 100) {
                if (entityItem.ticksExisted % (TileEntityConstants.PCB_ETCH_TIME / 5) == 0) {
                    setEtchProgress(stack, etchProgress + 1);
                }
                World world = entityItem.getEntityWorld();
                if (world.rand.nextInt(15) == 0) {
                    double x = entityItem.posX + world.rand.nextDouble() * 0.3 - 0.15;
                    double y = entityItem.posY - 0.15;
                    double z = entityItem.posZ + world.rand.nextDouble() * 0.3 - 0.15;
                    world.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0, 0.05, 0.0);
                }
            } else if (!entityItem.world.isRemote) {
                int successCount = 0;
                int failedCount = 0;
                int uvProgress = TileEntityUVLightBox.getExposureProgress(stack);
                for (int i = 0; i < stack.getCount(); i++) {
                    if (entityItem.world.rand.nextInt(100) <= uvProgress) {
                        successCount++;
                    } else {
                        failedCount++;
                    }
                }

                ItemStack successStack = new ItemStack(successCount == 0 ? ModItems.FAILED_PCB.get() : ModItems.UNASSEMBLED_PCB.get(),
                        successCount == 0 ? failedCount : successCount);
                entityItem.setItem(successStack);

                // Only when we have failed items and the existing item entity wasn't reused already for the failed items.
                if (successCount > 0 && failedCount > 0) {
                    ItemStack failedStack = new ItemStack(ModItems.FAILED_PCB.get(), failedCount);
                    entityItem.world.addEntity(new ItemEntity(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ, failedStack));
                }
            }
        }
        return false;
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            items.add(new ItemStack(this));

            ItemStack stack = new ItemStack(this);
            TileEntityUVLightBox.setExposureProgress(stack, 100);
            items.add(stack);
        }
    }
}
