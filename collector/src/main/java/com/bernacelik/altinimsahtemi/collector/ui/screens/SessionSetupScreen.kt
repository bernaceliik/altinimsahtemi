package com.bernacelik.altinimsahtemi.collector.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.collector.data.SessionRequest
import com.bernacelik.altinimsahtemi.collector.data.model.Material
import com.bernacelik.altinimsahtemi.collector.data.model.Taxonomy
import com.bernacelik.altinimsahtemi.collector.data.model.TaxonomyItem
import com.bernacelik.altinimsahtemi.collector.ui.components.ChipRow
import com.bernacelik.altinimsahtemi.collector.ui.components.NoticeCard
import com.bernacelik.altinimsahtemi.collector.ui.components.SectionLabel
import com.bernacelik.altinimsahtemi.collector.ui.theme.DarkBackground
import com.bernacelik.altinimsahtemi.collector.ui.theme.GoldPrimary
import com.bernacelik.altinimsahtemi.collector.ui.theme.SignalAmber
import com.bernacelik.altinimsahtemi.collector.ui.theme.TextGray
import com.bernacelik.altinimsahtemi.collector.ui.theme.White

/**
 * Oturumun etiketleri. Bu ekranda yapılan seçimler oturum boyunca sabit kalır ve
 * her kaydın metadata'sına aynen kopyalanır — sahada kayıt sırasında etiket
 * değiştirmek, veri setinin tutarlılığını bozan en yaygın hatadır.
 */
@Composable
fun SessionSetupScreen(
    onBack: () -> Unit,
    onContinue: (SessionRequest) -> Unit,
) {
    var material by remember { mutableStateOf(Taxonomy.materials.first()) }
    var objectType by remember { mutableStateOf(Taxonomy.objectTypes.first()) }
    var surface by remember { mutableStateOf(Taxonomy.surfaces.first()) }
    var strikeMethod by remember { mutableStateOf(Taxonomy.strikeMethods.first()) }
    var distance by remember { mutableStateOf(Taxonomy.micDistancesCm[1]) }
    var angle by remember { mutableStateOf(Taxonomy.micAnglesDeg.first()) }
    var repetitions by remember { mutableStateOf(Taxonomy.repetitionTargets[1]) }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "←",
                color = White,
                fontSize = 24.sp,
                modifier = Modifier.clickable { onBack() }.padding(end = 16.dp),
            )
            Text("Oturum Kurulumu", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        SectionLabel("MATERYAL")
        Spacer(Modifier.height(10.dp))
        ChipRow(
            items = Taxonomy.materials,
            isSelected = { it.slug == material.slug },
            label = Material::label,
            onSelect = { material = it },
        )

        Spacer(Modifier.height(22.dp))
        SectionLabel("NESNE TÜRÜ")
        Spacer(Modifier.height(10.dp))
        ChipRow(
            items = Taxonomy.objectTypes,
            isSelected = { it.slug == objectType.slug },
            label = TaxonomyItem::label,
            onSelect = { objectType = it },
        )

        Spacer(Modifier.height(22.dp))
        SectionLabel("DARBE YÜZEYİ")
        Spacer(Modifier.height(10.dp))
        ChipRow(
            items = Taxonomy.surfaces,
            isSelected = { it.slug == surface.slug },
            label = TaxonomyItem::label,
            onSelect = { surface = it },
        )

        Spacer(Modifier.height(22.dp))
        SectionLabel("VURUŞ YÖNTEMİ")
        Spacer(Modifier.height(10.dp))
        ChipRow(
            items = Taxonomy.strikeMethods,
            isSelected = { it.slug == strikeMethod.slug },
            label = TaxonomyItem::label,
            onSelect = { strikeMethod = it },
        )

        Spacer(Modifier.height(22.dp))
        SectionLabel("MİKROFON GEOMETRİSİ")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column {
                Text("Mesafe", color = TextGray, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                ChipRow(
                    items = Taxonomy.micDistancesCm,
                    isSelected = { it == distance },
                    label = { "$it cm" },
                    onSelect = { distance = it },
                )
            }
            Column {
                Text("Açı", color = TextGray, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                ChipRow(
                    items = Taxonomy.micAnglesDeg,
                    isSelected = { it == angle },
                    label = { "$it°" },
                    onSelect = { angle = it },
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        SectionLabel("HEDEF TEKRAR SAYISI")
        Spacer(Modifier.height(10.dp))
        ChipRow(
            items = Taxonomy.repetitionTargets,
            isSelected = { it == repetitions },
            label = { "$it vuruş" },
            onSelect = { repetitions = it },
        )

        Spacer(Modifier.height(22.dp))
        SectionLabel("NOT (opsiyonel)")
        Spacer(Modifier.height(10.dp))
        CollectorTextField(
            value = notes,
            onValueChange = { notes = it },
            placeholder = "Örn. referans numune #12, Kapalıçarşı",
        )

        Spacer(Modifier.height(20.dp))
        NoticeCard(
            icon = "📐",
            title = "Düzeneği sabitleyin",
            body = "Aynı obje için tüm tekrarlar aynı zemin, aynı vuruş yöntemi ve aynı " +
                "mikrofon geometrisiyle alınmalı. Değişen tek şey objenin yönü olmalı.",
            accent = SignalAmber,
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                onContinue(
                    SessionRequest(
                        materialSlug = material.slug,
                        materialLabel = material.label,
                        isGenuineGold = material.isGenuineGold,
                        objectTypeSlug = objectType.slug,
                        objectTypeLabel = objectType.label,
                        surfaceSlug = surface.slug,
                        surfaceLabel = surface.label,
                        strikeMethodSlug = strikeMethod.slug,
                        strikeMethodLabel = strikeMethod.label,
                        micDistanceCm = distance,
                        micAngleDeg = angle,
                        targetRepetitions = repetitions,
                        notes = notes,
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                "İleri: Ortam Kalibrasyonu",
                color = DarkBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(40.dp))
    }
}
