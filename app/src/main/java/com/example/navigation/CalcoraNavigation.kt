package com.example.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.CalcoraViewModel
import com.example.engine.Routes
import com.example.model.CalculationWorld
import com.example.ui.screens.ArtsCalculatorScreen
import com.example.ui.screens.ArtsDateTimeHubScreen
import com.example.ui.screens.AverageCalculatorScreen
import com.example.ui.screens.BirthdayAgeCalculatorScreen
import com.example.ui.screens.BmiCalculatorScreen
import com.example.ui.screens.BmrCalculatorScreen
import com.example.ui.screens.CgpaCalculatorScreen
import com.example.ui.screens.ChemistryCalculatorScreen
import com.example.ui.screens.CommerceCalculatorScreen
import com.example.ui.screens.CommerceToolScreen
import com.example.ui.screens.DateAddSubtractCalculatorScreen
import com.example.ui.screens.DateDifferenceCalculatorScreen
import com.example.ui.screens.EquationSolverScreen
import com.example.ui.screens.FractionCalculatorScreen
import com.example.ui.screens.GeometryCalculatorScreen
import com.example.ui.screens.GpaCalculatorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MarksCalculatorScreen
import com.example.ui.screens.MatrixCalculatorScreen
import com.example.ui.screens.NumberSystemConverterScreen
import com.example.ui.screens.PercentageCalculatorScreen
import com.example.ui.screens.PhysicsCalculatorScreen
import com.example.ui.screens.ProbabilityCalculatorScreen
import com.example.ui.screens.RatioProportionScreen
import com.example.ui.screens.ScientificCalculatorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatisticsCalculatorScreen
import com.example.ui.screens.ThemesScreen
import com.example.ui.screens.TimeDurationCalculatorScreen
import com.example.ui.screens.ToolsDirectoryScreen
import com.example.ui.screens.UnitConverterScreen
import com.example.ui.screens.UniversalHubScreen
import com.example.ui.screens.VectorCalculatorScreen
import com.example.ui.screens.WeightedAverageCalculatorScreen

