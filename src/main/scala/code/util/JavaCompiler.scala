package code.util

import javax.tools._
import JavaFileObject._
import java.net.URI
import java.io._

import scala.collection.JavaConversions._

class StringJavaFileObject(name: String, code: String) 
  extends SimpleJavaFileObject(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE) {
  override def getCharContent(ignoreEncodingErrors: Boolean) = code
}

object JavacUtil {
  val javac = ToolProvider.getSystemJavaCompiler
  assert(javac ne null)

  def getJavaFileObjects(files: Seq[File]): Seq[JavaFileObject] = {
    val errorCollector = new DiagnosticCollector[JavaFileObject]
    val fileManager = javac.getStandardFileManager(errorCollector, null, null)
    fileManager.getJavaFileObjectsFromFiles(files).asInstanceOf[java.lang.Iterable[JavaFileObject]].toSeq
  }

  def compile(sourceFiles: Seq[JavaFileObject],
              buildDir: File): Seq[Diagnostic[JavaFileObject]] = {
    println("compile(): sourceFiles = %s, buildDir = %s".format(sourceFiles, buildDir))
    val errorCollector = new DiagnosticCollector[JavaFileObject]
    val fileManager = javac.getStandardFileManager(errorCollector, null, null)
    val javacOpts = Seq("-d", buildDir.getAbsolutePath)
    javac.getTask(null, fileManager, errorCollector, javacOpts, null, sourceFiles).call()
    errorCollector.getDiagnostics.asInstanceOf[java.lang.Iterable[Diagnostic[JavaFileObject]]].toSeq
  }

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

  def run(classpath: File, mainClass: String): String = {
    val process = Runtime.getRuntime.exec("java -cp %s %s".format(classpath.getAbsolutePath, mainClass))
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
