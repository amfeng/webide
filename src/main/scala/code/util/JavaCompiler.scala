package code.util

import javax.tools._
import JavaFileObject._
import java.net.URI
import java.io._

import Diagnostic.{ Kind => DKind }

import scala.collection.JavaConversions._

class StringJavaFileObject(name: String, code: String) 
  extends SimpleJavaFileObject(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE) {
  override def getCharContent(ignoreEncodingErrors: Boolean) = code
}

sealed trait CompileDiagnostic {
  def message: String
  def lineNum: Long
}

case class CompileError(val lineNum: Long, columnNum: Long, error: String)
  extends CompileDiagnostic {
  def message = error
}


case class CompileWarning(val lineNum: Long, columnNum: Long, warning: String)
  extends CompileDiagnostic {
  def message = warning
}

case class CompileResult(success: Boolean, diagnostics: List[CompileDiagnostic])

object JavacUtil {
  val javac = ToolProvider.getSystemJavaCompiler
  assert(javac ne null)

  def compile(buildDir: File, source: String): CompileResult = {
    val errorCollector = new DiagnosticCollector[JavaFileObject]
    val fileManager = javac.getStandardFileManager(errorCollector, null, null)
    println("Compiling: " + source)
    val files = Seq(new StringJavaFileObject("Foo", source)) // TODO: parse for class name
    val javacOpts = Seq("-d", buildDir.getAbsolutePath, "-Xlint:all", "-deprecation") // set buildDir
    val success = synchronized {
      // javac does not allow concurrent access
      javac.getTask(null, fileManager, errorCollector, javacOpts, null, files).call()
    }
    val diags = errorCollector.getDiagnostics.iterator.toList.flatMap(diag =>
      if (diag.getKind == DKind.WARNING || diag.getKind == DKind.MANDATORY_WARNING)
        List(CompileWarning(diag.getLineNumber, diag.getColumnNumber, diag.getMessage(null)))
      else if (diag.getKind == DKind.ERROR)
        List(CompileError(diag.getLineNumber, diag.getColumnNumber, diag.getMessage(null)))
      else {
        println("Found unhandled diagnostic of kind %s, message %s".format(diag.getKind, diag.getMessage(null)))
        Nil
      })
    CompileResult(success.booleanValue, diags)
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
