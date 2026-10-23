package com.sally.liquidfilemanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var root: LinearLayout
    private lateinit var title: TextView
    private lateinit var status: TextView
    private lateinit var listContainer: LinearLayout
    private lateinit var backButton: Button

    private var currentDirectory: DocumentFile? = null
    private val directoryStack = ArrayDeque<DocumentFile>()

    private val openTree =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->

            if (uri == null) {
                showStatus("Pemilihan folder dibatalkan.")
                return@registerForActivityResult
            }

            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            runCatching {
                contentResolver.takePersistableUriPermission(uri, flags)
            }

            val directory = DocumentFile.fromTreeUri(this, uri)

            if (directory == null || !directory.isDirectory) {
                showStatus("Folder tidak dapat dibuka.")
                return@registerForActivityResult
            }

            directoryStack.clear()
            renderDirectory(directory)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildLayout()

        val nativeStatus = runCatching {
            NativeBridge().nativeVersion()
        }.getOrElse {
            "Rust core belum tersedia. File Engine Android tetap aktif."
        }

        showStatus(nativeStatus)
    }

    private fun buildLayout() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(12))
        }

        title = TextView(this).apply {
            text = "Liquid File Manager"
            textSize = 25f
            setTextColor(0xFF18212B.toInt())
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val subtitle = TextView(this).apply {
            text = "File Engine • AndroidX • SAF"
            textSize = 14f
            setTextColor(0xFF65717D.toInt())
            setPadding(0, dp(4), 0, dp(12))
        }

        root.addView(subtitle)

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val chooseButton = Button(this).apply {
            text = "Pilih folder"
            setOnClickListener {
                openTree.launch(null)
            }
        }

        backButton = Button(this).apply {
            text = "Kembali"
            isEnabled = false
            setOnClickListener {
                goUp()
            }
        }

        toolbar.addView(
            chooseButton,
            LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                marginEnd = dp(6)
            }
        )

        toolbar.addView(
            backButton,
            LinearLayout.LayoutParams(0, dp(48), 1f)
        )

        root.addView(toolbar)

        status = TextView(this).apply {
            textSize = 12f
            setTextColor(0xFF52606D.toInt())
            setPadding(0, dp(10), 0, dp(10))
        }

        root.addView(status)

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
        }

        listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scrollView.addView(listContainer)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun renderDirectory(directory: DocumentFile) {
        currentDirectory = directory
        title.text = directory.name ?: "Folder"

        listContainer.removeAllViews()

        val children = try {
            directory.listFiles().sortedWith(
                compareByDescending<DocumentFile> { it.isDirectory }
                    .thenBy {
                        it.name.orEmpty().lowercase(Locale.ROOT)
                    }
            )
        } catch (error: Exception) {
            showStatus(
                "Gagal membaca folder: " +
                    (error.message ?: "akses ditolak")
            )
            emptyList()
        }

        backButton.isEnabled = directoryStack.isNotEmpty()

        showStatus(
            "${children.size} item\n${directory.uri}"
        )

        if (children.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "Folder ini kosong."
                textSize = 15f
                setPadding(dp(8), dp(24), dp(8), dp(24))
            }

            listContainer.addView(emptyView)
            return
        }

        children.forEach { file ->
            listContainer.addView(createFileRow(file, directory))
        }
    }

    private fun createFileRow(
        file: DocumentFile,
        parent: DocumentFile
    ): View {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(0xFFF3F5F7.toInt())
            isClickable = true
            isFocusable = true

            setOnClickListener {
                if (file.isDirectory) {
                    directoryStack.addLast(parent)
                    renderDirectory(file)
                } else {
                    showFileDetails(file)
                }
            }
        }

        val nameView = TextView(this).apply {
            val icon = if (file.isDirectory) "📁" else "📄"

            text = "$icon  ${file.name ?: "(tanpa nama)"}"
            textSize = 16f
            setTextColor(0xFF18212B.toInt())
        }

        val metadataView = TextView(this).apply {
            val size = if (file.isDirectory) {
                "Folder"
            } else {
                formatSize(file.length())
            }

            val modified = DateFormat.getDateTimeInstance()
                .format(Date(file.lastModified()))

            text = "$size • $modified"
            textSize = 12f
            setTextColor(0xFF687582.toInt())
            setPadding(0, dp(4), 0, 0)
        }

        row.addView(nameView)
        row.addView(metadataView)

        return row.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(6)
            }
        }
    }

    private fun showFileDetails(file: DocumentFile) {
        val details = buildString {
            appendLine("Nama: ${file.name ?: "(tanpa nama)"}")
            appendLine("Tipe: ${file.type ?: "Tidak diketahui"}")
            appendLine("Ukuran: ${formatSize(file.length())}")
            appendLine("Dapat dibaca: ${file.canRead()}")
            appendLine("Dapat ditulis: ${file.canWrite()}")
            appendLine(
                "Terakhir diubah: " +
                    DateFormat.getDateTimeInstance()
                        .format(Date(file.lastModified()))
            )
            append("URI: ${file.uri}")
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("Detail file")
            .setMessage(details)
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun goUp() {
        if (directoryStack.isEmpty()) {
            showStatus("Kamu sudah berada di folder awal.")
            return
        }

        renderDirectory(directoryStack.removeLast())
    }

    private fun showStatus(message: String) {
        if (::status.isInitialized) {
            status.text = message
        }
    }

    private fun formatSize(bytes: Long): String {
        if (bytes < 1024) {
            return "$bytes B"
        }

        val units = arrayOf("KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = -1

        do {
            size /= 1024.0
            unitIndex++
        } while (
            size >= 1024.0 &&
            unitIndex < units.lastIndex
        )

        return String.format(
            Locale.US,
            "%.2f %s",
            size,
            units[unitIndex]
        )
    }

    private fun dp(value: Int): Int {
        return (
            value * resources.displayMetrics.density
        ).toInt()
    }
}