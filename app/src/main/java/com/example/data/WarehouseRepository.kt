package com.example.data

import kotlinx.coroutines.flow.Flow

class WarehouseRepository(private val warehouseDao: WarehouseDao) {
    val allWarehouses: Flow<List<Warehouse>> = warehouseDao.getAllWarehouses()
    val allEquipment: Flow<List<Equipment>> = warehouseDao.getAllEquipment()

    fun getEquipmentForWarehouse(warehouseId: Int): Flow<List<Equipment>> {
        return warehouseDao.getEquipmentByWarehouse(warehouseId)
    }

    suspend fun getWarehouseById(id: Int): Warehouse? {
        return warehouseDao.getWarehouseById(id)
    }

    suspend fun insertWarehouse(warehouse: Warehouse): Long {
        return warehouseDao.insertWarehouse(warehouse)
    }

    suspend fun updateWarehouse(warehouse: Warehouse) {
        warehouseDao.updateWarehouse(warehouse)
    }

    suspend fun deleteWarehouse(warehouse: Warehouse) {
        warehouseDao.deleteWarehouse(warehouse)
    }

    suspend fun getEquipmentById(id: Int): Equipment? {
        return warehouseDao.getEquipmentById(id)
    }

    suspend fun insertEquipment(equipment: Equipment): Long {
        return warehouseDao.insertEquipment(equipment)
    }

    suspend fun updateEquipment(equipment: Equipment) {
        warehouseDao.updateEquipment(equipment)
    }

    suspend fun deleteEquipment(equipment: Equipment) {
        warehouseDao.deleteEquipment(equipment)
    }
}
