package com.bernacelik.altinimsahtemi.collector.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernacelik.altinimsahtemi.audio.AudioProfile
import com.bernacelik.altinimsahtemi.collector.data.prefs.Operator
import com.bernacelik.altinimsahtemi.collector.ui.components.InfoRow
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

/**
 * İlk açılış ekranı: operatör kimliği ve cihaz ses profili.
 *
 * Profil özeti burada gösterilir çünkü saha ekibinin ne topladığını bilmesi
 * şart: 48 kHz mi 44.1 kHz mi, işlenmemiş zincir açık mı? Bu bilgi her dosyanın
 * metadata'sına da yazılır, ama sorun varsa kayda başlamadan görülmelidir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorSetupScreen(
    operator: Operator,
    profile: AudioProfile?,
    profileError: String?,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onSave: (name: String, deviceNote: String) -> Unit,
    onContinue: () -> Unit,
) {
    var name by remember(operator.name) { mutableStateOf(operator.name) }
    var deviceNote by remember(operator.deviceNote) { mutableStateOf(operator.deviceNote) }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) onRequestPermission()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Veri Toplama Aracı", color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Akustik tını korpusu için saha kayıt istasyonu.",
            color = TextGray,
            fontSize = 14.sp,
        )

        Spacer(Modifier.height(28.dp))
        SectionLabel("OPERATÖR")
        Spacer(Modifier.height(10.dp))
        CollectorTextField(value = name, onValueChange = { name = it }, placeholder = "Ad Soyad")
        Spacer(Modifier.height(10.dp))
        CollectorTextField(
            value = deviceNote,
            onValueChange = { deviceNote = it },
            placeholder = "Cihaz notu (örn. Pixel 7 - saha telefonu)",
        )

        Spacer(Modifier.height(28.dp))
        SectionLabel("DOĞRULANMIŞ SES PROFİLİ")
        Spacer(Modifier.height(10.dp))

        when {
            !hasPermission -> NoticeCard(
                icon = "🎙️",
                title = "Mikrofon izni gerekli",
                body = "Cihazın ses yeteneklerini ölçebilmek için kayıt iznini vermeniz gerekiyor.",
                accent = SignalAmber,
            )

            profile == null -> NoticeCard(
                icon = "⚠️",
                title = "Profil tespit edilemedi",
                body = profileError ?: "Cihaz yoklanıyor…",
                accent = SignalRed,
            )

            else -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                InfoRow("Örnekleme oranı", "${profile.sampleRate} Hz", GoldPrimary)
                InfoRow("Bit derinliği", "${profile.bitsPerSample}-bit ${profile.encoding.wireName}")
                InfoRow("Ses kaynağı", profile.source.name)
                InfoRow(
                    "İşlenmemiş zincir",
                    if (profile.source.name == "UNPROCESSED") "Aktif" else "Kullanılamıyor",
                    if (profile.source.name == "UNPROCESSED") SignalGreen else SignalAmber,
                )
            }
        }

        if (profile != null && profile.source.name != "UNPROCESSED") {
            Spacer(Modifier.height(12.dp))
            NoticeCard(
                icon = "⚠️",
                title = "Bu cihaz işlenmemiş kaydı desteklemiyor",
                body = "Platform gürültü engelleme ve otomatik kazanç uygulayabilir; bu, ölçülen " +
                    "sönümlenme eğrisini bozar. Mümkünse referans veri setini UNPROCESSED " +
                    "destekleyen bir cihazla toplayın. Kayıt yine de alınır ve bu durum " +
                    "metadata'ya işlenir.",
                accent = SignalAmber,
            )
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                onSave(name, deviceNote)
                onContinue()
            },
            enabled = name.isNotBlank() && profile != null,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                disabledContainerColor = TextGray.copy(alpha = 0.25f),
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Devam", color = DarkBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextGray, fontSize = 14.sp) },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = DarkSurface,
            unfocusedContainerColor = DarkSurface,
            focusedTextColor = White,
            unfocusedTextColor = White,
            cursorColor = GoldPrimary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(12.dp),
    )
}