private data class BottomNavTab(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

private val bottomTabs = listOf(
  BottomNavTab(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
  BottomNavTab(Routes.TOOLS, "Tools", Icons.Filled.Apps, Icons.Outlined.Apps),
  BottomNavTab(Routes.HISTORY, "History", Icons.Filled.History, Icons.Outlined.History),
  BottomNavTab(Routes.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

private val topLevelRoutes = setOf(Routes.HOME, Routes.TOOLS, Routes.HISTORY, Routes.SETTINGS)

@Composable
fun CalcoraApp(
  viewModel: CalcoraViewModel,
  startDestination: String = Routes.SPLASH,
  splashDurationMs: Long = 1100L,
  navController: NavHostController = rememberNavController(),
) {
  val preferences by viewModel.preferences.collectAsStateWithLifecycle()
  val historyEntries by viewModel.historyEntries.collectAsStateWithLifecycle()
  val memoryValue by viewModel.memoryValue.collectAsStateWithLifecycle()
  val pendingReuse by viewModel.pendingReuseInput.collectAsStateWithLifecycle()

  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route
  val showBottomNav = currentRoute in topLevelRoutes

  // Adaptive Navigation Rail on wide/tablet screens (>= 600dp)
  val isExpandedWidth = LocalConfiguration.current.screenWidthDp >= 600

  fun navigateSingleTop(route: String) {
    navController.navigate(route) {
      launchSingleTop = true
    }
  }

  fun switchBottomTab(route: String) {
    navController.navigate(route) {
      popUpTo(Routes.HOME) {
        saveState = true
      }
      launchSingleTop = true
      restoreState = true
    }
  }

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    bottomBar = {
      if (showBottomNav && !isExpandedWidth) {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
        ) {
          bottomTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
              selected = selected,
              onClick = { switchBottomTab(tab.route) },
              icon = {
                Icon(
                  imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                  contentDescription = tab.label,
                )
              },
              label = { Text(tab.label, maxLines = 1) },
              modifier = Modifier.testTag("nav_${tab.route}"),
            )
          }
        }
      }
    },
  ) { innerPadding ->
    Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      if (showBottomNav && isExpandedWidth) {
        NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
          bottomTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationRailItem(
              selected = selected,
              onClick = { switchBottomTab(tab.route) },
              icon = {
                Icon(
                  imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                  contentDescription = tab.label,
                )
              },
              label = { Text(tab.label) },
              modifier = Modifier.testTag("rail_${tab.route}"),
            )
          }
        }
      }

      Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
          navController = navController,
          startDestination = startDestination,
        ) {
          composable(Routes.SPLASH) {
            SplashScreen(
              splashDurationMs = splashDurationMs,
              onSplashFinished = {
                navController.navigate(Routes.HOME) {
                  popUpTo(Routes.SPLASH) { inclusive = true }
                  launchSingleTop = true
                }
              },
            )
          }

          composable(Routes.HOME) {
            HomeScreen(
              angleMode = preferences.defaultAngleMode,
              onNavigateToWorld = { world ->
                val target =
                  when (world) {
                    CalculationWorld.SCIENCE -> Routes.SCI_CALCULATOR
                    CalculationWorld.COMMERCE -> Routes.COM_CALCULATOR
                    CalculationWorld.ARTS -> Routes.ART_CALCULATOR
                    CalculationWorld.UNIVERSAL -> Routes.UNI_HUB
                  }
                navigateSingleTop(target)
              },
              onNavigateToRoute = { navigateSingleTop(it) },
              onOpenAllTools = { switchBottomTab(Routes.TOOLS) },
            )
          }

          composable(Routes.TOOLS) {
            ToolsDirectoryScreen(
              angleMode = preferences.defaultAngleMode,
              onSelectTool = { tool -> navigateSingleTop(tool.route) },
            )
          }

          composable(Routes.HISTORY) {
            HistoryScreen(
              entries = historyEntries,
              onReuseEntry = { entry ->
                entry.reusableInput?.let { viewModel.requestReuse(entry.route, it) }
                navigateSingleTop(entry.route)
              },
              onDeleteEntry = { viewModel.deleteHistoryEntry(it) },
              onClearAll = { viewModel.clearHistory() },
            )
          }

          composable(Routes.SETTINGS) {
            SettingsScreen(
              preferences = preferences,
              historyCount = historyEntries.size,
              onOpenThemes = { navigateSingleTop(Routes.THEMES) },
              onAppearanceChange = viewModel::setAppearanceMode,
              onPrecisionChange = viewModel::setDecimalPrecision,
              onAngleModeChange = viewModel::setDefaultAngleMode,
              onNumberFormatChange = viewModel::setNumberFormatMode,
              onThousandsSeparatorChange = viewModel::setThousandsSeparator,
              onShowBreakdownChange = viewModel::setShowStepBreakdown,
              onClearHistory = viewModel::clearHistory,
            )
          }

          composable(Routes.THEMES) {
            ThemesScreen(
              selectedTheme = preferences.themeId,
              onSelectTheme = viewModel::setTheme,
              onBack = { navController.popBackStack() },
            )
          }

          // SCIENCE WORLD
          composable(Routes.SCI_CALCULATOR) {
            val reuseExpr = if (pendingReuse?.first == Routes.SCI_CALCULATOR) pendingReuse?.second else null
            ScientificCalculatorScreen(
              preferences = preferences,
              memoryValue = memoryValue,
              reusedExpression = reuseExpr,
              onConsumeReuse = { viewModel.consumeReuse(Routes.SCI_CALCULATOR) },
              onMemoryChange = viewModel::setMemory,
              onRecordHistory = { expr, res, name, cat, route ->
                viewModel.recordHistory(expr, res, name, cat, route, expr.substringBefore(" ("))
              },
              onNavigateToRoute = { navigateSingleTop(it) },
              onBack = { navController.popBackStack() },
            )
          }
          composable(Routes.SCI_FRACTION) {
            FractionCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.SCI_EQUATION) {
            EquationSolverScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.SCI_MATRIX) {
            MatrixCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.SCI_VECTOR) {
            VectorCalculatorScreen(preferences.defaultAngleMode, preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.SCI_STATISTICS) {
            StatisticsCalculatorScreen("Science", Routes.SCI_STATISTICS, preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.SCI_PROBABILITY) {
            ProbabilityCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.SCI_PHYSICS) {
            PhysicsCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.SCI_CHEMISTRY) {
            ChemistryCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }

          // COMMERCE WORLD
          composable(Routes.COM_CALCULATOR) {
            val reuseExpr = if (pendingReuse?.first == Routes.COM_CALCULATOR) pendingReuse?.second else null
            CommerceCalculatorScreen(
              preferences = preferences,
              memoryValue = memoryValue,
              reusedExpression = reuseExpr,
              onConsumeReuse = { viewModel.consumeReuse(Routes.COM_CALCULATOR) },
              onMemoryChange = viewModel::setMemory,
              onRecordHistory = { expr, res, name, cat, route ->
                viewModel.recordHistory(expr, res, name, cat, route, expr)
              },
              onNavigateToRoute = { navigateSingleTop(it) },
              onBack = { navController.popBackStack() },
            )
          }
          listOf(
            Routes.COM_PROFIT_LOSS,
            Routes.COM_DISCOUNT,
            Routes.COM_MARKUP,
            Routes.COM_VAT_TAX,
            Routes.COM_SIMPLE_INTEREST,
            Routes.COM_COMPOUND_INTEREST,
            Routes.COM_LOAN_EMI,
            Routes.COM_ROI,
            Routes.COM_BREAK_EVEN,
            Routes.COM_COMMISSION,
            Routes.COM_SALARY,
            Routes.COM_REVENUE_EXPENSE,
            Routes.COM_DEPRECIATION,
            Routes.COM_ACCOUNTING,
          ).forEach { comRoute ->
            composable(comRoute) {
              CommerceToolScreen(
                route = comRoute,
                showBreakdown = preferences.showStepBreakdown,
                onRecordHistory = { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) },
                onBack = { navController.popBackStack() },
              )
            }
          }

          // ARTS WORLD
          composable(Routes.ART_CALCULATOR) {
            val reuseExpr = if (pendingReuse?.first == Routes.ART_CALCULATOR) pendingReuse?.second else null
            ArtsCalculatorScreen(
              preferences = preferences,
              memoryValue = memoryValue,
              reusedExpression = reuseExpr,
              onConsumeReuse = { viewModel.consumeReuse(Routes.ART_CALCULATOR) },
              onMemoryChange = viewModel::setMemory,
              onRecordHistory = { expr, res, name, cat, route ->
                viewModel.recordHistory(expr, res, name, cat, route, expr)
              },
              onNavigateToRoute = { navigateSingleTop(it) },
              onBack = { navController.popBackStack() },
            )
          }
          composable(Routes.ART_PERCENTAGE) {
            PercentageCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.ART_AVERAGE) {
            AverageCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.ART_RATIO) {
            RatioProportionScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.ART_STATISTICS) {
            StatisticsCalculatorScreen("Arts", Routes.ART_STATISTICS, preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.ART_DATETIME) {
            ArtsDateTimeHubScreen({ navigateSingleTop(it) }, { navController.popBackStack() })
          }

          // UNIVERSAL WORLD
          composable(Routes.UNI_HUB) {
            UniversalHubScreen({ navigateSingleTop(it) }, { navController.popBackStack() })
          }
          composable(Routes.UNI_AGE) {
            BirthdayAgeCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.UNI_DATE_DIFF) {
            DateDifferenceCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.UNI_DATE_ADD_SUB) {
            DateAddSubtractCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.UNI_TIME_DURATION) {
            TimeDurationCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.UNI_GEOMETRY) {
            GeometryCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.UNI_UNIT_CONVERTER) {
            UnitConverterScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.UNI_NUMBER_SYSTEM) {
            NumberSystemConverterScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.UNI_BMI) {
            BmiCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.UNI_BMR) {
            BmrCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }

          // STUDENT TOOLS
          composable(Routes.STU_GPA) {
            GpaCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.STU_CGPA) {
            CgpaCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.STU_MARKS) {
            MarksCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
          composable(Routes.STU_WEIGHTED_AVG) {
            WeightedAverageCalculatorScreen(preferences.showStepBreakdown, { e, r, n, c, rt -> viewModel.recordHistory(e, r, n, c, rt) }, { navController.popBackStack() })
          }
        }
      }
    }
  }
}
