package com.example.glanceexample.glance

import android.content.Context
import android.glance.GlanceId
import android.glance.GlanceModifier
import android.glance.GlanceTheme
import android.glance.appwidget.GlanceAppWidget
import android.glance.appwidget.provideContent
import android.glance.background
import android.glance.layout.Column
import android.glance.layout.fillMaxSize
import android.glance.layout.padding
import android.glance.text.Text
import android.glance.unit.dp
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StockAppWidget : GlanceAppWidget() {


    private var job: Job? = null

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        job?.cancel()


        job = CoroutineScope(Dispatchers.IO).launch {
            while (true) {

                PriceDataRepo.update()

                delay(20000)
            }
        }


        provideContent {
            GlanceTheme {
                GlanceContent()
            }
        }
    }

    @Composable
    fun GlanceContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(8.dp)
        ) {
            Text("Загрузка...")
        }
    }
}