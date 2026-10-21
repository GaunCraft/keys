package com.ornek.anahtar.item;

import java.util.List;

/**
 * config.yml'deki "items:" altinda tanimlanan tek bir anahtarin
 * bellek icindeki karsiligi. Tamamen veri tasiyan basit bir sinif (POJO).
 */
public class AnahtarItem {

    private final String id;
    private final String materialName;
    private final int customModelData;
    private final String isim;
    private final List<String> aciklama;

    public AnahtarItem(String id, String materialName, int customModelData, String isim, List<String> aciklama) {
        this.id = id;
        this.materialName = materialName;
        this.customModelData = customModelData;
        this.isim = isim;
        this.aciklama = aciklama;
    }

    public String getId() {
        return id;
    }

    public String getMaterialName() {
        return materialName;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public String getIsim() {
        return isim;
    }

    public List<String> getAciklama() {
        return aciklama;
    }
}
