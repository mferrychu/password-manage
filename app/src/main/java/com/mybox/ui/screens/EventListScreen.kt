@file:OptIn(ExperimentalMaterial3Api::class)

package com.mybox.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.RunningWithErrors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.foundation.background
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybox.data.model.Event
import com.mybox.ui.components.SearchBar
import com.mybox.ui.components.StatCard
import com.mybox.viewmodel.SnackbarState
import com.mybox.viewmodel.SnackbarType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun EventListScreen(
    events: List<Event>,
    futureEventCount: Int,
    pastEventCount: Int,
    snackbarEvents: Flow<SnackbarState>,
    searchQuery: String,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit,
    onEventClick: (Long) -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }

    LaunchedEffect(Unit) {
        snackbarEvents.collect { event ->
            currentSnackbarType = event.type
            snackbarHostState.showSnackbar(
                message = event.message,
                duration = SnackbarDuration.Short
            )
        }
    }

    val eventsGroupedByYear = remember(events) {
        events.groupBy { event ->
            event.date.substring(0, 4)
        }.toSortedMap(reverseOrder())
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    actionOnNewLine = false,
                    modifier = Modifier.padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 88.dp),
                    containerColor = if (currentSnackbarType == SnackbarType.SUCCESS) {
                        Color(0xFF34A853)
                    } else {
                        Color(0xFFEA4335)
                    },
                    contentColor = Color.White,
                    action = {
                        IconButton(onClick = { data.dismiss() }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (currentSnackbarType == SnackbarType.SUCCESS) {
                                Icons.Filled.Check
                            } else {
                                Icons.Filled.Close
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(data.visuals.message)
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(-14.dp)
            ) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "事件管理",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "菜单"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddClick) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "添加"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            SearchBar(
                query = searchQuery,
                onQueryChange = { onSearch(it) },
                placeholder = "请输入搜索内容"
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(label = "未来的事", value = futureEventCount.toString(), modifier = Modifier.weight(1f))
            StatCard(label = "总数", value = events.size.toString(), modifier = Modifier.weight(1f))
            StatCard(label = "过去的事", value = pastEventCount.toString(), modifier = Modifier.weight(1f))
        }

        Box(modifier = Modifier.weight(1f)) {
            if (events.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RunningWithErrors,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "暂无事件数据" else "未找到匹配的事件",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    eventsGroupedByYear.entries.withIndex().forEach { (idx, entry) ->
                        val year = entry.key
                        val yearEvents = entry.value
                        item(key = "year_$year") {
                            YearTimelineSection(year = year, events = yearEvents, isFirstYear = idx == 0) { event, action ->
                                when (action) {
                                    TimelineAction.CLICK -> onEventClick(event.id)
                                    TimelineAction.EDIT -> onEditClick(event.id)
                                    TimelineAction.DELETE -> onDeleteClick(event.id)
                                }
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(onClick = {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            }) {
                                Text(
                                    text = "- 没有了 点我返回顶部 -",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

enum class TimelineAction { CLICK, EDIT, DELETE }

val TIMELINE_CENTER_X = 32.dp

@Composable
fun YearTimelineSection(
    year: String,
    events: List<Event>,
    isFirstYear: Boolean,
    onEventAction: (Event, TimelineAction) -> Unit
) {
    val yearHeight = 36.dp
    val lineGap = 4.dp
    val timelineColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val lineX = TIMELINE_CENTER_X.toPx()
            val startY = yearHeight.toPx()
            val endY = size.height

            if (isFirstYear) {
                drawLine(
                    color = timelineColor.copy(alpha = 0.4f),
                    start = Offset(lineX, startY - lineGap.toPx()),
                    end = Offset(lineX, startY),
                    strokeWidth = 2.dp.toPx()
                )
            }

            if (events.isNotEmpty()) {
                drawLine(
                    color = timelineColor.copy(alpha = 0.4f),
                    start = Offset(lineX, startY),
                    end = Offset(lineX, endY),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(yearHeight)) {
                Text(
                    text = year,
                    style = MaterialTheme.typography.headlineSmall,
                    color = timelineColor.copy(alpha = 0.4f),
                    modifier = Modifier.layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            placeable.placeRelative(
                                TIMELINE_CENTER_X.toPx().toInt() - placeable.width / 2,
                                (yearHeight.toPx().toInt() - placeable.height) / 2
                            )
                        }
                    }
                )
            }

            events.forEachIndexed { index, event ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(bottom = if (index < events.size - 1) 8.dp else 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(TIMELINE_CENTER_X + 6.dp)
                            .fillMaxHeight()
                    ) {
                        val centerY = size.height / 2
                        drawCircle(
                            color = timelineColor,
                            radius = 6.dp.toPx(),
                            center = Offset(TIMELINE_CENTER_X.toPx(), centerY)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    TimelineEventCard(event = event) { action ->
                        onEventAction(event, action)
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineEventCard(
    event: Event,
    onAction: (TimelineAction) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        onClick = { onAction(TimelineAction.CLICK) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = try {
                Color(android.graphics.Color.parseColor(event.color))
            } catch (e: Exception) {
                Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val parts = event.date.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                Text(
                    text = "${year}年${month}月${day}日",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onAction(TimelineAction.EDIT) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            val lunarDate = com.mybox.ui.components.getLunarDate(event.date)
            if (lunarDate.isNotEmpty()) {
                Text(
                    text = lunarDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除此事件吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onAction(TimelineAction.DELETE)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("删除")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "取消",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("取消")
                }
            }
        )
    }
}