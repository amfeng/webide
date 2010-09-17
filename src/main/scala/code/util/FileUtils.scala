package code.util

import java.io._

object FileUtils {
  def newTempDir(): File = {
    val t = File.createTempFile("webide", "builddir")
    t.delete()
    t.mkdirs()
    t
  }
}
