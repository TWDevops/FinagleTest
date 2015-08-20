/**
 * Created by kenny.lee on 2015/1/2.
 */

import com.twitter.finagle._
import com.twitter.finagle.thrift.ClientId
import testthrift.Testthrift
import testthrift.Testthrift.{FinagledService, FinagledClient}
import com.twitter.finagle.memcached._

//import com.twitter.finagle.example.thriftscala.Hello.FinagledClient
import com.twitter.util.{Await, Future}
//import com.twitter.finagle.example.thriftscala.Hello

object ThriftClient {
  def main(args: Array[String]): Unit = {
    //val clientIface = Thrift.newIface[Hello.FutureIface]("localhost:8080")

    val dest = Resolver.eval("zk!172.17.0.3:2181!/finagle")
    //val dest = Resolver.eval("inet!127.0.0.1:8082")
    val service = ThriftMux.withClientId(ClientId("thrift mux client id")).newService(dest,"ThriftClient")
    val client = new FinagledClient(service)
    //val client = ThriftMux.withClientId(ClientId("client ajfsxxxx")).newIface[Testthrift.FutureIface](dest, "ThriftClient1")
    val response = Await.result(client.add(1, 2))
    println(response)
    Await.ready(service.close())
/*
    val response: Future[Int] = client.add(5, 3213)
    response onSuccess { response =>
      println("Received response: " + response)
    }  ensure {
      service.close()
      println("service close")
    }

    response onFailure  { cause: Throwable =>
      println("failed with " + cause)
    }
    Await.result(response)
    //Thread.sleep(100)
    */
  }
}
