# AnahtarPlugin (Paper/Purpur 1.21.1)

Bu plugin, **hiçbir özel işlevi olmayan** (kilit açma, sağ-tık davranışı
vs. yok), yalnızca elde/envanterde/sandıkta kendine özgü bir görünümle
(özel texture) görünen **20 adet anahtar** ekler. Bu anahtarları hangi
sandığa "ait" sayacağınıza siz karar verirsiniz (RP kuralı, farklı bir
kilit plugini, elle eşleştirme, vs.) — bu plugin sadece eşyanın kendisini
ve iki özel garantiyi sağlar:

1. **Hiçbir crafting tarifinde malzeme olarak kullanılamaz** — temel
   materyal olarak `SADDLE` (eyer) seçildi, çünkü vanilla Minecraft'ta
   hiçbir tarifte girdi olarak kullanılmaz ve kendisi de tezgahta
   craft edilemez.
2. **Kırılmaz ve yok edilemez** — eşya `unbreakable` olarak işaretli,
   ayrıca yerde dururken **ateş, lav ve patlamalardan (TNT, creeper vb.)
   hiçbir zarar görmez**, yanıp kül olmaz.

Oyuncuların bu texture'ları görebilmesi için **ayrıca bir resource pack
kurmasına gerek yoktur** — plugin, texture paketini kendi içinde taşır
ve oyuncu sunucuya girdiğinde otomatik olarak gönderir.

## 1) Derleme (build) — bilgisayarına HİÇBİR ŞEY kurmadan

İçine gerekli `.github/workflows/build.yml` dosyası zaten eklenmiş
durumda. GitHub Actions ile ücretsiz ve kurulumsuz derleme:

1. https://github.com adresinden ücretsiz bir hesap aç (varsa atla).
2. **New repository** oluştur.
3. Bu klasörün (`AnahtarPlugin`) **içindeki tüm dosya ve klasörleri**
   (pom.xml, src, .github dahil) **Add file → Upload files** ile
   sürükle-bırak yükle, **Commit changes** de.
4. **Actions** sekmesine git. Eğer workflow otomatik görünmüyorsa
   (bazen ilk push'ta gecikebilir), sol menüden "Jar Derle" yazısına
   tıkla ve sağ üstten **Run workflow** ile elle tetikle.
5. Yeşil ✅ olana kadar bekle (1-3 dakika).
6. Çalışmanın içine gir, en altta **Artifacts** bölümünden
   **AnahtarPlugin-jar** dosyasını indir. İçinden çıkan
   `AnahtarPlugin.jar` kurulacak dosya budur.

> Alternatif: Kendi bilgisayarında Java 21 JDK ve Maven kuruluysa,
> proje klasöründe `mvn clean package` komutu yeterli;
> `target/AnahtarPlugin.jar` oluşur.

## 2) Kurulum

- `AnahtarPlugin.jar` dosyasını sunucunun `plugins/` klasörüne koy.
- Sunucuyu başlat / yeniden başlat.
- İlk açılışta `plugins/AnahtarPlugin/` altında `config.yml` ve
  `resourcepack/` otomatik oluşturulur.

### ÇOK ÖNEMLİ — `config.yml` → `resourcepack.host`

```yaml
resourcepack:
  host: "localhost"   # <-- sunucunun GERÇEK/PUBLIC IP'si veya domaini ile degistir
  port: 8083           # <-- bu portu router/firewall'dan yonlendir (port forwarding)
  force: true
```

Eğer aynı sunucuda daha önce kurduğun **KristalPlugin** da varsa,
o plugin `8082` portunu kullanıyordu; bu yüzden AnahtarPlugin
çakışmasın diye varsayılan olarak **8083** portunu kullanır. İkisi de
kuruluysa her ikisinin de port yönlendirmesini ayrı ayrı yapman gerekir.

Ayarları değiştirdikten sonra: `/anahtar reload`

## 2b) Diger resourcepack'lerle (orn. ben10) birlikte calisma

Paket `addResourcePack` ile, sabit bir kimlikle EKLENIR; oyuncuda yuklu
baska sunucu paketlerini silmez. `resourcepack.send-delay-ticks` (varsayilan
80) ile giristen sonra gecikmeli gonderilir. Ayni esya modelini
(`saddle.json`) baska bir paket de degistiriyorsa, ustte yuklenen paketin
dosyasi digerini tamamen ezer; o durumda baska bir `material` secin.

## 3) Komutlar

| Komut | Açıklama | Yetki |
|---|---|---|
| `/anahtar liste` | Tanımlı tüm anahtarları listeler | `anahtar.kullan` (varsayılan: herkes) |
| `/anahtar ver <esya-id> [oyuncu]` | Anahtarı oyuncuya verir | `anahtar.ver` (varsayılan: op) |
| `/anahtar reload` | config.yml + resourcepack'i yeniden yükler | `anahtar.reload` (varsayılan: op) |

Örnek: `/anahtar ver anahtar_05 Notch`

## 4) Sandıklarla eşleştirme (bu plugin bunu yapmaz)

Bu plugin **kilit mekaniği içermez** — sadece görsel olarak farklı,
kırılmaz ve yanmaz anahtar eşyaları üretir. "Bu anahtar şu sandığın
anahtarıdır" bilgisini sen kendi sunucunda (RP kuralı olarak, ayrı bir
kilit/koruma plugini ile, ya da sahiplik/claim sisteminle) yönetirsin.
İstersen her anahtarın `isim` ve `aciklama` (lore) alanlarına hangi
sandığa ait olduğunu yazabilirsin, bu tamamen kozmetik bir ipucu olur.

