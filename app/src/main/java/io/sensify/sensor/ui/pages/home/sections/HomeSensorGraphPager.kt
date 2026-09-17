package io.sensify.sensor.ui.pages.home.sections

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.graphics.ColorUtils
import io.sensify.sensor.ui.pages.home.HomeViewModel
import io.sensify.sensor.ui.pages.home.items.HomeSensorChartItem
import io.sensify.sensor.ui.resource.effects.drawColoredShadow
import io.sensify.sensor.ui.resource.values.JlResDimens
import kotlin.math.absoluteValue

@Preview(showBackground = true, backgroundColor = 0xFF041B11)
@Composable
fun HomeSensorGraphPager(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = HomeViewModel(),
    pagerState: PagerState = rememberPagerState(
        pageCount = { 3 }
    )
) {
    val activeSensorStateList = viewModel.mActiveSensorListFlow.collectAsState(initial = mutableListOf())

    LaunchedEffect(pagerState) {
        snapshotFlow { "${pagerState.currentPage} ${pagerState.pageCount}" }.collect { page ->
            Log.d("HomeSensorGraphPager", "pager 2: $page")
            viewModel.setActivePage(pagerState.currentPage)
        }
    }
    
    if (activeSensorStateList.value.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = JlResDimens.dp32, vertical = JlResDimens.dp20)
                .height(180.dp),
            shape = RoundedCornerShape(JlResDimens.dp28),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(JlResDimens.dp20),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No Active Sensors",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Toggle a sensor switch below to view live graphs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = JlResDimens.dp32),
            key = { index ->
                val list = activeSensorStateList.value
                if (index < list.size) list[index].type else index
            },
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = JlResDimens.dp20)
        ) { page ->
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

        Card(
            modifier = Modifier
                .graphicsLayer {
                    lerp(
                        start = 0.85f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    ).also { scale ->
                        scaleX = scale
                        scaleY = scale
                    }
                    alpha = lerp(
                        start = 0.5f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                }
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(
                                ColorUtils.blendARGB(
                                    MaterialTheme.colorScheme.onSurface.toArgb(),
                                    MaterialTheme.colorScheme.surface.toArgb(),
                                    0.8f
                                )
                            ),
                            Color(
                                ColorUtils.blendARGB(
                                    MaterialTheme.colorScheme.onSurface.toArgb(),
                                    MaterialTheme.colorScheme.surface.toArgb(),
                                    0.97f
                                )
                            ),
                        ),
                        center = Offset(200f, -30f),
                        radius = 600.0f
                    ), shape = RoundedCornerShape(JlResDimens.dp28)
                )
                .drawColoredShadow(
                    Color.Black,
                    offsetY = JlResDimens.dp12,
                    shadowRadius = JlResDimens.dp16,
                    borderRadius = JlResDimens.dp32,
                    alpha = 0.1f
                ),
            shape = RoundedCornerShape(JlResDimens.dp28),
            border = BorderStroke(
                brush = Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                    )
                ),
                width = JlResDimens.dp1,
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                val list = activeSensorStateList.value
                if (list.isNotEmpty() && page < list.size) {
                    HomeSensorChartItem(
                        list[page],
                        viewModel.getChartDataManager(list[page].type)
                    )
                }
            }
        }
    }
}
}
