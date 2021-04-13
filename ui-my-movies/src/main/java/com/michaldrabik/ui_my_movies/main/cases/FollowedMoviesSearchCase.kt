package com.michaldrabik.ui_my_movies.main.cases

import com.michaldrabik.common.Config.DEFAULT_LANGUAGE
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_my_movies.mymovies.recycler.MyMoviesItem
import javax.inject.Inject

@AppScope
class FollowedMoviesSearchCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val imagesProvider: MovieImagesProvider
) {

  private val searchCache = mutableListOf<Movie>()
  private val searchTranslationsCache = mutableMapOf<Long, Translation>()

  suspend fun searchFollowed(query: String?): List<MyMoviesItem> {
    if (query.isNullOrBlank()) return emptyList()

    if (searchCache.isEmpty()) {
      val collection = moviesRepository.loadCollection()
      searchCache.clear()
      searchCache.addAll(collection)
    }

    val language = translationsRepository.getLanguage()
    if (searchTranslationsCache.isEmpty() && language != DEFAULT_LANGUAGE) {
      val collection = translationsRepository.loadAllMoviesLocal(language)
      searchTranslationsCache.clear()
      searchTranslationsCache.putAll(collection)
    }

    return searchCache
      .filter {
        it.title.contains(query, true) ||
          searchTranslationsCache[it.traktId]?.title?.contains(query, true) == true
      }
      .sortedBy { it.title }
      .take(30)
      .map {
        val image = imagesProvider.findCachedImage(it, ImageType.FANART)
        MyMoviesItem.createSearchItem(it, image, searchTranslationsCache[it.traktId])
      }
  }

  fun clearCache() {
    searchCache.clear()
    searchTranslationsCache.clear()
  }
}
