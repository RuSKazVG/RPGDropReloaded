package me.ruskaz.rpgdrop;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;

import static io.papermc.paper.text.PaperComponents.plainSerializer;

public class Events implements Listener {

    private final Plugin plugin = Main.getPlugin(Main.class);

    @EventHandler
    public void addTags (EntityDeathEvent e) {
        try {
            if (e.getEntity().getKiller() == null) return;
            Player killer = e.getEntity().getKiller();
            if (killer.hasPermission("rpgdrop.protection")) {
                List<ItemStack> dropped = e.getDrops();
                for (int i = 0; i < dropped.size(); i++) {
                    ItemOperations.addLore(dropped.get(i), killer);
                }
            }
        } catch (NullPointerException ignored) {}
    }

    @EventHandler
    public void preventPickingUp (EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            ItemStack item = e.getItem().getItemStack();
            if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                ItemMeta meta = item.getItemMeta();
                List<Component> lore = meta.lore();
                for (int i = 0; lore.size() > i; i++) {
                    Component comp = lore.get(i);
                    String line = plainSerializer().serialize(comp);
                    if (line.contains("affected")) {
                        String[] splitter = line.split(":");
                        if (!splitter[1].equals(String.valueOf(player.getUniqueId()))) {
                            e.setCancelled(true);
                            return;
                        }
                        else {
                            lore = ItemOperations.clearLore(lore);
                            meta.lore(lore);
                            item.setItemMeta(meta);
                            return;
                        }
                    }
                }
            }
        } else {
            if (!plugin.getConfig().getBoolean("mobsCanPickUp")) return;
            ItemStack item = e.getItem().getItemStack();
            if (item.getItemMeta().hasLore()) {
                ItemMeta meta = item.getItemMeta();
                List<Component> lore = meta.lore();
                for (Component comp : lore) {
                    String line = plainSerializer().serialize(comp);
                    if (line.contains("affected")) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void preventInHopperlikeBlocks (InventoryPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = meta.lore();
            if (plugin.getConfig().getBoolean("preventFromHoppers")) {
                lore = ItemOperations.clearLore(lore);
                meta.lore(lore);
                item.setItemMeta(meta);
            } else e.setCancelled(true);
        }
    }

    @EventHandler
    public void clearTagsOnLeave (PlayerQuitEvent e) {
        if (!plugin.getConfig().getBoolean("clearProtectionOnLeave")) return;
        for (World world : Bukkit.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
                    ItemStack item = ((Item) entity).getItemStack();
                    if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                        ItemMeta meta = item.getItemMeta();
                        List<Component> lore = meta.lore();
                        lore = ItemOperations.clearLore(lore, e.getPlayer());
                        meta.lore(lore);
                        item.setItemMeta(meta);
                    }
                }
            }
        }
    }

    @EventHandler
    public void clearTagsOnDeath (PlayerDeathEvent e) {
        if (!plugin.getConfig().getBoolean("clearProtectionOnLeave")) return;
        Player p = e.getEntity();
        for (World world : Bukkit.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getType().equals(EntityType.DROPPED_ITEM)) {
                    ItemStack item = ((Item) entity).getItemStack();
                    if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                        ItemMeta meta = item.getItemMeta();
                        List<Component> lore = meta.lore();
                        lore = ItemOperations.clearLore(lore, p);
                        meta.lore(lore);
                        item.setItemMeta(meta);
                    }
                }
            }
        }
    }
}
