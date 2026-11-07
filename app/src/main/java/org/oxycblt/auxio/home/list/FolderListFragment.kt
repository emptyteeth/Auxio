/*
 * Copyright (c) 2024 Auxio Project
 * FolderListFragment.kt is part of Auxio.
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
 
package org.oxycblt.auxio.home.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import org.oxycblt.auxio.R
import org.oxycblt.auxio.databinding.FragmentHomeListBinding
import org.oxycblt.auxio.databinding.ItemFolderBinding
import org.oxycblt.auxio.home.folders.FolderItem
import org.oxycblt.auxio.home.folders.FolderViewModel
import org.oxycblt.auxio.list.ListViewModel
import org.oxycblt.auxio.list.SelectableListListener
import org.oxycblt.auxio.list.recycler.SongViewHolder
import org.oxycblt.auxio.playback.PlaySong
import org.oxycblt.auxio.playback.PlaybackViewModel
import org.oxycblt.auxio.ui.ViewBindingFragment
import org.oxycblt.auxio.util.collectImmediately
import org.oxycblt.auxio.util.context
import org.oxycblt.auxio.util.getPlural
import org.oxycblt.auxio.util.inflater
import org.oxycblt.musikr.Song
import org.oxycblt.musikr.fs.Path
import timber.log.Timber as L

private const val VIEW_TYPE_DIRECTORY = 100

/**
 * A [ViewBindingFragment] that browses the music library by its directory structure.
 *
 * @author Alexander Capehart (OxygenCobalt)
 */
@AndroidEntryPoint
class FolderListFragment : ViewBindingFragment<FragmentHomeListBinding>() {
    private val folderModel: FolderViewModel by activityViewModels()
    private val listModel: ListViewModel by activityViewModels()
    private val playbackModel: PlaybackViewModel by activityViewModels()
    private val folderAdapter = FolderAdapter()

    private val songListener =
        object : SelectableListListener<Song> {
            override fun onClick(item: Song, viewHolder: RecyclerView.ViewHolder) {
                playFromFolder(item)
            }

            override fun onOpenMenu(item: Song) {
                listModel.openMenu(R.menu.song, item, PlaySong.FromAll)
            }

            // Long-pressing a song opens its menu, since there is no selection mode in the
            // folder browser.
            override fun onSelect(item: Song) {
                listModel.openMenu(R.menu.song, item, PlaySong.FromAll)
            }
        }

    override fun onCreateBinding(inflater: LayoutInflater) =
        FragmentHomeListBinding.inflate(inflater)

    override fun onBindingCreated(binding: FragmentHomeListBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)

        binding.homeRecycler.apply {
            id = R.id.home_folder_recycler
            layoutManager = LinearLayoutManager(requireContext())
            adapter = folderAdapter
        }

        binding.homeNoMusicPlaceholder.apply {
            setImageResource(R.drawable.ic_folder_24)
            contentDescription = getString(R.string.lbl_folders)
        }
        binding.homeNoMusicMsg.text = getString(R.string.lng_empty_folders)
        binding.homeNoMusicAction.isVisible = false

        collectImmediately(folderModel.items, folderModel.path, ::updateItems)
    }

    override fun onDestroyBinding(binding: FragmentHomeListBinding) {
        super.onDestroyBinding(binding)
        binding.homeRecycler.apply {
            adapter = null
            layoutManager = null
        }
    }

    private fun updateItems(items: List<FolderItem>, path: Path?) {
        val binding = requireBinding()
        folderAdapter.submit(items)
        binding.homeRecycler.isInvisible = items.isEmpty()
        binding.homeNoMusic.isInvisible = items.isNotEmpty()
    }

    private fun playFromFolder(song: Song) {
        val songs = folderModel.items.value.filterIsInstance<FolderItem.Track>().map { it.song }
        if (songs.isEmpty()) {
            L.w("No songs to play in the current folder")
            return
        }
        playbackModel.play(songs, song)
    }

    private fun openDirectory(directory: FolderItem.Directory) {
        folderModel.navigateTo(directory)
    }

    private fun openParent(up: FolderItem.Up) {
        folderModel.navigateTo(up.path)
    }

    private inner class FolderAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var items = listOf<FolderItem>()

        fun submit(newItems: List<FolderItem>) {
            val diff =
                DiffUtil.calculateDiff(
                    object : DiffUtil.Callback() {
                        override fun getOldListSize() = items.size

                        override fun getNewListSize() = newItems.size

                        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
                            val old = items[oldPos]
                            val new = newItems[newPos]
                            return when {
                                old is FolderItem.Directory && new is FolderItem.Directory ->
                                    old.path == new.path
                                old is FolderItem.Track && new is FolderItem.Track ->
                                    old.song.uid == new.song.uid
                                old is FolderItem.Up && new is FolderItem.Up -> old.path == new.path
                                else -> false
                            }
                        }

                        override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                            items[oldPos] == newItems[newPos]
                    }
                )
            items = newItems
            diff.dispatchUpdatesTo(this)
        }

        override fun getItemCount() = items.size

        override fun getItemViewType(position: Int) =
            when (items[position]) {
                is FolderItem.Directory,
                is FolderItem.Up -> VIEW_TYPE_DIRECTORY
                is FolderItem.Track -> SongViewHolder.VIEW_TYPE
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            when (viewType) {
                VIEW_TYPE_DIRECTORY ->
                    DirectoryViewHolder(
                        ItemFolderBinding.inflate(parent.context.inflater, parent, false)
                    )
                else -> SongViewHolder.from(parent)
            }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val item = items[position]) {
                is FolderItem.Directory -> {
                    (holder as DirectoryViewHolder).bind(item, ::openDirectory)
                }
                is FolderItem.Track -> {
                    (holder as SongViewHolder).bind(item.song, songListener)
                }
                is FolderItem.Up -> {
                    (holder as DirectoryViewHolder).bindUp(item, ::openParent)
                }
            }
        }
    }

    private class DirectoryViewHolder(private val binding: ItemFolderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(directory: FolderItem.Directory, onClick: (FolderItem.Directory) -> Unit) {
            val context = binding.context
            binding.folderName.text =
                directory.path.name ?: directory.path.volume.resolveName(context)
            binding.folderInfo.isVisible = true
            binding.folderInfo.text =
                context.getPlural(R.plurals.fmt_song_count, directory.songCount)
            binding.root.setOnClickListener { onClick(directory) }
        }

        fun bindUp(up: FolderItem.Up, onClick: (FolderItem.Up) -> Unit) {
            binding.folderName.text = binding.context.getString(R.string.lbl_parent_folder)
            binding.folderInfo.isVisible = false
            binding.root.setOnClickListener { onClick(up) }
        }
    }
}
