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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.collector.data.db.CaptureEntity
import com.bernacelik.altinimsahtemi.collector.data.db.SessionEntity
import com.bernacelik.altinimsahtemi.collector.ui.components.NoticeCard
import com.bernacelik.altinimsahtemi.collector.ui.components.SectionLabel
import com.bernacelik.altinimsahtemi.collector.ui.theme.DarkBackground
import com.bernacelik.altinimsahtemi.collector.ui.theme.DarkSurface
import com.bernacelik.altinimsahtemi.collector.ui.theme.GoldPrimary
import com.bernacelik.altinimsahtemi.collector.ui.theme.SignalAmber
import com.bernacelik.altinimsahtemi.collector.ui.theme.SignalGreen
import com.bernacelik.altinimsahtemi.collector.ui.theme.TextGray
import com.bernacelik.altinimsahtemi.collector.ui.theme.White

/**
 * Oturumu kapatmadan önceki son kalite kontrolü.
 *
 * Kırpılmış ve çok zayıf kayıtlar burada ayıklanır — bunlar veri setine girerse
 * modele yanlış genlik dağılımı öğretir.
 */
@Composable
fun SessionReviewScreen(
    session: SessionEntity?,
    captures: List<CaptureEntity>,
    onBack: () -> Unit,
    onDelete: (CaptureEntity) -> Unit,
    onFinish: () -> Unit,
) {
    val clean = captures.count { it.quality == "CLEAN" }
    val problematic = captures.size - clean

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp),
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
            Text("Oturumu İncele", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatTile("Toplam", "${captures.size}", White, Modifier.weight(1f))
            StatTile("Temiz", "$clean", SignalGreen, Modifier.weight(1f))
            StatTile("Sorunlu", "$problematic", SignalAmber, Modifier.weight(1f))
        }

        if (session != null && captures.size < session.targetRepetitions) {
            Spacer(Modifier.height(14.dp))
            NoticeCard(
                icon = "!",
                title = "Hedefin altındasınız",
                body = "${session.targetRepetitions} vuruş hedeflenmişti, ${captures.size} kayıt " +
                    "alındı. Az örnek, modelin bu materyali öğrenmesini zorlaştırır.",
                accent = SignalAmber,
            )
        }

        Spacer(Modifier.height(18.dp))
        SectionLabel("KAYITLAR")
        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(captures, key = { it.id }) { capture ->
                CaptureRow(capture = capture, onDelete = { onDelete(capture) })
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                "Oturumu Kapat ve Yüklemeye Gönder",
                color = DarkBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun StatTile(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, color = TextGray, fontSize = 11.sp)
    }
}

@Composable
private fun CaptureRow(capture: CaptureEntity, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "kayit-%03d".format(capture.sequence),
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(0.dp))
                Text("  ", fontSize = 14.sp)
                QualityBadge(capture.quality)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "%d ms · tepe %.0f dBFS · %s".format(
                    capture.durationMs,
                    capture.peakDbfs,
                    if (capture.trigger == "manual") "manuel" else "otomatik",
                ),
                color = TextGray,
                fontSize = 11.sp,
            )
        }
        Text(
            "Sil",
            color = SignalAmber,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onDelete() }.padding(8.dp),
        )
    }
}
