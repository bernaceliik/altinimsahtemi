package com.bernacelik.altinimsahtemi.ui.screens



import android.content.Intent

import android.net.Uri

import android.widget.Toast

import androidx.compose.foundation.background

import androidx.compose.foundation.border

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import androidx.compose.foundation.BorderStroke

import com.bernacelik.altinimsahtemi.ui.theme.*



@Composable

fun ApprovalPendingScreen(

    businessName: String = "Can kuyumculuk",

    taxNumber: String = "••••4821"

) {

    val context = LocalContext.current



    Column(

        modifier = Modifier

            .fillMaxSize()

            .background(DarkBackground)

            .padding(24.dp),

        verticalArrangement = Arrangement.SpaceBetween

    ) {

// Üst Kısım: İkon ve Bilgilendirme

        Column(

            modifier = Modifier

                .fillMaxWidth()

                .padding(top = 48.dp)

        ) {

// Kum Saati / Bekleme İkonu (Altın Sarısı Yuvarlak)

            Box(

                modifier = Modifier

                    .size(56.dp)

                    .background(GoldPrimary.copy(alpha = 0.15f), CircleShape),

                contentAlignment = Alignment.Center

            ) {

                Text("⏳", fontSize = 24.sp)

            }



            Spacer(modifier = Modifier.height(24.dp))



            Text(

                text = "Kuyumcu onayı bekleniyor",

                color = White,

                fontSize = 28.sp,

                fontWeight = FontWeight.Bold

            )



            Spacer(modifier = Modifier.height(12.dp))



            Text(

                text = "Ekibimiz işletme belgelerinizi inceliyor genelde 24 saat sürer.",

                color = TextGray,

                fontSize = 15.sp,

                lineHeight = 22.sp

            )



            Spacer(modifier = Modifier.height(40.dp))



// Durum Kartı (Figma tasarımındaki şık siyah alan)

            Card(

                colors = CardDefaults.cardColors(containerColor = DarkSurface),

                shape = RoundedCornerShape(16.dp),

                modifier = Modifier

                    .fillMaxWidth()

                    .border(1.dp, TextGray.copy(alpha = 0.1f), RoundedCornerShape(16.dp))

            ) {

                Column(

                    modifier = Modifier.padding(20.dp),

                    verticalArrangement = Arrangement.spacedBy(16.dp)

                ) {

// İşletme Adı Satırı

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {

                        Text("İşletme adı", color = TextGray, fontSize = 14.sp)

                        Text(businessName, color = White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

                    }



// Vergi No Satırı

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {

                        Text("Vergi no", color = TextGray, fontSize = 14.sp)

                        Text(taxNumber, color = White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

                    }



// Durum Satırı

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {

                        Text("Durum", color = TextGray, fontSize = 14.sp)

                        Text("İnceleniyor", color = GoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                    }

                }

            }

        }



// Alt Kısım: Bize Ulaşın Butonu (Destek)

        Button(

            onClick = {

// Destek e-postası gönderme aksiyonu tetiklenir

                val intent = Intent(Intent.ACTION_SENDTO).apply {

                    data = Uri.parse("mailto:destek@altinimsahtemi.com")

                    putExtra(Intent.EXTRA_SUBJECT, "Kuyumcu Onay Süreci Hakkında")

                }

                try {

                    context.startActivity(intent)

                } catch (e: Exception) {

                    Toast.makeText(context, "E-posta uygulaması bulunamadı. destek@altinimsahtemi.com adresinden bize yazabilirsiniz.", Toast.LENGTH_LONG).show()

                }

            },

            modifier = Modifier

                .fillMaxWidth()

                .height(56.dp)

                .padding(bottom = 16.dp),

            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),

            shape = RoundedCornerShape(12.dp),

            border = BorderStroke(1.dp, TextGray.copy(alpha = 0.2f))

        ) {

            Text(

                text = "Bize ulaşın",

                color = White,

                fontSize = 16.sp,

                fontWeight = FontWeight.Bold

            )

        }

    }

}