package com.bernacelik.altinimsahtemi.ui.screens



import androidx.compose.animation.core.*

import androidx.compose.foundation.background

import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import com.bernacelik.altinimsahtemi.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.delay

import androidx.compose.foundation.border



@Composable

fun AudioRecordScreen(

    material: String,

    objectType: String,

    surface: String,

    onNavigateBack: () -> Unit,

    onNavigateToReview: (duration: String, wavPath: String) -> Unit,

    onNavigateToProfile: () -> Unit // YENİ: Profil yönlendirme parametresi eklendi!

) {

    var isRecording by remember { mutableStateOf(false) }

    var seconds by remember { mutableStateOf(0) }

    var milliseconds by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var livePeakDbfs by remember { mutableStateOf(-120f) }
    var liveRmsDbfs by remember { mutableStateOf(-120f) }
    var liveNoiseFloorDbfs by remember { mutableStateOf(-120f) }
    var captureEngineJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    var hasPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }



// Kronometre Sayacı

    LaunchedEffect(isRecording) {

        if (isRecording) {

            while (isRecording) {

                delay(10)

                milliseconds += 10

                if (milliseconds >= 1000) {

                    seconds++

                    milliseconds = 0

                }

            }

        }

    }



    val formattedTime = String.format("%02d:%02d:%02d", seconds / 60, seconds % 60, milliseconds / 10)



// Canlı Ses Dalgası Simülasyonu için Değerler

    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val waveHeight1 by infiniteTransition.animateFloat(

        initialValue = 15f, targetValue = if (isRecording) 50f else 15f,

        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse), label = "h1"

    )

    val waveHeight2 by infiniteTransition.animateFloat(

        initialValue = 25f, targetValue = if (isRecording) 70f else 25f,

        animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse), label = "h2"

    )



    Column(

        modifier = Modifier

            .fillMaxSize()

            .background(DarkBackground)

            .padding(horizontal = 24.dp)

    ) {

// Üst Bar

        Row(

            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp),

            horizontalArrangement = Arrangement.SpaceBetween,

            verticalAlignment = Alignment.CenterVertically

        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(

                    text = "←", color = White, fontSize = 24.sp,

                    modifier = Modifier.clickable { onNavigateBack() }.padding(end = 16.dp)

                )

                Text("AudioCollect", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            }



// MODİFİKASYON: Profil butonuna tıklanınca artık profil ekranına gidecek!

            Box(

                modifier = Modifier

                    .size(36.dp)

                    .background(GoldPrimary, CircleShape)

                    .clickable { onNavigateToProfile() }, // Tıklama aksiyonu eklendi

                contentAlignment = Alignment.Center

            ) {

                Text("C", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)

            }

        }



// Adım Çizgisi (Adım 3/3)

        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {

            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(TextGray.copy(alpha = 0.2f))) {

                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().background(GoldPrimary))

            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Adım 3/3 • Kayıt", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)

        }



        Spacer(modifier = Modifier.height(20.dp))



// Seçilen Parametrelerin Rozetleri (Chips)

        Row(

            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement = Arrangement.spacedBy(8.dp),

            verticalAlignment = Alignment.CenterVertically

        ) {

            BadgeChip(text = material)

            BadgeChip(text = objectType)

            BadgeChip(text = surface)

        }



        Spacer(modifier = Modifier.height(24.dp))



// Ses Dalgası Paneli (Waveform Box)

        val visualHeight = remember(livePeakDbfs, isRecording) {
            if (!isRecording) 15f
            else {
                val db = livePeakDbfs.coerceIn(-60f, 0f)
                val fraction = (db + 60f) / 60f
                (15f + fraction * 100f)
            }
        }

        Box(

            modifier = Modifier

                .fillMaxWidth()

                .height(180.dp)

                .background(DarkSurface, RoundedCornerShape(16.dp)),

            contentAlignment = Alignment.Center

        ) {

            Row(

                horizontalArrangement = Arrangement.spacedBy(6.dp),

                verticalAlignment = Alignment.CenterVertically

            ) {

                listOf(visualHeight * 0.6f, visualHeight * 0.8f, visualHeight, visualHeight * 0.7f, visualHeight * 0.4f).forEach { height ->

                    Box(

                        modifier = Modifier

                            .size(6.dp, height.dp)

                            .background(GoldPrimary, RoundedCornerShape(3.dp))

                    )

                }

            }

        }



        Spacer(modifier = Modifier.height(16.dp))

        Text(

            text = "Telefonu 45° açıyla, 15 cm uzaklıkta tutun",

            color = TextGray, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center

        )

        Text(

            text = "15 CM • 45° AÇI • CH-1 STEREO",

            color = TextGray.copy(alpha = 0.6f), fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center

        )



        Spacer(modifier = Modifier.height(32.dp))



