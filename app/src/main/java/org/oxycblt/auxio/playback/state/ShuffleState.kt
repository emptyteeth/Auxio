/*
 * Copyright (c) 2026 Auxio Project
 * ShuffleState.kt is part of Auxio.
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
 
package org.oxycblt.auxio.playback.state

import org.oxycblt.auxio.R

/**
 * The shuffle mode of the current queue.
 *
 * @author Alexander Capehart (OxygenCobalt)
 */
enum class ShuffleState {
    /** Shuffle is disabled, songs play in their original order. */
    OFF,

    /** Shuffle is enabled, songs play in a fixed random permutation. */
    SHUFFLE,

    /**
     * Shuffle is enabled and a new random song is chosen after every track. The current song is
     * never chosen as the next song.
     */
    RANDOM;

    /** Whether the queue is shuffled in any way. */
    val isShuffled
        get() = this != OFF

    /** The icon representing this state. */
    val icon: Int
        get() =
            when (this) {
                OFF -> R.drawable.ic_shuffle_off_24
                SHUFFLE -> R.drawable.ic_shuffle_on_24
                RANDOM -> R.drawable.ic_shuffle_random_24
            }

    /** The next state when the shuffle button is pressed. */
    fun increment() =
        when (this) {
            OFF -> SHUFFLE
            SHUFFLE -> RANDOM
            RANDOM -> OFF
        }
}
