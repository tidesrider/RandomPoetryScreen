package online.dicemeow.dev_randompoetryscreen.ui.navigation

sealed class Route(val route: String) {
    data object Main : Route("main")
    data object AnthologyList : Route("anthology_list")
    data object AnthologyContent : Route("anthology_content/{anthologyId}") {
        fun createRoute(anthologyId: String) = "anthology_content/$anthologyId"
    }
    data object Edit : Route("edit/{anthologyId}/{entryId}") {
        fun createRoute(anthologyId: String, entryId: String = "new") = "edit/$anthologyId/$entryId"
    }
    data object Config : Route("config")
}
