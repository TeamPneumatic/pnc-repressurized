package pneumaticCraft.common.itemBlock;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.BlockFluidBase;
import pneumaticCraft.common.block.BlockPneumaticCraft;
import pneumaticCraft.common.block.pneumaticPlants.BlockPneumaticPlantBase;
import pneumaticCraft.lib.Log;

public class ItemBlockPneumaticCraft extends ItemBlock{
    private BlockPneumaticCraft block;

    public ItemBlockPneumaticCraft(Block block){
        super(block);
        if(block instanceof BlockPneumaticCraft) {
            this.block = (BlockPneumaticCraft)block;
        } else {
            if(!(block instanceof BlockPneumaticPlantBase) && !(block instanceof BlockAir) && !(block instanceof BlockFluidBase)) {
                Log.warning("Block " + block.getUnlocalizedName() + " does not extend BlockPneumaticCraft! No tooltip displayed");
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean extraInfo){
        super.addInformation(stack, player, info, extraInfo);
        if(block != null) block.addInformation(stack, player, info, extraInfo);
    }

}
