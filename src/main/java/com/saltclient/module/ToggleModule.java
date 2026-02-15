package com.saltclient.module;

/**
 * A simple toggleable module with no direct logic.
 *
 * Many Salt modules are implemented via mixins / global controllers that check
 * {@link com.saltclient.SaltClient#MODULES} for enabled states.
 */
public final class ToggleModule extends Module {
    public ToggleModule(String id, String name, String description, ModuleCategory category) {
        super(id, name, description, category, true);
    }
}

