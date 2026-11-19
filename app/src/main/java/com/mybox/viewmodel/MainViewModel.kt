package com.mybox.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.mybox.data.dao.EventDao
import com.mybox.data.dao.MasterPasswordDao
import com.mybox.data.dao.PasswordDao
import com.mybox.data.database.AppDatabase
import com.mybox.data.model.Event
import com.mybox.data.model.MasterPassword
import com.mybox.data.model.Password
import com.mybox.data.preferences.AppPreferences
import com.mybox.ui.theme.ThemeType
import com.mybox.util.EncryptUtils
import com.mybox.util.ExportImportUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 10
private const val LOAD_DELAY_MS = 500L

enum class SnackbarType { SUCCESS, ERROR }

data class SnackbarState(
    val message: String = "",
    val type: SnackbarType = SnackbarType.SUCCESS
)

data class AppInitState(
    val isInitializing: Boolean = true,
    val masterPasswordHash: String? = null,
    val isLocked: Boolean = true
)

sealed interface OperationState {
    object Idle : OperationState
    object Exporting : OperationState
    object Importing : OperationState
}

@HiltViewModel
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainViewModel @Inject constructor(
    private val appDatabase: AppDatabase,
    private val passwordDao: PasswordDao,
    private val eventDao: EventDao,
    private val masterPasswordDao: MasterPasswordDao,
    private val appPreferences: AppPreferences
) : ViewModel(), DefaultLifecycleObserver {

    val pageSize: Int get() = PAGE_SIZE

    @Volatile
    var isPerformingFileOperation = false

    private val _appInitState = MutableStateFlow(AppInitState())
    val appInitState: StateFlow<AppInitState> = _appInitState.asStateFlow()

    private val _isAppLocked = MutableStateFlow(true)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _masterPasswordHash = MutableStateFlow<String?>(null)
    val masterPasswordHash: StateFlow<String?> = _masterPasswordHash.asStateFlow()

    private fun updateAppInitState(newState: AppInitState) {
        _appInitState.value = newState
        _isAppLocked.value = newState.isLocked
        _masterPasswordHash.value = newState.masterPasswordHash
    }

    private val _passwordSearchQuery = MutableStateFlow("")
    val passwordSearchQuery: StateFlow<String> = _passwordSearchQuery.asStateFlow()

    private val _eventSearchQuery = MutableStateFlow("")
    val eventSearchQuery: StateFlow<String> = _eventSearchQuery.asStateFlow()

    val passwords: Flow<List<Password>> = _passwordSearchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                passwordDao.getAllPasswords()
            } else {
                passwordDao.searchPasswords(query)
            }
        }

    val events: Flow<List<Event>> = _eventSearchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                eventDao.getAllEvents()
            } else {
                eventDao.searchEvents(query)
            }
        }

    val passwordPagedData: Flow<PagingData<Password>> = _passwordSearchQuery
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    enablePlaceholders = false,
                    initialLoadSize = PAGE_SIZE,
                    prefetchDistance = 1
                )
            ) {
                object : PagingSource<Int, Password>() {
                    private val delegate = 
                        if (query.isEmpty()) passwordDao.getPasswordsPaged() 
                        else passwordDao.searchPasswordsPaged(query)
                    
                    init {
                        delegate.registerInvalidatedCallback {
                            invalidate()
                        }
                    }
                    
                    override suspend fun load(params: PagingSource.LoadParams<Int>): PagingSource.LoadResult<Int, Password> {
                        if (params is PagingSource.LoadParams.Append) {
                            delay(LOAD_DELAY_MS)
                        }
                        return delegate.load(params)
                    }
                    
                    override fun getRefreshKey(state: PagingState<Int, Password>): Int? {
                        return null
                    }
                }
            }.flow
        }
        .cachedIn(viewModelScope)

    private val _shouldScrollToTop = MutableStateFlow(false)
    val shouldScrollToTop: StateFlow<Boolean> = _shouldScrollToTop.asStateFlow()

    private val _isSubmittingPassword = MutableStateFlow(false)
    val isSubmittingPassword: StateFlow<Boolean> = _isSubmittingPassword.asStateFlow()

    private val _isSubmittingEvent = MutableStateFlow(false)
    val isSubmittingEvent: StateFlow<Boolean> = _isSubmittingEvent.asStateFlow()

    private val _weakPasswordCount = MutableStateFlow(0)
    val weakPasswordCount: StateFlow<Int> = _weakPasswordCount.asStateFlow()

    private val _futureEventCount = MutableStateFlow(0)
    val futureEventCount: StateFlow<Int> = _futureEventCount.asStateFlow()

    private val _pastEventCount = MutableStateFlow(0)
    val pastEventCount: StateFlow<Int> = _pastEventCount.asStateFlow()

    private val _importFileSelected = MutableStateFlow(false)
    val importFileSelected: StateFlow<Boolean> = _importFileSelected.asStateFlow()

    private val _snackbarEvents = Channel<SnackbarState>(Channel.BUFFERED)
    val snackbarEvents: Flow<SnackbarState> = _snackbarEvents.receiveAsFlow()

    fun showSnackbar(message: String, type: SnackbarType = SnackbarType.SUCCESS) {
        _snackbarEvents.trySend(SnackbarState(message = message, type = type))
    }

    private val _importFileContent = MutableStateFlow<String?>(null)
    val importFileContent: StateFlow<String?> = _importFileContent.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private fun setOperationState(state: OperationState) {
        _operationState.value = state
    }

    init {
        viewModelScope.launch {
            val masterPassword = masterPasswordDao.getMasterPassword()
            
            // 先更新主密码状态，确保状态已确定
            updateAppInitState(AppInitState(
                isInitializing = true,
                masterPasswordHash = masterPassword?.passwordHash,
                isLocked = true
            ))
            
            // 再将isInitializing设为false，此时masterPasswordHash已确定，不会闪现设置密码页
            updateAppInitState(AppInitState(
                isInitializing = false,
                masterPasswordHash = masterPassword?.passwordHash,
                isLocked = true
            ))
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // 文件选择器打开中、或导出/导入操作进行中时，不触发自动锁定，避免中断操作
        val isBusy = isPerformingFileOperation ||
                _operationState.value is OperationState.Exporting ||
                _operationState.value is OperationState.Importing
        if (!isBusy && appPreferences.isAutoLockEnabled()) {
            updateAppInitState(_appInitState.value.copy(isLocked = true))
        }
    }

    fun setMasterPassword(password: String) {
        viewModelScope.launch {
            val hash = EncryptUtils.hash(password)
            masterPasswordDao.setMasterPassword(MasterPassword(passwordHash = hash))
            updateAppInitState(_appInitState.value.copy(
                masterPasswordHash = hash,
                isLocked = false
            ))
        }
    }

    fun unlock(password: String): Boolean {
        val storedHash = _appInitState.value.masterPasswordHash ?: return false
        return if (EncryptUtils.verify(password, storedHash)) {
            updateAppInitState(_appInitState.value.copy(isLocked = false))
            true
        } else {
            false
        }
    }

    /**
     * 生物识别解锁：仅当用户已启用且主密码已设置时才放行。
     * 不绕过"必须先设置主密码"这一前提。
     */
    fun unlockByBiometric(): Boolean {
        if (!appPreferences.isBiometricEnabled()) return false
        val hash = _appInitState.value.masterPasswordHash ?: return false
        updateAppInitState(_appInitState.value.copy(isLocked = false))
        return true
    }

    fun setBiometricEnabled(enabled: Boolean) {
        appPreferences.setBiometricEnabled(enabled)
    }

    fun isBiometricEnabled(): Boolean = appPreferences.isBiometricEnabled()

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            try {
                val hash = EncryptUtils.hash(newPassword)
                masterPasswordDao.setMasterPassword(MasterPassword(passwordHash = hash))
                updateAppInitState(_appInitState.value.copy(masterPasswordHash = hash))
                showSnackbar("修改主密码成功！")
            } catch (e: Exception) {
                showSnackbar("修改主密码失败，请重试~", SnackbarType.ERROR)
            }
        }
    }

    fun addPassword(title: String, account: String, password: String, remark: String) {
        if (_isSubmittingPassword.value) return
        _isSubmittingPassword.value = true

        viewModelScope.launch {
            try {
                val encryptedPassword = EncryptUtils.encrypt(password)
                passwordDao.insertPassword(
                    Password(title = title, account = account, password = encryptedPassword, remark = remark)
                )
                showSnackbar("添加密码成功！")
            } catch (e: Exception) {
                showSnackbar("添加密码失败，请重试~", SnackbarType.ERROR)
            } finally {
                _isSubmittingPassword.value = false
                triggerScrollToTop()
            }
        }
    }

    fun updatePassword(password: Password) {
        viewModelScope.launch {
            try {
                val encryptedPassword = EncryptUtils.encrypt(password.password)
                passwordDao.updatePassword(password.copy(password = encryptedPassword))
                triggerScrollToTop()
                showSnackbar("修改密码成功！")
            } catch (e: Exception) {
                showSnackbar("修改密码失败，请重试~", SnackbarType.ERROR)
            }
        }
    }

    fun deletePassword(id: Long) {
        viewModelScope.launch {
            try {
                passwordDao.deletePasswordById(id)
                showSnackbar("删除密码成功！")
            } catch (e: Exception) {
                showSnackbar("删除密码失败，请重试~", SnackbarType.ERROR)
            }
        }
    }

    fun addEvent(date: String, description: String, color: String = "#FFFFFF") {
        if (_isSubmittingEvent.value) return
        _isSubmittingEvent.value = true

        viewModelScope.launch {
            try {
                eventDao.insertEvent(Event(date = date, description = description, color = color))
                showSnackbar("添加事件成功！")
            } catch (e: Exception) {
                showSnackbar("添加事件失败，请重试~", SnackbarType.ERROR)
            } finally {
                _isSubmittingEvent.value = false
            }
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            try {
                eventDao.updateEvent(event)
                showSnackbar("修改事件成功！")
            } catch (e: Exception) {
                showSnackbar("修改事件失败，请重试~", SnackbarType.ERROR)
            }
        }
    }

    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            try {
                eventDao.deleteEventById(id)
                showSnackbar("删除事件成功！")
            } catch (e: Exception) {
                showSnackbar("删除事件失败，请重试~", SnackbarType.ERROR)
            }
        }
    }

    fun searchPasswords(query: String) {
        _passwordSearchQuery.value = query
    }

    fun searchEvents(query: String) {
        _eventSearchQuery.value = query
    }

    fun updateStats(allPasswords: List<Password>, allEvents: List<Event>) {
        _weakPasswordCount.value = allPasswords.count { password ->
            val plainPassword = EncryptUtils.decrypt(password.password)
            plainPassword.length < 8 ||
                    plainPassword.matches(Regex("^\\d+$")) ||
                    plainPassword.matches(Regex("^[a-zA-Z]+$"))
        }
        val today = java.time.LocalDate.now().toString()
        _futureEventCount.value = allEvents.count { it.date >= today }
        _pastEventCount.value = allEvents.count { it.date < today }
    }

    fun triggerScrollToTop() {
        _shouldScrollToTop.value = true
    }

    fun consumedScrollToTop() {
        _shouldScrollToTop.value = false
    }

    fun notifyImportFileSelected(content: String? = null) {
        _importFileContent.value = content
        _importFileSelected.value = true
    }

    fun clearImportFileSelected() {
        _importFileContent.value = null
        _importFileSelected.value = false
    }

    fun startExportOperation() {
        setOperationState(OperationState.Exporting)
    }

    fun completeExportOperation() {
        setOperationState(OperationState.Idle)
    }

    suspend fun exportData(password: String): Pair<String, Int> {
        return withContext(Dispatchers.IO) {
            val allPasswords = passwordDao.getAllPasswords().first()
            val allEvents = eventDao.getAllEvents().first()
            val totalCount = allPasswords.size + allEvents.size
            val jsonData = ExportImportUtils.exportData(allPasswords, allEvents)
            Pair(ExportImportUtils.encryptExportData(jsonData, password), totalCount)
        }
    }

    suspend fun performImport(password: String): String {
        setOperationState(OperationState.Importing)
        val content = _importFileContent.value ?: return "文件内容为空"
        return try {
            val jsonData = ExportImportUtils.decryptExportData(content, password)
            val importResult = ExportImportUtils.importData(
                appDatabase, jsonData
            )
            val result = when (importResult) {
                is ExportImportUtils.ImportResult.Success -> {
                    "成功导入 ${importResult.passwordCount + importResult.eventCount} 条数据"
                }
                is ExportImportUtils.ImportResult.Error -> importResult.message
            }
            result
        } catch (e: IllegalArgumentException) {
            "解密密码错误，请重试~"
        } catch (e: Exception) {
            "导入失败: ${e.message}"
        } finally {
            setOperationState(OperationState.Idle)
        }
    }

    fun getAppPreferences() = appPreferences

    // 主题状态
    private val _themeType = MutableStateFlow(appPreferences.getThemeType())
    val themeType: StateFlow<ThemeType> = _themeType.asStateFlow()

    fun setThemeType(themeType: ThemeType) {
        appPreferences.setThemeType(themeType)
        _themeType.value = themeType
    }
}