## 5) Kendi anahtarınızı eklemek (örnek şema)

`config.yml` dosyasının en üstünde adım adım açıklanmış bir şablon
bulunuyor. Özetle:

1. **`plugins/AnahtarPlugin/config.yml`** içindeki `items:` listesine
   yeni bir madde ekleyin:

   ```yaml
   benim-yeni-anahtarim:
     material: SADDLE                 # crafting'de KULLANILMAYAN bir materyal
     custom-model-data: 2021          # her anahtar icin FARKLI bir sayi!
     isim: "&6&lBenim Yeni Anahtarim"
     aciklama:
       - "&7Bu tamamen kozmetik bir anahtardir."
       - "&7Hicbir otomatik islevi yoktur."
   ```

2. **Texture ekleyin** (16x16 piksel PNG, şeffaf arka plan önerilir):

   ```
   plugins/AnahtarPlugin/resourcepack/assets/minecraft/textures/item/anahtar/benim_yeni_anahtarim.png
   ```

3. **Model dosyası ekleyin**:

   `plugins/AnahtarPlugin/resourcepack/assets/minecraft/models/item/anahtar/benim_yeni_anahtarim.json`

   ```json
   {
     "parent": "item/generated",
     "textures": { "layer0": "item/anahtar/benim_yeni_anahtarim" }
   }
   ```

4. **`saddle.json` dosyasına override ekleyin**:

   `plugins/AnahtarPlugin/resourcepack/assets/minecraft/models/item/saddle.json`
   içindeki `"overrides"` listesine, `custom-model-data` ile **aynı sayıyı**
   kullanarak şu satırı ekleyin:

   ```json
   { "predicate": { "custom_model_data": 2021 }, "model": "item/anahtar/benim_yeni_anahtarim" }
   ```

5. Oyunda `/anahtar reload` yazın.

> Neden `SADDLE`? Çünkü vanilla Minecraft'ta hiçbir crafting tarifinde
> malzeme olarak geçmez ve kendisi de craft edilemez — bu yüzden
> oyuncular anahtarı yanlışlıkla bir tarifte harcayamaz. Farklı bir
> materyal kullanmak istersen (örn. `NAME_TAG` ya da `TRIDENT`), aynı
> kurala dikkat et: o materyalin de hiçbir tarifte girdi olmadığından
> emin ol, ve override'ı `saddle.json` yerine o materyalin kendi model
> dosyasına (örn. `name_tag.json`) ekle.

## 6) Eşyaların özellikleri hakkında özet

- **İşlev**: Yok. Sağ tık, yeme, kullanma, craft, enchant — hiçbiri
  eklenmemiştir. Tamamen görsel/kozmetik.
- **Kırılmazlık**: `ItemMeta#setUnbreakable(true)` ile işaretli.
- **Yok edilemezlik**: Yerde (drop) haldeyken ateş, lav, TNT patlaması
  ve yaratık patlamasından (creeper vb.) kaynaklı hasar tamamen iptal
  edilir (bkz. `ItemKorumaListener`). Bu koruma, eşyanın üzerindeki
  görünmez bir NBT etiketine (`anahtar_id`) bakılarak uygulanır; yani
  plugin dışında üretilmiş sıradan bir `SADDLE` bu korumadan
  yararlanmaz, sadece `/anahtar ver` ile verilen gerçek anahtarlar korunur.
