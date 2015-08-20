import com.twitter.finagle.http.Http
import com.twitter.finagle.zipkin.thrift.ZipkinTracer
import com.twitter.finagle._
import com.twitter.util.{Await, Future}
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.util.CharsetUtil
import com.twitter.finagle.tracing.{ConsoleTracer, Trace}
import com.twitter.finagle.MemcachedClient
import org.jboss.netty.buffer.ChannelBuffers
import com.twitter.io.Charsets

/**
 * Created by kenny.lee on 2014/11/17.
 */

//VM option with zipkin
//-Dcom.twitter.finagle.zipkin.host=192.168.1.9:9410 -Dcom.twitter.finagle.zipkin.initialSampleRate=1.0

object Client {
  def main(args: Array[String]): Unit = {

    //val dest = Resolver.eval("zk!localhost:2181!/finagle")
    //val dest = Resolver.eval("zk!54.65.124.82:2181!/finagle")
    //val dest = Resolver.eval("inet!172.19.9.43:8080")
    //val dest = Resolver.eval("zk!172.17.0.5:2181!/finagles2")
    val dest = Resolver.eval("zk!172.17.0.5:2181!/finagle")
    //val dest = Resolver.eval("inet!127.0.0.1:9001")
      val client: Service[HttpRequest, HttpResponse] =
         com.twitter.finagle.Http.newService(dest, "FinagleClient")

      val request =  new DefaultHttpRequest(
        HttpVersion.HTTP_1_1, HttpMethod.GET, "/exit2")

      for (i <- 1 to 40) {
        println("GO:" + i)
        val response: Future[HttpResponse] = client(request)
        response onSuccess { resp: HttpResponse =>
          println("GET success: " + resp.getStatus())
          val content = resp.getContent()
          if (content.readable()) {
            println(content.toString(CharsetUtil.UTF_8))
          }
        }
        response onFailure { cause: Throwable =>
          println("failed with " + cause)
        }
        Await.ready(response)
        Thread.sleep(500)
      }
    client.close()
  }
}
