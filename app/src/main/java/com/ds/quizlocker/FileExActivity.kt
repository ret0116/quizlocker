package com.ds.quizlocker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_file_ex.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

class FileExActivity : AppCompatActivity() {

    //데이터 저장에 사용할 파일 이름
    val filename = "data.txt"

    var granted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_ex)

        checkPermission()

        loadButton.setOnClickListener {
            val text = textField.text.toString()
            when {
                TextUtils.isEmpty(text) -> {
                    Toast.makeText(applicationContext, "텍스트가 비어있습니다.", Toast.LENGTH_LONG).show()
                }
                !isExternalStrorageWritable() -> {
                    Toast.makeText(applicationContext, "외부 저장장치가 없습니다.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    // saveToInnerStorage(text, filename)
                    // saveToExternalStorage(text, filename)
                    saveToExternalCustomDirectory(text)
                }
            }
        }

        loadButton.setOnClickListener {
            try {
                // textField.setText(loadFromInnerStorage(filename))
                // textField.setText(loadFromExternalStorage(filename))
                textField.setText(loadFromExternalCustomDirectory())
            } catch (e: FileNotFoundException) {
                Toast.makeText(applicationContext, "저장된 텍스트가 없습니다.", Toast.LENGTH_LONG).show()
            }
        }

        fun saveToInnerStorage(text: String, filename: String) {
            val fileOutputStream = openFileOutput(filename, Context.MODE_PRIVATE)
            fileOutputStream.write(text.toByteArray())
            fileOutputStream.close()
        }

        fun loadFromInnerStorage(filename: String): String {
            val fileInputStream = openFileInput(filename)
            return fileInputStream.reader().readText()
        }
    }

    fun isExternalStrorageWritable(): Boolean {
        when {
            Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED -> return true
            else -> return false
        }
    }

    fun getAppDataFileFromExternalStorage(filename: String): File {
        val dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        } else {
            File(Environment.getExternalStorageDirectory().absolutePath + "/Documents")
        }
        dir?.mkdirs()
        return File("${dir?.absolutePath}${File.separator}${filename}")
    }

    fun saveToExternalStorage(text: String, filename: String) {
        val fileOutputStream = FileOutputStream(getAppDataFileFromExternalStorage(filename))
        fileOutputStream.write(text.toByteArray())
        fileOutputStream.close()
    }

    fun loadFromExternalStorage(filename: String): String {
        return FileInputStream(getAppDataFileFromExternalStorage(filename)).reader().readText()
    }

    val MY_PERMISSION_REQUEST = 999

    fun checkPermission() {
        val permissionCheck = ContextCompat.checkSelfPermission(
            this@FileExActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        when {
            permissionCheck != PackageManager.PERMISSION_GRANTED -> {
                ActivityCompat.requestPermissions(
                    this@FileExActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSION_REQUEST
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray){
        when (requestCode) {
            MY_PERMISSION_REQUEST -> {
                when {
                    grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                        granted = true
                    }
                    else -> {
                        granted = false
                    }
                }
            }
        }
    }

    fun saveToExternalCustomDirectory(text: String, filepath: String = "/sdcard/data.txt") {
        when{
            granted -> {
                val fileOutputStream = FileOutputStream(File(filepath))
                fileOutputStream.write(text.toByteArray())
                fileOutputStream.close()
            }
            else -> {
                Toast.makeText(applicationContext,"권한이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun loadFromExternalCustomDirectory(filepath: String = "/sdcard/data.txt"): String {
        when {
            granted -> {
                return FileInputStream(File(filepath)).reader().readText()
            }
            else -> {
                Toast.makeText(applicationContext, "권한이 없습니다.", Toast.LENGTH_SHORT).show()
                return ""
            }
        }
    }
}
