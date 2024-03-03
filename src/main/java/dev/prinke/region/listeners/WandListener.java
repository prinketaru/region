package dev.prinke.region.listeners;

import dev.prinke.region.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;

public class WandListener implements Listener {

    Region plugin;
    HashMap<Player, Location> selectedFirstPoints = new HashMap<>();
    HashMap<Player, Location> selectedSecondPoints = new HashMap<>();

    public WandListener(Region plugin) {
        this.plugin = plugin;
        this.selectedFirstPoints = plugin.selectedFirstPoints;
        this.selectedSecondPoints = plugin.selectedSecondPoints;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        // check if player is holding wand
        if (e.getItem() != null && e.getItem().getItemMeta().getDisplayName().equals("§d§lRegion Wand")) {
            // check if player has permission and is left clicking
            if (e.getPlayer().hasPermission("region.wand")) {
                // check if player is left or right clicking
                if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    // set first point
                    selectedFirstPoints.put(e.getPlayer(), e.getClickedBlock().getLocation());
                    e.getPlayer().sendMessage("§dFirst region point selected!");
                } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    // set second point
                    selectedSecondPoints.put(e.getPlayer(), e.getClickedBlock().getLocation());
                    e.getPlayer().sendMessage("§dSecond region point selected!");
                }
            }
            // cancel interaction
            e.setCancelled(true);
        }
    }

}
