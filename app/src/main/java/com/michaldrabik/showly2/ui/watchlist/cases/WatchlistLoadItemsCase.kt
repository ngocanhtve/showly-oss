package com.michaldrabik.showly2.ui.watchlist.cases

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.model.SortOrder.EPISODES_LEFT
import com.michaldrabik.showly2.model.SortOrder.NAME
import com.michaldrabik.showly2.model.SortOrder.RECENTLY_WATCHED
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.PinnedItemsRepository
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.nowUtc
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.EpisodeWatchlist
import java.util.Locale.ROOT
import javax.inject.Inject

@AppScope
class WatchlistLoadItemsCase @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository
) {

  suspend fun loadMyShows() = showsRepository.myShows.loadAll()

  suspend fun loadWatchlistItem(show: Show): WatchlistItem {
    val episodes = database.episodesDao().getAllForShowWatchlist(show.traktId)
    val seasons = database.seasonsDao().getAllForShow(show.traktId)

    val episodesCount = episodes.count()
    val unwatchedEpisodes = episodes.filter { !it.isWatched }
    val unwatchedEpisodesCount = unwatchedEpisodes.count()

    val nextEpisode = unwatchedEpisodes
      .sortedWith(compareBy<EpisodeWatchlist> { it.seasonNumber }.thenBy { it.episodeNumber })
      .firstOrNull() ?: return WatchlistItem.EMPTY

    val upcomingEpisode = unwatchedEpisodes
      .filter { it.firstAired != null }
      .sortedBy { it.firstAired }
      .firstOrNull { it.firstAired?.isAfter(nowUtc()) == true }

    val isPinned = pinnedItemsRepository.isItemPinned(show.traktId)
    val season = seasons.first { it.idTrakt == nextEpisode.idSeason }
    val episode = database.episodesDao().getById(nextEpisode.idTrakt)
    val upEpisode = upcomingEpisode?.let {
      val epDb = database.episodesDao().getById(it.idTrakt)
      mappers.episode.fromDatabase(epDb)
    } ?: Episode.EMPTY

    return WatchlistItem(
      show,
      mappers.season.fromDatabase(season),
      mappers.episode.fromDatabase(episode),
      upEpisode,
      Image.createUnavailable(ImageType.POSTER),
      episodesCount,
      episodesCount - unwatchedEpisodesCount,
      isPinned = isPinned
    )
  }

  fun prepareWatchlistItems(
    input: List<WatchlistItem>,
    searchQuery: String,
    sortOrder: SortOrder
  ): List<WatchlistItem> {
    val items = input
      .filter { it.episodesCount != 0 && it.episode.firstAired != null }
      .groupBy { it.episode.hasAired(it.season) }

    val aired = (items[true] ?: emptyList())
      .sortedWith(when (sortOrder) {
        NAME -> compareBy { it.show.title.toUpperCase(ROOT) }
        RECENTLY_WATCHED -> compareByDescending { it.show.updatedAt }
        EPISODES_LEFT -> compareBy { it.episodesCount - it.watchedEpisodesCount }
        else -> throw IllegalStateException("Invalid sort order")
      })

    val notAired = (items[false] ?: emptyList())
      .sortedBy { it.episode.firstAired?.toInstant()?.toEpochMilli() }

    return (aired + notAired)
      .filter {
        if (searchQuery.isBlank()) true
        else it.show.title.contains(searchQuery, true) || it.episode.title.contains(searchQuery, true)
      }
      .toMutableList()
  }
}
