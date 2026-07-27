package com.bernacelik.altinimsahtemi.collector.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.collector.data.db.SessionEntity
import com.bernacelik.altinimsahtemi.collector.ui.ImpactPreview
import com.bernacelik.altinimsahtemi.collector.ui.LevelSnapshot
import com.bernacelik.altinimsahtemi.collector.ui.components.LevelMeter
import com.bernacelik.altinimsahtemi.collector.ui.components.WaveformStrip
import com.bernacelik.altinimsahtemi.collector.ui.theme.DarkBackground
import com.bernacelik.altinimsahtemi.collector.ui.theme.DarkSurface
import com.bernacelik.altinimsahtemi.collector.ui.theme.GoldPrimary
import com.bernacelik.altinimsahtemi.collector.ui.theme.SignalAmber
import com.bernacelik.altinimsahtemi.collector.ui.theme.SignalGreen
import com.bernacelik.altinimsahtemi.collector.ui.theme.SignalRed
import com.bernacelik.altinimsahtemi.collector.ui.theme.TextGray
import com.bernacelik.altinimsahtemi.collector.ui.theme.White

/**
 * Ana kayıt ekranı.
 *
 * Sürekli dinleme modelinin sebebi: aynı objeden 50-100 tekrar alınacak. Her
 * vuruş için başlat/durdur'a basmak hem 200 dokunuş hem de tutarsız pencere
 * uzunluğu demektir. Burada operatör sadece vurur; kesme işini dedektör yapar.
 */
@Composable
fun CaptureScreen(
    session: SessionEntity?,
    capturedCount: Int,
    isListening: Boolean,
    autoTrigger: Boolean,
    level: LevelSnapshot?,
    lastImpact: ImpactPreview?,
    onToggleListening: () -> Unit,
    onToggleAutoTrigger: (Boolean) -> Unit,
    onManualCapture: () -> Unit,
    onUndoLast: () -> Unit,
    onFinish: () -> Unit,
) {
    val target = session?.targetRepetitions ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(20.dp))

        // Oturum etiketleri — operatör neyi kaydettiğini her an görmeli.
        Text(
            text = listOfNotNull(
                session?.materialLabel,
                session?.objectTypeLabel,
                session?.surfaceLabel,
            ).joinToString(" • "),
            color = White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = listOfNotNull(
                session?.strikeMethodLabel,
                session?.let { "${it.micDistanceCm} cm / ${it.micAngleDeg}°" },
                session?.let { "${it.sampleRate} Hz ${it.bitDepth}-bit" },
                session?.audioSource,
            ).joinToString(" · "),
            color = TextGray,
            fontSize = 11.sp,
        )

        Spacer(Modifier.height(20.dp))

        // İlerleme
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text("$capturedCount", color = GoldPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Text("/ $target vuruş", color = TextGray, fontSize = 14.sp)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { if (target > 0) (capturedCount.toFloat() / target).coerceIn(0f, 1f) else 0f },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = GoldPrimary,
            trackColor = DarkSurface,
        )

        Spacer(Modifier.height(24.dp))

        // Canlı seviye
        Text(
            text = if (isListening) "CANLI GİRİŞ" else "DİNLEME KAPALI",
            color = if (isListening) SignalGreen else TextGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(8.dp))
        LevelMeter(
            peakDbfs = level?.peakDbfs ?: -90f,
            thresholdDbfs = level?.thresholdDbfs ?: -45f,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = level?.let { "Tepe %.0f dBFS".format(it.peakDbfs) } ?: "Tepe —",
                color = TextGray,
                fontSize = 11.sp,
            )
            Text(
                text = level?.let {
                    "Taban %.0f · Eşik %.0f dBFS".format(it.noiseFloorDbfs, it.thresholdDbfs)
                } ?: "Taban — · Eşik —",
                color = TextGray,
                fontSize = 11.sp,
            )
        }

        Spacer(Modifier.height(20.dp))

        // Son yakalanan darbe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(DarkSurface, RoundedCornerShape(14.dp))
                .padding(14.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (lastImpact == null) {
                Text(
                    text = if (isListening) {
                        "Vuruş bekleniyor…"
                    } else {
                        "Dinlemeyi başlatın ve numuneye vurun"
                    },
                    color = TextGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "kayit-%03d".format(lastImpact.sequence),
                            color = White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        QualityBadge(lastImpact.quality)
                    }
                    Spacer(Modifier.height(10.dp))
                    WaveformStrip(
                        envelope = lastImpact.envelope,
                        color = qualityColor(lastImpact.quality),
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Otomatik darbe tespiti", color = White, fontSize = 14.sp)
                Text(
                    "Kapalıysa yalnızca manuel kayıt alınır",
                    color = TextGray,
                    fontSize = 11.sp,
                )
            }
            Switch(
                checked = autoTrigger,
                onCheckedChange = onToggleAutoTrigger,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkBackground,
                    checkedTrackColor = GoldPrimary,
                    uncheckedThumbColor = TextGray,
                    uncheckedTrackColor = DarkSurface,
                ),
            )
        }

        Spacer(Modifier.weight(1f))

        // Ana tetik
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(96.dp)
                .background(if (isListening) SignalRed else GoldPrimary, CircleShape)
                .clickable { onToggleListening() },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        DarkBackground,
                        if (isListening) RoundedCornerShape(6.dp) else CircleShape,
                    )
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (isListening) "DİNLEMEYİ DURDUR" else "DİNLEMEYİ BAŞLAT",
            color = TextGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlineAction(
                text = "Manuel Kayıt",
                enabled = isListening,
                modifier = Modifier.weight(1f),
                onClick = onManualCapture,
            )
            OutlineAction(
                text = "Son Kaydı Sil",
                enabled = capturedCount > 0,
                modifier = Modifier.weight(1f),
                onClick = onUndoLast,
            )
        }

        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onFinish,
            enabled = capturedCount > 0,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                disabledContainerColor = TextGray.copy(alpha = 0.25f),
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                "Oturumu İncele ve Bitir",
                color = DarkBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun OutlineAction(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(46.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkSurface,
            disabledContainerColor = DarkSurface.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text,
            color = if (enabled) White else TextGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun QualityBadge(quality: String) {
    val color = qualityColor(quality)
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(qualityLabel(quality), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

fun qualityColor(quality: String): Color = when (quality) {
    "CLEAN" -> SignalGreen
    "CLIPPED" -> SignalRed
    else -> SignalAmber
}

fun qualityLabel(quality: String): String = when (quality) {
    "CLEAN" -> "TEMİZ"
    "CLIPPED" -> "KIRPILDI"
    "TOO_QUIET" -> "ÇOK ZAYIF"
    else -> quality
}
