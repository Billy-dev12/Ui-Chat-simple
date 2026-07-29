package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.ui.theme.OnlineGreen

@Composable
fun UserAvatar(
    user: User,
    size: Dp = 48.dp,
    showOnlineStatus: Boolean = true,
    hasStoryRing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val avatarBgColor = Color(user.avatarColorHex)
    
    val ringModifier = if (hasStoryRing) {
        modifier.border(
            width = 2.dp,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF10B981), Color(0xFF38BDF8), Color(0xFFA855F7))
            ),
            shape = CircleShape
        ).padding(3.dp)
    } else {
        modifier
    }

    Box(
        modifier = ringModifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(avatarBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.avatarInitials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.38f).sp
            )
        }

        if (showOnlineStatus && user.isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .clip(CircleShape)
                    .background(OnlineGreen)
            )
        }
    }
}
