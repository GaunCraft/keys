package com.ornek.anahtar.listeners;

import com.ornek.anahtar.pack.ResourcePackManager;
import net.kyori.adventure.text.Component;
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
        String url = packManager.getUrl();
        byte[] hash = packManager.getHash();

        if (url == null || hash == null) {
            plugin.getLogger().warning("Resourcepack hazir degil, " + player.getName() + " icin gonderilemedi.");
            return;
        }

        boolean force = plugin.getConfig().getBoolean("resourcepack.force", true);
        String promptRaw = plugin.getConfig().getString("resourcepack.prompt-mesaji", "");
        Component prompt = Component.text(org.bukkit.ChatColor.translateAlternateColorCodes('&', promptRaw));

        // Sunucudan istemciye kucuk bir gecikmeyle gonderiyoruz ki
        // oyuncu tam olarak dunyaya girdikten sonra istek gelsin.
        // DIKKAT: Paper API imzasi (url, hash, prompt, force) seklindedir.
        plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                player.setResourcePack(url, hash, prompt, force), 20L);
    }
}
