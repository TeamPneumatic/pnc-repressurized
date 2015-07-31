package pneumaticCraft.common.inventory;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import pneumaticCraft.common.recipes.AmadronOffer;
import pneumaticCraft.common.recipes.PneumaticRecipeRegistry;

public class ContainerAmadron extends ContainerPneumaticBase{
    public static final int ROWS = 4;

    private final InventoryBasic inv = new InventoryBasic("amadron", true, ROWS * 4);

    public ContainerAmadron(){
        super(null);
        for(int y = 0; y < ROWS; y++) {
            for(int x = 0; x < 2; x++) {
                addSlotToContainer(new SlotUntouchable(inv, y * 4 + x * 2, x * 73 + 10, y * 40 + 50));
                addSlotToContainer(new SlotUntouchable(inv, y * 4 + x * 2 + 1, x * 73 + 60, y * 40 + 50));
            }
        }
        int page = 0;
        List<AmadronOffer> offers = PneumaticRecipeRegistry.getInstance().amadronOffers;
        for(int i = page * 2 * ROWS; i < offers.size() && i * 2 - page * 2 * ROWS + 1 < inv.getSizeInventory(); i++) {
            AmadronOffer offer = offers.get(i);
            if(offer.getInput() instanceof ItemStack) inv.setInventorySlotContents(i * 2 - page * 2 * ROWS, (ItemStack)offer.getInput());
            if(offer.getOutput() instanceof ItemStack) inv.setInventorySlotContents(i * 2 - page * 2 * ROWS + 1, (ItemStack)offer.getOutput());
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player){
        return true;
    }

    public void clearStacks(){
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            inv.setInventorySlotContents(i, null);
        }
    }

    public void setStack(ItemStack stack, int index){
        inv.setInventorySlotContents(index, stack);
    }
}
