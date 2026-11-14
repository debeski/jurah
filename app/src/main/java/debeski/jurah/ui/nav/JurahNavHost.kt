package debeski.jurah.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import debeski.jurah.ui.about.AboutScreen
import debeski.jurah.ui.add.AddScheduleScreen
import debeski.jurah.ui.history.HistoryScreen
import debeski.jurah.ui.licenses.LicensesScreen
import debeski.jurah.ui.schedules.SchedulesScreen

private object Routes {
    const val SCHEDULES = "schedules"
    const val ADD = "add"
    const val HISTORY = "history"
    const val ABOUT = "about"
    const val LICENSES = "licenses"
}

@Composable
fun JurahNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.SCHEDULES) {
        composable(Routes.SCHEDULES) {
            SchedulesScreen(
                onAddSchedule = { navController.navigate(Routes.ADD) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenAbout = { navController.navigate(Routes.ABOUT) },
            )
        }
        composable(Routes.ADD) {
            AddScheduleScreen(
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
