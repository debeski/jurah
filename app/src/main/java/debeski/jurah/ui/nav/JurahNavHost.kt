package debeski.jurah.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import debeski.jurah.ui.about.AboutScreen
import debeski.jurah.ui.editor.ScheduleEditorScreen
import debeski.jurah.ui.history.HistoryScreen
import debeski.jurah.ui.licenses.LicensesScreen
import debeski.jurah.ui.schedules.SchedulesScreen

private object Routes {
    const val SCHEDULES = "schedules"
    const val ADD = "add"
    const val ARG_SCHEDULE_ID = "scheduleId"
    const val EDIT = "edit/{$ARG_SCHEDULE_ID}"
    const val HISTORY = "history"
    const val ABOUT = "about"
    const val LICENSES = "licenses"

    fun edit(scheduleId: Long) = "edit/$scheduleId"
}

@Composable
fun JurahNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.SCHEDULES) {
        composable(Routes.SCHEDULES) {
            SchedulesScreen(
                onAddSchedule = { navController.navigate(Routes.ADD) },
                onEditSchedule = { id -> navController.navigate(Routes.edit(id)) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenAbout = { navController.navigate(Routes.ABOUT) },
            )
        }
        composable(Routes.ADD) {
            ScheduleEditorScreen(
                scheduleId = null,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument(Routes.ARG_SCHEDULE_ID) { type = NavType.LongType }),
        ) { entry ->
            ScheduleEditorScreen(
                scheduleId = entry.arguments?.getLong(Routes.ARG_SCHEDULE_ID),
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ABOUT) {
            AboutScreen(
                onBack = { navController.popBackStack() },
                onOpenLicenses = { navController.navigate(Routes.LICENSES) },
            )
        }
        composable(Routes.LICENSES) {
            LicensesScreen(onBack = { navController.popBackStack() })
        }
    }
}
