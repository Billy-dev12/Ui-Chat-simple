package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Message
import com.example.model.MessageStatus
import com.example.model.MessageType
import com.example.ui.theme.IncomingBubbleDark
import com.example.ui.theme.IncomingBubbleLight
import com.example.ui.theme.OutgoingBubbleDark
import com.example.ui.theme.OutgoingBubbleLight
import com.example.ui.theme.SentCheckColor

@Composable
fun MessageBubbleItem(
    message: Message,
    isMe: Boolean,
    isPlayingAudio: Boolean,
    onPlayAudioToggle: () -> Unit,
    onLongClick: () -> Unit,
    onSwipeReply: () -> Unit
) {
    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val bubbleColor = if (isMe) {
        if (isDark) OutgoingBubbleDark else OutgoingBubbleLight
    } else {
        if (isDark) IncomingBubbleDark else IncomingBubbleLight
    }

    val textColor = if (isMe) {
        if (isDark) Color(0xFFD6E3FF) else MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Box {
            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .clip(bubbleShape)
                    .clickable { onLongClick() }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Reply preview box inside bubble
                    if (!message.replyToText.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Column {
                                Text(
                                    text = message.replyToSender ?: "Pesan",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMe) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = message.replyToText ?: "",
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Content by type
                    when (message.type) {
                        MessageType.TEXT -> {
                            Text(
                                text = message.text,
                                fontSize = 15.sp,
                                color = textColor,
                                lineHeight = 21.sp
                            )
                        }

                        MessageType.VOICE -> {
                            VoiceNotePlayerContent(
                                durationSec = message.voiceDurationSeconds,
                                isPlaying = isPlayingAudio,
                                isMe = isMe,
                                onPlayToggle = onPlayAudioToggle
                            )
                        }

                        else -> {
                            Text(
                                text = message.text,
                                fontSize = 15.sp,
                                color = textColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Timestamp & Delivery status
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message.formattedTime,
                            fontSize = 11.sp,
                            color = if (isMe) textColor.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )

                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            val (icon, color) = when (message.status) {
                                MessageStatus.SENDING -> Icons.Default.Done to textColor.copy(alpha = 0.5f)
                                MessageStatus.SENT -> Icons.Default.Done to textColor.copy(alpha = 0.8f)
                                MessageStatus.DELIVERED -> Icons.Default.DoneAll to textColor.copy(alpha = 0.8f)
                                MessageStatus.READ -> Icons.Default.DoneAll to SentCheckColor
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = "Status",
                                tint = color,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Reaction Chip on bottom corner
            if (message.reaction != null) {
                Box(
                    modifier = Modifier
                        .align(if (isMe) Alignment.BottomStart else Alignment.BottomEnd)
                        .padding(top = 16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = message.reaction,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceNotePlayerContent(
    durationSec: Int,
    isPlaying: Boolean,
    isMe: Boolean,
    onPlayToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        IconButton(
            onClick = onPlayToggle,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isMe) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play voice note",
                tint = if (isMe) Color.White else MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Waveform bars
        val infiniteTransition = rememberInfiniteTransition(label = "wave")
        val waveAnim by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wave"
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.weight(1f)
        ) {
            val barHeights = listOf(12, 20, 8, 24, 16, 28, 14, 22, 10, 18, 26, 12)
            barHeights.forEachIndexed { index, h ->
                val activeHeight = if (isPlaying) (h * (if (index % 2 == 0) waveAnim else (1.2f - waveAnim))).dp else (h * 0.7f).dp
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(activeHeight)
                        .clip(CircleShape)
                        .background(
                            if (isMe) Color.White.copy(alpha = if (index < 6) 1.0f else 0.5f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = if (index < 6) 1.0f else 0.4f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "0:${if (durationSec < 10) "0$durationSec" else durationSec}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isMe) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ReplyPreviewBanner(
    replyingMessage: Message,
    onCancel: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Membalas pesan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = replyingMessage.text.ifEmpty { "Pesan Suara / Media" },
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Batal Balas",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun QuickReactionPicker(
    onSelectReaction: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val reactions = listOf("❤️", "👍", "🔥", "😂", "😮", "🙏")
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
        modifier = Modifier.padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            reactions.forEach { emoji ->
                Text(
                    text = emoji,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            onSelectReaction(emoji)
                            onDismiss()
                        }
                        .padding(4.dp)
                )
            }
        }
    }
}
