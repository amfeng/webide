package code.util

import javax.tools._
import JavaFileObject._
import java.net.URI
import java.io._

class StringJavaFileObject(name: String, code: String) 
  extends SimpleJavaFileObject(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE) {
  override def getCharContent(ignoreEncodingErrors: Boolean) = code
}

object JavacUtil {
  val javac = ToolProvider.getSystemJavaCompiler
  assert(javac ne null)

  def compile(source: String): Option[List[String]] = {
    val errorCollector = new DiagnosticCollector[JavaFileObject]
    val fileManager = javac.getStandardFileManager(errorCollector, null, null)
    println("Compiling: " + source)
    val files = new java.util.ArrayList[JavaFileObject]
    files.add(new StringJavaFileObject("Foo", source))
    val success = javac.getTask(null, fileManager, errorCollector, null, null, files).call()
    val buf = new scala.collection.mutable.ListBuffer[String]
    val errors = errorCollector.getDiagnostics.iterator
    while (errors.hasNext) {
      val error = errors.next()
      println("Error: " + error.getMessage(null))
      buf.append(error.getMessage(null))
    }


    if (success.booleanValue)
        None
    else
        Some(buf.toList)
  }

  def run(mainClass: String): String = {
    val runtime = Runtime.getRuntime 
    val process = runtime.exec("java %s".format(mainClass))
    println("new process created")
    val inputStream = new BufferedInputStream(process.getInputStream)
    var cur = inputStream.read()
    val baos = new ByteArrayOutputStream(1024)
    while (cur != -1) {
      baos.write(cur)
      cur = inputStream.read()
    }
    inputStream.close()
    new String(baos.toByteArray)
  }


  val defaultCode = """
  |public class Foo {
  |  public static void main(String[] args) {
  |    System.out.println("Hello, web IDE");
  |  }
  |}
  """.stripMargin

}
