package io.sensify.sensor.ui.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.sensify.sensor.R
import io.sensify.sensor.ui.navigation.NavDirectionsApp
import io.sensify.sensor.ui.resource.values.JlResShapes
import io.sensify.sensor.ui.resource.values.JlResTxtStyles
import kotlinx.coroutines.delay

@Composable
fun SplashPage(navController: NavController) {

    LaunchedEffect(key1 = true) {


        delay(timeMillis = 500)

        navController.navigate(NavDirectionsApp.HomePage.route) {
            popUpTo(NavDirectionsApp.Splash.route) {
                inclusive = true
            }
        }

    }
    SplashScreen()

}

@OptIn(ExperimentalTextApi::class)
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,

) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),

                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f),
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.pic_logo),
                contentDescription = "Logotipo Splash Screen",
                modifier = modifier
                    .size(120.dp)
//                    .scale(scale = scaleAnimation.value),
            )
            Spacer(modifier = JlResShapes.Space.H24)
            Image(
                painter = painterResource(id = R.drawable.pic_launcher_eye),
                contentDescription = "Logotipo Splash Screen",
                modifier = modifier
                    .size(220.dp)
//                    .scale(scale = scaleAnimation.value),
            )
            Spacer(modifier = JlResShapes.Space.H56)
            Text(
                text = "Sensify",
                style = JlResTxtStyles.h1.merge(
                    other = TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.onSurface,
                                MaterialTheme.colorScheme.onSurface.copy(0.1f)
                            ),
                            tileMode = TileMode.Mirror,
                            start = Offset(0f, 0f),
                            end = Offset(0f, Float.POSITIVE_INFINITY),
                        )
                    )
                ),

            )
        }
    }
}