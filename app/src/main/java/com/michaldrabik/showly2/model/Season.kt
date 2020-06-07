package com.michaldrabik.showly2.model

import org.threeten.bp.ZonedDateTime

data class Season(
  val ids: Ids,
  val number: Int,
  val episodeCount: Int,
  val airedEpisodes: Int,
  val title: String,
  val firstAired: ZonedDateTime?,
  val overview: String,
  val episodes: List<Episode>
) {

  companion object {
    val EMPTY = Season(
      ids = Ids.EMPTY,
      number = 0,
      episodeCount = 0,
      airedEpisodes = 0,
      title = "",
      firstAired = null,
      overview = "",
      episodes = listOf()
    )
  }
}
