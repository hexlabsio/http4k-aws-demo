package io.hexlabs.vehicle.inventory.model

data class Car(val make: String, val model: String, val year: Int, val colour: String)
data class CarInfo(val id: String, val car: Car)
