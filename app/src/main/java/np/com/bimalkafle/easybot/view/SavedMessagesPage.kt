package np.com.bimalkafle.easybot.view

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import np.com.bimalkafle.easybot.LocalNavController
import np.com.bimalkafle.easybot.model.MessageModel
import np.com.bimalkafle.easybot.ui.theme.Purple80
import np.com.bimalkafle.easybot.viewModel.ChatViewModel
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun SavedMessagesPage(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    navController: NavHostController
) {
    val messages by viewModel.favoriteList.collectAsState()
    val savedMessages = messages.filter { it.isFavorite && it.message.isNotBlank() }
    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var multiSelectMode by remember { mutableStateOf(false) }
    val selectedMessages = remember { mutableStateListOf<String>() }

    Column(modifier = Modifier.fillMaxSize()) {
        SavedHeader(
            onBack = { navController.popBackStack() },
            multiSelectMode = multiSelectMode,
            hasSelection = selectedMessages.isNotEmpty(),
            selectedMessagesIds = selectedMessages.toList(),
            savedMessages = savedMessages,
            onMultiSelectToggle = { multiSelectMode = !multiSelectMode },
            navController = navController
        )

        if (savedMessages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved messages yet",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(savedMessages, key = { it.id }) { message ->
                    var isVisible by remember { mutableStateOf(true) }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut(animationSpec = tween(400)) + slideOutVertically(targetOffsetY = { it / 2 })
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {

                            SavedMessageItem(
                                messageModel = message,
                                showCheckbox = multiSelectMode,
                                isSelected = selectedMessages.contains(message.id),
                                onCheckedChange = { checked ->
                                    if (checked) selectedMessages.add(message.id)
                                    else selectedMessages.remove(message.id)
                                }
                            )

                            if (!multiSelectMode) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp, start = 12.dp, end = 12.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = {
                                        isVisible = false
                                        coroutineScope.launch {
                                            delay(400)
                                            viewModel.deleteFavorite(message)
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete saved",
                                            tint = Color.Red
                                        )
                                    }

                                    IconButton(onClick = {
                                        navController.navigate("bluetooth?message=${Uri.encode(message.message)}")
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = Purple80
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = multiSelectMode && selectedMessages.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                BulkShareBar(
                    onBulkShare = {
                        val messagesToSend = savedMessages
                            .filter { selectedMessages.contains(it.id) }
                            .joinToString(separator = "\n") { it.message }

                        navController.navigate("bluetooth?message=${Uri.encode(messagesToSend)}")
                        selectedMessages.clear()
                        multiSelectMode = false
                    }
                )
            }
        }
    }
}

@Composable
fun SavedHeader(
    onBack: () -> Unit,
    multiSelectMode: Boolean,
    hasSelection: Boolean,
    selectedMessagesIds: List<String>,
    savedMessages: List<MessageModel>,
    onMultiSelectToggle: () -> Unit,
    navController: NavHostController
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset(y = (-3).dp)
            .border(
                width = 1.7.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Purple80.copy(alpha = 0.5f),
                        Color.Gray.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(bottomEnd = 22.dp, bottomStart = 22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Purple80.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.05f)
                    ),
                    start = Offset(0f, .05f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            ),
        contentAlignment = Alignment.BottomStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { -it / 2 })
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Purple80
                        )
                    }
                    Text(
                        text = "Saved Messages",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.W800,
                        color = Purple80,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // 🔹 Animated switch between modes
            AnimatedContent(
                targetState = multiSelectMode,
                transitionSpec = {
                    fadeIn(tween(300)) + slideInHorizontally(initialOffsetX = { it / 2 }) togetherWith
                            fadeOut(tween(300)) + slideOutHorizontally(targetOffsetX = { -it / 2 })
                },
                label = "Header action animation"
            ) { isMultiSelect ->
                if (isMultiSelect) {
                    Row {
                        // ❌ Cancel button — exit multi-select
                        IconButton(onClick = onMultiSelectToggle) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Selection",
                                tint = Color.Red.copy(alpha = 0.8f)
                            )
                        }

                        // ✅ DoneAll — only if items are selected
                        if (hasSelection) {
                            IconButton(onClick = {
                                val messagesToSend = savedMessages
                                    .filter { selectedMessagesIds.contains(it.id) }
                                    .joinToString(separator = "\n") { it.message }

                                navController.navigate("bluetooth?message=${Uri.encode(messagesToSend)}")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Confirm Multi Select",
                                    tint = Purple80
                                )
                            }
                        }
                    }
                } else {
                    // ⋮ Default menu when not in multi-select mode
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Purple80
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Select Multiple") },
                                onClick = {
                                    onMultiSelectToggle()
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SavedMessageItem(
    messageModel: MessageModel,
    showCheckbox: Boolean,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (showCheckbox) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Purple80,
                    uncheckedColor = Color.DarkGray,
                    checkmarkColor = Color.White
                )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(25.dp))
                .background(Color.Transparent)
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Purple80.copy(alpha = 0.5f),
                            Color.Gray.copy(alpha = 0.15f)
                        )
                    ),
                    shape = RoundedCornerShape(25.dp)
                )
                .padding(14.dp)
        ) {
            SelectionContainer {
                MarkdownText(
                    markdown = messageModel.message.ifBlank { "⚠️ Deleted message" },
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.W500,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun BulkShareBar(onBulkShare: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = onBulkShare) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Bulk Share",
                tint = Purple80
            )
        }
    }
}