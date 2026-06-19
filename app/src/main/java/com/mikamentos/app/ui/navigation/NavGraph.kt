package com.mikamentos.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mikamentos.app.ui.agenda.AgendaScreen
import com.mikamentos.app.ui.alarm.AlarmInfoScreen
import com.mikamentos.app.ui.catalog.MedicationCatalogScreen
import com.mikamentos.app.ui.editor.MedicationEditorScreen
import com.mikamentos.app.ui.history.HistoryScreen
import com.mikamentos.app.ui.home.DashboardScreen
import com.mikamentos.app.ui.home.HomeScreen
import com.mikamentos.app.ui.settings.ProfileScreen
import com.mikamentos.app.ui.settings.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val HOME = "home"
    const val EDITOR = "editor"
    const val EDITOR_WITH_ID = "editor/{medicationId}"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val CATALOG = "catalog"
    const val ALARM_INFO = "alarm_info"
    const val HISTORY = "history"
    const val AGENDA = "agenda"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onPlanificar = { navController.navigate(Routes.HOME) },
                onInstrucciones = { navController.navigate(Routes.ALARM_INFO) },
                onCatalogo = { navController.navigate(Routes.CATALOG) },
                onPerfil = { navController.navigate(Routes.PROFILE) },
                onHistorial = { navController.navigate(Routes.HISTORY) },
                onAjustes = { navController.navigate(Routes.SETTINGS) },
                onAgenda = { navController.navigate(Routes.AGENDA) }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onAddMedication = { navController.navigate(Routes.EDITOR) },
                onEditMedication = { id -> navController.navigate("editor/$id") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.EDITOR) {
            MedicationEditorScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.EDITOR_WITH_ID) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getString("medicationId") ?: ""
            MedicationEditorScreen(
                medicationId = medicationId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CATALOG) {
            MedicationCatalogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ALARM_INFO) {
            AlarmInfoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AGENDA) {
            AgendaScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
