package io.hexlabs.vehicle.inventory.service

import io.hexlabs.vehicle.inventory.model.Car
import org.slf4j.LoggerFactory

interface CarInventoryService {
  fun create(car: Car): String
  fun read(id: String): Car?
  fun readAll(): Map<String, Car>
  fun update(id: String, car: Car): String
  fun delete(id: String): String
}

class BrokenCarInventoryService : CarInventoryService {

  val logger = LoggerFactory.getLogger(BrokenCarInventoryService::class.java)
  override fun create(car: Car): String {
    logger.info("create car [$car] called: update persistence here")
    return "777-X"
  }

  override fun read(id: String): Car? = staticCarInventory[id]

  override fun readAll(): Map<String, Car> {
    logger.info("readAll cars called")
    return staticCarInventory
  }

  override fun update(id: String, car: Car): String {
    logger.info("update car [$car] called: update persistence here")
    return id
  }

  override fun delete(id: String): String {
    logger.info("delete car id [$id] called: update persistence here")
    return id
  }

  val staticCarInventory: Map<String, Car> = mapOf(
    "123-X" to Car(make = "Ford", model = "Fiesta", year = 1985, colour = "Blue"),
    "541-Z" to Car(make = "Renault", model = "Capture", year = 2015, colour = "Red"),
    "000-S" to Car(make = "McClaren", model = "Senna", year = 2018, colour = "Grey"),
    "333-S" to Car(make = "BMW", model = "i8", year = 2019, colour = "White"),
    "222-D" to Car(make = "Hyundai", model = "Sante Fe", year = 2013, colour = "Black")
  )
}
