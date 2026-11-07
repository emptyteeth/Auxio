/*
 * Copyright (c) 2024 Auxio Project
 * FolderItem.kt is part of Auxio.
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
 
package org.oxycblt.auxio.home.folders

import org.oxycblt.musikr.Song
import org.oxycblt.musikr.fs.Path

/** An entry shown in the folder browser. */
sealed interface FolderItem {
    /**
     * A directory contained in the currently browsed directory.
     *
     * @param path The path of the directory.
     * @param songCount The number of songs contained in this directory, recursively.
     */
    data class Directory(val path: Path, val songCount: Int) : FolderItem

    /**
     * A song directly contained in the currently browsed directory.
     *
     * @param song The song.
     */
    data class Track(val song: Song) : FolderItem

    /**
     * An entry that navigates to the parent directory.
     *
     * @param path The parent directory to navigate to, or null to show the volume list.
     */
    data class Up(val path: Path?) : FolderItem
}
