package com.azhar.note.utils

import com.azhar.note.model.ModelNote

/**
 * Created by Azhar Rivaldi on 6/11/2020.
 */

interface onClickItemListener {
    fun onClick(modelNote: ModelNote, position: Int)
}