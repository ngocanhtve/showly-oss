package com.michaldrabik.data_remote

import java.time.Duration

object Config {
  const val TRAKT_VERSION = "2"
  const val TRAKT_BASE_URL = "https://api.trakt.tv/"
  const val TRAKT_CLIENT_ID = BuildConfig.TRAKT_CLIENT_ID
  const val TRAKT_CLIENT_SECRET = BuildConfig.TRAKT_CLIENT_SECRET
  const val TRAKT_REDIRECT_URL = "showlyoss://trakt"
  const val TRAKT_AUTHORIZE_URL = "https://trakt.tv/oauth/authorize?response_type=code&client_id=$TRAKT_CLIENT_ID&redirect_uri=$TRAKT_REDIRECT_URL"
  val TRAKT_TOKEN_REFRESH_DURATION: Duration = Duration.ofDays(30)

  const val TRAKT_POPULAR_SHOWS_LIMIT = 100
  const val TRAKT_POPULAR_MOVIES_LIMIT = 50
  const val TRAKT_TRENDING_SHOWS_LIMIT = 298
  const val TRAKT_TRENDING_MOVIES_LIMIT = 252
  const val TRAKT_ANTICIPATED_SHOWS_LIMIT = 40
  const val TRAKT_ANTICIPATED_MOVIES_LIMIT = 30
  const val TRAKT_RELATED_SHOWS_LIMIT = 20
  const val TRAKT_RELATED_MOVIES_LIMIT = 20
  const val TRAKT_SEARCH_LIMIT = 50
  const val TRAKT_SYNC_PAGE_LIMIT = 100

  const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
  const val TMDB_API_KEY = BuildConfig.TMDB_API_KEY

  const val OMDB_BASE_URL = "https://www.omdbapi.com/"
  const val OMDB_API_KEY = BuildConfig.OMDB_API_KEY

  const val GCLOUD_BASE_URL = "https://showly-gateway-5kzyanut.uc.gateway.dev/"

  const val AWS_BASE_URL = "https://showly2.s3.eu-west-2.amazonaws.com/"
}
