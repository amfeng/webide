package code.util

import java.io._
import java.nio.channels._

object FileUtils {
  def newTempDir(): File = {
    val t = File.createTempFile("webide", "builddir")
    t.delete()
    t.mkdirs()
    t
  }
  def mainClassFromBuildDir(buildDir: File): Option[String] = {
    val candidates = buildDir.listFiles.filter(_.getName == "MAINCLASS")
    if (candidates.isEmpty) None
    else Some(readContentsOfFile(candidates(0)).trim)
  }
  def readContentsOfFile(file: File): String = {
    val inCh = new FileInputStream(file).getChannel
    val baos = new ByteArrayOutputStream(1024)
    val outCh = Channels.newChannel(baos)
    inCh.transferTo(0, inCh.size, outCh)
    inCh.close()
    outCh.close()
    new String(baos.toByteArray)
  }
  def writeContentsToFile(file: File, contents: String) {
    val inCh = new ByteArrayInputStream(contents.getBytes)
    val outCh = new FileOutputStream(file)

    var cur = inCh.read()
    while (cur != -1) {
      outCh.write(cur)
      cur = inCh.read()
    }

    inCh.close()
    outCh.close()
  }
}
