package com.ornek.anahtar.commands;

import com.ornek.anahtar.item.AnahtarItem;
import com.ornek.anahtar.item.ItemManager;
import com.ornek.anahtar.pack.ResourcePackManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /anahtar ver <esya-id> [oyuncu]   - anahtari oyuncuya verir
 * /anahtar liste                     - tanimli tum anahtarlari listeler
 * /anahtar reload                    - config.yml ve resourcepack'i yeniden yukler
 */
public class AnahtarCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final ItemManager itemManager;
    private final ResourcePackManager packManager;

    public AnahtarCommand(JavaPlugin plugin, ItemManager itemManager, ResourcePackManager packManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.packManager = packManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(renkli("&e/anahtar ver <esya-id> [oyuncu]"));
            sender.sendMessage(renkli("&e/anahtar liste"));
            sender.sendMessage(renkli("&e/anahtar reload"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "ver" -> handleVer(sender, args);
            case "liste" -> handleListe(sender);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage(renkli("&cBilinmeyen alt komut. /anahtar yaz."));
        }
        return true;
    }

    private void handleVer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("anahtar.ver")) {
            sender.sendMessage(renkli("&cBu komutu kullanma yetkin yok."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(renkli("&cKullanim: /anahtar ver <esya-id> [oyuncu]"));
            return;
        }

        AnahtarItem tanim = itemManager.bul(args[1]);
        if (tanim == null) {
            sender.sendMessage(renkli("&cBoyle bir anahtar yok: " + args[1] + " (bkz: /anahtar liste)"));
            return;
        }

        Player hedef;
        if (args.length >= 3) {
            hedef = Bukkit.getPlayerExact(args[2]);
            if (hedef == null) {
                sender.sendMessage(renkli("&cOyuncu bulunamadi: " + args[2]));
                return;
            }
        } else if (sender instanceof Player p) {
            hedef = p;
        } else {
            sender.sendMessage(renkli("&cKonsoldan kullanirken bir oyuncu adi belirtmelisin."));
            return;
        }

        hedef.getInventory().addItem(itemManager.itemUret(tanim));
        sender.sendMessage(renkli("&a" + tanim.getIsim() + "&a anahtari " + hedef.getName() + " adli oyuncuya verildi."));
        if (!hedef.equals(sender)) {
            hedef.sendMessage(renkli("&aSana bir " + tanim.getIsim() + "&a verildi!"));
        }
    }

    private void handleListe(CommandSender sender) {
        sender.sendMessage(renkli("&e--- Anahtarlar (" + itemManager.tumEsyalar().size() + ") ---"));
        for (AnahtarItem item : itemManager.tumEsyalar().values()) {
            sender.sendMessage(renkli("&7- &f" + item.getId() + " &7-> " + item.getIsim()));
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("anahtar.reload")) {
            sender.sendMessage(renkli("&cBu komutu kullanma yetkin yok."));
            return;
        }
        plugin.reloadConfig();
        itemManager.yukle();
        try {
            packManager.hazirlaVeBaslat();
            sender.sendMessage(renkli("&aConfig ve resourcepack yeniden yuklendi."));
        } catch (IOException e) {
            sender.sendMessage(renkli("&cResourcepack yeniden paketlenirken hata olustu: " + e.getMessage()));
            plugin.getLogger().severe("Resourcepack reload hatasi: " + e.getMessage());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            result.addAll(List.of("ver", "liste", "reload"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("ver")) {
            result.addAll(itemManager.tumEsyalar().keySet());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("ver")) {
            result.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        }

        String son = args[args.length - 1].toLowerCase();
        return result.stream().filter(s -> s.toLowerCase().startsWith(son)).collect(Collectors.toList());
    }

    private String renkli(String metin) {
        return ChatColor.translateAlternateColorCodes('&', metin);
    }
}
