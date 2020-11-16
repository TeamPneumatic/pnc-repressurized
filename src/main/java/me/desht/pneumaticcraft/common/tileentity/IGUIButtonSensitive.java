package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.entity.player.PlayerEntity;

/**
 * Implement on a Container/Tile Entity/EntitySemiblock to allow it to receive messages from the client when a GUI button is
 * clicked.
 */
@FunctionalInterface
public interface IGUIButtonSensitive {
//    Pattern RS_MATCHER = Pattern.compile("^redstone:(\\d+)$");

    void handleGUIButtonPress(String tag, boolean shiftHeld, PlayerEntity player);

//    static boolean parseRedstoneMode(String tag, Consumer<Integer> ifMatched) {
//        Matcher m = RS_MATCHER.matcher(tag);
//        if (m.matches() && m.groupCount() == 1) {
//            ifMatched.accept(Integer.parseInt(m.group(1)));
//            return true;
//        }
//        return false;
//    }

}