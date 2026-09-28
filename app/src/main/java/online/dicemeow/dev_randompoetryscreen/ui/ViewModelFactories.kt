package online.dicemeow.dev_randompoetryscreen.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import online.dicemeow.dev_randompoetryscreen.AppContainer
import online.dicemeow.dev_randompoetryscreen.ui.anthology.AnthologyContentViewModel
import online.dicemeow.dev_randompoetryscreen.ui.anthology.AnthologyListViewModel
import online.dicemeow.dev_randompoetryscreen.ui.config.ConfigViewModel
import online.dicemeow.dev_randompoetryscreen.ui.editor.EditViewModel
import online.dicemeow.dev_randompoetryscreen.ui.main.MainViewModel

object ViewModelFactories {

    fun mainFactory(container: AppContainer): ViewModelProvider.Factory =
        viewModelFactory {
            initializer { MainViewModel(container.poetryRepository, container.preferencesStore) }
        }

    fun anthologyListFactory(container: AppContainer): ViewModelProvider.Factory =
        viewModelFactory {
            initializer { AnthologyListViewModel(container.poetryRepository) }
        }

    fun anthologyContentFactory(container: AppContainer, anthologyId: String): ViewModelProvider.Factory =
        viewModelFactory {
            initializer { AnthologyContentViewModel(container.poetryRepository, anthologyId) }
        }

    fun editFactory(container: AppContainer, anthologyId: String, entryId: String): ViewModelProvider.Factory =
        viewModelFactory {
            initializer { EditViewModel(container.poetryRepository, anthologyId, entryId) }
        }

    fun configFactory(container: AppContainer): ViewModelProvider.Factory =
        viewModelFactory {
            initializer { ConfigViewModel(container.preferencesStore, container.poetryRepository) }
        }
}
