package com.github.gavvydizzle.anvilcolors.commands;

import com.github.gavvydizzle.anvilcolors.AnvilColors;
import com.github.gavvydizzle.anvilcolors.gui.ColorMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminCommands implements TabExecutor {

    private final ArrayList<String> args2 = new ArrayList<>(Collections.singletonList("reload"));
    private final AnvilColors instance;
    private final ColorMenu colorMenu;

    public AdminCommands(AnvilColors instance, ColorMenu colorMenu) {
        this.instance = instance;
        this.colorMenu = colorMenu;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must supply a sub-command");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            try {
                instance.reloadConfig();
                colorMenu.reload();
                sender.sendMessage(ChatColor.GREEN + "[AnvilColors] Reloaded");
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "[AnvilColors] Failed to reload. Check the console for errors");
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        ArrayList<String> list = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], args2, list);
        }
        return list;
    }
}