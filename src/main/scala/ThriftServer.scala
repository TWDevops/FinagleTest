/**
 * Created by kenny.lee on 2015/1/2.
 */

import java.net.InetSocketAddress

import com.twitter.finagle.stats.{NullStatsReceiver, StatsReceiver}
import com.twitter.finagle.thrift.{Protocols, NoClientIdSpecifiedException, ClientId, ClientIdRequiredFilter}
import com.twitter.finagle.filter.RequestSemaphoreFilter
import com.twitter.finagle._
import com.twitter.io.Buf
import com.twitter.ostrich.admin.{TimeSeriesCollectorFactory, StatsFactory, AdminServiceFactory, RuntimeEnvironment}
import com.twitter.util.{Await, Future}
import org.apache.thrift.protocol.{TProtocol, TProtocolFactory}
import org.apache.thrift.transport.TTransport
import testthrift.Testthrift
import testthrift.Testthrift.{FinagledService, FutureIface}

class Authorize extends ClientIdRequiredFilter

object ThriftServer {
  def main(args: Array[String]): Unit = {

    //#thriftserverapi
    /*
    val server = Thrift.serveIface("FinagleThriftServer=:8080", new Hello[Future] {
      def hi() = Future.value("hi")
    })*/

    /*val server = Thrift.serveIface("FinagleThriftServer=:8080", iface = new Testthrift[Future] {
         def add(num1: Int, num2: Int) = Future.value(num1 + num2)

      //def hi() = Future.value("hi")
      override def ping(): Future[Unit] = Future {println("pong")}

  */
/*
    val MuxToArrayFilter =
      new Filter[mux.Request, mux.Response, Array[Byte], Array[Byte]] {
        def apply(
                   request: mux.Request, service: Service[Array[Byte], Array[Byte]]
                   ): Future[mux.Response] = {
          val reqBytes = Buf.ByteArray.Owned.extract(request.body)
          service(reqBytes) map { repBytes =>
            mux.Response(Buf.ByteArray.Owned(repBytes))
          }
        }
      }
*/

    val iface = new FutureIface {
      override def ping(): Future[Unit] = Future {println("pong")}

      override def add(num1: Int, num2: Int): Future[Int] = {
        println("current client id" + ClientId.current.get.name)
        Future.value(num1 + num2)
      }
    }
    val svc = new FinagledService(iface, Protocols.binaryFactory())
   val server = ThriftMux.serve("FinagleThriftServer=:9001", new MyFilter2 andThen svc)
    server.announce("zk!172.17.0.3:2181!/finagle!0")
    Await.ready(server)
    /*
    val server = ThriftMux.serveIface("FinagleThriftServer=:8082", new FutureIface {
      override def ping(): Future[Unit] = Future {println("pong")}

      override def add(num1: Int, num2: Int): Future[Int] = {
        println("current client id" + ClientId.current.get.name)
        Future.value(num1 + num2)
      }
    })
*/

    //#thriftserverapi
  }
}

class MyFilter2[Req, Rep](statsReceiver: StatsReceiver = NullStatsReceiver)
  extends SimpleFilter[Req, Rep]
{
  private[this] val noClientIdSpecifiedEx = new NoClientIdSpecifiedException
  private[this] val filterCounter = statsReceiver.counter("no_client_id_specified")

  def apply(req: Req, service: Service[Req, Rep]) = {
    println("filtering........" + ClientId.current.get.name)
    ClientId.current match {
      case Some(_) => service(req)
      case None =>
        filterCounter.incr()
        Future.exception(noClientIdSpecifiedEx)
    }
  }
}
