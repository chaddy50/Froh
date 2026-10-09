package com.chaddy50.froh.data.api.deezer

import com.google.gson.annotations.SerializedName

data class DeezerArtistSearchResponse(
    @SerializedName("data") val artists: List<DeezerArtist>?,
)

data class DeezerArtist(
    @SerializedName("picture_big") val pictureUrl: String?,
)
