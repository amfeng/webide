package code.util

import java.util.regex._

object ParseUtils {

  private val pkgPattern = Pattern.compile(
    "^(.*)package((\\w|\\s|\\d|\\.|\\$)*)?;.*$", Pattern.DOTALL) 
  private val clzPattern = Pattern.compile(
    "^(.*?)(class|interface)\\s+((\\w|\\d|\\.|\\$)+).*$", Pattern.DOTALL)

  def getPackageName(code: String): Option[String] = {
    val m = pkgPattern.matcher(code)
    if (m.matches) {
      val tokens = m.group(2).replaceAll("\\s", "")
      Some(tokens)
    }
    else None
  }

  def getClassName(code: String): Option[String] = {
    val m = clzPattern.matcher(code)
    if (m.matches) Some(m.group(3))
    else None
  }

}
