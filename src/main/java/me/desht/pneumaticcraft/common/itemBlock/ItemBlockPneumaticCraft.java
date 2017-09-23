package me.desht.pneumaticcraft.common.itemBlock;

import me.desht.pneumaticcraft.common.block.BlockPneumaticCraft;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;

import java.util.List;

public class ItemBlockPneumaticCraft extends ItemBlock {
    private BlockPneumaticCraft block;

    public ItemBlockPneumaticCraft(Block block) {
        super(block);
        if (block instanceof BlockPneumaticCraft) {
            this.block = (BlockPneumaticCraft) block;
        } else {
            if (!(block instanceof BlockAir) && !(block instanceof BlockFluidBase)) {
                Log.warning("Block " + block.getUnlocalizedName() + " does not extend BlockPneumaticCraft! No tooltip displayed");
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> info, ITooltipFlag flag) {
        super.addInformation(stack, world, info, flag);
        if (block != null) block.addInformation(stack, world, info, flag);
    }

//    public void registerItemVariants() {
//        List<ItemStack> stacks = new ArrayList<ItemStack>();
//        getSubItems(this, null, stacks);
//        for (ItemStack stack : stacks) {
//            ResourceLocation resLoc = new ResourceLocation(Names.MOD_ID, getModelLocation(stack));
//            ModelBakery.registerItemVariants(this, resLoc);
//            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(this, stack.getItemDamage(), new ModelResourceLocation(resLoc, "inventory"));
//        }
//    }

//    protected String getModelLocation(ItemStack stack) {
//        return stack.getUnlocalizedName().substring(5);
//    }

}
