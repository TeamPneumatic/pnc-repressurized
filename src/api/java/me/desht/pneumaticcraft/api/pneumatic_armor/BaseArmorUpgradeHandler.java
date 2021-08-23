package me.desht.pneumaticcraft.api.pneumatic_armor;

import org.apache.commons.lang3.Validate;

public abstract class BaseArmorUpgradeHandler<T extends IArmorExtensionData> implements IArmorUpgradeHandler<T> {
    int idx = -1;

    @Override
    public int getIndex() {
        return idx;
    }

    @Override
    public void setIndex(int index) {
        Validate.isTrue(index >= 0, "negative index not permitted!");
        if (idx != -1) throw new IllegalStateException("attempt to overwrite existing index");
        this.idx = index;
    }
}
