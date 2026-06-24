package com.example.glanceexample.glance

import android.content.Context
import android.glance.GlanceId
import android.glance.GlanceModifier
import android.glance.GlanceTheme
import android.glance.ImageProvider
import android.glance.LocalSize
import android.glance.action.clickable
import android.glance.appwidget.GlanceAppWidget
import android.glance.appwidget.provideContent
import android.glance.background
import android.glance.layout.Alignment
import android.glance.layout.Column
import android.glance.layout.DpSize
import android.glance.layout.Image
import android.glance.layout.fillMaxSize
import android.glance.layout.fillMaxWidth
import android.glance.layout.padding
import android.glance.layout.sizeMode.SizeMode
import android.glance.layout.cornerRadius
import android.glance.text.FontWeight
import android.glance.text.Text
import android.glance.text.TextStyle
import android.glance.unit.dp
import android.glance.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Класс виджета Glance для отображения цен на акции GOOGL
 * Поддерживает малый и средний размер с адаптивным интерфейсом
 *
 * Функционал:
 * - Отображение тикера, цены и процента изменения
 * - Автоматическое обновление каждые 20 секунд
 * - Обновление по клику на виджет
 * - Адаптивный дизайн (малый и средний размер)
 * - Цветовая индикация (зелёный/красный)
 * - Отображение стрелок в среднем режиме
 */
class StockAppWidget : GlanceAppWidget() {


    // Job для фонового обновления данных
    // Позволяет запускать, отслеживать и отменять фоновые операции
    private var job: Job? = null


    companion object {
        private val smallMode = DpSize(100.dp, 80.dp)   // Малый размер (2x1 ячейки)
        private val mediumMode = DpSize(120.dp, 120.dp) // Средний размер (2x2 ячейки)
    }


    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(smallMode, mediumMode)
    )


    /**
     * Основной метод, вызываемый при создании или обновлении виджета
     *
     * @param context Контекст приложения
     * @param id Уникальный идентификатор виджета
     */
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Отменяем предыдущий Job, если он был
        job?.cancel()

        // Запускаем фоновое обновление цен каждые 20 секунд
        job = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                PriceDataRepo.update()  // Обновляем данные
                delay(20000)            // Ждём 20 секунд
            }
        }

        // Предоставляем контент виджета
        provideContent {
            GlanceTheme {
                GlanceContent()
            }
        }
    }


    /**
     * Основной контент виджета с логикой выбора размера
     */
    @Composable
    fun GlanceContent() {
        // Получаем текущую цену из репозитория
        val stateCount by PriceDataRepo.currentPrice.collectAsState()

        // Определяем текущий размер виджета
        val size = LocalSize.current

        // Выбираем макет в зависимости от размера
        when (size) {
            smallMode -> Small(stateCount)    // Малый размер
            mediumMode -> Medium(stateCount)  // Средний размер
        }
    }


    /**
     * Метод для обновления цены при клике на виджет
     * Вызывается при нажатии пользователя на виджет
     */
    private fun refreshPrice() {
        PriceDataRepo.update()
    }


    /**
     * Отображение данных о цене акции
     *
     * @param stateCount Текущая цена
     */
    @Composable
    private fun StockDisplay(stateCount: Float) {

        // Определяем цвет в зависимости от изменения цены
        val color = if (PriceDataRepo.change > 0) {
            GlanceTheme.colors.primary    // Зелёный (рост)
        } else {
            GlanceTheme.colors.error      // Красный (падение)
        }


        // Стиль текста для цены и процента
        val textStyle = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )

        // Стиль для тикера
        val tickerStyle = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )


        // Отображаем данные в столбец
        Column(
            modifier = GlanceModifier.fillMaxWidth()
        ) {
            // Тикер (GOOGL)
            Text(
                text = PriceDataRepo.ticker,
                style = tickerStyle
            )

            // Текущая цена
            Text(
                text = String.format(Locale.getDefault(), "%.2f", stateCount),
                style = textStyle
            )

            // Изменение в процентах
            Text(
                text = "${PriceDataRepo.change}%",
                style = textStyle
            )
        }
    }

    // ============================================================
    // 📱 МАКЕТ ДЛЯ МАЛОГО РАЗМЕРА
    // ============================================================
    /**
     * Макет для малого размера виджета (2x1 ячейки)
     *
     * @param stateCount Текущая цена
     */
    @Composable
    private fun Small(stateCount: Float) {
        Column(
            modifier = GlanceModifier

                .clickable {
                    refreshPrice()  // ← При клике обновляем цену
                }
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(8.dp)
        ) {
            StockDisplay(stateCount)
        }
    }


    /**
     * Макет для среднего размера виджета (2x2 ячейки)
     *
     * @param stateCount Текущая цена
     */
    @Composable
    private fun Medium(stateCount: Float) {
        Column(
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            modifier = GlanceModifier

                .clickable {
                    refreshPrice()  // ← При клике обновляем цену
                }
                .fillMaxSize()
                .cornerRadius(15.dp)
                .background(GlanceTheme.colors.background)
                .padding(8.dp)
        ) {
            // Отображаем данные о цене
            StockDisplay(stateCount)


            Image(
                provider = ImageProvider(
                    if (PriceDataRepo.change > 0)
                        R.drawable.up_arrow      // Зелёная стрелка вверх (рост)
                    else
                        R.drawable.down_arrow    // Красная стрелка вниз (падение)
                ),
                contentDescription = if (PriceDataRepo.change > 0)
                    "Цена растёт"
                else
                    "Цена падает",
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(20.dp)
            )
        }
    }
}