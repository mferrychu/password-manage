package com.mybox.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.mybox.ui.screens.*
import com.mybox.ui.theme.ThemeType
import com.mybox.util.BiometricHelper
import com.mybox.viewmodel.MainViewModel
import com.mybox.data.model.Password
import com.mybox.data.model.Event

sealed class Screen(val route: String) {
    object SetupPassword : Screen("setup_password")
    object Unlock : Screen("unlock")
    object PasswordList : Screen("password_list")
    object AddPassword : Screen("add_password")
    object PasswordDetail : Screen("password_detail/{passwordId}") {
        fun createRoute(passwordId: Long) = "password_detail/$passwordId"
    }
    object EditPassword : Screen("edit_password/{passwordId}") {
        fun createRoute(passwordId: Long) = "edit_password/$passwordId"
    }
    object EventList : Screen("event_list")
    object AddEvent : Screen("add_event")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: Long) = "event_detail/$eventId"
    }
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: Long) = "edit_event/$eventId"
    }
    object Settings : Screen("settings")
    object ChangePassword : Screen("change_password")
    object About : Screen("about")
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    viewModel: MainViewModel,
    passwords: List<Password>,
    events: List<Event>,
    weakPasswordCount: Int,
    futureEventCount: Int,
    pastEventCount: Int,
    importFileSelected: Boolean,
    onExport: (String) -> Unit,
    onImport: () -> Unit
) {
    val shouldScrollToTop by viewModel.shouldScrollToTop.collectAsState()
    val isSubmittingPassword by viewModel.isSubmittingPassword.collectAsState()
    val isSubmittingEvent by viewModel.isSubmittingEvent.collectAsState()
    val passwordItems = viewModel.passwordPagedData.collectAsLazyPagingItems()
    val passwordSearchQuery by viewModel.passwordSearchQuery.collectAsState()
    val eventSearchQuery by viewModel.eventSearchQuery.collectAsState()
    val operationState by viewModel.operationState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(0)) },
        exitTransition = { fadeOut(animationSpec = tween(0)) },
        popEnterTransition = { fadeIn(animationSpec = tween(0)) },
        popExitTransition = { fadeOut(animationSpec = tween(0)) }
    ) {
        composable(Screen.SetupPassword.route) {
            SetupPasswordScreen(
                onPasswordSet = { password ->
                    viewModel.setMasterPassword(password)
                    navController.navigate(Screen.PasswordList.route) {
                        popUpTo(Screen.SetupPassword.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PasswordList.route) {
            val context = LocalContext.current
            var showEnrollDialog by remember { mutableStateOf(false) }

            // 进入主页时检查是否需要弹"启用生物识别"引导（仅弹一次）
            LaunchedEffect(Unit) {
                val prefs = viewModel.getAppPreferences()
                val needEnroll = !prefs.isBiometricEnabled() &&
                        !prefs.isBiometricPromptShown() &&
                        BiometricHelper.isBiometricAvailable(context)
                if (needEnroll) {
                    showEnrollDialog = true
                }
            }

            PasswordListScreen(
                passwordItems = passwordItems,
                weakPasswordCount = weakPasswordCount,
                totalCount = passwords.size,
                pageSize = viewModel.pageSize,
                shouldScrollToTop = shouldScrollToTop,
                snackbarEvents = viewModel.snackbarEvents,
                searchQuery = passwordSearchQuery,
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onAddClick = { navController.navigate(Screen.AddPassword.route) },
                onPasswordClick = { passwordId ->
                    navController.navigate(Screen.PasswordDetail.createRoute(passwordId))
                },
                onEditClick = { passwordId ->
                    navController.navigate(Screen.EditPassword.createRoute(passwordId))
                },
                onDeleteClick = { passwordId ->
                    viewModel.deletePassword(passwordId)
                },
                onSearch = { query -> viewModel.searchPasswords(query) },
                onScrollToTopConsumed = { viewModel.consumedScrollToTop() }
            )

            // 首次进入主页的生物识别启用引导
            if (showEnrollDialog) {
                AlertDialog(
                    onDismissRequest = {
                        viewModel.getAppPreferences().setBiometricPromptShown(true)
                        showEnrollDialog = false
                    },
                    title = { Text("启用指纹快捷解锁？") },
                    text = { Text("后续进入应用时可直接使用指纹/面部解锁，无需每次输入主密码。你随时可以在设置中关闭。") },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.setBiometricEnabled(true)
                            viewModel.getAppPreferences().setBiometricPromptShown(true)
                            showEnrollDialog = false
                        }) {
                            Text("启用")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = {
                            viewModel.getAppPreferences().setBiometricPromptShown(true)
                            showEnrollDialog = false
                        }) {
                            Text("以后再说")
                        }
                    }
                )
            }
        }

        composable(Screen.AddPassword.route) {
            AddEditPasswordScreen(
                title = "添加密码",
                isSubmitting = isSubmittingPassword,
                onSave = { title, account, password, remark ->
                    viewModel.addPassword(title, account, password, remark)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PasswordDetail.route) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getString("passwordId")?.toLongOrNull() ?: 0
            val password = passwords.find { it.id == passwordId }
            PasswordDetailScreen(
                password = password,
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(Screen.EditPassword.createRoute(id))
                },
                onDelete = { id ->
                    viewModel.deletePassword(id)
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditPassword.route) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getString("passwordId")?.toLongOrNull() ?: 0
            val existingPassword = passwords.find { it.id == passwordId }
            AddEditPasswordScreen(
                title = "修改密码",
                existingPassword = existingPassword,
                isSubmitting = isSubmittingPassword,
                onSave = { title, account, password, remark ->
                    existingPassword?.let {
                        viewModel.updatePassword(
                            it.copy(
                                title = title,
                                account = account,
                                password = password,
                                remark = remark,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                    navController.popBackStack(Screen.PasswordList.route, inclusive = false)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EventList.route) {
            EventListScreen(
                events = events,
                futureEventCount = futureEventCount,
                pastEventCount = pastEventCount,
                snackbarEvents = viewModel.snackbarEvents,
                searchQuery = eventSearchQuery,
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onAddClick = { navController.navigate(Screen.AddEvent.route) },
                onEventClick = { eventId ->
                    navController.navigate(Screen.EventDetail.createRoute(eventId))
                },
                onEditClick = { eventId ->
                    navController.navigate(Screen.EditEvent.createRoute(eventId))
                },
                onDeleteClick = { eventId ->
                    viewModel.deleteEvent(eventId)
                },
                onSearch = { query -> viewModel.searchEvents(query) }
            )
        }

        composable(Screen.AddEvent.route) {
            AddEditEventScreen(
                title = "添加事件",
                isSubmitting = isSubmittingEvent,
                onSave = { date, description, color ->
                    viewModel.addEvent(date, description, color)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EventDetail.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")?.toLongOrNull() ?: 0
            val event = events.find { it.id == eventId }
            EventDetailScreen(
                event = event,
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(Screen.EditEvent.createRoute(id))
                },
                onDelete = { id ->
                    viewModel.deleteEvent(id)
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditEvent.route) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")?.toLongOrNull() ?: 0
            val existingEvent = events.find { it.id == eventId }
            AddEditEventScreen(
                title = "修改事件",
                existingEvent = existingEvent,
                isSubmitting = isSubmittingEvent,
                onSave = { date, description, color ->
                    existingEvent?.let {
                        viewModel.updateEvent(
                            it.copy(
                                date = date,
                                description = description,
                                color = color,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                    navController.popBackStack(Screen.EventList.route, inclusive = false)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                appPreferences = viewModel.getAppPreferences(),
                onBack = { navController.popBackStack() },
                onChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onExport = onExport,
                onImport = onImport,
                onImportWithPassword = { password ->
                    val result = viewModel.performImport(password)
                    viewModel.clearImportFileSelected()
                    result
                },
                onImportCancelled = {
                    viewModel.clearImportFileSelected()
                },
                importFileSelected = importFileSelected,
                onCloseApp = {
                    (navController.context as? android.app.Activity)?.finishAndRemoveTask()
                },
                onAbout = { navController.navigate(Screen.About.route) },
                onThemeChanged = { themeType ->
                    viewModel.setThemeType(themeType)
                },
                snackbarEvents = viewModel.snackbarEvents,
                operationState = operationState
            )
        }

        composable(Screen.ChangePassword.route) {
            viewModel.masterPasswordHash.value?.let { hash ->
                ChangePasswordScreen(
                    currentPasswordHash = hash,
                    onBack = { navController.popBackStack() },
                    onSave = { newPassword ->
                        viewModel.changePassword(newPassword)
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}