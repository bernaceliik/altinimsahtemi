package com.bernacelik.altinimsahtemi.collector.data.model

/**
 * Etiket taksonomisi.
 *
 * Her öğenin bir [slug]'ı vardır: dosya yolları ve Firebase Storage anahtarları
 * yalnızca slug kullanır. Türkçe karakterli klasör adları Storage anahtarlarında
 * ve Python tarafındaki glob desenlerinde kodlama sorunu çıkardığı için etiket
 * (görünen ad) ile yol (slug) bilinçli olarak ayrılmıştır.
 */
data class TaxonomyItem(
    val slug: String,
    val label: String,
)

/** Materyal etiketi. [isGenuineGold] modelin öğreneceği asıl ikili hedeftir. */
data class Material(
    val slug: String,
    val label: String,
    val isGenuineGold: Boolean,
)

object Taxonomy {

    val materials = listOf(
        Material("24k", "24 Ayar", isGenuineGold = true),
        Material("22k", "22 Ayar", isGenuineGold = true),
        Material("18k", "18 Ayar", isGenuineGold = true),
        Material("14k", "14 Ayar", isGenuineGold = true),
        Material("8k", "8 Ayar", isGenuineGold = true),
        Material("pirinc", "Pirinç", isGenuineGold = false),
        Material("bakir", "Bakır", isGenuineGold = false),
        Material("tungsten", "Tungsten", isGenuineGold = false),
        Material("kaplama_ince", "Kaplama (ince)", isGenuineGold = false),
        Material("kaplama_kalin", "Kaplama (kalın çekirdek)", isGenuineGold = false),
        Material("gumus", "Gümüş", isGenuineGold = false),
        Material("celik", "Çelik", isGenuineGold = false),
    )

    val objectTypes = listOf(
        TaxonomyItem("ceyrek", "Çeyrek"),
        TaxonomyItem("yarim", "Yarım"),
        TaxonomyItem("tam", "Tam"),
        TaxonomyItem("gram_1", "Gram 1g"),
        TaxonomyItem("gram_2_5", "Gram 2.5g"),
        TaxonomyItem("gram_5", "Gram 5g"),
        TaxonomyItem("bilezik", "Bilezik"),
        TaxonomyItem("yuzuk", "Yüzük"),
        TaxonomyItem("kolye", "Kolye"),
        TaxonomyItem("kulce", "Külçe"),
    )

    val surfaces = listOf(
        TaxonomyItem("mermer", "Mermer"),
        TaxonomyItem("cam", "Cam"),
        TaxonomyItem("ahsap", "Ahşap"),
        TaxonomyItem("celik_plaka", "Çelik plaka"),
    )

    /**
     * Darbe yöntemi. İnsan elinin vuruş şiddeti değişken olduğu için saha ekibi
     * hangi mekanik düzeneği kullandığını işaretlemek zorundadır — aksi hâlde
     * genlik farkları materyal farkı sanılır.
     */
    val strikeMethods = listOf(
        TaxonomyItem("serbest_dusme_10cm", "Serbest düşme 10cm"),
        TaxonomyItem("pleksi_cubuk", "Pleksi çubuk"),
        TaxonomyItem("celik_bilye", "Çelik bilye"),
        TaxonomyItem("plastik_kalem_ucu", "Plastik kalem ucu"),
    )

    val micDistancesCm = listOf(10, 15, 20)
    val micAnglesDeg = listOf(45, 90)

    val repetitionTargets = listOf(25, 50, 100)

    fun material(slug: String): Material? = materials.firstOrNull { it.slug == slug }
}
