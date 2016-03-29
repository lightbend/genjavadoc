package com.typesafe.genjavadoc
package util

import java.io.File

object IO {

  /** Creates a new temporary directory, guaranteed to be empty. */
  def tempDir(prefix: String): File = {
    val dir = File.createTempFile("genjavadoc-" + prefix, "")
    dir.delete()
    dir.mkdir()
    dir
  }

}
