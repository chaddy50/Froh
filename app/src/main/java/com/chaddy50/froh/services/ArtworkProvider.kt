package com.chaddy50.froh.services

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileNotFoundException

/**
 * Read-only content provider that exposes artwork files stored in the app's private filesDir
 * to external processes such as Android Auto.
 *
 * URI format: content://com.chaddy50.froh.artwork/<relative-path>
 * e.g.        content://com.chaddy50.froh.artwork/album_artwork/123.jpg
 *             content://com.chaddy50.froh.artwork/artist_portraits/456.jpg
 */
class ArtworkProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        val relativePath = uri.path ?: throw FileNotFoundException("null path")
        val artworkRoot = context!!.filesDir.canonicalFile
        val file = File(artworkRoot, relativePath).canonicalFile
        if (!file.path.startsWith(artworkRoot.path + File.separator)) throw FileNotFoundException(relativePath)
        if (!file.exists()) throw FileNotFoundException(relativePath)
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun getType(uri: Uri): String = "image/jpeg"

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0
}
