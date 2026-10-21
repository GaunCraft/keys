package com.ornek.anahtar.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * config.yml icindeki "items:" bolumunu okuyup her biri icin
 * bir {@link AnahtarItem} tanimi ve karsilik gelen {@link ItemStack}
 * uretmekten sorumludur.
 *
 * Bu esyalarin HICBIR OZEL ISLEVI (kilit acma, event, sag-tik davranisi
 * vs.) YOKTUR. Sadece isim, aciklama (lore) ve custom-model-data ile
 * ozel bir gorunume sahip, kirilmaz ve ates/lav/patlamadan etkilenmeyen
 * bir esyadir. Hangi sandigin "bu anahtarla" acilacagina plugin karar
 * vermez; bunu sunucu sahibi kendi uygulamasinda (RP / kurallarla) belirler.
 */
public class ItemManager {

    private final JavaPlugin plugin;
    private final NamespacedKey anahtarKey;
    private final Map<String, AnahtarItem> items = new LinkedHashMap<>();

    public ItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.anahtarKey = new NamespacedKey(plugin, "anahtar_id");
    }

    public static NamespacedKey anahtarEtiketi(JavaPlugin plugin) {
        return new NamespacedKey(plugin, "anahtar_id");
    }

    /**
     * config.yml'i (yeniden) okur. /anahtar reload komutunda da cagrilir.
     */
    public void yukle() {
        items.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("items");
        if (root == null) {
            plugin.getLogger().warning("config.yml icinde 'items:' bolumu bulunamadi!");
            return;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(id);
            if (sec == null) continue;

            String materialName = sec.getString("material", "SADDLE");
            int cmd = sec.getInt("custom-model-data", -1);
            String isim = sec.getString("isim", id);
            List<String> aciklama = sec.getStringList("aciklama");

            if (cmd < 0) {
                plugin.getLogger().log(Level.WARNING, "'" + id + "' anahtari icin gecerli bir custom-model-data tanimlanmamis, atlaniyor.");
                continue;
            }

            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getLogger().log(Level.WARNING, "'" + id + "' anahtari icin '" + materialName + "' gecersiz bir materyal, SADDLE kullaniliyor.");
                material = Material.SADDLE;
            }

            items.put(id, new AnahtarItem(id, material.name(), cmd, isim, aciklama));
        }

        plugin.getLogger().info(items.size() + " adet anahtar yuklendi.");
    }

    public Map<String, AnahtarItem> tumEsyalar() {
        return items;
    }

    public AnahtarItem bul(String id) {
        return items.get(id);
    }

    /**
     * Bir AnahtarItem tanimindan gercek, oyuncuya verilebilecek bir
     * ItemStack olusturur. Hicbir davranis/islev eklenmez; sadece
     * gorunum (isim, lore, custom-model-data) ayarlanir ve esya
     * kirilmaz (unbreakable) olarak isaretlenir.
     *
     * Esyanin ates/lav/patlamadan etkilenmemesi ise bu metotta degil,
     * ItemKorumaListener icinde, PersistentDataContainer'daki
     * "anahtar_id" etiketine bakilarak saglanir.
     */
    public ItemStack itemUret(AnahtarItem tanim) {
        Material material = Material.matchMaterial(tanim.getMaterialName());
        if (material == null) material = Material.SADDLE;

        ItemStack stack = new ItemStack(material, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(renkli(tanim.getIsim()));
            meta.setLore(tanim.getAciklama().stream().map(this::renkli).toList());
            meta.setCustomModelData(tanim.getCustomModelData());
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.values());

            // Bu esyanin plugin tarafindan uretilmis bir anahtar oldugunu
            // isaretlemek icin. ItemKorumaListener bu etikete bakarak
            // esyayi ates/lav/patlamaya karsi korur.
            meta.getPersistentDataContainer().set(anahtarKey, PersistentDataType.STRING, tanim.getId());

            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String renkli(String metin) {
        return ChatColor.translateAlternateColorCodes('&', metin);
    }
}
