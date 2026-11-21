package com.plexus.iberiamusic.domain.model

/**
 * A music track as the app understands it, independent of any API or UI representation. The cover
 * comes in two sizes: [artworkUrl] is a small thumbnail for lists, [largeArtworkUrl] a bigger one.
 */
data class Track(
    val id: Long,
    val name: String,
    val artistName: String,
    val albumName: String? = null,
    val artworkUrl: String? = null,
    val largeArtworkUrl: String? = null,
    val genre: String? = null,
    val durationMillis: Long? = null,
    val releaseDate: String? = null,
)
