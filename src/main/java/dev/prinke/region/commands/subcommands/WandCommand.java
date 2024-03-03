package dev.prinke.region.commands.subcommands;

import dev.prinke.region.commands.SubCommand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WandCommand extends SubCommand {
    @Override
    public String getName() {
        return "wand";
    }

    @Override
    public String getDescription() {
        return "Gives the player the region wand";
    }

    @Override
    public String getSyntax() {
        return "/region wand";
    }

    @Override
    public void perform(Player p, String[] args) {
        // create a stick with the name "§d§lRegion Wand"
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta wandMeta = wand.getItemMeta();
        wandMeta.setDisplayName("§d§lRegion Wand");
        wand.setItemMeta(wandMeta);
        p.getInventory().addItem(wand);
        p.sendMessage("§aRegion wand given!");
    }
}
