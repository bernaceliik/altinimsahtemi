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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.collector.data.db.CaptureEntity
import com.bernacelik.altinimsahtemi.collector.data.db.UploadState
import com.bernacelik.altinimsahtemi.collector.ui.components.NoticeCard
import com.bernacelik.altinimsahtemi.collector.ui.components.SectionLabel
import com.bernacelik.altinimsahtemi.collector.ui.theme.DarkBackground
import com.bernacelik.altinimsahtemi.collector.ui.theme.DarkSurface
import com.bernacelik.altinimsahtemi.collector.ui.theme.GoldPrimary
import com.bernacelik.altinimsahtemi.collector.ui.theme.SignalAmber
import com.bernacelik.altinimsahtemi.collector.ui.theme.SignalGreen
import com.bernacelik.altinimsahtemi.collector.ui.theme.SignalRed
import com.bernacelik.altinimsahtemi.collector.ui.theme.TextGray
import com.bernacelik.altinimsahtemi.collector.ui.theme.White

@Composable
fun UploadQueueScreen(
    captures: List<CaptureEntity>,
    uploaderConfigured: Boolean,
    totalBytes: Long,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    val byState = captures.groupBy { it.uploadState }
    val pending = byState[UploadState.PENDING.name].orEmpty()
    val failed = byState[UploadState.FAILED.name].orEmpty()
    val uploaded = byState[UploadState.UPLOADED.name].orEmpty()

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
            Text("Yükleme Kuyruğu", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        if (!uploaderConfigured) {
            NoticeCard(
                icon = "☁️",
                title = "Firebase henüz yapılandırılmadı",
                body = "Kayıtlar cihazda güvenle birikiyor. google-services.json eklenip uygulama " +
                    "yeniden derlendiğinde kuyruktaki her şey geriye dönük yüklenecek. " +
                    "O zamana kadar uygulamayı kaldırmayın.",
                accent = SignalAmber,
            )
            Spacer(Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QueueTile("Bekleyen", "${pending.size}", SignalAmber, Modifier.weight(1f))
            QueueTile("Yüklendi", "${uploaded.size}", SignalGreen, Modifier.weight(1f))
            QueueTile("Hatalı", "${failed.size}", SignalRed, Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Diskteki toplam veri: ${formatBytes(totalBytes)}",
            color = TextGray,
            fontSize = 12.sp,
        )

        Spacer(Modifier.height(18.dp))
        SectionLabel("HATALI KAYITLAR")
        Spacer(Modifier.height(8.dp))

        if (failed.isEmpty()) {
            Text("Hatalı kayıt yok.", color = TextGray, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(failed, key = { it.id }) { capture ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                    ) {
                        Text(
                            "kayit-%03d".format(capture.sequence),
                            color = White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            capture.uploadError ?: "Bilinmeyen hata",
                            color = SignalRed,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            enabled = uploaderConfigured && (pending.isNotEmpty() || failed.isNotEmpty()),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                disabledContainerColor = TextGray.copy(alpha = 0.25f),
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Şimdi Yükle", color = DarkBackground, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun QueueTile(label: String, value: String, color: Color, modifier: Modifier) {
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

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024 * 1024 -> "%.1f GB".format(bytes / (1024.0 * 1024 * 1024))
    bytes >= 1024L * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
    bytes >= 1024L -> "%.0f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}
