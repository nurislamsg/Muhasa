package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.MuhasabaDatabase
import com.example.data.repository.MuhasabaRepository
import com.example.ui.AppTab
import com.example.ui.MuhasabaViewModel
import com.example.ui.components.ActiveTimerBanner
import com.example.ui.components.HeaderCard
import com.example.ui.components.MuhasabaBottomBar
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.MuhasabaTheme
import com.example.util.DateUtils

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = MuhasabaDatabase.getInstance(applicationContext)
    val repository = MuhasabaRepository(database.muhasabaDao())
    val factory = MuhasabaViewModel.Factory(repository)

    setContent {
      val vm: MuhasabaViewModel = viewModel(factory = factory)
      MuhasabaApp(viewModel = vm)
    }
  }
}

@Composable
fun MuhasabaApp(viewModel: MuhasabaViewModel) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val strings = state.strings
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val listState = rememberLazyListState()

  val exportLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
      if (uri != null) {
        runCatching {
          val json = viewModel.buildBackupJson()
          context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(json.toByteArray(Charsets.UTF_8))
          }
          viewModel.notifyExportSuccess()
        }
      }
    }

  val importLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
      if (uri != null) {
        runCatching {
          val json =
            context.contentResolver.openInputStream(uri)?.use { input ->
              input.bufferedReader(Charsets.UTF_8).readText()
            }
          if (json != null) {
            viewModel.importBackupJson(json)
          }
        }
      }
    }

  LaunchedEffect(Unit) {
    viewModel.toastMessages.collect { msg ->
      snackbarHostState.currentSnackbarData?.dismiss()
      snackbarHostState.showSnackbar(msg)
    }
  }

  LaunchedEffect(state.currentTab) {
    listState.scrollToItem(0)
  }

  BackHandler(enabled = state.currentTab != AppTab.TODAY) {
    viewModel.switchTab(AppTab.TODAY)
  }

  MuhasabaTheme(darkTheme = state.isDarkTheme) {
    val isTimerRunning = state.activeTimerStartMs != null

    BoxWithConstraints(
      modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
      val useNavRail = maxWidth >= 840.dp

      Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
          ActiveTimerBanner(
            isRunning = isTimerRunning,
            elapsedMs = state.elapsedTimerMs,
            strings = strings,
            onStop = viewModel::stopCarelessnessTimer,
          )
        },
        bottomBar = {
          if (!useNavRail) {
            MuhasabaBottomBar(
              currentTab = state.currentTab,
              strings = strings,
              onSelectTab = viewModel::switchTab,
            )
          }
        },
      ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          if (useNavRail) {
            NavigationRail(
              containerColor = MaterialTheme.colorScheme.surface,
              modifier = Modifier.fillMaxHeight(),
            ) {
              Spacer(modifier = Modifier.height(12.dp))
              AppTab.entries.forEach { tab ->
                val icon =
                  when (tab) {
                    AppTab.TODAY -> Icons.Filled.CalendarToday
                    AppTab.CALENDAR -> Icons.Filled.CalendarMonth
                    AppTab.TIMER -> Icons.Filled.Timer
                    AppTab.REPORTS -> Icons.Filled.BarChart
                    AppTab.SETTINGS -> Icons.Filled.Settings
                  }
                val label = tab.label(strings)
                NavigationRailItem(
                  selected = state.currentTab == tab,
                  onClick = { viewModel.switchTab(tab) },
                  icon = { Icon(icon, contentDescription = label) },
                  label = { Text(label) },
                )
              }
            }
          }

          Box(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.TopCenter,
          ) {
            LazyColumn(
              state = listState,
              modifier = Modifier.widthIn(max = 600.dp).fillMaxSize(),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              item(key = "header") {
                HeaderCard(
                  selectedDateStr = state.selectedDateStr,
                  isDarkTheme = state.isDarkTheme,
                  strings = strings,
                  onPrevDate = { viewModel.changeDate(-1) },
                  onNextDate = { viewModel.changeDate(1) },
                  onTodayClick = viewModel::setToday,
                  onDatePicked = viewModel::selectDate,
                  onTimerShortcut = { viewModel.switchTab(AppTab.TIMER) },
                  onToggleTheme = viewModel::toggleTheme,
                  onToggleLanguage = viewModel::toggleLanguage,
                )
              }

              item(key = "tab_content_${state.currentTab.name}") {
                when (state.currentTab) {
                  AppTab.TODAY ->
                    TodayScreen(
                      state = state,
                      onSetFasting = viewModel::setFasting,
                      onSelectPrayer = viewModel::selectPrayer,
                      onTogglePrayerStatus = viewModel::toggleCurrentPrayerStatus,
                      onSetPrayerDelay = viewModel::setPrayerDelay,
                      onToggleRawatib = viewModel::toggleCurrentRawatib,
                      onTogglePrayerQuality = viewModel::togglePrayerQuality,
                      onSetPrayerExtraRakats = viewModel::setPrayerExtraRakats,
                      onToggleNafl = viewModel::toggleNafl,
                      onSetNaflCount = viewModel::setNaflCount,
                      onUpdateQuran = viewModel::updateQuran,
                      onToggleAdhkar = viewModel::toggleAdhkar,
                      onUpdateDhikr = viewModel::updateDhikr,
                      onToggleTruth = viewModel::toggleTruth,
                      onTogglePatience = viewModel::togglePatience,
                      onUpdateBookPages = viewModel::updateBookPages,
                      onGoToBookSettings = viewModel::goToBookSettings,
                      onSetSelfAnalysisAnswer = viewModel::setSelfAnalysisAnswer,
                    )
                  AppTab.CALENDAR ->
                    CalendarScreen(
                      state = state,
                      onSelectDate = viewModel::selectDate,
                      onOpenDateInToday = viewModel::openDateInToday,
                    )
                  AppTab.TIMER ->
                    TimerScreen(
                      state = state,
                      onStartTimer = viewModel::startCarelessnessTimer,
                      onStopTimer = viewModel::stopCarelessnessTimer,
                      onDeleteSession = viewModel::deleteTimerSession,
                    )
                  AppTab.REPORTS ->
                    ReportsScreen(
                      state = state,
                      onSelectPeriod = viewModel::setReportPeriod,
                    )
                  AppTab.SETTINGS ->
                    SettingsScreen(
                      state = state,
                      onSelectLanguage = viewModel::setLanguage,
                      onAddBook = viewModel::addBook,
                      onUpdateBook = viewModel::updateBook,
                      onDeleteBook = viewModel::deleteBook,
                      onSaveRawatibConfig = viewModel::updateRawatibConfig,
                      onAddQuestion = viewModel::addQuestion,
                      onUpdateQuestion = viewModel::updateQuestionText,
                      onDeleteQuestion = viewModel::deleteQuestion,
                      onExportJson = {
                        exportLauncher.launch("Muhasaba_Backup_${DateUtils.todayStr()}.json")
                      },
                      onImportJson = {
                        importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                      },
                      onShareBackup = {
                        val json = viewModel.buildBackupJson()
                        val sendIntent =
                          Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, strings.shareBackupSubject)
                            putExtra(Intent.EXTRA_TEXT, json)
                          }
                        context.startActivity(
                          Intent.createChooser(sendIntent, strings.shareBackupChooser)
                        )
                      },
                    )
                }
              }

              item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(12.dp)) }
            }
          }
        }
      }
    }
  }
}
