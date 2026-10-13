package dev.kuro.app.ui.navigation

/** Single-source destinations for Navigation Compose. */
object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val VAULT = "vault"
    const val IMPORT = "import"
    const val IMPORT_FLOW = "import_flow?source={source}"
    const val ACTIVITY = "activity"
    const val SETTINGS = "settings"
    const val DEBUG_LOGS = "debug_logs"
    const val APP_DETAIL = "app_detail/{appId}"

    fun importFlow(source: String) = "import_flow?source=$source"
    fun appDetail(appId: String) = "app_detail/$appId"
}
