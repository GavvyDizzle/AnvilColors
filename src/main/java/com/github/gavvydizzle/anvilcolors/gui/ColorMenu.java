package com.github.gavvydizzle.anvilcolors.gui;

import com.github.gavvydizzle.anvilcolors.AnvilColors;
import com.github.mittenmc.serverutils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ColorMenu implements Listener {

    private final AnvilColors instance;
    private final ArrayList<InventoryItem> inventoryItems;
    private final HashMap<Integer, AnvilColor> colorsMap;
    private final HashSet<UUID> playersInInventory;
    private final HashSet<UUID> playersInAnvil;

    private String inventoryName;
    private int inventorySize;
    private ItemStack filler;

    public ColorMenu(AnvilColors instance) {
        this.instance = instance;
        inventoryItems = new ArrayList<>();
        colorsMap = new HashMap<>();
        playersInInventory = new HashSet<>();
        playersInAnvil = new HashSet<>();

        reload();
        startAnvilTask();
    }

    public void reload() {
        FileConfiguration config = instance.getConfig();
        config.options().copyDefaults(true);
        config.addDefault("menu.name", "Anvil Colors");
        config.addDefault("menu.rows", 4);
        config.addDefault("menu.filler", "gray");
        config.addDefault("items", new HashMap<>());
        config.addDefault("colors", new HashMap<>());
        instance.saveConfig();

        inventoryName = Colors.conv(config.getString("menu.name"));
        inventorySize = Numbers.constrain(config.getInt("menu.rows"), 1, 6) * 9;
        filler = ColoredItems.getGlassByName(config.getString("menu.filler"));

        inventoryItems.clear();
        if (config.getConfigurationSection("items") != null) {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("items")).getKeys(false)) {
                String path = "items." + key;

                int slot = config.getInt(path + ".slot");
                if (!Numbers.isWithinRange(slot, 0, inventorySize-1)) {
                    instance.getLogger().warning("Menu item " + path + " has an invalid slot. It will not show up in the inventory");
                    continue;
                }

                Material material;
                try {
                    material = Material.getMaterial(Objects.requireNonNull(config.getString(path + ".material")));
                } catch (Exception e) {
                    instance.getLogger().warning("Menu item " + path + " has an invalid material. It will not show up in the inventory");
                    continue;
                }

                assert material != null;
                ItemStack itemStack = new ItemStack(material);
                ItemMeta meta = itemStack.getItemMeta();
                assert meta != null;
                meta.setDisplayName(Colors.conv(config.getString(path + ".name")));
                meta.setLore(Colors.conv(config.getStringList(path + ".lore")));
                itemStack.setItemMeta(meta);
                inventoryItems.add(new InventoryItem(itemStack, slot));
            }
        }

        colorsMap.clear();
        if (config.getConfigurationSection("colors") != null) {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("colors")).getKeys(false)) {
                String path = "colors." + key;

                int slot = config.getInt(path + ".slot");
                if (!Numbers.isWithinRange(slot, 0, inventorySize-1)) {
                    instance.getLogger().warning("Color " + path + " has an invalid slot. It will not be loaded");
                    continue;
                }

                String code = config.getString(path + ".code");
                if (code == null || code.length() < 2 || !code.contains("&")) {
                    instance.getLogger().warning("Color " + path + " has an invalid code field. It will not be loaded");
                    continue;
                }

                String permission = config.getString(path + ".permission");
                if (permission == null || permission.trim().isEmpty()) {
                    instance.getLogger().warning("Color " + path + " has an invalid permission (you must define one). It will not be loaded");
                    continue;
                }

                try {
                    int customModelData = config.getInt(path + ".item.customModelID");

                    ItemStack lockedItem = new ItemStack(ConfigUtils.getMaterial(config.getString(path + ".item.lockedMaterial"), Material.GRAY_DYE));
                    ItemMeta meta = lockedItem.getItemMeta();
                    assert meta != null;
                    meta.setDisplayName(Colors.conv(config.getString(path + ".item.lockedName")));
                    meta.setLore(Colors.conv(config.getStringList(path + ".item.lore")));
                    if (customModelData > 0) meta.setCustomModelData(customModelData);
                    lockedItem.setItemMeta(meta);

                    ItemStack unlockedItem = lockedItem.clone();
                    unlockedItem.setType(ConfigUtils.getMaterial(config.getString(path + ".item.material"), Material.PAPER));
                    meta = unlockedItem.getItemMeta();
                    assert meta != null;
                    meta.setDisplayName(Colors.conv(config.getString(path + ".item.name")));
                    unlockedItem.setItemMeta(meta);

                    colorsMap.put(slot, new AnvilColor(code, permission, unlockedItem, lockedItem));
                }
                catch (Exception e) {
                    instance.getLogger().warning("Color " + path + " failed to load its item. Fix it in the config. It will not be loaded");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Attempts to recolor the name of the result item in an anvil every second
     */
    private void startAnvilTask() {
        new RepeatingTask(instance, 20, 10) {
            @Override
            public void run() {
                for (UUID uuid : playersInAnvil) {
                    handleAnvilResultSlotUpdate(Bukkit.getPlayer(uuid));
                }
            }
        };
    }

    private void handleAnvilResultSlotUpdate(@Nullable Player player) {
        if (player == null || player.getOpenInventory().getTopInventory().getType() != InventoryType.ANVIL) return;

        AnvilInventory anvilInventory = (AnvilInventory) player.getOpenInventory().getTopInventory();
        String newName = anvilInventory.getRenameText();
        if (newName == null || newName.length() < 2 || !newName.contains("&")) return;

        ItemStack resultItem = anvilInventory.getItem(2);
        if (resultItem == null || resultItem.getType() == Material.AIR) return;

        ItemMeta meta = resultItem.getItemMeta();
        if (meta == null) return;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', getAnvilColoredString(player, newName)));
        resultItem.setItemMeta(meta);
    }

    private String getAnvilColoredString(Player player, String string) {
        for (AnvilColor anvilColor : colorsMap.values()) {
            string = anvilColor.handleInput(player, string);
        }
        return string;
    }

    public void openInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(player, inventorySize, inventoryName);
        for (int i = 0; i < inventorySize; i++) {
            inventory.setItem(i, filler);
        }
        for (InventoryItem inventoryItem : inventoryItems) {
            inventory.setItem(inventoryItem.getSlot(), inventoryItem.getItemStack());
        }
        for (Map.Entry<Integer, AnvilColor> entry : colorsMap.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getMenuItem(player));
        }

        playersInInventory.add(player.getUniqueId());
        player.openInventory(inventory);
    }

    @EventHandler(ignoreCancelled = true)
    private void onInventoryClick(InventoryClickEvent e) {
        if (playersInInventory.contains(e.getWhoClicked().getUniqueId())) e.setCancelled(true);

        // Handle click to anvil slot
        if (playersInAnvil.contains(e.getWhoClicked().getUniqueId())) handleAnvilResultSlotUpdate((Player) e.getWhoClicked());
    }

    @EventHandler
    private void onAnvilOpen(InventoryOpenEvent e) {
        if (e.getInventory().getType() == InventoryType.ANVIL) playersInAnvil.add(e.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent e) {
        playersInInventory.remove(e.getPlayer().getUniqueId());
        playersInAnvil.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        playersInInventory.remove(e.getPlayer().getUniqueId());
        playersInAnvil.remove(e.getPlayer().getUniqueId());
    }


}
