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

case class CompileError(lineNum: Long, columnNum: Long, error: String)

object JavacUtil {
  val javac = ToolProvider.getSystemJavaCompiler
  assert(javac ne null)

  def compile(buildDir: File, source: String): Option[List[CompileError]] = {
    val errorCollector = new DiagnosticCollector[JavaFileObject]
    val fileManager = javac.getStandardFileManager(errorCollector, null, null)
    println("Compiling: " + source)
    val files = Seq(new StringJavaFileObject("Foo", source)) // TODO: parse for class name
    val javacOpts = Seq("-d", buildDir.getAbsolutePath) // set buildDir
    val success = synchronized {
      // javac does not allow concurrent access
      javac.getTask(null, fileManager, errorCollector, javacOpts, null, files).call()
    }
    val errors = errorCollector.getDiagnostics.iterator.map(diag =>
      CompileError(diag.getLineNumber, diag.getColumnNumber, diag.getMessage(null))).toList
    if (success.booleanValue) None
    else Some(errors)
  }

  def run(classpath: File, mainClass: String): Process = {
    val process = Runtime.getRuntime.exec("java -cp %s %s".format(classpath.getAbsolutePath, mainClass))
    println("new process created")
    process
  }

  val defaultCode = """
  |public class Foo {
  |  public static void main(String[] args) {
  |    System.out.println("Hello, Yahoo hackathon!");
  |  }
  |}
  """.stripMargin

}
