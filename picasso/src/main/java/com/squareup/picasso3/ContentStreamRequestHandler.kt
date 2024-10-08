/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.picasso3

import android.content.ContentResolver
import android.content.Context
import android.media.ExifInterface
import android.media.ExifInterface.ORIENTATION_NORMAL
import android.media.ExifInterface.TAG_ORIENTATION
import android.net.Uri
import android.os.Build
import com.squareup.picasso3.Picasso.LoadedFrom.DISK
import okio.Source
import okio.source
import java.io.FileNotFoundException
import java.io.IOException

internal open class ContentStreamRequestHandler(@JvmField val context: Context) : RequestHandler() {
  override fun canHandleRequest(data: Request): Boolean =
    ContentResolver.SCHEME_CONTENT == data.uri?.scheme ?: false

  override fun load(
    picasso: Picasso,
    request: Request,
    callback: Callback
  ) {

    var signaledCallback = false
    try {
      val requestUri = checkNotNull(request.uri)
      val source = getSource(requestUri)
      val bitmap = BitmapUtils.decodeStream(source, request)
      val exifRotation = getExifOrientation(requestUri)
      signaledCallback = true
      callback.onSuccess(Result.Bitmap(bitmap, DISK, exifRotation))
    } catch (e: Exception) {
      if (!signaledCallback) {
        callback.onError(e)
      }
    }
  }

  @Throws(FileNotFoundException::class) fun getSource(uri: Uri): Source {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(uri)
        ?: throw FileNotFoundException("can't open input stream, uri: $uri")
    return inputStream.source()
  }

  @Throws(IOException::class)
  protected open fun getExifOrientation(uri: Uri): Int {
    val contentResolver = context.contentResolver
    contentResolver.openInputStream(uri)?.use { input ->
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        ExifInterface(input).getAttributeInt(TAG_ORIENTATION, ORIENTATION_NORMAL)
      } else {
        TODO("VERSION.SDK_INT < N")
      }
    } ?: throw FileNotFoundException("can't open input stream, uri: $uri")
  }
}