package code.model

import code.util._

import java.io._
import java.nio.channels._

import scala.collection.mutable.HashMap
import scala.collection.JavaConversions._

class ProjectAlreadyExistsException(name: String) extends Exception
class ProjectDoesNotExistException(name: String) extends Exception

object Project {

  private def lockFileFor(name: String) =
    new File("%s.project.lock".format(name)).getAbsoluteFile

  private def rootDirFor(name: String) =
    new File(name).getAbsoluteFile

  /**
   * Create a new Project with name. Throws an exception if one already eixsts
   * with the same name. Uses the file system to be atomic.
   */
  def apply(name: String): Project = {
    require(name ne null)
    val lockFile = lockFileFor(name)
    if (lockFile.createNewFile()) {
      val rootDir = rootDirFor(name)
      rootDir.mkdirs()
      Project(name, rootDir, lockFile) 
    } else throw new ProjectAlreadyExistsException(name)
  }

  /**
   *
   */
  def getOrCreate(name: String): Project = {
    require(name ne null)
    val lockFile = lockFileFor(name)
    lockFile.createNewFile() // don't care about return value
    val rootDir = rootDirFor(name)
    rootDir.mkdirs()
    Project(name, rootDir, lockFile) 
  }

  /**
   * Returns an existing project with name. Throws an exception if one does
   * not already exist.
   */
  def get(name: String): Project = {
    require(name ne null)
    val lockFile = lockFileFor(name)
    if (lockFile.exists()) {
      val rootDir = rootDirFor(name)
      Project(name, rootDir, lockFile) 
    } else throw new ProjectDoesNotExistException(name)
  }


}

case class Project(name: String, rootDir: File, lockFile: File) {
  //assert(rootDir.exists(), "Root dir does not exist")
  //assert(lockFile.exists(), "Lock file does not exist")

  /**
   * Returns a file with name relative to this project root
   */
  def projectFile(name: String): File = 
    new File(rootDir, name).getAbsoluteFile

  val sourceDir = projectFile("src")

  sourceDir.mkdir()

  def allSourceFiles: List[File] = 
    allFilesRecursively(sourceDir)

  private def allFilesRecursively(root: File): List[File] = {
    if (!root.isDirectory) List(root)
    else root.listFiles.flatMap(x => allFilesRecursively(x)).toList
  }

  def newWorkingCopy: WorkingCopy = {
    val tempRoot = File.createTempFile("webide", ".wc")

    // make tempRoot a directory, instead of file
    tempRoot.delete()
    tempRoot.mkdirs()

    // gross shell out to copy main project dir into working copy, but that's
    // because writing a deep copy in java is more lines of code than i care
    // to write right now 
    val ret = Runtime.getRuntime.exec("cp -R %s %s".format(sourceDir.getAbsolutePath, tempRoot.getAbsolutePath)).waitFor()
    if (ret != 0)
      throw new RuntimeException("Bad return code from cp: " + ret)
    
    WorkingCopy(tempRoot)
  }

  case class WorkingCopy(tempRoot: File) {
    /**
     * Maps dirty files in the WORKING COPY to a string buffer containing the
     * dirty contents
     */ 
    val dirtyFiles = new HashMap[File, String]

    val tempSrcRoot = new File(tempRoot, "src").getAbsoluteFile
    val buildDir = new File(tempRoot, "build").getAbsoluteFile
    buildDir.mkdir()

    def allSourceFiles: List[File] = 
      allFilesRecursively(tempSrcRoot)

    def allDirtyFiles: List[(File, String)] = 
      dirtyFiles.toList

    /**
     * view of the working copy. dirty files take precedent over ones that are
     * not dirty. also dirty file which dont exist on disk are also included
     * (aka new unsaved buffers)
     */
    def viewOf: List[(File, String)] =
      allSourceFiles.filterNot(dirtyFiles.contains).map(f => (f, FileUtils.contentsOf(f))) ++
      allDirtyFiles

    def sourceFile(name: String): File = 
      new File(tempSrcRoot, name).getAbsoluteFile

    /**
     * List of errors, or Nil if compilation succeeded
     */
    def compile(): List[String] = {
      val cleanFiles = JavacUtil.getJavaFileObjects(allSourceFiles filterNot (dirtyFiles.contains) toSeq)
      val dirty = dirtyFiles map {
        case (file, contents) =>
          new StringJavaFileObject(className(file), contents)
      } toSeq
      val diag = JavacUtil.compile(cleanFiles ++ dirty, buildDir)
      diag.iterator.map(_.getMessage(null)).toList
    }

    /**
     * Either add from the project to the working copy if it doesn't already
     * exist, or modify its contents. File must exist in the
     * project. If the file to be added is currently dirty, reject the update
     *
     * TODO: be smarter about merging instead of just rejecting
     *
     * @param file - file existing IN THE PROJECT
     */
    def syncWithProject(file: File) {
      require(file != null)
      if (dirtyFiles.contains(file))
        throw new IllegalArgumentException("File is already dirty in working copy")
      FileUtils.copy(file, findWorkingCopy(file))
    }

    /**
     * Given a file existing IN THE PROJECT, find the equivalent file in the
     * WORKING COPY
     */
    def findWorkingCopy(file: File): File = {
      require(file != null)
      var paths: List[String] = Nil
      var cur = file
      while (cur != rootDir) {
        paths ::= cur.getName
        cur = cur.getParentFile
      }
      new File(tempRoot, paths.mkString(File.pathSeparator)).getAbsoluteFile
    }

    /**
     * Given a file existing IN THE WORKING COPY, find the class name
     * represented by the file
     */
    def className(file: File): String = {
      require(file != null)
      println("className(): file %s, rootDir: %s".format(file, tempSrcRoot))
      var paths: List[String] = Nil
      var cur = file
      while (cur != tempSrcRoot) {
        if (cur == file)
          paths ::= cur.getName.split("\\.").apply(0) // remove the .java name
        else paths ::= cur.getName
        cur = cur.getParentFile
      }
      paths.mkString(".")
    }

  }

}

object FileUtils {
  def copy(src: File, dest: File) {
    val inCh = new FileInputStream(src).getChannel
    val outCh = new FileOutputStream(dest).getChannel
    inCh.transferTo(0, inCh.size, outCh)
    inCh.close()
    outCh.close()
  }

  def contentsOf(file: File): String = {
    val inCh = new FileInputStream(file).getChannel
    val baos = new ByteArrayOutputStream(1024)
    val outCh = Channels.newChannel(baos)
    inCh.transferTo(0, inCh.size, outCh)
    inCh.close()
    outCh.close()
    new String(baos.toByteArray)
  }

}

