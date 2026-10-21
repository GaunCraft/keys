package com.ornek.anahtar.listeners;

import com.ornek.anahtar.pack.ResourcePackManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Oyuncu sunucuya her girdiginde, plugin tarafindan sunulan ozel
 * anahtar texture paketini otomatik olarak oyuncunun istemcisine gonderir.
 * Boylece oyuncularin ayrica elle bir resourcepack kurmasina gerek kalmaz.
 */
public class PlayerJoinListener implements Listener {

    private final JavaPlugin plugin;
    private final ResourcePackManager packManager;

    public PlayerJoinListener(JavaPlugin plugin, ResourcePackManager packManager) {
        this.plugin = plugin;
        this.packManager = packManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (packManager.getUrl() == null || packManager.getHash() == null) {
            plugin.getLogger().warning("Resourcepack hazir degil, " + player.getName() + " icin gonderilemedi.");
            return;
        }

        // Diger pluginlerin paketleriyle cakismamasi icin biraz gecikmeyle
        // (varsayilan 80 tick = 4 sn) ve addResourcePack ile gonderilir.
        long delay = plugin.getConfig().getLong("resourcepack.send-delay-ticks", 80L);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> packManager.gonder(player), delay);
    }
}
