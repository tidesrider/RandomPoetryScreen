package online.dicemeow.dev_randompoetryscreen.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import online.dicemeow.dev_randompoetryscreen.AppContainer
import online.dicemeow.dev_randompoetryscreen.ui.anthology.AnthologyContentScreen
import online.dicemeow.dev_randompoetryscreen.ui.anthology.AnthologyListScreen
import online.dicemeow.dev_randompoetryscreen.ui.config.ConfigScreen
import online.dicemeow.dev_randompoetryscreen.ui.editor.EditScreen
import online.dicemeow.dev_randompoetryscreen.ui.main.MainScreen

@Composable
fun AppNavigation(appContainer: AppContainer) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.Main.route) {
        composable(Route.Main.route) {
            MainScreen(
                onNavigateToAnthologyList = { navController.navigate(Route.AnthologyList.route) },
                onNavigateToConfig = { navController.navigate(Route.Config.route) },
                appContainer = appContainer
            )
        }

        composable(Route.AnthologyList.route) {
            AnthologyListScreen(
                onBack = { navController.popBackStack() },
                onOpenAnthology = { id -> navController.navigate(Route.AnthologyContent.createRoute(id)) },
                onCreateAndEdit = { id ->
                    navController.navigate(Route.AnthologyContent.createRoute(id))
                    navController.navigate(Route.Edit.createRoute(id, "new"))
                },
                appContainer = appContainer
            )
        }

        composable(
            route = Route.AnthologyContent.route,
            arguments = listOf(navArgument("anthologyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val anthologyId = backStackEntry.arguments?.getString("anthologyId") ?: return@composable
            AnthologyContentScreen(
                anthologyId = anthologyId,
                onBack = { navController.popBackStack() },
                onEditEntry = { aId, eId -> navController.navigate(Route.Edit.createRoute(aId, eId)) },
                appContainer = appContainer
            )
        }

        composable(
            route = Route.Edit.route,
            arguments = listOf(
                navArgument("anthologyId") { type = NavType.StringType },
                navArgument("entryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val anthologyId = backStackEntry.arguments?.getString("anthologyId") ?: return@composable
            val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
            EditScreen(
                anthologyId = anthologyId,
                entryId = entryId,
                onBack = { navController.popBackStack() },
                appContainer = appContainer
            )
        }

        composable(Route.Config.route) {
            ConfigScreen(
                onBack = { navController.popBackStack() },
                appContainer = appContainer
            )
        }
    }
}
