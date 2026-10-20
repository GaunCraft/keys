package com.ornek.anahtar;

import com.ornek.anahtar.commands.AnahtarCommand;
import com.ornek.anahtar.item.ItemManager;
import com.ornek.anahtar.listeners.ItemKorumaListener;
import com.ornek.anahtar.listeners.PlayerJoinListener;
import com.ornek.anahtar.pack.ResourcePackManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;

public class AnahtarPlugin extends JavaPlugin {

    private ItemManager itemManager;
    private ResourcePackManager packManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.itemManager = new ItemManager(this);
        this.itemManager.yukle();

        this.packManager = new ResourcePackManager(this);
        try {
            this.packManager.hazirlaVeBaslat();
        } catch (IOException e) {
            getLogger().severe("Resourcepack hazirlanirken hata olustu: " + e.getMessage());
            e.printStackTrace();
        }

        AnahtarCommand anahtarCommand = new AnahtarCommand(this, itemManager, packManager);
        Objects.requireNonNull(getCommand("anahtar")).setExecutor(anahtarCommand);
        Objects.requireNonNull(getCommand("anahtar")).setTabCompleter(anahtarCommand);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, packManager), this);
        getServer().getPluginManager().registerEvents(new ItemKorumaListener(this), this);

        getLogger().info("AnahtarPlugin etkinlestirildi. " + itemManager.tumEsyalar().size() + " anahtar hazir.");
    }

    @Override
    public void onDisable() {
        if (packManager != null) {
            packManager.durdur();
        }
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public ResourcePackManager getPackManager() {
        return packManager;
    }
}
