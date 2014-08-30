package pneumaticCraft.common.inventory;

public interface IPhantomSlot{
    /*
     * Phantom Slots don't "use" items, they are used for filters and various
     * other logic slots.
     */
    boolean canAdjust();
}
