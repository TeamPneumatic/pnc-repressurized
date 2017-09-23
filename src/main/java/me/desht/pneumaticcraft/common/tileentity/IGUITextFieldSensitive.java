package me.desht.pneumaticcraft.common.tileentity;

public interface IGUITextFieldSensitive {

    void setText(int textFieldID, String text);

    String getText(int textFieldID);
}
