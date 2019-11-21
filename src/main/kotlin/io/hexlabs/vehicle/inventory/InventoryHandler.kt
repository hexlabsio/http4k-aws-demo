package io.hexlabs.vehicle.inventory

import io.hexlabs.vehicle.inventory.model.Car
import io.hexlabs.vehicle.inventory.model.CarInfo
import io.hexlabs.vehicle.inventory.service.BrokenCarInventoryService
import io.hexlabs.vehicle.inventory.service.CarInventoryService
import org.http4k.core.Body
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.PUT
import org.http4k.core.Method.POST
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.format.Jackson.auto
import org.http4k.lens.Path
import org.http4k.routing.RoutingHttpHandler
import org.http4k.serverless.AppLoader
import org.slf4j.LoggerFactory

object InventoryHandlerLambdaFunction : AppLoader {
  private val inventoryHandler = InventoryHandler(BrokenCarInventoryService())
  override fun invoke(env: Map<String, String>): HttpHandler = inventoryHandler.service
}

class InventoryHandler(val carInventoryService: CarInventoryService) {
  private val logger = LoggerFactory.getLogger(InventoryHandler::class.java)

  private val idPathParam = Path.of("id")
  private val carLens = Body.auto<Car>().toLens()

  val inventoryRoute: RoutingHttpHandler = routes(
    "/cars" bind GET to { getCars() },
    "/cars" bind POST to { request -> postCar(carLens.extract(request)) },
    "/cars/{id}" bind GET to { request -> getCar(idPathParam(request)) },
    "/cars/{id}" bind PUT to { request -> putCar(idPathParam(request), carLens.extract(request)) },
    "/cars/{id}" bind DELETE to { request -> deleteCar(idPathParam(request)) }
  )

  val latencyFilter: Filter = Filter { httpHandler: HttpHandler ->
    { request: Request ->
      val start = System.currentTimeMillis()
      val response = httpHandler(request)
      val latency = System.currentTimeMillis() - start
      logger.info("request took $latency ms")
      response
    }
  }

  val service = ServerFilters.CatchLensFailure.then(latencyFilter).then(inventoryRoute.withBasePath("inventory"))

  fun getCars(): Response = Response(Status.OK).with(
    carInventoryService.readAll().map { (id, car) -> CarInfo(id, car) }.jsonBody()
  )

  fun getCar(id: String): Response = carInventoryService.read(id)?.let { carFound ->
    Response(Status.OK).with(carFound.jsonBody())
  }.orNotFound()

  fun postCar(car: Car): Response {
    val id = carInventoryService.create(car)
    return Response(Status.CREATED).with(
      CarInfo(id, car).jsonBody()
    )
  }

  fun putCar(id: String, car: Car) = carInventoryService.read(id)?.let { carFound ->
    carInventoryService.update(id, car)
    Response(Status.OK).with(
      CarInfo(id, carFound).jsonBody()
    )
  }.orNotFound()

  fun deleteCar(id: String): Response = carInventoryService.read(id)?.let {
    carInventoryService.delete(id)
    Response(Status.OK).with(
      ("deleted" to id).jsonBody()
    )
  }.orNotFound()

  fun Response?.orNotFound(): Response = this ?: Response(Status.NOT_FOUND)
  inline fun <reified T : Any> T.jsonBody(): (Response) -> Response = Body.auto<T>().toLens() of this
}
