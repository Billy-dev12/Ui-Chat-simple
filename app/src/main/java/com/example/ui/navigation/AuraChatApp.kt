package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.ConnectScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WelcomeNameScreen
import com.example.ui.theme.AuraChatTheme
import com.example.viewmodel.ChatViewModel
import com.example.viewmodel.Screen

@Composable
fun AuraChatApp(viewModel: ChatViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val chatThreads by viewModel.chatThreads.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val connectionError by viewModel.connectionError.collectAsState()

    val totalUnread = chatThreads.sumOf { it.unreadCount }

    AuraChatTheme(themeMode = themeMode) {
        val showBottomNav = currentScreen !is Screen.ChatDetail
                && currentScreen !is Screen.WelcomeName
                && currentScreen !is Screen.Connect

        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        NavigationBarItem(
                            selected = currentScreen is Screen.ChatList,
                            onClick = { viewModel.navigateTo(Screen.ChatList) },
                            icon = {
                                if (totalUnread > 0) {
                                    BadgedBox(badge = {
                                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                                            Text(totalUnread.toString(), fontWeight = FontWeight.Bold)
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (currentScreen is Screen.ChatList) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                            contentDescription = "Pesan"
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (currentScreen is Screen.ChatList) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                        contentDescription = "Pesan"
                                    )
                                }
                            },
                            label = {
                                Text(
                                    "Pesan",
                                    fontSize = 12.sp,
                                    fontWeight = if (currentScreen is Screen.ChatList) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color(0xFF3F474E)
                            )
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.Contacts,
                            onClick = { viewModel.navigateTo(Screen.Contacts) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is Screen.Contacts) Icons.Filled.People else Icons.Outlined.PeopleOutline,
                                    contentDescription = "Kontak"
                                )
                            },
                            label = {
                                Text(
                                    "Kontak",
                                    fontSize = 12.sp,
                                    fontWeight = if (currentScreen is Screen.Contacts) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color(0xFF3F474E)
                            )
                        )

                        NavigationBarItem(
                            selected = currentScreen is Screen.Settings,
                            onClick = { viewModel.navigateTo(Screen.Settings) },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen is Screen.Settings) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Pengaturan"
                                )
                            },
                            label = {
                                Text(
                                    "Pengaturan",
                                    fontSize = 12.sp,
                                    fontWeight = if (currentScreen is Screen.Settings) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color(0xFF3F474E)
                            )
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        is Screen.WelcomeName -> {
                            WelcomeNameScreen(
                                onNameEntered = { newName ->
                                    viewModel.saveAndSetUserName(newName)
                                }
                            )
                        }

                        is Screen.Connect -> {
                            val savedName = viewModel.currentUser.collectAsState().value.name
                            val savedIp by viewModel.savedIp.collectAsState()
                            val savedPort by viewModel.savedPort.collectAsState()
                            val hasSavedIp = !savedIp.isNullOrBlank()
                            ConnectScreen(
                                connectionState = connectionState,
                                savedName = savedName,
                                savedIp = savedIp,
                                savedPort = savedPort,
                                onConnect = { ip, port, name ->
                                    viewModel.connectToServer(ip, port, name)
                                },
                                onBack = if (hasSavedIp) {
                                    { viewModel.navigateTo(Screen.ChatList) }
                                } else null,
                                errorMessage = connectionError
                            )
                        }

                        is Screen.ChatList -> {
                            ChatListScreen(
                                viewModel = viewModel,
                                onOpenChat = { id -> viewModel.openChat(id) },
                                onOpenSettings = { viewModel.navigateTo(Screen.Settings) },
                                onOpenContacts = { viewModel.navigateTo(Screen.Contacts) }
                            )
                        }

                        is Screen.ChatDetail -> {
                            ChatDetailScreen(
                                chatId = screen.chatId,
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo(Screen.ChatList) }
                            )
                        }

                        is Screen.Contacts -> {
                            BackHandler {
                                viewModel.navigateTo(Screen.ChatList)
                            }
                            ContactsScreen(
                                viewModel = viewModel,
                                onOpenChatWithPartnerId = { id -> viewModel.openChat(id) }
                            )
                        }

                        is Screen.Settings -> {
                            BackHandler {
                                viewModel.navigateTo(Screen.ChatList)
                            }
                            SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
