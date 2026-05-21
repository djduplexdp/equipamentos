package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Equipment
import com.example.data.Warehouse
import com.example.data.WarehouseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WarehouseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WarehouseRepository

    val allWarehouses: StateFlow<List<Warehouse>>
    val allEquipment: StateFlow<List<Equipment>>

    // Filters and search
    val searchQuery = MutableStateFlow("")
    val selectedWarehouseId = MutableStateFlow<Int?>(null)
    val selectedCategory = MutableStateFlow<String>("Todos")
    val selectedStatus = MutableStateFlow<String>("Todos")

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.warehouseDao()
        repository = WarehouseRepository(dao)

        allWarehouses = repository.allWarehouses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allEquipment = repository.allEquipment.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Prepopulate if completely empty
        viewModelScope.launch {
            allWarehouses.collect { list ->
                if (list.isEmpty()) {
                    prepopulateDatabase()
                }
            }
        }
    }

    // Reactive Combined Query
    val filteredEquipment: StateFlow<List<Equipment>> = combine(
        repository.allEquipment,
        searchQuery,
        selectedWarehouseId,
        selectedCategory,
        selectedStatus
    ) { equipmentList, query, warehouseId, category, status ->
        equipmentList.filter { eq ->
            val matchesQuery = query.isBlank() ||
                    eq.name.contains(query, ignoreCase = true) ||
                    eq.tag.contains(query, ignoreCase = true) ||
                    eq.description.contains(query, ignoreCase = true)

            val matchesWarehouse = warehouseId == null || eq.warehouseId == warehouseId
            val matchesCategory = category == "Todos" || eq.category == category
            val matchesStatus = status == "Todos" || eq.status == status

            matchesQuery && matchesWarehouse && matchesCategory && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private suspend fun prepopulateDatabase() {
        val g1Id = repository.insertWarehouse(
            Warehouse(
                name = "Galpão Alfa",
                location = "Bloco A - Setor Norte",
                description = "Galpão principal destinado a maquinários pesados e geradores secundários."
            )
        )
        val g2Id = repository.insertWarehouse(
            Warehouse(
                name = "Galpão Beta",
                location = "Bloco B - Setor Logística",
                description = "Galpão refrigerado para ferramentas eletrônicas e instrumentos de medição."
            )
        )

        repository.insertEquipment(
            Equipment(
                warehouseId = g1Id.toInt(),
                name = "Gerador Diesel Yanmar 50kVA",
                tag = "GER-001",
                category = "Maquinário",
                status = "Operativo",
                description = "Gerador de backup automático em perfeito estado."
            )
        )
        repository.insertEquipment(
            Equipment(
                warehouseId = g1Id.toInt(),
                name = "Transformador Trifásico WEG",
                tag = "TRF-042",
                category = "Elétrico",
                status = "Em Manutenção",
                description = "Apresentando ruídos leves de aterramento. Técnico agendado."
            )
        )
        repository.insertEquipment(
            Equipment(
                warehouseId = g2Id.toInt(),
                name = "Empilhadeira Elétrica Toyota",
                tag = "EMP-105",
                category = "Maquinário",
                status = "Operativo",
                description = "Capacidade de 1.5 toneladas com carregador rápido incluso."
            )
        )
        repository.insertEquipment(
            Equipment(
                warehouseId = g2Id.toInt(),
                name = "Termômetro Digital Fluke 62 Max",
                tag = "MET-098",
                category = "Instrumento",
                status = "Inativo",
                description = "Necessita de nova calibração certificada."
            )
        )
    }

    // --- Warehouse CRUD ---
    fun addWarehouse(name: String, location: String, description: String) {
        viewModelScope.launch {
            repository.insertWarehouse(
                Warehouse(name = name, location = location, description = description)
            )
        }
    }

    fun updateWarehouse(id: Int, name: String, location: String, description: String) {
        viewModelScope.launch {
            repository.updateWarehouse(
                Warehouse(id = id, name = name, location = location, description = description)
            )
        }
    }

    fun deleteWarehouse(warehouse: Warehouse) {
        viewModelScope.launch {
            if (selectedWarehouseId.value == warehouse.id) {
                selectedWarehouseId.value = null
            }
            repository.deleteWarehouse(warehouse)
        }
    }

    // --- Equipment CRUD ---
    fun addEquipment(
        warehouseId: Int,
        name: String,
        tag: String,
        category: String,
        status: String,
        description: String
    ) {
        viewModelScope.launch {
            repository.insertEquipment(
                Equipment(
                    warehouseId = warehouseId,
                    name = name,
                    tag = tag,
                    category = category,
                    status = status,
                    description = description
                )
            )
        }
    }

    fun updateEquipment(
        id: Int,
        warehouseId: Int,
        name: String,
        tag: String,
        category: String,
        status: String,
        description: String
    ) {
        viewModelScope.launch {
            repository.updateEquipment(
                Equipment(
                    id = id,
                    warehouseId = warehouseId,
                    name = name,
                    tag = tag,
                    category = category,
                    status = status,
                    description = description
                )
            )
        }
    }

    fun deleteEquipment(equipment: Equipment) {
        viewModelScope.launch {
            repository.deleteEquipment(equipment)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WarehouseViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WarehouseViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
