package me.desht.pneumaticcraft.common.tubemodules;

import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Set;

public abstract class AbstractNetworkedRedstoneModule extends AbstractTubeModule {
    private int inputLevel = -1; // the cached input level
    private boolean inputChangedThisTick = false;
    private boolean inputUpdateQueued = false;

    AbstractNetworkedRedstoneModule(Direction dir, PressureTubeBlockEntity pressureTube) {
        super(dir, pressureTube);
    }

    @Override
    public void tickServer() {
        super.tickServer();

        if (getInputChannel() != -1 && inputLevel == -1) updateInputLevel(); // first tick

        inputChangedThisTick = false;
        boolean wasInputUpdateQueued = inputUpdateQueued;
        inputUpdateQueued = false;
        if (wasInputUpdateQueued) updateInputLevel();
    }

    /**
     * @return the input channel of this module, or -1 if not inputting
     */
    protected int getInputChannel() { return -1; }

    public int getInputLevel() {
        return inputLevel;
    }

    /**
     * Should only be used on client side
     * Called on client when a sync packet is received, and on server when saved data is loaded
     */
    public void setInputLevel(int inputLevel) {
        this.inputLevel = inputLevel;
    }

    /**
     * @return the current input signal level
     */
    protected int calculateInputLevel() { return 0; }

    /**
     * Checks and updates the input level.
     *
     * @return did the input level change
     */
    public final boolean updateInputLevel() {
        if (getInputChannel() == -1) return false; // not inputting
        if (inputChangedThisTick) {
            inputUpdateQueued = true;
            return false;
        }

        int in = calculateInputLevel();
        if (in != inputLevel) {
            inputLevel = in;
            inputChangedThisTick = true;
            onInputLevelChange(in);
            notifyInputLevelsChanged(getInputChannel());
            return true;
        }
        return false;
    }

    /**
     * Called when this module's input level is changed.
     *
     * @param level the new input level
     */
    protected void onInputLevelChange(int level) { }

    protected final byte[] fetchNetworkInputLevels() {
        final byte[] levels = new byte[16];
        for (AbstractTubeModule module : ModuleNetworkManager.getInstance(getTube().nonNullLevel()).getConnectedModules(this)) {
            if (module instanceof AbstractNetworkedRedstoneModule rsModule) {
                int inChannel = rsModule.getInputChannel();
                if (inChannel != -1) {
                    int level = rsModule.inputLevel;
                    if (level > levels[inChannel]) levels[inChannel] = (byte) level;
                }
            }
        }
        return levels;
    }

    /**
     * Notify all modules in the network about an input level change of {@code channel}.
     * Call this whenever an "inputting" module's input changes.
     *
     * @param channel the changed channel, or -1 to update all channels
     */
    protected final void notifyInputLevelsChanged(int channel) {
        byte[] levels = fetchNetworkInputLevels();

        for (AbstractTubeModule module : ModuleNetworkManager.getInstance(getTube().nonNullLevel()).getConnectedModules(this)) {
            if (module instanceof AbstractNetworkedRedstoneModule rsModule) {
                if (channel == -1 || rsModule.isWatchingChannel(channel)) {
                    rsModule.updateOutput(levels);
                }
            }
        }
    }

    /**
     * @return if this module is "watching" {@code channel}
     * (if {@link #updateOutput(byte[])} should be called when the respective channels updates)
     */
    protected boolean isWatchingChannel(int channel) { return false; }

    /**
     * This gets called by {@link #notifyInputLevelsChanged(int)}
     * when the input levels of the channels that this module is watching changes.
     *
     * @param levels the input levels of all channels
     */
    protected void updateOutput(@Nullable byte[] levels) { }

    /**
     * Called when the network is reformed (e.g. tube place and destroy)
     */
    public static void onNetworkReform(Level level, BlockPos pos) {
        if (level.isClientSide()) return;

        ModuleNetworkManager netManager = ModuleNetworkManager.getInstance(level);
        Set<AbstractTubeModule> modules = netManager.computeConnections(level, pos);
        for (Direction dir : Direction.values()) {
            modules.addAll(netManager.computeConnections(level, pos.relative(dir)));
        }
        modules.forEach(module -> {
            if (module instanceof AbstractNetworkedRedstoneModule rsModule) {
                rsModule.notifyInputLevelsChanged(-1);
            }
        });
    }

    @Override
    public final boolean canConnectTo(AbstractTubeModule other) {
        return other instanceof AbstractNetworkedRedstoneModule;
    }

    @Override
    public void onPlaced() {
        super.onPlaced();
        updateOutput(null);
    }
}
