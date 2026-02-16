package com.saltclient.installer;

public enum InstallType {
    MOD("Mod", ".jar"),
    TEXTURE_PACK("Texture Pack", ".zip"),
    WORLD("World", ".zip");

    public final String label;
    public final String preferredExt;

    InstallType(String label, String preferredExt) {
        this.label = label;
        this.preferredExt = preferredExt;
    }

    @Override
    public String toString() {
        return label;
    }
}
