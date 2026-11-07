/*
 * Copyright (c) 2024 Auxio Project
 * HomeTab.kt is part of Auxio.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
 
package org.oxycblt.auxio.home.tabs

import org.oxycblt.auxio.R

/**
 * The kind of content shown by a home [Tab].
 *
 * Unlike [org.oxycblt.auxio.music.MusicType], this also includes tabs that are not backed by a
 * music library collection, such as folders and the playback queue.
 *
 * @author Alexander Capehart (OxygenCobalt)
 */
enum class HomeTab {
    SONGS,
    ALBUMS,
    ARTISTS,
    GENRES,
    PLAYLISTS,
    FOLDERS,
    QUEUE;

    /** The string resource used to name this tab. */
    val nameRes: Int
        get() =
            when (this) {
                SONGS -> R.string.lbl_songs
                ALBUMS -> R.string.lbl_albums
                ARTISTS -> R.string.lbl_artists
                GENRES -> R.string.lbl_genres
                PLAYLISTS -> R.string.lbl_playlists
                FOLDERS -> R.string.lbl_folders
                QUEUE -> R.string.lbl_queue
            }
}
