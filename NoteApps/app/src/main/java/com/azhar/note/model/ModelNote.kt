package com.azhar.note.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Created by Azhar Rivaldi on 6/11/2020.
 */

@Entity(tableName = "notes")
class ModelNote : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "title")
    var title: String? = null

    @ColumnInfo(name = "date_time")
    var dateTime: String? = null

    @ColumnInfo(name = "sub_title")
    var subTitle: String? = null

    @ColumnInfo(name = "note_text")
    var noteText: String? = null

    @ColumnInfo(name = "image_path")
    var imagePath: String? = null

    @ColumnInfo(name = "color")
    var color: String? = null

    @ColumnInfo(name = "web_url")
    var url: String? = null
    override fun toString(): String {
        return "$title : $dateTime"
    }
}