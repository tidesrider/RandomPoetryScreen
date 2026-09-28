package online.dicemeow.dev_randompoetryscreen

import android.content.Context
import online.dicemeow.dev_randompoetryscreen.data.importer.FileImportService
import online.dicemeow.dev_randompoetryscreen.data.importer.RdpoemFileService
import online.dicemeow.dev_randompoetryscreen.data.repository.PoetryRepository
import online.dicemeow.dev_randompoetryscreen.data.store.PreferencesStore

class
AppContainer(private val context: Context) {
    val poetryRepository: PoetryRepository by lazy { PoetryRepository.getInstance(context) }
    val preferencesStore: PreferencesStore by lazy { PreferencesStore(context) }
    val fileImportService: FileImportService by lazy { FileImportService(context) }
    val rdpoemFileService: RdpoemFileService by lazy { RdpoemFileService(context) }
}
