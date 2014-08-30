package k4unl.minecraft.Hydraulicraft.api;

import net.minecraft.item.ItemStack;

public interface IPressurizableItem {
	public float getPressure(ItemStack itemStack);
	public void setPressure(ItemStack itemStack, float newPressure);
	
	
	public PressureTier getMaxPressure();
	public int getMaxStorage();
	
	public void setStorage(ItemStack itemStack, int newStored);
	public int getStorage(ItemStack itemStack);
	
}
