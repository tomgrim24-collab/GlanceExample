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
    private fun StockDisplay(stateCount: Float) {
        val color = if (PriceDataRepo.change > 0) {
            GlanceTheme.colors.primary
        } else {
            GlanceTheme.colors.error
        }

        val textStyle = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = PriceDataRepo.ticker,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = String.format(Locale.getDefault(), "%.2f", stateCount),
            style = textStyle
        )

        Text(
            text = "${PriceDataRepo.change} %",
            style = textStyle
        )
    }