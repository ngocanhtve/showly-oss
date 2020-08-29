package com.michaldrabik.showly2.model.mappers

import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.storage.database.model.Episode
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Season as SeasonNetwork
import com.michaldrabik.storage.database.model.Season as SeasonDb

class SeasonMapper @Inject constructor(
  private val idsMapper: IdsMapper,
  private val episodeMapper: EpisodeMapper
) {

  fun fromNetwork(season: SeasonNetwork) = Season(
    idsMapper.fromNetwork(season.ids),
    season.number ?: -1,
    season.episode_count ?: -1,
    season.aired_episodes ?: -1,
    season.title ?: "",
    if (season.first_aired.isNullOrBlank()) null else ZonedDateTime.parse(season.first_aired),
    season.overview ?: "",
    season.episodes?.map { episodeMapper.fromNetwork(it) } ?: emptyList()
  )

  fun fromDatabase(seasonDb: SeasonDb, episodes: List<Episode> = emptyList()) = Season(
    Ids.EMPTY.copy(trakt = IdTrakt(seasonDb.idTrakt)),
    seasonDb.seasonNumber,
    seasonDb.episodesCount,
    seasonDb.episodesAiredCount,
    seasonDb.seasonTitle,
    seasonDb.seasonFirstAired,
    seasonDb.seasonOverview,
    episodes.map { episodeMapper.fromDatabase(it) }
  )

  fun toDatabase(
    season: Season,
    showId: IdTrakt,
    isWatched: Boolean
  ): SeasonDb {
    return SeasonDb(
      season.ids.trakt.id,
      showId.id,
      season.number,
      season.title,
      season.overview,
      season.firstAired,
      season.episodeCount,
      season.airedEpisodes,
      isWatched
    )
  }
}
