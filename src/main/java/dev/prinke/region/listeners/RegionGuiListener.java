package dev.prinke.region.listeners;

import dev.prinke.region.Region;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RegionGuiListener implements Listener {

    Region plugin;
    private final HashMap<UUID, CompletableFuture<String>> awaitingInput = new HashMap<>();

    public RegionGuiListener(Region plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // if the menu is the regions menu
        if (e.getView().getTitle().equals("Regions")) {
            e.setCancelled(true);

            for (Document region : plugin.getRegions()) {
                if (e.getCurrentItem().getItemMeta().getDisplayName().equals(region.getString("name"))) {
                    e.getWhoClicked().closeInventory();
                    openRegionMenu(region, (Player) e.getWhoClicked(), plugin);
                }
            }
        }

        // if the menu is for a specific region
        if (e.getView().getTitle().contains("Region: ")) {
            e.setCancelled(true);

            for (Document region : plugin.getRegions()) {
                if (e.getView().getTitle().contains(region.getString("name"))) {
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aRename")) {
                        e.getWhoClicked().closeInventory();

                        // rename region
                        Player player = (Player) e.getWhoClicked();
                        promptInput(player, "§aEnter the new name for the region: ")
                                .thenAccept(newName -> {

                                    // get locations
                                    Location firstPoint = new Location(player.getWorld(),
                                            region.get("firstPoint", Document.class).get("x", Number.class).doubleValue(),
                                            region.get("firstPoint", Document.class).get("y", Number.class).doubleValue(),
                                            region.get("firstPoint", Document.class).get("z", Number.class).doubleValue());
                                    Location secondPoint = new Location(player.getWorld(),
                                            region.get("secondPoint", Document.class).get("x", Number.class).doubleValue(),
                                            region.get("secondPoint", Document.class).get("y", Number.class).doubleValue(),
                                            region.get("secondPoint", Document.class).get("z", Number.class).doubleValue());

                                    // get the whitelist
                                    ArrayList<String> whitelistStrings = (ArrayList<String>) region.get("whitelist");
                                    ArrayList<UUID> whitelistUUIDs = new ArrayList<>();
                                    for (String uuidStr : whitelistStrings) {
                                        whitelistUUIDs.add(UUID.fromString(uuidStr));
                                    }

                                    plugin.modifyRegion(region.getString("name"), newName, player.getUniqueId(), firstPoint, secondPoint, whitelistUUIDs);

                                    player.sendMessage("§aRegion renamed to " + newName + "!");
                                });

                        return;
                    }
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aAdd player")) {
                        e.getWhoClicked().closeInventory();

                        // add player to whitelist
                        Player player = (Player) e.getWhoClicked();
                        promptInput(player, "§aEnter the name of the player to add to the whitelist: ")
                                .thenAccept(playerName -> {
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        plugin.getServer().dispatchCommand(player, "region add " + region.getString("name") + " " + playerName);
                                    });
                                });

                        return;
                    }
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§cRemove player")) {
                        e.getWhoClicked().closeInventory();

                        // remove player from whitelist
                        Player player = (Player) e.getWhoClicked();
                        promptInput(player, "§aEnter the name of the player to remove from the whitelist: ")
                                .thenAccept(playerName -> {
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        plugin.getServer().dispatchCommand(player, "region remove " + region.getString("name") + " " + playerName);
                                    });
                                });

                        return;
                    }
                    if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aRedefine location")) {
                        e.getWhoClicked().closeInventory();

                        // redefine location
                        Player player = (Player) e.getWhoClicked();
                        if (plugin.selectedFirstPoints.containsKey(player) && plugin.selectedSecondPoints.containsKey(player)) {
                            Location firstPoint = plugin.selectedFirstPoints.get(player);
                            Location secondPoint = plugin.selectedSecondPoints.get(player);

                            // get the whitelist
                            ArrayList<String> whitelistStrings = (ArrayList<String>) region.get("whitelist");
                            ArrayList<UUID> whitelistUUIDs = new ArrayList<>();
                            for (String uuidStr : whitelistStrings) {
                                whitelistUUIDs.add(UUID.fromString(uuidStr));
                            }

                            plugin.modifyRegion(region.getString("name"), region.getString("name"), player.getUniqueId(), firstPoint, secondPoint, whitelistUUIDs);

                            player.sendMessage("§aRegion " + region.getString("name") + " has been redefined to your selected area!");
                        } else {
                            player.sendMessage("§cYou need to select the first and second points of the new region first!");
                        }

                        return;
                    }
                }
            }
        }
    }

    public static void openRegionMenu(Document region, Player player, Region plugin) {
        Inventory regionInv = plugin.getServer().createInventory(null, 27, "Region: " + region.getString("name"));

        // rename item
        ItemStack renameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta renameMeta = renameItem.getItemMeta();
        renameMeta.setDisplayName("§aRename");
        renameItem.setItemMeta(renameMeta);

        regionInv.setItem(10, renameItem);

        // whitelist add item
        ItemStack whitelistAddItem = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta whitelistAddMeta = whitelistAddItem.getItemMeta();
        whitelistAddMeta.setDisplayName("§aAdd player");
        whitelistAddItem.setItemMeta(whitelistAddMeta);

        regionInv.setItem(12, whitelistAddItem);

        // whitelist remove item
        ItemStack whitelistRemoveItem = new ItemStack(Material.RED_CONCRETE);
        ItemMeta whitelistRemoveMeta = whitelistRemoveItem.getItemMeta();
        whitelistRemoveMeta.setDisplayName("§cRemove player");
        whitelistRemoveItem.setItemMeta(whitelistRemoveMeta);

        regionInv.setItem(14, whitelistRemoveItem);

        //redefine location item
        ItemStack redefineItem = new ItemStack(Material.STICK);
        ItemMeta redefineMeta = redefineItem.getItemMeta();
        redefineMeta.setDisplayName("§aRedefine location");
        redefineItem.setItemMeta(redefineMeta);

        regionInv.setItem(16, redefineItem);

        player.openInventory(regionInv);
    }

    // get input from chat
    public CompletableFuture<String> promptInput(Player player, String message) {
        CompletableFuture<String> future = new CompletableFuture<>();
        awaitingInput.put(player.getUniqueId(), future);
        player.sendMessage(message);
        return future;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (awaitingInput.containsKey(playerId)) {
            CompletableFuture<String> future = awaitingInput.remove(playerId);
            future.complete(event.getMessage());
            event.setCancelled(true);
        }
    }
}
