package me.desht.pneumaticcraft.common.semiblock;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.util.Objects;

public interface IProvidingInventoryListener {
    void notify(TileEntityAndFace teAndFace);

    class TileEntityAndFace {
        private final TileEntity te;
        private final EnumFacing face;

        public TileEntityAndFace(TileEntity te, EnumFacing face) {
            this.te = te;
            this.face = face;
        }

        public TileEntity getTileEntity() {
            return te;
        }

        public EnumFacing getFace() {
            return face;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TileEntityAndFace)) return false;
            TileEntityAndFace tileEntityAndFace = (TileEntityAndFace) o;
            return te.equals(tileEntityAndFace.te) &&
                    face == tileEntityAndFace.face;
        }

        @Override
        public int hashCode() {
            return Objects.hash(te, face);
        }
    }
}

