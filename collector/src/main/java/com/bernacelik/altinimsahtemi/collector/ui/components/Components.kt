package com.bernacelik.altinimsahtemi.collector.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.collector.ui.theme.DarkBackground
import com.bernacelik.altinimsahtemi.collector.ui.theme.DarkSurface
import com.bernacelik.altinimsahtemi.collector.ui.theme.GoldPrimary
import com.bernacelik.altinimsahtemi.collector.ui.theme.SignalGreen
import com.bernacelik.altinimsahtemi.collector.ui.theme.TextGray
import com.bernacelik.altinimsahtemi.collector.ui.theme.White

/** Seçilebilir etiket düğmesi. */
@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
            )
            .border(
                width = 1.dp,
                color = if (isSelected) GoldPrimary else TextGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (isSelected) GoldPrimary else TextGray,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

/** Yatay kaydırılabilir etiket grubu. */
@Composable
fun <T> ChipRow(
    items: List<T>,
    isSelected: (T) -> Boolean,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items) { item ->
            SelectableChip(
                text = label(item),
                isSelected = isSelected(item),
                onClick = { onSelect(item) },
            )
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = TextGray,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = modifier,
    )
}

/**
 * dBFS seviye çubuğu.
 *
 * Ölçek -90..0 dBFS aralığına eşlenir. Tetik eşiği ayrı bir işaretle gösterilir:
 * saha ekibi bir vuruşun eşiği geçip geçmediğini anında görebilsin diye.
 */
@Composable
fun LevelMeter(
    peakDbfs: Float,
    thresholdDbfs: Float,
    modifier: Modifier = Modifier,
) {
    val minDb = -90f
    fun fraction(db: Float) = ((db - minDb) / -minDb).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(DarkBackground),
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(14.dp)) {
            val level = fraction(peakDbfs) * size.width
            drawRect(
                color = if (peakDbfs >= thresholdDbfs) GoldPrimary else SignalGreen,
                size = androidx.compose.ui.geometry.Size(level, size.height),
            )
            val markerX = fraction(thresholdDbfs) * size.width
            drawRect(
                color = White.copy(alpha = 0.7f),
                topLeft = androidx.compose.ui.geometry.Offset(markerX, 0f),
                size = androidx.compose.ui.geometry.Size(2f, size.height),
            )
        }
    }
}

/**
 * Yakalanan darbenin dalga formu — kova başına tepe değerlerinden çizilir.
 *
 * Amaç estetik değil kalite kontrolü: kırpılmış bir kayıt düz tavanlı, çok
 * zayıf bir kayıt ise neredeyse düz görünür ve gözle anında ayırt edilir.
 */
@Composable
fun WaveformStrip(
    envelope: FloatArray,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (envelope.isEmpty()) return@Canvas
        val barWidth = size.width / envelope.size
        envelope.forEachIndexed { index, amplitude ->
            val barHeight = (amplitude.coerceIn(0f, 1f) * size.height).coerceAtLeast(1f)
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = index * barWidth,
                    y = (size.height - barHeight) / 2f,
                ),
                size = androidx.compose.ui.geometry.Size(barWidth * 0.7f, barHeight),
            )
        }
    }
}

/** Etiket/değer satırı. */
@Composable
fun InfoRow(label: String, value: String, valueColor: Color = White) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = TextGray, fontSize = 13.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

/** Bilgi/uyarı kutusu. */
@Composable
fun NoticeCard(
    icon: String,
    title: String,
    body: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (body.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(body, color = TextGray, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}
