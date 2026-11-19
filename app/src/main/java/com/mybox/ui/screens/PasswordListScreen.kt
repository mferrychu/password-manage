@file:OptIn(ExperimentalMaterial3Api::class)

package com.mybox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.RunningWithErrors
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.mybox.data.model.Password
import com.mybox.ui.components.SearchBar
import com.mybox.ui.components.StatCard
import com.mybox.util.EncryptUtils
import com.mybox.viewmodel.SnackbarState
import com.mybox.viewmodel.SnackbarType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun PasswordListScreen(
    passwordItems: LazyPagingItems<Password>,
    weakPasswordCount: Int,
    totalCount: Int,
    pageSize: Int,
    shouldScrollToTop: Boolean,
    snackbarEvents: Flow<SnackbarState>,
    searchQuery: String,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit,
    onPasswordClick: (Long) -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
    onSearch: (String) -> Unit,
    onScrollToTopConsumed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isFirstLoad by remember { mutableStateOf(true) }
    var isManualRefreshing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by remember { mutableStateOf(SnackbarType.SUCCESS) }

    LaunchedEffect(passwordItems.loadState.refresh) {
        if (passwordItems.loadState.refresh is LoadState.NotLoading && isFirstLoad) {
            isFirstLoad = false
        }
    }

    val isRefreshing = !isFirstLoad && (isManualRefreshing || passwordItems.loadState.refresh is LoadState.Loading)
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(shouldScrollToTop) {
        if (shouldScrollToTop) {
            coroutineScope.launch {
                listState.scrollToItem(0)
            }
            onScrollToTopConsumed()
        }
    }

    LaunchedEffect(Unit) {
        snackbarEvents.collect { event ->
            currentSnackbarType = event.type
            snackbarHostState.showSnackbar(
                message = event.message,
                duration = SnackbarDuration.Short
            )
        }
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
                        text = "密码管理",
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
                placeholder = "搜索标题、账号或备注"
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val loadedPages = if (passwordItems.itemCount == 0) 0 else (passwordItems.itemCount + pageSize - 1) / pageSize
            val totalPages = if (totalCount == 0) 0 else (totalCount + pageSize - 1) / pageSize
            StatCard(
                label = "${loadedPages}/${totalPages} 页",
                value = passwordItems.itemCount.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(label = "总数", value = totalCount.toString(), modifier = Modifier.weight(1f))
            StatCard(label = "弱密码", value = weakPasswordCount.toString(), modifier = Modifier.weight(1f))
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                passwordItems.loadState.refresh is LoadState.Loading && passwordItems.itemCount == 0 -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                passwordItems.itemCount == 0 -> {
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
                                text = if (searchQuery.isEmpty()) "暂无密码数据" else "未找到匹配的密码",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isManualRefreshing = true
                            passwordItems.refresh()
                            coroutineScope.launch {
                                delay(500L)
                                isManualRefreshing = false
                            }
                        },
                        state = pullToRefreshState,
                        indicator = {
                            PullToRefreshDefaults.Indicator(
                                modifier = Modifier.align(Alignment.TopCenter),
                                isRefreshing = isRefreshing,
                                state = pullToRefreshState,
                                containerColor = MaterialTheme.colorScheme.surface,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(passwordItems.itemCount) { index ->
                                val password = passwordItems[index]
                                password?.let { p ->
                                    PasswordCard(
                                        password = p,
                                        onClick = { onPasswordClick(p.id) },
                                        onEditClick = { onEditClick(p.id) },
                                        onDeleteClick = { onDeleteClick(p.id) }
                                    )
                                }
                            }

                            passwordItems.apply {
                                when {
                                    loadState.append is LoadState.Loading -> {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                                    Text(
                                                        text = "正在加载下一页...",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    loadState.append is LoadState.Error -> {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                TextButton(onClick = { retry() }) {
                                                    Text(
                                                        text = "加载失败，点击重试",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    loadState.append is LoadState.NotLoading && loadState.append.endOfPaginationReached -> {
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
                                                        text = "- 没有了，点我返回顶部 -",
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
            }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordCard(
    password: Password,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = password.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(20.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "编辑",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(20.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "删除",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = password.account,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 芥末黄主题下，密码框用不透明色，与统计区块视觉色一致
            val isYellowTheme = MaterialTheme.colorScheme.primary == Color(0xFFE8C547)
            val passwordBoxColor = if (isYellowTheme) {
                Color(0xFFF8EFD4)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = passwordBoxColor
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (passwordVisible) EncryptUtils.decrypt(password.password) else "**********",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除此密码吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
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