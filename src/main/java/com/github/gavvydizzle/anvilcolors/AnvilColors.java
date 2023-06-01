package com.github.gavvydizzle.anvilcolors;

import com.github.gavvydizzle.anvilcolors.commands.AdminCommands;
import com.github.gavvydizzle.anvilcolors.commands.PlayerCommands;
import com.github.gavvydizzle.anvilcolors.gui.ColorMenu;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class AnvilColors extends JavaPlugin {

    private static AnvilColors instance;

    @Override
    public void onEnable() {
        instance = this;
        ColorMenu colorMenu = new ColorMenu(instance);

        getServer().getPluginManager().registerEvents(colorMenu, this);

        Objects.requireNonNull(getServer().getPluginCommand("anvilcolor")).setExecutor(new PlayerCommands(colorMenu));
        Objects.requireNonNull(getServer().getPluginCommand("anvilcoloradmin")).setExecutor(new AdminCommands(this, colorMenu));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
