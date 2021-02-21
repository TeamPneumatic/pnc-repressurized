package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.IFormattableTextComponent;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

@FunctionalInterface
public interface IRedstoneControl<T extends TileEntity & IRedstoneControl<T>> {
    /**
     * Get the redstone controller object for this TE
     *
     * @return the redstone controller
     */
    RedstoneController<T> getRedstoneController();

    default IFormattableTextComponent getRedstoneTabTitle() {
        return ((IRedstoneControl<?>) this).getRedstoneController().isEmitter() ?
                xlate("pneumaticcraft.gui.tab.redstoneBehaviour.emitRedstoneWhen") :
                xlate("pneumaticcraft.gui.tab.redstoneBehaviour.enableOn");
    }

    /**
     * Get the current redstone level for this TE.
     *
     * @return the current redstone level for the TE
     */
    default int getCurrentRedstonePower() {
        return getRedstoneController().getCurrentRedstonePower();
    }

    default int getRedstoneMode() {
        return getRedstoneController().getCurrentMode();
    }

    default void onRedstoneModeChanged(int newModeIdx) { }
}
