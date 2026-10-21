package com.ornek.anahtar.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Yerde duran (drop edilmis) anahtar esyalarini atesten, lavdan ve
 * patlamalardan (TNT, creeper, vb.) korur; bu kaynaklardan hicbir
 * sekilde yok edilemezler.
 *
 * Nasil calisir: Bukkit'te yerde duran her esya bir "Item" varligidir
 * (entity) ve diger varliklar gibi hasar alip "can" kaybedebilir; cani
 * sifirlanirsa esya tamamen yok olur. Bu dinleyici, hasar alan varligin
 * bir Item olup olmadigina, ustundeki ItemStack'in bizim
 * "anahtar_id" etiketini tasiyip tasimadigina bakar ve tasiyorsa
 * ates/lav/patlama kaynakli hasari iptal eder. Boylece esya hem
 * yanmaz hem de patlamadan zarar gormez.
 *
 * Not: Sandik/envanter icindeki esyalar zaten birer "entity" olmadigi
 * icin bu olaylara hic maruz kalmazlar; bu koruma sadece yerde (dropped)
 * haldeyken devreye girer, ama bu sayede sandik yanip/patlayip esyalar
 * yere sacilsa bile anahtarlar yok olmaz.
 */
public class ItemKorumaListener implements Listener {

    private final NamespacedKey anahtarKey;

    public ItemKorumaListener(JavaPlugin plugin) {
        this.anahtarKey = new NamespacedKey(plugin, "anahtar_id");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item itemEntity)) return;
        if (!tasirAnahtarEtiketi(itemEntity.getItemStack())) return;

        switch (event.getCause()) {
            case FIRE, FIRE_TICK, LAVA, HOT_FLOOR, BLOCK_EXPLOSION, ENTITY_EXPLOSION -> event.setCancelled(true);
            default -> { /* diger hasar turlerine dokunulmuyor */ }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        if (!(event.getEntity() instanceof Item itemEntity)) return;
        if (!tasirAnahtarEtiketi(itemEntity.getItemStack())) return;

        // Yanma gorsel efektini de baslatmadan iptal ediyoruz.
        event.setCancelled(true);
    }

    private boolean tasirAnahtarEtiketi(ItemStack stack) {
        if (stack == null) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(anahtarKey, PersistentDataType.STRING);
    }
}
