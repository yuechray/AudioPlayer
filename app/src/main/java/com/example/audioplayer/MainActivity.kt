package com.example.audioplayer.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.audioplayer.ui.theme.AudioPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioPlayerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            SongListScreen { selectedSong ->
                navController.navigate("player/$selectedSong")
            }
        }
        composable(
            route = "player/{song}",
            arguments = listOf(navArgument("song") { type = NavType.StringType })
        ) { backStackEntry ->
            val songName = backStackEntry.arguments?.getString("song") ?: ""
            PlayerScreen(
                songName = songName,
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}