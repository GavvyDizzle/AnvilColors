package com.github.gavvydizzle.anvilcolors.commands;

import com.github.gavvydizzle.anvilcolors.gui.ColorMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerCommands implements TabExecutor {

    private final ColorMenu colorMenu;

    public PlayerCommands(ColorMenu colorMenu) {
        this.colorMenu = colorMenu;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        colorMenu.openInventory((Player) sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return new ArrayList<>();
    }
}
