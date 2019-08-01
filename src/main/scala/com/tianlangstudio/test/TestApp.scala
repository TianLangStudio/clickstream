import com.tianlangstudio.clickstream.ClickStreamApp

object TestApp {
  val nginxLogPattern = """(\d+.\d+.\d+.\d+)\^([-\w\d]+)\^\[(.*)\]\^"(\w+) /stat.png\?([^\^]+)& (\w+/[\d.]*)"\^\d*\^\d*\^"[^"]*"\^"[^"]*"\^"[^"]*"""".r
  val sidPattern = """.*&sid=([\w\d]+)&.*""".r
  def main(args: Array[String]): Unit
  =
  {
    val log = "clientY=98&clientX=71&windowWidth=848&windowHeight=83&sid=sid2"
/*
    {
      val log = """172.17.0.1^a^[23/Jul/2019:10:05:59 +0000]^"GET /stat.png?clientY=93&clientX=63&windowWidth=1904&windowHeight=83&sid=sid2& HTTP/1.1"^200^92^"http://localhost:9080/"^"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36"^"-"""";
      val extrat = log match  {
        case nginxLogPattern(remoteAddr,remoteUser,timeLocal,reqMethod,queryString,schema) => {
          val sid = queryString match {
            case sidPattern(sid) => sid
            case _ => ""
          }
          List(remoteAddr,remoteUser, timeLocal,reqMethod,queryString,schema,sid)
        }
        case _ => List()
      }
      println(extrat);
    }
    println("Hello TianLangStudio")*/
  }
}