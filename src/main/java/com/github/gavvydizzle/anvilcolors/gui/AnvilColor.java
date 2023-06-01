package com.github.gavvydizzle.anvilcolors.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AnvilColor {

    private final String code; // If the player does not have permission, the code will be removed from the final string
    private final String permission;
    private final ItemStack lockedItem, unlockedItem;

    public AnvilColor(String code, String permission, ItemStack unlockedItem, ItemStack lockedItem) {
        this.code = code;
        this.permission = permission;
        this.unlockedItem = unlockedItem;
        this.lockedItem = lockedItem;
    }

    /**
     * @param player The player
     * @param str The string to edit
     * @return The input unchanged if the player has permission or the input with
     * the code removed if the player does not have permission
     */
    public String handleInput(Player player, String str) {
        return hasPermission(player) ? str : str.replace(code, "");
    }

    /**
     * @param player The player
     * @return If the player has permission to use this tag
     */
    public boolean hasPermission(Player player) {
        return player.hasPermission(permission);
    }

    /**
     * Gets the correct menu item for this player depending on their permissions
     * @param player The player
     * @return The locked or unlocked ItemStack depending on permissions
     */
    @NotNull
    public ItemStack getMenuItem(Player player) {
        return hasPermission(player) ? unlockedItem : lockedItem;
    }
}
