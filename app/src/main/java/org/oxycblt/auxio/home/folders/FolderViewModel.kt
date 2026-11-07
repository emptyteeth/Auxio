/*
 * Copyright (c) 2024 Auxio Project
 * FolderViewModel.kt is part of Auxio.
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

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.oxycblt.auxio.music.MusicRepository
import org.oxycblt.musikr.Song
import org.oxycblt.musikr.fs.Components
import org.oxycblt.musikr.fs.Path
import timber.log.Timber as L

/**
 * A [ViewModel] that exposes the music library organized by its directory structure.
 *
 * @author Alexander Capehart (OxygenCobalt)
 */
@HiltViewModel
class FolderViewModel @Inject constructor(private val musicRepository: MusicRepository) :
    ViewModel(), MusicRepository.UpdateListener {
    private val history = ArrayDeque<Path?>()

    private val _path = MutableStateFlow<Path?>(null)
    /** The directory currently being browsed, or null if the volume list is being shown. */
    val path: StateFlow<Path?> = _path

    private val _items = MutableStateFlow(listOf<FolderItem>())
    /** The entries contained in [path]. */
    val items: StateFlow<List<FolderItem>> = _items

    init {
        musicRepository.addUpdateListener(this)
        rebuild()
    }

    override fun onMusicChanges(changes: MusicRepository.Changes) {
        rebuild()
    }

    override fun onCleared() {
        musicRepository.removeUpdateListener(this)
    }

    /**
     * Browse into the given directory.
     *
     * @param directory The directory to browse into.
     */
    fun navigateTo(directory: FolderItem.Directory) {
        navigateTo(directory.path)
    }

    /**
     * Browse into the given [Path], or to the volume list if null.
     *
     * @param path The path to browse into.
     */
    fun navigateTo(path: Path?) {
        L.d("Browsing into $path")
        history.addLast(_path.value)
        _path.value = path
        rebuild()
    }

    /**
     * Browse to the parent directory.
     *
     * @return true if the browser moved up, false if it was already at the top level.
     */
    fun navigateUp(): Boolean {
        if (history.isEmpty()) {
            return false
        }
        val parent = history.removeLast()
        L.d("Browsing back to $parent")
        _path.value = parent
        rebuild()
        return true
    }

    private fun rebuild() {
        val songs = musicRepository.library?.songs ?: emptyList()
        val current = _path.value
        _items.value =
            if (current == null) {
                buildRoot(songs)
            } else {
                buildDirectory(current, songs)
            }
    }

    private fun buildRoot(songs: Collection<Song>): List<FolderItem> {
        return songs
            .groupBy { it.path.volume }
            .map { (volume, volumeSongs) ->
                FolderItem.Directory(Path(volume, Components.root()), volumeSongs.size)
            }
            .sortedBy { it.path.volume.mediaStoreName.orEmpty() }
    }

    private fun buildDirectory(directory: Path, songs: Collection<Song>): List<FolderItem> {
        val directories = LinkedHashMap<Path, Int>()
        val tracks = mutableListOf<FolderItem.Track>()

        for (song in songs) {
            val songPath = song.path
            if (songPath.volume != directory.volume) continue
            if (!directory.components.contains(songPath.components)) continue

            val remaining = directory.components.containing(songPath.components)
            if (remaining.components.size <= 1) {
                // The song is directly contained in this directory.
                tracks += FolderItem.Track(song)
            } else {
                // The song is contained in a child directory.
                val childPath =
                    Path(
                        directory.volume,
                        directory.components.child(remaining.components.first()),
                    )
                directories[childPath] = (directories[childPath] ?: 0) + 1
            }
        }

        val parentItem =
            FolderItem.Up(
                if (directory.components.components.isEmpty()) null else directory.directory
            )
        val directoryItems =
            directories
                .map { (path, count) -> FolderItem.Directory(path, count) }
                .sortedBy { it.path.name.orEmpty() }
        val trackItems = tracks.sortedWith(compareBy { it.song.name })
        return listOf(parentItem) + directoryItems + trackItems
    }
}
