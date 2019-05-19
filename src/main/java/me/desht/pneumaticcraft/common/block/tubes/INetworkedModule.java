package me.desht.pneumaticcraft.common.block.tubes;

/**
 * Represents a module which needs to be aware of other modules of its type which are connected to it via a tube.
 */
interface INetworkedModule {
    int getColorChannel();

    void setColorChannel(int channel);
}
