package com.ornek.anahtar.pack;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Bu sinif ayri bir resourcepack kurulumuna gerek kalmadan, ozel
 * anahtar texture'larinin oyunculara otomatik gonderilmesini saglar:
 *
 *  1) Plugin ilk calistiginda, jar icine gomulu varsayilan
 *     "resourcepack/" klasorunu plugins/AnahtarPlugin/resourcepack/ 'a cikartir.
 *     (Eger klasor zaten varsa dokunmaz; boylece kullanicinin kendi
 *     eklediği texture/model dosyalari korunur.)
 *  2) Bu klasoru resourcepack.zip olarak paketler ve SHA-1 hash'ini hesaplar.
 *  3) Basit bir yerlesik HTTP sunucusu ile bu zip dosyasini sunar.
 *  4) Oyuncu sunucuya girdiginde, Paper API'nin setResourcePack metodu
 *     ile bu URL ve hash oyuncunun istemcisine gonderilir; Minecraft
 *     istemcisi paketi otomatik indirir ve uygular.
 */
public class ResourcePackManager {

    private final JavaPlugin plugin;
    private final File resourcepackDir;
    private final File zipFile;

    private HttpServer httpServer;
    private byte[] cachedHash;
    private String cachedUrl;

    public ResourcePackManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.resourcepackDir = new File(plugin.getDataFolder(), "resourcepack");
        this.zipFile = new File(plugin.getDataFolder(), "resourcepack.zip");
    }

    /**
     * Plugin acilirken ya da /anahtar reload komutunda cagrilir.
     * Varsayilan paketi (ilk kurulumda) diske cikartir, zip'i yeniden
     * olusturur ve HTTP sunucusunu (yoksa) baslatir.
     */
    public void hazirlaVeBaslat() throws IOException {
        if (!resourcepackDir.exists()) {
            varsayilanPaketiCikart();
        }

        ziple();
        hashHesapla();

        if (httpServer == null) {
            sunucuyuBaslat();
        }
    }

    public void durdur() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
    }

    public byte[] getHash() {
        return cachedHash;
    }

    public String getUrl() {
        return cachedUrl;
    }

    // ------------------------------------------------------------------

    private void varsayilanPaketiCikart() throws IOException {
        resourcepackDir.mkdirs();
        String jarPath;
        try {
            jarPath = new File(ResourcePackManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getPath();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith("resourcepack/")) continue;

                String relative = name.substring("resourcepack/".length());
                if (relative.isEmpty()) continue;

                File out = new File(resourcepackDir, relative);
                if (entry.isDirectory()) {
                    out.mkdirs();
                    continue;
                }
                out.getParentFile().mkdirs();
                try (InputStream in = jar.getInputStream(entry);
                     OutputStream os = new FileOutputStream(out)) {
                    in.transferTo(os);
                }
            }
        }
        plugin.getLogger().info("Varsayilan resourcepack, plugins/AnahtarPlugin/resourcepack/ klasorune cikartildi.");
    }

    private void ziple() throws IOException {
        if (zipFile.exists()) {
            Files.delete(zipFile.toPath());
        }
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
             Stream<Path> walk = Files.walk(resourcepackDir.toPath())) {

            Path base = resourcepackDir.toPath();
            walk.filter(Files::isRegularFile).forEach(path -> {
                String entryName = base.relativize(path).toString().replace(File.separatorChar, '/');
                try {
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    plugin.getLogger().warning("Zip'e eklenemedi: " + entryName + " -> " + e.getMessage());
                }
            });
        }
    }

    private void hashHesapla() throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] fileBytes = Files.readAllBytes(zipFile.toPath());
            cachedHash = digest.digest(fileBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

    private void sunucuyuBaslat() throws IOException {
        String host = plugin.getConfig().getString("resourcepack.host", "localhost");
        int port = plugin.getConfig().getInt("resourcepack.port", 8083);

        httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        httpServer.createContext("/resourcepack.zip", this::handleRequest);
        httpServer.setExecutor(Executors.newFixedThreadPool(2));
        httpServer.start();

        cachedUrl = "http://" + host + ":" + port + "/resourcepack.zip";
        plugin.getLogger().info("Resourcepack sunucusu baslatildi: " + cachedUrl);
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        byte[] data = Files.readAllBytes(zipFile.toPath());
        exchange.getResponseHeaders().add("Content-Type", "application/zip");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }
}
