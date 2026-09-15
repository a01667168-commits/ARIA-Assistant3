package com.aria.assistant.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.aria.assistant.ui.theme.AriaAccentCyan
import com.aria.assistant.ui.theme.AriaAccentViolet
import com.aria.assistant.ui.theme.AriaError

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel(), onBack: (() -> Unit)? = null) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.onMicClick() }

    fun onMicPressed() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.onMicClick()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
                StatusBar(statusText = uiState.statusText, coreState = uiState.coreState)
            }

            Spacer(Modifier.height(32.dp))
            AriaCore(state = uiState.coreState)

            Spacer(Modifier.height(24.dp))
            Text(
                text = "A.R.I.A.",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(24.dp))
            ConversationList(
                entries = uiState.conversation,
                modifier = Modifier.weight(1f)
            )

            MicButton(
                coreState = uiState.coreState,
                onClick = ::onMicPressed,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        }
    }
}

@Composable
private fun StatusBar(statusText: String, coreState: AriaCoreState) {
    val label = when (coreState) {
        AriaCoreState.IDLE -> statusText
        AriaCoreState.LISTENING -> "Escuchando…"
        AriaCoreState.PROCESSING -> "Procesando…"
        AriaCoreState.RESPONDING -> "Respondiendo…"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(AriaAccentCyan)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Núcleo visual animado de A.R.I.A. (identidad visual original: anillo
 * pulsante cian/violeta). Cambia de color e intensidad de pulso según
 * el estado real del VoiceEngine (Fase 3): más lento en reposo, más
 * rápido escuchando/procesando, y con tono distinto al responder.
 */
@Composable
private fun AriaCore(state: AriaCoreState) {
    val infiniteTransition = rememberInfiniteTransition(label = "aria_core")

    val pulseDurationMs = when (state) {
        AriaCoreState.IDLE -> 1800
        AriaCoreState.LISTENING -> 700
        AriaCoreState.PROCESSING -> 450
        AriaCoreState.RESPONDING -> 1000
    }

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDurationMs),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val coreColors = when (state) {
        AriaCoreState.IDLE -> listOf(AriaAccentCyan, AriaAccentViolet)
        AriaCoreState.LISTENING -> listOf(AriaAccentCyan, AriaAccentCyan)
        AriaCoreState.PROCESSING -> listOf(AriaAccentViolet, AriaAccentViolet)
        AriaCoreState.RESPONDING -> listOf(AriaAccentViolet, AriaAccentCyan)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            coreColors.first().copy(alpha = 0.35f),
                            coreColors.last().copy(alpha = 0.10f),
                            androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(coreColors))
        )
    }
}

@Composable
private fun ConversationList(entries: List<ConversationEntry>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(entries) { entry ->
            ConversationBubble(entry)
        }
    }
}

@Composable
private fun ConversationBubble(entry: ConversationEntry) {
    val alignment = if (entry.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (entry.isUser)
        MaterialTheme.colorScheme.surfaceVariant
    else
        MaterialTheme.colorScheme.surface

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Box(
            modifier = Modifier
                .background(bubbleColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = entry.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MicButton(coreState: AriaCoreState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val containerColor = if (coreState == AriaCoreState.LISTENING)
        AriaError
    else
        MaterialTheme.colorScheme.primary

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Filled.Mic, contentDescription = "Activar micrófono")
        }
    }
}
