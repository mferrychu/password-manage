package com.mybox

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import dagger.hilt.android.AndroidEntryPoint
import com.mybox.navigation.AppNavHost
import com.mybox.navigation.Screen
import com.mybox.ui.components.BottomNavBar
import com.mybox.ui.screens.UnlockScreen
import com.mybox.ui.theme.MyBoxTheme
import com.mybox.ui.theme.ThemeType
import com.mybox.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    companion object {
        private const val MAX_IMPORT_FILE_SIZE = 10L * 1024 * 1024 // 10MB
    }

    private lateinit var createFileLauncher: ActivityResultLauncher<Intent>
    private lateinit var openFileLauncher: ActivityResultLauncher<Intent>

    private var pendingExportPassword: String? = null
    private var splashScreen: SplashScreen? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // 安装SplashScreen，必须在super.onCreate之前调用
        splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.parseColor("#EADDFF"),
                android.graphics.Color.parseColor("#EADDFF")
            )
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        initFileLaunchers()

        setContent {
            MainScreen()
        }
    }

    @Composable
    fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
        val appInitState by viewModel.appInitState.collectAsState()
        val themeType by viewModel.themeType.collectAsState()
        
        // 设置SplashScreen保持显示的条件：初始化未完成时持续显示
        LaunchedEffect(appInitState.isInitializing) {
            splashScreen?.setKeepOnScreenCondition {
                appInitState.isInitializing
            }
        }

        // 初始化未完成时不渲染NavHost，确保masterPasswordHash已确定后再创建导航
        if (appInitState.isInitializing) return

        MyBoxTheme(themeType = themeType) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainContent(viewModel, themeType)
            }
        }
    }

    @Composable
    fun MainContent(viewModel: MainViewModel, themeType: ThemeType) {
        val appInitState by viewModel.appInitState.collectAsState()

        val allPasswords by viewModel.passwords.collectAsState(emptyList())
        val allEvents by viewModel.events.collectAsState(emptyList())

        LaunchedEffect(allPasswords, allEvents) {
            viewModel.updateStats(allPasswords, allEvents)
        }

        val weakPasswordCount by viewModel.weakPasswordCount.collectAsState(0)
        val futureEventCount by viewModel.futureEventCount.collectAsState(0)
        val pastEventCount by viewModel.pastEventCount.collectAsState(0)
        val importFileSelected by viewModel.importFileSelected.collectAsState(false)

        val startDestination = when {
            appInitState.masterPasswordHash == null -> Screen.SetupPassword.route
            else -> Screen.PasswordList.route
        }

        // 锁定状态：已设主密码且 isLocked 时，UnlockScreen 作为覆盖层显示
        val isLocked = appInitState.isLocked && appInitState.masterPasswordHash != null

        val navController = rememberNavController()
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        val context = LocalContext.current
        LaunchedEffect(currentRoute, themeType, isLocked) {
            val activity = context as? ComponentActivity
            activity?.window?.let { window ->
                if (isLocked) {
                    // UnlockScreen 覆盖层：背景色 + 深色图标
                    window.statusBarColor = com.mybox.ui.theme.getBackgroundColor(themeType).toArgb()
                    val controller = WindowCompat.getInsetsController(window, window.decorView)
                    controller.isAppearanceLightStatusBars = true
                } else {
                    val darkBarScreens = listOf(
                        Screen.PasswordList.route,
                        Screen.EventList.route,
                        Screen.AddPassword.route,
                        Screen.EditPassword.route,
                        Screen.PasswordDetail.route,
                        Screen.AddEvent.route,
                        Screen.EditEvent.route,
                        Screen.EventDetail.route,
                        Screen.Settings.route,
                        Screen.ChangePassword.route,
                        Screen.About.route
                    )
                    val lightBarScreens = listOf(
                        Screen.SetupPassword.route
                    )
                    if (currentRoute in darkBarScreens) {
                        // 有TopAppBar的页面：使用深色主色
                        window.statusBarColor = com.mybox.ui.theme.getPrimaryColor(themeType).toArgb()
                        val controller = WindowCompat.getInsetsController(window, window.decorView)
                        controller.isAppearanceLightStatusBars = false
                    } else if (currentRoute in lightBarScreens) {
                        // 无TopAppBar的全屏页面：使用背景色
                        window.statusBarColor = com.mybox.ui.theme.getBackgroundColor(themeType).toArgb()
                        val controller = WindowCompat.getInsetsController(window, window.decorView)
                        controller.isAppearanceLightStatusBars = true
                    } else {
                        // 其他页面：使用浅色容器色
                        window.statusBarColor = com.mybox.ui.theme.getPrimaryContainerColor(themeType).toArgb()
                        val controller = WindowCompat.getInsetsController(window, window.decorView)
                        controller.isAppearanceLightStatusBars = true
                    }
                }
            }
        }

        val selectedTab = remember(currentRoute) {
            when (currentRoute) {
                Screen.PasswordList.route,
                Screen.AddPassword.route,
                Screen.PasswordDetail.route,
                Screen.EditPassword.route -> 0
                Screen.EventList.route,
                Screen.AddEvent.route,
                Screen.EventDetail.route,
                Screen.EditEvent.route -> 1
                else -> 0
            }
        }

        val showBottomBar = currentRoute == Screen.PasswordList.route || currentRoute == Screen.EventList.route

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        BottomNavBar(
                            selectedItem = selectedTab,
                            onItemSelected = { index ->
                                when (index) {
                                    0 -> navController.navigate(Screen.PasswordList.route) {
                                        popUpTo(Screen.PasswordList.route) { inclusive = false }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    1 -> navController.navigate(Screen.EventList.route) {
                                        popUpTo(Screen.EventList.route) { inclusive = false }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            ) {
                AppNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    viewModel = viewModel,
                    passwords = allPasswords,
                    events = allEvents,
                    weakPasswordCount = weakPasswordCount,
                    futureEventCount = futureEventCount,
                    pastEventCount = pastEventCount,
                    importFileSelected = importFileSelected,
                    onExport = { password ->
                        viewModel.isPerformingFileOperation = true
                        pendingExportPassword = password
                        val fileName = "mybox_export_${System.currentTimeMillis()}.enc"
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/octet-stream"
                            putExtra(Intent.EXTRA_TITLE, fileName)
                        }
                        createFileLauncher.launch(intent)
                    },
                    onImport = {
                        viewModel.isPerformingFileOperation = true
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                        }
                        openFileLauncher.launch(intent)
                    }
                )
            }

            // UnlockScreen 覆盖层：锁定时盖在 NavHost 之上，解锁后消失，露出原页面
            if (isLocked) {
                BackHandler { /* 拦截返回键，防止绕过锁定 */ }
                UnlockScreen(
                    onUnlockAttempt = { password -> viewModel.unlock(password) },
                    biometricEnabled = viewModel.isBiometricEnabled(),
                    onBiometricUnlock = { viewModel.unlockByBiometric() },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }
    }

    private fun initFileLaunchers() {
        createFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            ViewModelProvider(this@MainActivity)[MainViewModel::class.java].isPerformingFileOperation = false
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null && pendingExportPassword != null) {
                    performExport(uri, pendingExportPassword!!)
                }
            }
            pendingExportPassword = null
        }

        openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            ViewModelProvider(this@MainActivity)[MainViewModel::class.java].isPerformingFileOperation = false
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    // 导入前检查文件大小，防止恶意超大文件导致 OOM
                    val fileSize = contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        pfd.statSize
                    } ?: 0L
                    if (fileSize > MAX_IMPORT_FILE_SIZE) {
                        showToast("文件过大（${fileSize / 1024 / 1024}MB），最多支持 10MB 的导出文件")
                        return@registerForActivityResult
                    }
                    lifecycleScope.launch {
                        try {
                            val encryptedData = contentResolver.openInputStream(uri)?.use { stream ->
                                stream.bufferedReader().readText()
                            }
                            val viewModel = ViewModelProvider(this@MainActivity)[MainViewModel::class.java]
                            viewModel.notifyImportFileSelected(encryptedData)
                        } catch (e: Exception) {
                            showToast("读取文件失败: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    private fun performExport(uri: Uri, password: String) {
        lifecycleScope.launch {
            try {
                val viewModel = ViewModelProvider(this@MainActivity)[MainViewModel::class.java]
                viewModel.startExportOperation()
                val (encryptedData, totalCount) = viewModel.exportData(password)

                contentResolver.openOutputStream(uri)?.use {
                    it.write(encryptedData.toByteArray(Charsets.UTF_8))
                }

                showToast("成功导出 ${totalCount} 条数据")
            } catch (e: Exception) {
                showToast("导出失败: ${e.message}")
            } finally {
                ViewModelProvider(this@MainActivity)[MainViewModel::class.java].completeExportOperation()
            }
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            android.widget.Toast.makeText(this@MainActivity, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}