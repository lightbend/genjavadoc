package scala.javadoc

import org.scalatest.matchers.MustMatchers
import org.scalatest.WordSpec
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.File

class TheSpec extends WordSpec with MustMatchers {

  "GenJavaDoc" must {
    
    "generate the expected output" in {
      lines(run("tests", "diff", "-wurI", "^\\s*//", "expected_output/akka", "target/java/akka")) foreach println
    }
    
  }
  
  def run(dir: String, cmd: String*): Process = {
    new ProcessBuilder(cmd: _*).directory(new File(dir)).redirectErrorStream(true).start()
  }

  def lines(proc: Process) = {
    val b = new BufferedReader(new InputStreamReader(proc.getInputStream()))
    Iterator.continually(b.readLine).takeWhile(_ != null || { proc.waitFor(); assert(proc.exitValue == 0); false })
  }

}