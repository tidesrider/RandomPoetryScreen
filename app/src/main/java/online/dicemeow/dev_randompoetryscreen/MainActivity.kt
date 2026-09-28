package online.dicemeow.dev_randompoetryscreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import online.dicemeow.dev_randompoetryscreen.ui.navigation.AppNavigation
import online.dicemeow.dev_randompoetryscreen.ui.theme.Dev_RandomPoetryScreenTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(applicationContext)
        enableEdgeToEdge()
        setContent {
            val config by appContainer.preferencesStore.userConfig.collectAsState(initial = online.dicemeow.dev_randompoetryscreen.data.model.UserConfig())
            Dev_RandomPoetryScreenTheme(colorSchemeStyle = config.colorSchemeStyle) {
                AppNavigation(appContainer = appContainer)
            }
        }
    }
}