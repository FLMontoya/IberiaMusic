package com.plexus.iberiamusic.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.plexus.iberiamusic.data.cache.InMemorySearchCache
import com.plexus.iberiamusic.data.remote.ITunesApi
import com.plexus.iberiamusic.data.repository.SearchRepositoryImpl
import com.plexus.iberiamusic.domain.repository.SearchRepository
import com.plexus.iberiamusic.domain.usecase.SearchTracksUseCase
import com.plexus.iberiamusic.logging.AndroidErrorLogger
import com.plexus.iberiamusic.presentation.search.SearchViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Manual dependency injection: builds the object graph once per process. The single
 * [InMemorySearchCache] lives here so cached searches outlive any one screen or ViewModel.
 */
class AppContainer {

  private val api: ITunesApi =
      Retrofit.Builder()
          .baseUrl(ITunesApi.BASE_URL)
          .client(OkHttpClient())
          .addConverterFactory(
              Json { ignoreUnknownKeys = true }
                  .asConverterFactory("application/json".toMediaType()),
          )
          .build()
          .create(ITunesApi::class.java)

  private val repository: SearchRepository =
      SearchRepositoryImpl(api, InMemorySearchCache(), AndroidErrorLogger)

  private val searchTracks = SearchTracksUseCase(repository)

  val searchViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
    initializer { SearchViewModel(searchTracks) }
  }
}
