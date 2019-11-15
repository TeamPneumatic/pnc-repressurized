package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModItems;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class ItemBucketPneumaticCraft extends BucketItem {
    private static final Item.Properties PROPS = new Item.Properties()
            .group(ModItems.Groups.PNC_CREATIVE_TAB)
            .maxStackSize(1)
            .containerItem(Items.BUCKET);

    public ItemBucketPneumaticCraft(String name, Supplier<? extends Fluid> supplier) {
        super(supplier, PROPS);

        setRegistryName(RL(name + "_bucket"));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new FluidBucketWrapper(stack);
    }
}
