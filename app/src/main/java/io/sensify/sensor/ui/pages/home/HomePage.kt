package io.sensify.sensor.ui.pages.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.sensify.sensor.R
import io.sensify.sensor.ui.components.cards.ModernSensorCard
import io.sensify.sensor.ui.composables.isScrollingUp
import io.sensify.sensor.ui.navigation.NavDirectionsApp
import io.sensify.sensor.ui.pages.home.sections.HomeHeader
import io.sensify.sensor.ui.pages.home.sections.HomeSensorGraphPager
import io.sensify.sensor.ui.resource.values.JlResTxtStyles
import kotlinx.coroutines.launch

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory())
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val sensorUiState = viewModel.mUiState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val activeSensors = viewModel.mActiveSensorListFlow.collectAsState()
    val filteredSensors by viewModel.filteredSensors.collectAsState()
    val pagerState = rememberPagerState(pageCount = { activeSensors.value.size })

    val isAtTop = remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = if (!isAtTop.value) MaterialTheme.colorScheme.surface.copy(alpha = 0.85f) else Color.Transparent
                ),
                navigationIcon = {
                    Box(Modifier.padding(horizontal = 20.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.pic_sensify_logo),
                            modifier = Modifier
                                .width(32.dp)
                                .height(36.dp),
                            contentDescription = "Sensify Logo",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                },
                title = {
                    Text(
                        text = "Sensify",
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        style = JlResTxtStyles.h4,
                        fontWeight = FontWeight.SemiBold,
                        modifier = modifier.fillMaxWidth()
                    )
                },
                actions = {
                    Box(Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.width(32.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = lazyListState.isScrollingUp(),
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(
                    onClick = { navController?.navigate(NavDirectionsApp.AboutPage.route) },
                    shape = RoundedCornerShape(50.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .border(
                            brush = Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                )
                            ),
                            width = 1.dp,
                            shape = RoundedCornerShape(50.dp)
                        )
                ) {
                    Icon(Icons.Rounded.Info, contentDescription = "About")
                }
            }
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val contentHorizontalPadding = if (maxWidth > 600.dp) (maxWidth - 560.dp) / 2 else 20.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                            )
                        )
                    ),
                state = lazyListState
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Top Header: Active Sensor Carousel Header
                item {
                    Box(
                        modifier = Modifier.padding(horizontal = contentHorizontalPadding)
                    ) {
                        HomeHeader(
                            sensorUiState.value.currentSensor,
                            totalActive = sensorUiState.value.activeSensorCounts,
                            onClickArrow = { isLeft ->
                                val currentPage = pagerState.currentPage
                                val totalPage = pagerState.pageCount

                                if (!isLeft && currentPage + 1 < totalPage) {
                                    coroutineScope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                                } else if (isLeft && currentPage > 0 && totalPage > 0) {
                                    coroutineScope.launch { pagerState.animateScrollToPage(currentPage - 1) }
                                }
                            }
                        )
                    }
                }

                // Top Waveform / Chart Visualizer Pager
                item {
                    HomeSensorGraphPager(viewModel = viewModel, pagerState = pagerState)
                }

                // Category Filter Chips
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = contentHorizontalPadding, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Telemetry & Visualizers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(SensorCategory.entries.toTypedArray()) { cat ->
                                val isSelected = cat == selectedCategory
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { viewModel.setCategory(cat) }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = cat.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Modern Interactive Sensor Cards
                items(filteredSensors) { sensor ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = contentHorizontalPadding, vertical = 6.dp)
                    ) {
                        val liveVal = viewModel.liveSensorValues[sensor.type]
                        ModernSensorCard(
                            modelSensor = sensor,
                            liveValues = liveVal,
                            onClick = { type ->
                                navController?.navigate("${NavDirectionsApp.SensorDetailPage.route}/$type")
                            },
                            onCheckChange = { type, isChecked ->
                                viewModel.onSensorChecked(type, isChecked)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}
