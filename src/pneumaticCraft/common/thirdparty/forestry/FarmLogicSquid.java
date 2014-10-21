package pneumaticCraft.common.thirdparty.forestry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmComponent;
import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmLogic;

public class FarmLogicSquid implements IFarmLogic{
    private final IFarmHousing housing;
    private IIcon icon;
    private final FarmablePlastic farmable = new FarmablePlastic(Blockss.squidPlant);

    public FarmLogicSquid(IFarmHousing farmHousing){
        housing = farmHousing;
    }

    @Override
    public int getFertilizerConsumption(){
        return 5;
    }

    @Override
    public int getWaterConsumption(float hydrationModifier){
        return (int)(20 * hydrationModifier);
    }

    @Override
    public boolean isAcceptedResource(ItemStack itemstack){
        return itemstack.getItem() instanceof ItemBlock && Forestry.farmStructureBlocks.contains(((ItemBlock)itemstack.getItem()).field_150939_a);
    }

    @Override
    public boolean isAcceptedGermling(ItemStack itemstack){
        return itemstack.getItem() == Itemss.plasticPlant && itemstack.getItemDamage() == ItemPlasticPlants.SQUID_PLANT_DAMAGE;
    }

    @Override
    public Collection<ItemStack> collect(){
        List<ItemStack> col = new ArrayList<ItemStack>();
        int[] coords = housing.getCoords();
        int[] area = housing.getArea();
        int[] offset = housing.getOffset();

        AxisAlignedBB harvestBox = AxisAlignedBB.getBoundingBox(coords[0] + offset[0], coords[1] + offset[1], coords[2] + offset[2], coords[0] + offset[0] + area[0], coords[1] + offset[1] + area[1], coords[2] + offset[2] + area[2]);
        List<EntityItem> list = housing.getWorld().getEntitiesWithinAABB(EntityItem.class, harvestBox);

        for(EntityItem item : list) {
            if(!item.isDead) {
                ItemStack contained = item.getEntityItem();
                if(isAcceptedGermling(contained)) {
                    col.add(contained.copy());
                    item.setDead();
                }
            }
        }
        return col;
    }

    @Override
    public boolean cultivate(int x, int y, int z, ForgeDirection d, int extent){
        for(int i = 0; i < extent; i++) {
            if(tryPlaceSoil(x + d.offsetX * i, y + d.offsetY * i, z + d.offsetZ * i, i == extent - 1)) return true;
        }
        for(int i = 0; i < extent; i++) {
            if(manageCrops(x + d.offsetX * i, y + d.offsetY * i + 1, z + d.offsetZ * i)) return true;
        }
        return false;
    }

    private boolean tryPlaceSoil(int x, int y, int z, boolean isLast){
        if(housing.getWorld().getBlock(x, y, z).isReplaceable(housing.getWorld(), x, y, z)) {
            if(!isLast && shouldTurnIntoWater(x, y, z)) {
                if((housing.getWorld().getBlock(x, y, z) != Blocks.water || housing.getWorld().getBlockMetadata(x, y, z) != 0) && housing.hasLiquid(new FluidStack(FluidRegistry.getFluid("water"), 1000))) {
                    housing.removeLiquid(new FluidStack(FluidRegistry.getFluid("water"), 1000));
                    housing.getWorld().setBlock(x, y, z, Blocks.water);
                    return true;
                }
            } else {
                for(Block res : Forestry.farmStructureBlocks) {
                    if(housing.hasResources(new ItemStack[]{new ItemStack(res)})) {
                        housing.removeResources(new ItemStack[]{new ItemStack(res)});
                        housing.getWorld().setBlock(x, y, z, res);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean manageCrops(int x, int y, int z){
        if(housing.getWorld().getBlock(x, y, z).isReplaceable(housing.getWorld(), x, y, z)) {
            return housing.getWorld().getBlockMetadata(x, y - 1, z) == 0 && housing.plantGermling(farmable, housing.getWorld(), x, y, z);
        }
        return false;
    }

    private boolean shouldTurnIntoWater(int x, int y, int z){
        y--;
        for(int i = 2; i < 6; i++) {
            ForgeDirection d = ForgeDirection.getOrientation(i);
            if(!Forestry.farmStructureBlocks.contains(housing.getWorld().getBlock(x + d.offsetX, y + d.offsetY, z + d.offsetZ)) && !(housing.getWorld().getTileEntity(x + d.offsetX, y + d.offsetY, z + d.offsetZ) instanceof IFarmComponent)) return false;
        }
        return true;
    }

    @Override
    public Collection<ICrop> harvest(int x, int y, int z, ForgeDirection d, int extent){
        List<ICrop> crops = new ArrayList<ICrop>();
        for(int i = 0; i < extent; i++) {
            ICrop crop = farmable.getCropAt(housing.getWorld(), x + d.offsetX * i, y + d.offsetY * i + 1, z + d.offsetZ * i);
            if(crop != null) crops.add(crop);
        }
        return crops;
    }

    @Override
    public IIcon getIcon(){
        if(icon == null) {
            ItemStack stack = new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.SQUID_PLANT_DAMAGE);
            icon = stack.getIconIndex();
        }
        return icon;
    }

    @Override
    public ResourceLocation getSpriteSheet(){
        return TextureMap.locationItemsTexture;
    }

    @Override
    public String getName(){
        return new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.SQUID_PLANT_DAMAGE).getDisplayName();
    }

    @Override
    public IFarmLogic setManual(boolean manual){
        return this;
    }

}
