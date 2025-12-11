package np.com.bimalkafle.easybot.view

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import dev.jeziellago.compose.markdowntext.MarkdownText
import np.com.bimalkafle.easybot.LocalNavController
import np.com.bimalkafle.easybot.R
import np.com.bimalkafle.easybot.model.MessageModel
import np.com.bimalkafle.easybot.ui.theme.Purple80
import np.com.bimalkafle.easybot.viewModel.ChatViewModel

@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    navController: NavHostController
) {
    val messages by viewModel.messageList.collectAsState()
    val listState = rememberLazyListState()

    // Track menu state
    var isMenuExpanded by remember { mutableStateOf(false) }

    // Observe scroll offset
    val scrollOffset = listState.firstVisibleItemScrollOffset
    val shrinkFraction = (scrollOffset / 400f).coerceIn(0f, 1f)

    // Animate header height based on scroll + menu state
    val headerHeight by animateDpAsState(
        targetValue = if (isMenuExpanded)
            165.dp // expand fully when menu is open
        else
            165.dp - (80.dp * shrinkFraction), // shrink when scrolling
        animationSpec = tween(durationMillis = 250)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(
            modifier = Modifier.height(headerHeight),
            isMenuExpanded = isMenuExpanded,
            onMenuClick = { isMenuExpanded = !isMenuExpanded } // toggle menu
        )

        MessageList(
            modifier = Modifier.weight(1f),
            messageList = messages,
            listState = listState,
            onAddFavorite = { viewModel.addToFavorites(it) },
        )

        // Hide input when menu is expanded
        if (!isMenuExpanded) {
            MessageInput(
                onMessageSend = { viewModel.sendMessage(it) }
            )
        }
    }
}


@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messageList: List<MessageModel>,
    onAddFavorite: (MessageModel) -> Unit,
    listState: LazyListState
) {
    if (messageList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(100.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Icon",
                tint = Color(0xFF372C46).copy(alpha = 0.6f)
            )
            Text(
                text = "Ask me Idea",
                fontSize = 22.sp,
                color = Color.Gray.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
        ) {
            items(messageList,key = { it.id }) { message ->
                MessageRow(
                    messageModel = message,
                    onAddFavorite = onAddFavorite
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MessageRow(
    messageModel: MessageModel,
    onAddFavorite: (MessageModel) -> Unit,
) {
    val isModel = messageModel.role == "model"

    // Persist visible text across recompositions
    val visibleText by rememberSaveable(key = messageModel.id) { mutableStateOf(messageModel.message) }

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(
                    start = if (isModel) 8.dp else 70.dp,
                    end = if (isModel) 40.dp else 8.dp,
                    top = 8.dp,
                    bottom = 6.dp
                )
                .fillMaxWidth(),
            horizontalAlignment = if (isModel) Alignment.Start else Alignment.End
        ) {
            // 🔹 Chat bubble with gradient/border styling
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(25.dp))
                    .background(if (isModel) Color.Transparent else Purple80.copy(alpha = 0.2f))
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
                        markdown = visibleText,
                        style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.W500),
                    )
                }
            }

            // ⭐ Favorite button for model messages
            if (isModel) {
                IconButton(
                    onClick = {
                        if (!messageModel.isFavorite) {
                            onAddFavorite(messageModel)
                        }
                    },
                    enabled = !messageModel.isFavorite,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        imageVector = if (messageModel.isFavorite)
                            Icons.Filled.Favorite
                        else
                            Icons.Outlined.FavoriteBorder,
                        contentDescription = if (messageModel.isFavorite) "Saved" else "Save",
                        tint = if (messageModel.isFavorite) Purple80 else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun AppHeader(
    modifier: Modifier = Modifier,
    isMenuExpanded: Boolean,
    onMenuClick: (Boolean) -> Unit
) {
    val navController = LocalNavController.current
    var expanded by remember { mutableStateOf(isMenuExpanded) }

    // Keep expanded state in sync with parent
    LaunchedEffect(isMenuExpanded) {
        expanded = isMenuExpanded
    }

    Box(
        modifier = modifier
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
            .clip(RoundedCornerShape(bottomEnd = 22.dp, bottomStart = 22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Purple80.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            ),
        contentAlignment = Alignment.BottomStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Idea...",
                fontSize = 43.sp,
                fontWeight = FontWeight.W800,
                color = Purple80
            )

            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 12.dp, bottom = 8.dp)
            ) {
                IconButton(onClick = {
                    expanded = !expanded
                    onMenuClick(expanded) // notify ChatPage
                }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Purple80)
                }

                AnimatedVisibility(expanded) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("profile")
                                    expanded = false
                                    onMenuClick(false)
                                }
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Purple80)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Profile", color = Purple80)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("saved")
                                    expanded = false
                                    onMenuClick(false)
                                }
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Saved", tint = Purple80)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Favorites", color = Purple80)
                        }
                    }
                }
            }
        }
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MessageInput(onMessageSend: (String) -> Unit) {
    var message by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // 🔹 BoxWithConstraints + animateContentSize to smoothly expand
    BoxWithConstraints(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Purple80.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Purple80.copy(alpha = 0.5f),
                        Color.Gray.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(28.dp),
                clip = false,
                ambientColor = Color.Gray.copy(alpha = 0.1f),
                spotColor = Color.Gray.copy(alpha = 0.1f)
            )
            .animateContentSize() // 🔹 Smooth expansion
            .padding(1.dp)
    ) {
        TextField(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()), // optional: allows scrolling if maxLines reached
            value = message,
            onValueChange = { message = it },
            shape = RoundedCornerShape(28.dp),
            maxLines = 4,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Purple80.copy(alpha = 0.8f)
            ),
            placeholder = {
                Text(
                    text = "Ask Idea",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W400,
                    color = Purple80.copy(alpha = 0.8f)
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    focusManager.clearFocus()
                    if (message.isNotEmpty()) {
                        onMessageSend(message)
                        message = ""
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (message.isNotEmpty())
                            Purple80.copy(alpha = 0.8f)
                        else
                            Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier
                            .padding(bottom = 4.dp, end = 8.dp)
                            .size(32.dp)
                            .rotate(325f)
                    )
                }
            }
        )
    }
}