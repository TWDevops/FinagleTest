import java.net.InetSocketAddress
import java.text.SimpleDateFormat
import java.util.Calendar

import com.sun.net.httpserver.HttpServer
import com.twitter.common.quantity.{Time, Amount}
import com.twitter.common.zookeeper.{ServerSetImpl, ZooKeeperClient}
import com.twitter.finagle.{Resolver, SimpleFilter, Service, Http}
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.zipkin.thrift.ZipkinTracer
import com.twitter.finagle.zookeeper.ZookeeperServerSetCluster
import com.twitter.io.Charsets._
import com.twitter.util.Future
import org.jboss.netty.buffer.ChannelBuffers._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import org.jboss.netty.handler.codec.http._
import com.twitter.ostrich._
import com.twitter.ostrich.stats._
import com.twitter.ostrich.admin._
import com.twitter.ostrich.admin.config._
import com.twitter.finagle.stats.{NullStatsReceiver, StatsReceiver, OstrichStatsReceiver}
import com.twitter.ostrich.stats.StatsCollection
import com.twitter.util.{Await, Future}
import com.twitter.finagle.tracing.Trace
import org.jboss.netty.util.CharsetUtil

/**
 * Created by kenny.lee on 2014/11/14.
 */
// VM Option with zipkin
// -Dcom.twitter.finagle.zipkin.host=192.168.1.9:9410 -Dcom.twitter.finagle.zipkin.initialSampleRate=1.0

class Respond extends Service[HttpRequest, HttpResponse] {
  def apply(request: HttpRequest) = {
    val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, OK)
    response.setContent(copiedBuffer("hello world", Utf8))
    Stats.incr("widgets_sold", 5)
    Future.value(response)
  }
}


object Server {
  def main(args: Array[String]): Unit = {
 //   val zipkinTracer = ZipkinTracer.mk(host = "192.168.1.104", port = 9410, sampleRate = 1.0f)
  //  val respond = new Respond
   // val myService: Service[HttpRequest, HttpResponse] =  respond
    println("server start")

    val runtime = RuntimeEnvironment(this, Array[String]())
    val admin = new AdminServiceFactory (
      8081,
      statsNodes = List(new StatsFactory(
        reporters = List(new TimeSeriesCollectorFactory())
      ))
    )(runtime)

    val dest = Resolver.eval("zk!172.17.0.5:2181!/finagles2")
    val client: Service[HttpRequest, HttpResponse] =
      com.twitter.finagle.Http.newService(dest, "Finagleserverclient")
    val request =  new DefaultHttpRequest(
      HttpVersion.HTTP_1_1, HttpMethod.GET, "/exitserver")

    val service = new Service[HttpRequest, HttpResponse] {
      def apply(req: HttpRequest): Future[HttpResponse] = {
        println(1)
        val s2 = Await.result(client(request))
        println(2)
        val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, OK)
        response.setContent(copiedBuffer("hello world" + s2.getContent.toString(Utf8), Utf8))
        Stats.incr("widgets_sold", 5)
        println(4)
        val today = Calendar.getInstance().getTime()
        val nowtime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val currentTimeStr = nowtime.format(today)
        println(currentTimeStr + " URL : " + req.getUri())
        if (req.getUri() == "/exit") {
          System.exit(0)
          Future.exception(new Exception("exit"))
        } else {
          Future.value(response)
        }
      }
    }

  //  Trace.enable()
  //  Trace.record("11")

   // Trace.recordServiceName("kenny")
    val server = Http.serve("FinagleServer=:9001", new MyFilter andThen service)
    //service.announce("zk!zkhost.com:2181!/path/to/my/serverset!shardId")
    //server.announce("zk!localhost:2181!/finagle!0")
    server.announce("zk!172.17.0.5:2181!/finagle!0")
    // Trace.pushTracer(zipkinTracer)
    Await.ready(server)
    server.close()

  /* val runtime = RuntimeEnvironment(this, Array[String]())
    val admin = new AdminServiceFactory (
      8080,
      statsNodes = List(new StatsFactory(
        reporters = List(new TimeSeriesCollectorFactory())
      ))
    )(runtime)*/


    /*
    val stc = new TimeSeriesCollector ()
    val admin = new AdminHttpService(8080,8081, RuntimeEnvironment(this, Array()))
    stc.registerWith(admin)
    stc.start()
    admin.start()*/
/*
    val zkClient = new ZooKeeperClient(Amount.of(100, Time.MILLISECONDS), new InetSocketAddress(2181))
    val serverSet = new ServerSetImpl(zkClient, "/kenny")
    val serverSetCluster = new ZookeeperServerSetCluster(serverSet)
    serverSetCluster.join(new InetSocketAddress(3006))

    val server = ServerBuilder()
      .codec(Http().enableTracing(true))
      .bindTo(new InetSocketAddress(3006))
      .name("Server")
      //.tracer(zipkinTracer)
      // .daemon(true)
      .reportTo(new OstrichStatsReceiver)
      .build(myService)


  }*/
}
}

class MyFilter[Req, Rep](statsReceiver: StatsReceiver = NullStatsReceiver)
  extends SimpleFilter[Req, Rep]
{
  private[this] val filterCounter = statsReceiver.counter("no_client_id_specified")

  def apply(req: Req, service: Service[Req, Rep]) = {
    println("filtering........" + req.toString)
    service(req)
  }
}
