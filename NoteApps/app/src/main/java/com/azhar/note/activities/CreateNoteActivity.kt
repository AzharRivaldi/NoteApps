package com.azhar.note.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.azhar.modelNote.R
import com.azhar.note.database.NoteDatabase
import com.azhar.note.model.ModelNote
import kotlinx.android.synthetic.main.activity_create_note.*
import kotlinx.android.synthetic.main.layout_delete.*
import kotlinx.android.synthetic.main.layout_url.*
import kotlinx.android.synthetic.main.layout_url.view.*
import java.text.SimpleDateFormat
import java.util.*

class CreateNoteActivity : AppCompatActivity() {

    var alertDialog: AlertDialog? = null
    var selectImagePath: String? = null
    var modelNoteExtra: ModelNote? = null

    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        //EEEE, dd MMMM yyyy HH:mm a
        //Hari, Tanggal bulan tahun, jam a = malam m = pagi
        tvDateTime.setText("Terakhir diubah : " + SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date()))

        //image Path
        selectImagePath = ""

        if (intent.getBooleanExtra("EXTRA", false)) {
            modelNoteExtra = intent.getSerializableExtra("EXTRA_NOTE") as ModelNote
            setViewOrUpdateNote()
        }

        if (modelNoteExtra != null) {
            linearDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener {
                showDeleteDialog()
            }
        }

        btnHapusUrl.setOnClickListener {
            tvUrlNote.setText(null)
            tvUrlNote.setVisibility(View.GONE)
            btnHapusUrl.setVisibility(View.GONE)
        }

        btnAddUrl.setOnClickListener {
            showDialogUrl()
        }

        btnAddImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(applicationContext,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@CreateNoteActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
            } else {
                selectImage()
            }
        }

        fabDeleteImage.setOnClickListener {
            imageNote.setImageBitmap(null)
            imageNote.setVisibility(View.GONE)
            fabDeleteImage.setVisibility(View.GONE)
            selectImagePath = ""
        }

        fabSaveNote.setOnClickListener(View.OnClickListener {
            if (editTextTitle.getText().toString().isEmpty()) {
                Toast.makeText(this@CreateNoteActivity, "Judul Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            } else if (editTextSubTitle.getText().toString().isEmpty() && editTextDesc.getText().toString().isEmpty()) {
                Toast.makeText(this@CreateNoteActivity, "Catatan Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            val modelNote = ModelNote()
            modelNote.title = editTextTitle.getText().toString()
            modelNote.subTitle = editTextSubTitle.getText().toString()
            modelNote.noteText = editTextDesc.getText().toString()
            modelNote.dateTime = tvDateTime.getText().toString()
            modelNote.imagePath = selectImagePath

            if (tvUrlNote.getVisibility() == View.VISIBLE) {
                modelNote.url = tvUrlNote.getText().toString()
                btnHapusUrl.visibility = View.VISIBLE
            }

            if (modelNoteExtra != null) {
                modelNote.id = modelNoteExtra!!.id
            }

            class saveNoteAsyncTask : AsyncTask<Void?, Void?, Void?>() {
                override fun doInBackground(vararg p0: Void?): Void? {
                    NoteDatabase.getInstance(applicationContext)?.noteDao()?.insert(modelNote)
                    return null
                }

                override fun onPostExecute(aVoid: Void?) {
                    super.onPostExecute(aVoid)
                    val intent = Intent()
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
            saveNoteAsyncTask().execute()
        })
    }

    @SuppressLint("RestrictedApi")
    private fun setViewOrUpdateNote() {
        editTextTitle.setText(modelNoteExtra?.title)
        editTextSubTitle.setText(modelNoteExtra?.subTitle)
        editTextDesc.setText(modelNoteExtra?.noteText)

        if (modelNoteExtra?.imagePath != null && modelNoteExtra?.imagePath?.trim()?.isEmpty()!!) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(modelNoteExtra?.imagePath))
            imageNote.visibility = View.VISIBLE
            selectImagePath = modelNoteExtra?.imagePath
            fabDeleteImage.visibility = View.VISIBLE
        }

        if (modelNoteExtra?.url != null && modelNoteExtra?.url?.trim()?.isEmpty()!!) {
            tvUrlNote.text = modelNoteExtra?.url
            tvUrlNote.visibility = View.VISIBLE
            btnHapusUrl.visibility = View.VISIBLE
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_SELECT)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT && resultCode == RESULT_OK) {
            if (data != null) {
                val selectImgUri = data.data
                if (selectImgUri != null) {
                    try {
                        val inputStream = contentResolver.openInputStream(selectImgUri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        imageNote.setImageBitmap(bitmap)
                        imageNote.visibility = View.VISIBLE
                        fabDeleteImage.visibility = View.VISIBLE
                        selectImagePath = getPathFromUri(selectImgUri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getPathFromUri(contentUri: Uri): String? {
        val filePath: String?
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null) {
            filePath = contentUri.path
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    private fun showDeleteDialog() {
        val dialog = Dialog(this@CreateNoteActivity)
        dialog.setContentView(R.layout.layout_delete)
        dialog.tvHapusCatatan.setOnClickListener {

            class HapusNoteAsyncTask : AsyncTask<Void?, Void?, Void?>() {
                private fun doInBackground(var p0: Void?): Void? {
                    NoteDatabase.getInstance(applicationContext)?.noteDao()?.delete(modelNoteExtra)
                    return null
                }

                private fun onPostExecute(aVoid: Void?) {
                    super.onPostExecute(aVoid)
                    val intent = Intent()
                    intent.putExtra("NoteDelete", true)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
            HapusNoteAsyncTask().execute()
        }
        dialog.tvBatalHapus.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDialogUrl() {
        if (alertDialog == null) {
            val builder = AlertDialog.Builder(this@CreateNoteActivity)
            val view = LayoutInflater.from(this).inflate(R.layout.layout_url, findViewById(R.id.layoutUrl) as? ViewGroup)
            builder.setView(view)

            alertDialog = builder.create()
            if (alertDialog?.window != null) {
                alertDialog?.window?.setBackgroundDrawable(ColorDrawable(0))
            }

            val etUrl = view.editTextAddUrl
            etUrl.requestFocus()

            view.tvOk.setOnClickListener {
                if (etUrl.text.toString().trim().isEmpty()) {
                    Toast.makeText(this@CreateNoteActivity, "Masukan Url", Toast.LENGTH_SHORT).show()
                } else if (!Patterns.WEB_URL.matcher(etUrl.text.toString()).matches()) {
                    Toast.makeText(this@CreateNoteActivity, "Url Anda Tidak Benar", Toast.LENGTH_SHORT).show()
                } else {
                    tvUrlNote.text = etUrl.text.toString()
                    tvUrlNote.visibility = View.VISIBLE
                    btnHapusUrl.visibility = View.VISIBLE
                    alertDialog?.dismiss()
                }
            }

            view.tvBatal.setOnClickListener {
                alertDialog?.dismiss()
            }
        }
        alertDialog?.show()
    }

    companion object {
        private const val REQUEST_PERMISSION = 1
        private const val REQUEST_SELECT = 2
    }

}