// Kronometre

        Text(

            text = formattedTime,

            color = White, fontSize = 48.sp, fontWeight = FontWeight.Bold,

            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center

        )



        Spacer(modifier = Modifier.height(8.dp))

        Text(

            text = "• CANLI GİRİŞ • KAZANÇ +12dB • 44.1kHz",

            color = TextGray, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center

        )



        Spacer(modifier = Modifier.weight(1f))



// Büyük Kayıt Tetikleme Butonu

        Column(

            modifier = Modifier.fillMaxWidth(),

            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            Box(

                modifier = Modifier

                    .size(110.dp)

                    .background(GoldPrimary, CircleShape)

                    .clickable {

                        if (isRecording) {
                            isRecording = false
                            captureEngineJob?.cancel()
                            val mockWav = java.io.File(context.cacheDir, "temp_capture.wav")
                            if (!mockWav.exists()) {
                                com.bernacelik.altinimsahtemi.audio.WavWriter.write(
                                    file = mockWav,
                                    samples = FloatArray(1024),
                                    sampleRate = 44100,
                                    encoding = com.bernacelik.altinimsahtemi.audio.PcmEncoding.PCM_16,
                                    channelCount = 1
                                )
                            }
                            val encodedPath = android.net.Uri.encode(mockWav.absolutePath)
                            onNavigateToReview(formattedTime, encodedPath)
                        } else {
                            if (!hasPermission) {
                                permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                return@clickable
                            }
                            seconds = 0
                            milliseconds = 0
                            isRecording = true
                            
                            val profile = com.bernacelik.altinimsahtemi.audio.AudioCapabilities.detectBestProfile(context)
                            if (profile != null) {
                                val engine = com.bernacelik.altinimsahtemi.audio.CaptureEngine(profile)
                                captureEngineJob = coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        engine.events().collect { event ->
                                            when (event) {
                                                is com.bernacelik.altinimsahtemi.audio.CaptureEvent.Level -> {
                                                    livePeakDbfs = event.peakDbfs
                                                    liveRmsDbfs = event.rmsDbfs
                                                    liveNoiseFloorDbfs = event.noiseFloorDbfs
                                                }
                                                is com.bernacelik.altinimsahtemi.audio.CaptureEvent.Impact -> {
                                                    val impact = event.capture
                                                    val wavFile = java.io.File(context.cacheDir, "temp_capture.wav")
                                                    com.bernacelik.altinimsahtemi.audio.WavWriter.write(
                                                        file = wavFile,
                                                        samples = impact.samples,
                                                        sampleRate = impact.profile.sampleRate,
                                                        encoding = impact.profile.encoding,
                                                        channelCount = 1
                                                    )
                                                    
                                                    coroutineScope.launch(Dispatchers.Main) {
                                                        isRecording = false
                                                        captureEngineJob?.cancel()
                                                        val encodedPath = android.net.Uri.encode(wavFile.absolutePath)
                                                        onNavigateToReview(formattedTime, encodedPath)
                                                    }
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            } else {
                                android.widget.Toast.makeText(context, "Kayıt donanımı başlatılamadı.", android.widget.Toast.LENGTH_SHORT).show()
                                isRecording = false
                            }
                        }

                    },

                contentAlignment = Alignment.Center

            ) {

// Ortadaki Yuvarlak / Kare İkon

                Box(

                    modifier = Modifier

                        .size(32.dp)

                        .background(DarkBackground, if (isRecording) RoundedCornerShape(6.dp) else CircleShape)

                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(

                text = if (isRecording) "KAYDI DURDURMAK İÇİN DOKUN" else "KAYDA BAŞLAMAK İÇİN DOKUN",

                color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp

            )

        }



        Spacer(modifier = Modifier.height(80.dp))

    }

}



@Composable

fun BadgeChip(text: String) {

    Box(

        modifier = Modifier

            .background(DarkSurface, RoundedCornerShape(8.dp))

            .border(1.dp, TextGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))

            .padding(horizontal = 16.dp, vertical = 8.dp)

    ) {

        Text(text = text, color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)

    }

}