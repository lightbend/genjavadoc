package scala.javadoc

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import java.net.URLClassLoader
import java.io.File

class SignatureSpec extends WordSpec with MustMatchers {
  
  "The generated java files" must {
    
    "contain the same methods and classes as the original Scala files" in {
      val scalaCL = new URLClassLoader(Array(new File("tests/target/scala-2.10/test-classes/").toURI.toURL), classOf[List[_]].getClassLoader)
      
      def check(jc: Class[_]) {
        println(s"checking $jc")
        val sc = scalaCL.loadClass(jc.getName)
        
        val jm = getMethods(jc)
        val sm = getMethods(sc)
        printIfNotEmpty(sm -- jm, "missing methods:")
        printIfNotEmpty(jm -- sm, "extraneous methods:")
        sm must be === jm
        
        val jsub = getClasses(jc)
        val ssub = getClasses(sc)
        printIfNotEmpty(ssub.keySet -- jsub.keySet, "missing classes:")
        printIfNotEmpty(jsub.keySet -- ssub.keySet, "extraneous classes:")
        ssub.keySet must be === jsub.keySet
        
        for (n <- ssub.keys) {
          val js = jsub(n)
          val ss = ssub(n)
          println(s"prodding $js")
          (js.getModifiers & ~7) must be === (ss.getModifiers & ~7)
          js.getInterfaces.map(_.getName) must be === ss.getInterfaces.map(_.getName)
          js.getSuperclass.getName must be === ss.getSuperclass.getName
          check(js)
        }
      }
      
      def printIfNotEmpty(s: Set[String], msg: String): Unit = if (s.nonEmpty) {
        println(msg)
        s.toList.sorted foreach println
      }
      
      def getMethods(c: Class[_]): Set[String] = {
        import language.postfixOps
        c.getDeclaredMethods filterNot (_.getName contains '$') map (_.toGenericString) toSet
      }
      
      def getClasses(c: Class[_]): Map[String, Class[_]] = {
        import language.postfixOps
        Map() ++ c.getDeclaredClasses.filterNot(_.getName contains "anon").map(x => x.getName -> x)
      }
      
      check(classOf[akka.rk.buh.is.it.A])
    }
    
  }

}