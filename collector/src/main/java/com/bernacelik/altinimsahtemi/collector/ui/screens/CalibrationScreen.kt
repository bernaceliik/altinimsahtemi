package com.bernacelik.altinimsahtemi.collector.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.collector.ui.CalibrationState
import com.bernacelik.altinimsahtemi.collector.ui.components.InfoRow
import com.bernacelik.altinimsahtemi.collector.ui.components.NoticeCard
import com.bernacelik.altinimsahtemi.collector.ui.theme.DarkBackground
import com.bernacelik.altinimsahtemi.collector.ui.theme.DarkSurface
import com.bernacelik.altinimsahtemi.collector.ui.theme.GoldPrimary
import com.bernacelik.altinimsahtemi.collector.ui.theme.SignalAmber
import com.bernacelik.altinimsahtemi.collector.ui.theme.SignalGreen
import com.bernacelik.altinimsahtemi.collector.ui.theme.SignalRed
import com.bernacelik.altinimsahtemi.collector.ui.theme.TextGray
import com.bernacelik.altinimsahtemi.collector.ui.theme.White

/**
 * Ortam gürültü tabanını ölçer ve tetik eşiğini buradan türetir.
 *
 * Bu adım atlanamaz: eşik ortamdan bağımsız sabit olursa sessiz laboratuvarda
 * hışırtı tetikler, gürültülü kuyumcuda ise gerçek darbeler kaçar.
 */
@Composable
fun CalibrationScreen(
    state: CalibrationState,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onRetry: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
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
            Text("Ortam Kalibrasyonu", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))

        val currentDb = when (state) {
            is CalibrationState.Measuring -> state.currentDbfs
            is CalibrationState.Done -> state.noiseFloorDbfs
            CalibrationState.Idle -> null
        }
        val ringColor = when {
            state is CalibrationState.Done -> state.verdict.color()
            state is CalibrationState.Measuring -> GoldPrimary
            else -> TextGray
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(180.dp)
                .border(3.dp, ringColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentDb?.let { "%.0f".format(it) } ?: "—",
                    color = White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text("dBFS", color = TextGray, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        when (state) {
            CalibrationState.Idle -> {
                Text(
                    "Ölçüme başlamadan önce sessiz olun ve düzeneğe dokunmayın.",
                    color = TextGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.weight(1f))
                PrimaryButton("5 Saniyelik Ölçümü Başlat", onStart)
            }

            is CalibrationState.Measuring -> {
                Text(
                    "Ortam dinleniyor…",
                    color = GoldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = GoldPrimary,
                    trackColor = DarkSurface,
                )
                Spacer(Modifier.weight(1f))
            }

            is CalibrationState.Done -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                ) {
                    InfoRow("Gürültü tabanı", "%.1f dBFS".format(state.noiseFloorDbfs))
                    InfoRow(
                        "Hesaplanan tetik eşiği",
                        "%.1f dBFS".format(state.thresholdDbfs),
                        GoldPrimary,
                    )
                }
                Spacer(Modifier.height(16.dp))
                NoticeCard(
                    icon = state.verdict.icon(),
                    title = state.verdict.title(),
                    body = state.verdict.body(),
                    accent = state.verdict.color(),
                )
                Spacer(Modifier.weight(1f))
                SecondaryButton("Yeniden Ölç", onRetry)
                Spacer(Modifier.height(10.dp))
                PrimaryButton(
                    text = if (state.verdict == CalibrationState.Verdict.TOO_NOISY) {
                        "Yine de Kayda Geç"
                    } else {
                        "Kayda Geç"
                    },
                    onClick = onContinue,
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(text, color = DarkBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(text, color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

private fun CalibrationState.Verdict.color(): Color = when (this) {
    CalibrationState.Verdict.QUIET -> SignalGreen
    CalibrationState.Verdict.ACCEPTABLE -> SignalAmber
    CalibrationState.Verdict.TOO_NOISY -> SignalRed
}

private fun CalibrationState.Verdict.icon(): String = when (this) {
    CalibrationState.Verdict.QUIET -> "✓"
    CalibrationState.Verdict.ACCEPTABLE -> "!"
    CalibrationState.Verdict.TOO_NOISY -> "⚠️"
}

private fun CalibrationState.Verdict.title(): String = when (this) {
    CalibrationState.Verdict.QUIET -> "Ortam sessiz — referans kalitede kayıt alınabilir"
    CalibrationState.Verdict.ACCEPTABLE -> "Ortam kabul edilebilir"
    CalibrationState.Verdict.TOO_NOISY -> "Ortam fazla gürültülü"
}

private fun CalibrationState.Verdict.body(): String = when (this) {
    CalibrationState.Verdict.QUIET ->
        "Sönümlenme eğrisi gürültüye gömülmeden ölçülebilir."
    CalibrationState.Verdict.ACCEPTABLE ->
        "Kayıt alınabilir ama sönümlenmenin kuyruğu gürültüye karışabilir. " +
            "Mümkünse daha sessiz bir ortama geçin."
    CalibrationState.Verdict.TOO_NOISY ->
        "Bu seviyede toplanan veri referans korpusuna alınmamalı. Klima, TV ve " +
            "konuşmayı kesin; mümkünse ölçümü tekrarlayın."
}
