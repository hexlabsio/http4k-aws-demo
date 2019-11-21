import io.hexlabs.vehicle.inventory.InventoryHandler
import io.hexlabs.vehicle.inventory.model.Car
import io.hexlabs.vehicle.inventory.service.BrokenCarInventoryService
import org.http4k.client.OkHttp
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.server.KtorCIO
import org.http4k.server.asServer

fun main() {
  val service = InventoryHandler(BrokenCarInventoryService()).service
  println(service(Request(Method.GET, "/cars")))
  service.asServer(KtorCIO(9000)).start()
  val client = OkHttp()
  println(client(Request(Method.GET, "http://localhost:9000/cars")))
  val carLens = Body.auto<Car>().toLens()
  val ford: Car = carLens(Request(Method.POST, "/cars").body(
    """{"make":"Ford","model":"Fiesta","year":1985, "colour":"Blue"}"""))
  println(ford)
  val bmwResponse: Response = Response(Status.OK).with(carLens of Car(make = "BMW", model = "i8", year = 2019, colour = "White"))
  println(bmwResponse)
}
