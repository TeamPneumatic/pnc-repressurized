package pneumaticCraft.common.semiblock;

import pneumaticCraft.lib.ModIds;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.common.Optional;

public class SemiBlockRequesterAE extends SemiBlockRequester implements IMEInventoryHandler<IAEItemStack>{
    //IMEInventoryHandler

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public IAEItemStack extractItems(IAEItemStack arg0, Actionable arg1, BaseActionSource arg2){
        return null;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> arg0){
        for(IAEItemStack stack : getProvidingItems()) {
            stack.setCountRequestable(stack.getStackSize());
            arg0.addRequestable(stack);
        }
        return arg0;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public StorageChannel getChannel(){
        return StorageChannel.ITEMS;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public IAEItemStack injectItems(IAEItemStack arg0, Actionable arg1, BaseActionSource arg2){
        return arg0;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public boolean canAccept(IAEItemStack arg0){
        return false;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public AccessRestriction getAccess(){
        return AccessRestriction.READ;
    }

    @Override
    public int getSlot(){
        return 0;
    }

    @Override
    @Optional.Method(modid = ModIds.AE2)
    public boolean isPrioritized(IAEItemStack arg0){
        return false;
    }

    @Override
    public boolean validForPass(int arg0){
        return true;
    }
}
