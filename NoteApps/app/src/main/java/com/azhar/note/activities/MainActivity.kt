package com.azhar.note.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.azhar.modelNote.R
import com.azhar.note.adapter.NoteAdapter
import com.azhar.note.database.NoteDatabase
import com.azhar.note.model.ModelNote
import com.azhar.note.utils.onClickItemListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), onClickItemListener {

    private val modelNoteList: MutableList<ModelNote> = ArrayList()
    private var noteAdapter: NoteAdapter? = null
    private var onClickPosition = -1

    @SuppressLint("Assert")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        assert(supportActionBar != null)

        fabCreateNote.setOnClickListener {
            startActivityForResult(Intent(this@MainActivity, CreateNoteActivity::class.java), REQUEST_ADD)
        }

        noteAdapter = NoteAdapter(modelNoteList, this)
        rvListNote.setAdapter(noteAdapter)

        //change mode List to Grid
        modeGrid()

        //get Data Catatan
        getNote(REQUEST_SHOW, false)
    }

    private fun modeGrid() {
        rvListNote.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun modeList() {
        rvListNote.layoutManager = LinearLayoutManager(this)
    }

    private fun getNote(requestCode: Int, deleteNote: Boolean) {

        @Suppress("UNCHECKED_CAST")
        class GetNoteAsyncTask : AsyncTask<Void?, Void?, List<ModelNote>>() {
            override fun doInBackground(vararg p0: Void?): List<ModelNote>? {
                return NoteDatabase.getInstance(this@MainActivity)?.noteDao()?.allNote as List<ModelNote>?
            }

            override fun onPostExecute(notes: List<ModelNote>) {
                super.onPostExecute(notes)
                if (requestCode == REQUEST_SHOW) {
                    modelNoteList.addAll(notes)
                    noteAdapter?.notifyDataSetChanged()
                } else if (requestCode == REQUEST_ADD) {
                    modelNoteList.add(0, notes[0])
                    noteAdapter?.notifyItemInserted(0)
                    rvListNote.smoothScrollToPosition(0)
                } else if (requestCode == REQUEST_UPDATE) {
                    modelNoteList.removeAt(onClickPosition)
                    if (deleteNote) {
                        noteAdapter?.notifyItemRemoved(onClickPosition)
                    } else {
                        modelNoteList.add(onClickPosition, notes[onClickPosition])
                        noteAdapter?.notifyItemChanged(onClickPosition)
                    }
                }
            }
        }
        GetNoteAsyncTask().execute()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD && resultCode == RESULT_OK) {
            getNote(REQUEST_ADD, false)
        } else if (requestCode == REQUEST_UPDATE && resultCode == RESULT_OK) {
            if (data != null) {
                getNote(REQUEST_UPDATE, data.getBooleanExtra("NoteDelete", false))
            }
        }
    }

    override fun onClick(modelNote: ModelNote, position: Int) {
        onClickPosition = position
        val intent = Intent(this, CreateNoteActivity::class.java)
        intent.putExtra("EXTRA", true)
        intent.putExtra("EXTRA_NOTE", modelNote)
        startActivityForResult(intent, REQUEST_UPDATE)
    }

    companion object {
        private const val REQUEST_ADD = 1
        private const val REQUEST_UPDATE = 2
        private const val REQUEST_SHOW = 3
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.listView -> modeList()
            R.id.gridView -> modeGrid()
        }
        return super.onOptionsItemSelected(item)
    }

}