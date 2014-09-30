package pneumaticCraft.common.thirdparty.forestry;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import pneumaticCraft.common.item.Itemss;
import forestry.api.farming.Farmables;
import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmLogic;
import forestry.api.farming.IFarmable;

public abstract class FarmLogicPlasticNormal extends FarmLogicWrapper{

    private IIcon icon;

    public FarmLogicPlasticNormal(IFarmHousing housing) throws Throwable{
        super(housing);
    }

    protected abstract Block getBlock();

    @Override
    protected IFarmLogic getFarmLogic(IFarmHousing housing) throws Throwable{
        ArrayList<IFarmable> origList = (ArrayList<IFarmable>)Farmables.farmables.get("farmVegetables");
        ArrayList<IFarmable> backup = new ArrayList<IFarmable>(origList);
        origList.clear();
        origList.add(new FarmablePlastic(getBlock()));
        IFarmLogic logic = getLogicClass("FarmLogicVegetable").getConstructor(IFarmHousing.class).newInstance(housing);
        origList.clear();
        origList.addAll(backup);
        return logic;
    }

    @Override
    public IIcon getIcon(){
        if(icon == null) {
            ItemStack stack = new ItemStack(Itemss.plasticPlant, 1, FarmablePlastic.getItemMetaForBlock(getBlock()));
            icon = stack.getIconIndex();
        }
        return icon;
    }

    @Override
    public String getName(){
        return new ItemStack(Itemss.plasticPlant, 1, FarmablePlastic.getItemMetaForBlock(getBlock())).getDisplayName();
    }
}
