package me.desht.pneumaticcraft.common.variables;

import me.desht.pneumaticcraft.api.misc.IGlobalVariableHelper;
import me.desht.pneumaticcraft.api.misc.IVariableProvider;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public enum GlobalVariableHelper implements IGlobalVariableHelper {
    INSTANCE;

    public static GlobalVariableHelper getInstance() {
        return INSTANCE;
    }

    @Override
    public BlockPos getPos(@Nullable UUID id, String varName, BlockPos def) {
        GlobalVariableManager gvm = GlobalVariableManager.getInstance();
        if (varName.startsWith("%")) {
            return gvm.hasPos(varName.substring(1)) ? gvm.getPos(varName.substring(1)) : def;
        }
        if (id == null) {
            Log.warning("querying player-global var {} with no player context?", varName);
            return def;
        }
        if (varName.startsWith("#")) {
            varName = varName.substring(1);
        }
        return gvm.hasPos(id, varName) ? gvm.getPos(id, varName) : def;
    }

    @Override
    public BlockPos getPos(@Nullable UUID id, String varName) {
        return getPos(id, varName, null);
    }

    @Override
    public ItemStack getStack(@Nullable UUID id, String varName, ItemStack def) {
        GlobalVariableManager gvm = GlobalVariableManager.getInstance();
        if (varName.startsWith("%")) {
            return gvm.hasStack(varName.substring(1)) ? gvm.getStack(varName.substring(1)) : def;
        }
        if (id == null) {
            Log.warning("querying player-global var {} with no player context?", varName);
            return def;
        }
        if (varName.startsWith("#")) {
            varName = varName.substring(1);
        }
        return gvm.hasStack(id, varName) ? gvm.getStack(id, varName) : def;
    }

    @Override
    public ItemStack getStack(@Nullable UUID id, String varName) {
        return getStack(id, varName, ItemStack.EMPTY);
    }

    @Override
    public void setPos(UUID id, String varName, BlockPos pos) {
        GlobalVariableManager gvm = GlobalVariableManager.getInstance();
        if (varName.startsWith("#") && id != null) {
            gvm.setPos(id, varName.substring(1), pos);
        } else if (varName.startsWith("%")) {
            gvm.setPos(varName.substring(1), pos);
        } else if (id != null) {
            gvm.setPos(id, varName, pos);
        }
    }

    @Override
    public void setStack(UUID id, String varName, ItemStack stack) {
        GlobalVariableManager gvm = GlobalVariableManager.getInstance();
        if (varName.startsWith("#") && id != null) {
            gvm.setStack(id, varName.substring(1), stack);
        } else if (varName.startsWith("%")) {
            gvm.setStack(varName.substring(1), stack);
        } else if (id != null) {
            gvm.setStack(id, varName, stack);
        }
    }

    @Override
    public boolean getBool(UUID id, String varName) {
        return getInt(id, varName) != 0;
    }

    @Override
    public int getInt(UUID id, String varName) {
        return getPos(id, varName, BlockPos.ZERO).getX();
    }

    @Override
    public String getPrefixedVar(String varName, boolean playerGlobal) {
        return varName.isEmpty() ? "" : getVarPrefix(playerGlobal) + varName;
    }

    @Override
    public String getVarPrefix(boolean playerGlobal) {
        return playerGlobal ? "#" : "%";
    }

    @Override
    public String stripVarPrefix(String varName) {
        return hasPrefix(varName) ? varName.substring(1) : varName;
    }

    @Override
    public boolean hasPrefix(String varName) {
        return varName.length() > 1 && (varName.startsWith("#") || varName.startsWith("%"));
    }

    @Override
    public Set<String> getRelevantVariables(String string, UUID playerId) {
        TextVariableParser parser = new TextVariableParser(string, playerId);
        parser.parse();
        return parser.getRelevantVariables();
    }

    public IVariableProvider getVariableProvider() {
        return VariableProviderWrapper.INSTANCE;
    }

    private enum VariableProviderWrapper implements IVariableProvider {
        INSTANCE;

        @Override
        public Optional<BlockPos> getCoordinate(UUID id, String varName) {
            return Optional.ofNullable(GlobalVariableHelper.INSTANCE.getPos(id, varName));
        }

        @Nonnull
        @Override
        public ItemStack getStack(UUID id, String varName) {
            return GlobalVariableHelper.INSTANCE.getStack(id, varName);
        }
    }
}
