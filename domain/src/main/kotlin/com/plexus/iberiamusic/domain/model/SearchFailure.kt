package com.plexus.iberiamusic.domain.model

/** Why a search failed, as far as the user can tell and act on. */
enum class SearchFailure {
  /** The request never got an answer: no connection, timeout, DNS. Checking the network helps. */
  NETWORK,

  /** The service answered, but with an error or with data that cannot be understood. */
  SERVER,

  /** Anything else, such as a bug on our side. */
  UNKNOWN,
}
