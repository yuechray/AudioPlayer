package com.example.audioplayer.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(onSongClick: (String) -> Unit) {
    val context = LocalContext.current

    // Получаем список всех файлов в папке raw
    val songsList = remember { getSongsFromRaw(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Аудиоплеер") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (songsList.isEmpty()) {
                EmptyLibraryMessage()
            } else {
                Text(
                    "Выберите песню для воспроизведения",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(songsList) { song ->
                        SongListItem(song, onSongClick)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListItem(song: String, onSongClick: (String) -> Unit) {
    Card(
        onClick = { onSongClick(song) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = formatSongName(song),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyLibraryMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Песни не найдены!",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Добавьте MP3 файлы в папку res/raw/ вашего проекта",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

// Функция для получения списка песен из папки raw
fun getSongsFromRaw(context: Context): List<String> {
    // Получаем список всех ресурсов из папки raw
    val packageName = context.packageName

    val rawClass = try {
        Class.forName("$packageName.R\$raw")
    } catch (e: ClassNotFoundException) {
        return emptyList()
    }

    return rawClass.fields
        .map { it.name }
        .filter { !it.startsWith("__") } // Исключаем системные ресурсы
}

// Функция для форматирования имени песни
fun formatSongName(name: String): String {
    return name.replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            if (word.isNotEmpty()) word.replaceFirstChar { it.uppercase() } else ""
        }
}