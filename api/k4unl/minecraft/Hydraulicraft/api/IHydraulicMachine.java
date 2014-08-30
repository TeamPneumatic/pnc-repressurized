package k4unl.minecraft.Hydraulicraft.api;

import net.minecraftforge.common.util.ForgeDirection;

public interface IHydraulicMachine {
	public IBaseClass getHandler();
	
	/**
	 * Called whenever the fluid level has changed
	 */
	public void onFluidLevelChanged(int old);
	
	/**
	 * Function that gets called to check if a network can connect here.
	 * @param side
	 * @return
	 */
	public boolean canConnectTo(ForgeDirection side);
}
