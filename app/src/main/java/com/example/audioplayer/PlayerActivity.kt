package com.example.audioplayer.ui

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(songName: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }
    var volume by remember { mutableStateOf(0.7f) }

    val coroutineScope = rememberCoroutineScope()

    // Получаем идентификатор ресурса
    val resId = getResourceId(context, songName, "raw")

    // Обновляем прогресс воспроизведения
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                mediaPlayer?.let { player ->
                    currentPosition = player.currentPosition
                }
                delay(1000)
            }
        }
    }

    DisposableEffect(songName) {
        try {
            if (resId != 0) {
                val player = MediaPlayer.create(context, resId)
                mediaPlayer = player
                duration = player.duration
                player.setVolume(volume, volume)

                // Добавляем слушатель завершения воспроизведения
                player.setOnCompletionListener {
                    isPlaying = false
                    currentPosition = 0
                    Log.d("AudioPlayer", "Воспроизведение завершено")
                }

                Log.d("AudioPlayer", "MediaPlayer успешно создан для $songName, ресурс ID: $resId")
            } else {
                Log.e("AudioPlayer", "Ресурс не найден: $songName")
                errorMessage = "Аудиофайл '$songName' не найден. Убедитесь, что файл находится в папке res/raw/"
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Ошибка при создании MediaPlayer", e)
            errorMessage = "Ошибка при запуске плеера: ${e.message}"
        }

        onDispose {
            try {
                mediaPlayer?.release()
                Log.d("AudioPlayer", "MediaPlayer освобожден")
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Ошибка при освобождении MediaPlayer", e)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аудиоплеер") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Сейчас играет:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        formatSongName(songName),
                        fontSize = 22.sp,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Прогресс воспроизведения
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(currentPosition))
                    Text(formatTime(duration))
                }

                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = { newPosition ->
                        currentPosition = newPosition.toInt()
                    },
                    onValueChangeFinished = {
                        coroutineScope.launch {
                            try {
                                mediaPlayer?.seekTo(currentPosition)
                            } catch (e: Exception) {
                                Log.e("AudioPlayer", "Ошибка при перемотке", e)
                            }
                        }
                    },
                    valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Управление воспроизведением
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        try {
                            mediaPlayer?.start()
                            isPlaying = true
                        } catch (e: Exception) {
                            Log.e("AudioPlayer", "Ошибка при воспроизведении", e)
                            errorMessage = "Ошибка при воспроизведении: ${e.message}"
                        }
                    },
                    enabled = mediaPlayer != null && !isPlaying,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("▶ Воспроизвести")
                }

                Button(
                    onClick = {
                        try {
                            mediaPlayer?.pause()
                            isPlaying = false
                        } catch (e: Exception) {
                            Log.e("AudioPlayer", "Ошибка при постановке на паузу", e)
                        }
                    },
                    enabled = mediaPlayer != null && isPlaying,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("⏸ Пауза")
                }

                Button(
                    onClick = {
                        try {
                            mediaPlayer?.stop()
                            mediaPlayer?.prepare()
                            currentPosition = 0
                            isPlaying = false
                        } catch (e: Exception) {
                            Log.e("AudioPlayer", "Ошибка при остановке", e)
                            errorMessage = "Ошибка при остановке: ${e.message}"
                        }
                    },
                    enabled = mediaPlayer != null && isPlaying,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("⏹ Стоп")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Управление громкостью
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Громкость",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeDown,
                            contentDescription = "Тихо",
                            modifier = Modifier.size(24.dp)
                        )

                        Slider(
                            value = volume,
                            onValueChange = { newVolume ->
                                volume = newVolume
                                mediaPlayer?.setVolume(volume, volume)
                            },
                            valueRange = 0f..1f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Громко",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// Функция для форматирования времени (миллисекунды в формат MM:SS)
fun formatTime(timeMs: Int): String {
    val seconds = (timeMs / 1000) % 60
    val minutes = (timeMs / (1000 * 60))
    return String.format("%02d:%02d", minutes, seconds)
}

// Функция для получения идентификатора ресурса по имени
fun getResourceId(context: Context, resourceName: String, resourceType: String): Int {
    return context.resources.getIdentifier(resourceName, resourceType, context.packageName)
}