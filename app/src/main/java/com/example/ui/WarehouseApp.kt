package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Equipment
import com.example.data.Warehouse

// App Screens model for standard safe navigation
sealed interface AppScreen {
    object Dashboard : AppScreen
    object WarehouseList : AppScreen
    data class WarehouseDetail(val warehouseId: Int) : AppScreen
    data class AddEditWarehouse(val warehouseId: Int? = null) : AppScreen
    data class AddEditEquipment(val equipmentId: Int? = null, val initialWarehouseId: Int) : AppScreen
}

@Composable
fun WarehouseManageApp(viewModel: WarehouseViewModel) {
    // Collect Reactive State From Room
    val warehouses by viewModel.allWarehouses.collectAsStateWithLifecycle()
    val equipments by viewModel.allEquipment.collectAsStateWithLifecycle()
    val filteredEquips by viewModel.filteredEquipment.collectAsStateWithLifecycle()

    // Navigation Stack (Simple stack for reliable, crash-free navigation)
    val screenStack = remember { mutableStateListOf<AppScreen>(AppScreen.Dashboard) }
    val currentScreen = screenStack.lastOrNull() ?: AppScreen.Dashboard

    fun navigateTo(screen: AppScreen) {
        screenStack.add(screen)
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
        }
    }

    // Capture physical Android back gestures
    BackHandler(enabled = screenStack.size > 1) {
        navigateBack()
    }

    // Modal delete confirmation states
    var warehouseToDelete by remember { mutableStateOf<Warehouse?>(null) }
    var equipmentToDelete by remember { mutableStateOf<Equipment?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpanded = maxWidth >= 600.dp

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isExpanded) {
                // DESKTOP/TABLET DUAL PANES
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Side: Global Navigation Feed and Stats
                    Column(
                        modifier = Modifier
                            .width(340.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AppBrandHeader()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Compact Quick Stats Pane
                        DashboardSummarySection(
                            warehouses = warehouses,
                            equipments = equipments,
                            horizontalMode = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Warehouses Quick Picker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nossos Galpões",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { navigateTo(AppScreen.AddEditWarehouse(null)) },
                                modifier = Modifier.testTag("desktop_add_warehouse")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Cadastrar Galpão"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                // Default selection block: 'All'
                                val isSelected = viewModel.selectedWarehouseId.collectAsState().value == null
                                WarehouseRowItem(
                                    name = "Todos os Galpões",
                                    location = "Estoque Central Geral",
                                    equipCount = equipments.size,
                                    isSelected = isSelected,
                                    onClick = {
                                        viewModel.selectedWarehouseId.value = null
                                    },
                                    onEditClick = null,
                                    onDeleteClick = null
                                )
                            }

                            items(warehouses) { warehouse ->
                                val count = equipments.count { it.warehouseId == warehouse.id }
                                val isSelected = viewModel.selectedWarehouseId.collectAsState().value == warehouse.id
                                WarehouseRowItem(
                                    name = warehouse.name,
                                    location = warehouse.location,
                                    equipCount = count,
                                    isSelected = isSelected,
                                    onClick = {
                                        viewModel.selectedWarehouseId.value = warehouse.id
                                    },
                                    onEditClick = { navigateTo(AppScreen.AddEditWarehouse(warehouse.id)) },
                                    onDeleteClick = { warehouseToDelete = warehouse }
                                )
                            }
                        }
                    }

                    // Divider
                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // Right Side: Focus list (Equipments inside selected warehouse)
                    val activeWarehouseId = viewModel.selectedWarehouseId.collectAsState().value
                    val activeWarehouse = warehouses.find { it.id == activeWarehouseId }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activeWarehouse?.name ?: "Todos os Galpões",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activeWarehouse?.location ?: "Inventário consolidado geral do sistema",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    navigateTo(
                                        AppScreen.AddEditEquipment(
                                            equipmentId = null,
                                            initialWarehouseId = activeWarehouseId ?: (warehouses.firstOrNull()?.id ?: 0)
                                        )
                                    )
                                },
                                enabled = warehouses.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("desktop_add_equipment")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cadastrar Equipamento")
                            }
                        }

                        // Filtering Bar
                        Spacer(modifier = Modifier.height(16.dp))
                        FilterBarSection(viewModel = viewModel)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Equipments Grid/List
                        if (filteredEquips.isEmpty()) {
                            EmptyInventoryState(
                                queryActive = viewModel.searchQuery.collectAsState().value.isNotEmpty(),
                                hasWarehouses = warehouses.isNotEmpty()
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(filteredEquips) { equipment ->
                                    val belongsTo = warehouses.find { it.id == equipment.warehouseId }
                                    EquipmentCardItem(
                                        equipment = equipment,
                                        warehouseName = belongsTo?.name ?: "Indefinido",
                                        onEdit = {
                                            navigateTo(
                                                AppScreen.AddEditEquipment(
                                                    equipmentId = equipment.id,
                                                    initialWarehouseId = equipment.warehouseId
                                                )
                                            )
                                        },
                                        onDelete = { equipmentToDelete = equipment }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                // PORTRAIT/MOBILE STANDARD LAYOUT
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        when (currentScreen) {
                            // 1. Dashboard / Summary
                            is AppScreen.Dashboard -> {
                                DashboardMobileScreen(
                                    warehouses = warehouses,
                                    equipments = equipments,
                                    viewModel = viewModel,
                                    onNavigateToWarehouses = { navigateTo(AppScreen.WarehouseList) },
                                    onWarehouseSelected = { warehouseId ->
                                        viewModel.selectedWarehouseId.value = warehouseId
                                        navigateTo(AppScreen.WarehouseDetail(warehouseId))
                                    },
                                    onWarehouseAdd = { navigateTo(AppScreen.AddEditWarehouse(null)) },
                                    onWarehouseEdit = { id -> navigateTo(AppScreen.AddEditWarehouse(id)) },
                                    onWarehouseDelete = { warehouse -> warehouseToDelete = warehouse }
                                )
                            }

                            // 2. Full Warehouse List screen
                            is AppScreen.WarehouseList -> {
                                MobileWarehousesScreen(
                                    warehouses = warehouses,
                                    equipments = equipments,
                                    onWarehouseSelected = { warehouseId ->
                                        viewModel.selectedWarehouseId.value = warehouseId
                                        navigateTo(AppScreen.WarehouseDetail(warehouseId))
                                    },
                                    onWarehouseAdd = { navigateTo(AppScreen.AddEditWarehouse(null)) },
                                    onWarehouseEdit = { id -> navigateTo(AppScreen.AddEditWarehouse(id)) },
                                    onWarehouseDelete = { warehouse -> warehouseToDelete = warehouse },
                                    onBack = { navigateBack() }
                                )
                            }

                            // 3. Equipment screen for specific warehouse
                            is AppScreen.WarehouseDetail -> {
                                val currentDetail = currentScreen as AppScreen.WarehouseDetail
                                val warehouse = warehouses.find { it.id == currentDetail.warehouseId }
                                MobileWarehouseDetailScreen(
                                    warehouse = warehouse,
                                    equipments = filteredEquips,
                                    allWarehouses = warehouses,
                                    viewModel = viewModel,
                                    onAddEquipment = {
                                        navigateTo(
                                            AppScreen.AddEditEquipment(
                                                equipmentId = null,
                                                initialWarehouseId = currentDetail.warehouseId
                                            )
                                        )
                                    },
                                    onEditEquipment = { eqId ->
                                        navigateTo(
                                            AppScreen.AddEditEquipment(
                                                equipmentId = eqId,
                                                initialWarehouseId = currentDetail.warehouseId
                                            )
                                        )
                                    },
                                    onDeleteEquipment = { equipment ->
                                        equipmentToDelete = equipment
                                    },
                                    onBack = { navigateBack() }
                                )
                            }

                            // Handling Forms directly in screen flow
                            is AppScreen.AddEditWarehouse -> {
                                val currentForm = currentScreen as AppScreen.AddEditWarehouse
                                val existingWarehouse = warehouses.find { it.id == currentForm.warehouseId }
                                WarehouseFormScreen(
                                    warehouse = existingWarehouse,
                                    onSave = { name, location, desc ->
                                        if (existingWarehouse != null) {
                                            viewModel.updateWarehouse(existingWarehouse.id, name, location, desc)
                                        } else {
                                            viewModel.addWarehouse(name, location, desc)
                                        }
                                        navigateBack()
                                    },
                                    onCancel = { navigateBack() }
                                )
                            }

                            is AppScreen.AddEditEquipment -> {
                                val currentForm = currentScreen as AppScreen.AddEditEquipment
                                val existingEquipment = equipments.find { it.id == currentForm.equipmentId }
                                EquipmentFormScreen(
                                    equipment = existingEquipment,
                                    warehouses = warehouses,
                                    initialWarehouseId = currentForm.initialWarehouseId,
                                    onSave = { whId, name, tag, cat, stat, desc ->
                                        if (existingEquipment != null) {
                                            viewModel.updateEquipment(existingEquipment.id, whId, name, tag, cat, stat, desc)
                                        } else {
                                            viewModel.addEquipment(whId, name, tag, cat, stat, desc)
                                        }
                                        navigateBack()
                                    },
                                    onCancel = { navigateBack() }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action sheets or confirmation Dialogs
        warehouseToDelete?.let { warehouse ->
            AlertDialog(
                onDismissRequest = { warehouseToDelete = null },
                title = { Text("Excluir Galpão?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Deseja mesmo excluir o galpão '${warehouse.name}'?\n\nAlerta: Todos os equipamentos cadastrados neste galpão também serão removidos permanentemente!"
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteWarehouse(warehouse)
                            warehouseToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("confirm_delete_warehouse")
                    ) {
                        Text("Excluir", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { warehouseToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        equipmentToDelete?.let { equipment ->
            AlertDialog(
                onDismissRequest = { equipmentToDelete = null },
                title = { Text("Excluir Equipamento?") },
                text = { Text("Isso removerá permanently o item '${equipment.name}' (TAG: ${equipment.tag}) do cadastro.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteEquipment(equipment)
                            equipmentToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("confirm_delete_equipment")
                    ) {
                        Text("Excluir", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { equipmentToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// ======================== COMMON REUSABLE COMPONENTS ========================

@Composable
fun AppBrandHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "SiloGestão",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Controle de Estoque",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun DashboardSummarySection(
    warehouses: List<Warehouse>,
    equipments: List<Equipment>,
    horizontalMode: Boolean = false
) {
    if (horizontalMode) {
        // Horizontal feed (Mobile View)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Galpões",
                value = warehouses.size.toString(),
                icon = Icons.Default.Home,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Equip.",
                value = equipments.size.toString(),
                icon = Icons.Default.Inventory,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f)
            )
            val underMaintenance = equipments.count { it.status == "Em Manutenção" }
            StatCard(
                title = "Reparo",
                value = underMaintenance.toString(),
                icon = Icons.Default.Warning,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        // Vertical stacked list (Desktop Side navigation)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(
                title = "Total de Galpões Cadastrados",
                value = warehouses.size.toString(),
                icon = Icons.Default.Home,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
            StatCard(
                title = "Equipamentos em Inventário",
                value = equipments.size.toString(),
                icon = Icons.Default.Inventory,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
            val normal = equipments.count { it.status == "Operativo" }
            val repairs = equipments.count { it.status == "Em Manutenção" }
            val offline = equipments.count { it.status == "Inativo" }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Distribuição de Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusRow(label = "Operativo", count = normal, color = Color(0xFF2E7D32))
                    StatusRow(label = "Em Manutenção", count = repairs, color = Color(0xFFEF6C00))
                    StatusRow(label = "Inativo", count = offline, color = Color(0xFFC62828))
                }
            }
        }
    }
}

@Composable
fun StatusRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(count.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WarehouseRowItem(
    name: String,
    location: String,
    equipCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?
) {
    val containerBg = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (onEditClick == null) Icons.Default.Assessment else Icons.Default.Home,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (location.isBlank()) "Sem endereço" else location,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            ) {
                Text(
                    text = "$equipCount",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Options for user-defined warehouses
            if (onEditClick != null && onDeleteClick != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EquipmentCardItem(
    equipment: Equipment,
    warehouseName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (equipment.status) {
        "Operativo" -> Color(0xFFD1E9CF) to Color(0xFF146C2E)
        "Em Manutenção" -> Color(0xFFE7E0EC) to Color(0xFF49454F)
        "Inativo" -> Color(0xFFFFD8D4) to Color(0xFF8C1D18)
        else -> Color(0xFFE7E0EC) to Color(0xFF49454F)
    }

    val icon = when (equipment.status) {
        "Operativo" -> Icons.Default.CheckCircle
        "Em Manutenção" -> Icons.Default.Warning
        else -> Icons.Default.Cancel
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = equipment.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "TAG: ${equipment.tag}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = warehouseName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Action buttons right aligned
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp).testTag("edit_equipment_btn")) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Equipamento",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp).testTag("delete_equipment_btn")) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir Equipamento",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (equipment.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = equipment.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges metadata block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = equipment.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Status Indicator Badge
                Surface(
                    color = statusColor.first,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = statusColor.second,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = equipment.status,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor.second
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterBarSection(viewModel: WarehouseViewModel) {
    val query by viewModel.searchQuery.collectAsState()
    val selectCategory by viewModel.selectedCategory.collectAsState()
    val selectStatus by viewModel.selectedStatus.collectAsState()

    val categories = listOf("Todos", "Maquinário", "Ferramenta", "Elétrico", "Instrumento", "TI / Software", "Outros")
    val statuses = listOf("Todos", "Operativo", "Em Manutenção", "Inativo")

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Search Input
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Buscar por nome, TAG ou descrição...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Dropdown status and category chips
        Text(
            text = "Filtros rápidos:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Category Dropdown Filter Selector
            var catMenuExpanded by remember { mutableStateOf(false) }
            Box {
                SuggestionChip(
                    onClick = { catMenuExpanded = true },
                    label = { Text("Categoria: $selectCategory") },
                    icon = { Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (selectCategory != "Todos") MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                )
                ExposedDropdownMenuBox(
                    expanded = catMenuExpanded,
                    onExpandedChange = { catMenuExpanded = it }
                ) {
                    ExposedDropdownMenu(
                        expanded = catMenuExpanded,
                        onDismissRequest = { catMenuExpanded = false },
                        modifier = Modifier.exposedFilter()
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    viewModel.selectedCategory.value = cat
                                    catMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Status Dropdown Filter Selector
            var statusMenuExpanded by remember { mutableStateOf(false) }
            Box {
                SuggestionChip(
                    onClick = { statusMenuExpanded = true },
                    label = { Text("Status: $selectStatus") },
                    icon = { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (selectStatus != "Todos") MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent
                    )
                )
                ExposedDropdownMenuBox(
                    expanded = statusMenuExpanded,
                    onExpandedChange = { statusMenuExpanded = it }
                ) {
                    ExposedDropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false },
                        modifier = Modifier.exposedFilter()
                    ) {
                        statuses.forEach { stat ->
                            DropdownMenuItem(
                                text = { Text(stat) },
                                onClick = {
                                    viewModel.selectedStatus.value = stat
                                    statusMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Clear Filters Button (Visible if active filters exist)
            if (query.isNotEmpty() || selectCategory != "Todos" || selectStatus != "Todos" || viewModel.selectedWarehouseId.collectAsState().value != null) {
                TextButton(
                    onClick = {
                        viewModel.searchQuery.value = ""
                        viewModel.selectedCategory.value = "Todos"
                        viewModel.selectedStatus.value = "Todos"
                        viewModel.selectedWarehouseId.value = null
                    },
                    modifier = Modifier.testTag("clear_filters_btn")
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpar filtros", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun EmptyInventoryState(queryActive: Boolean, hasWarehouses: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (!hasWarehouses) Icons.Default.Home else Icons.Default.Build,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (!hasWarehouses) "Nenhum Galpão Cadastrado" else if (queryActive) "Nenhum resultado encontrado" else "Nenhum equipamento cadastrado",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (!hasWarehouses) "Cadastre seu primeiro galpão no painel lateral para começar a organizar seu estoque." else if (queryActive) "Tente ajustar seus termos de busca ou mudar os filtros de categoria e status." else "Clique em 'Cadastrar Equipamento' para inserir ativos neste galpão.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ======================== PORTRAIT / MOBILE SCREENS ========================

@Composable
fun DashboardMobileScreen(
    warehouses: List<Warehouse>,
    equipments: List<Equipment>,
    viewModel: WarehouseViewModel,
    onNavigateToWarehouses: () -> Unit,
    onWarehouseSelected: (Int) -> Unit,
    onWarehouseAdd: () -> Unit,
    onWarehouseEdit: (Int) -> Unit,
    onWarehouseDelete: (Warehouse) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        AppBrandHeader()
        Spacer(modifier = Modifier.height(16.dp))

        // Large stats row
        DashboardSummarySection(
            warehouses = warehouses,
            equipments = equipments,
            horizontalMode = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Warehouses Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meus Galpões",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onNavigateToWarehouses) {
                Text("Ver todos")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (warehouses.isEmpty()) {
            EmptyWarehousesPlaceholder(onAdd = onWarehouseAdd)
        } else {
            // Display top 3 warehouses on Dashboard
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                warehouses.take(3).forEach { warehouse ->
                    val count = equipments.count { it.warehouseId == warehouse.id }
                    WarehouseRowItem(
                        name = warehouse.name,
                        location = warehouse.location,
                        equipCount = count,
                        isSelected = false,
                        onClick = { onWarehouseSelected(warehouse.id) },
                        onEditClick = { onWarehouseEdit(warehouse.id) },
                        onDeleteClick = { onWarehouseDelete(warehouse) }
                    )
                }

                if (warehouses.size > 3) {
                    OutlinedButton(
                        onClick = onNavigateToWarehouses,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Visualizar todos os ${warehouses.size} galpões")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Inventory management prompt card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Gerenciar Equipamentos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Acesse qualquer galpão acima para visualizar, cadastrar, buscar e filtrar os equipamentos alocados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        // Resets selection and navigates to Galpão list to pick
                        viewModel.selectedWarehouseId.value = null
                        onNavigateToWarehouses()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Abrir Inventário Geral")
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun EmptyWarehousesPlaceholder(onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Nenhum galpão cadastrado", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Cadastre galpões para organizar os equipamentos de cada ambiente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAdd, shape = RoundedCornerShape(8.dp)) {
                Text("Cadastrar Galpão")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileWarehousesScreen(
    warehouses: List<Warehouse>,
    equipments: List<Equipment>,
    onWarehouseSelected: (Int) -> Unit,
    onWarehouseAdd: () -> Unit,
    onWarehouseEdit: (Int) -> Unit,
    onWarehouseDelete: (Warehouse) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Galpões", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = onWarehouseAdd, modifier = Modifier.testTag("mobile_add_warehouse")) {
                        Icon(Icons.Default.Add, contentDescription = "Cadastrar Galpão")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onWarehouseAdd,
                modifier = Modifier.testTag("mobile_fab_add_warehouse"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Cadastrar Galpão")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (warehouses.isEmpty()) {
                item {
                    EmptyWarehousesPlaceholder(onAdd = onWarehouseAdd)
                }
            } else {
                items(warehouses) { warehouse ->
                    val count = equipments.count { it.warehouseId == warehouse.id }
                    WarehouseRowItem(
                        name = warehouse.name,
                        location = warehouse.location,
                        equipCount = count,
                        isSelected = false,
                        onClick = { onWarehouseSelected(warehouse.id) },
                        onEditClick = { onWarehouseEdit(warehouse.id) },
                        onDeleteClick = { onWarehouseDelete(warehouse) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileWarehouseDetailScreen(
    warehouse: Warehouse?,
    equipments: List<Equipment>,
    allWarehouses: List<Warehouse>,
    viewModel: WarehouseViewModel,
    onAddEquipment: () -> Unit,
    onEditEquipment: (Int) -> Unit,
    onDeleteEquipment: (Equipment) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(warehouse?.name ?: "Inventário", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = onAddEquipment, enabled = warehouse != null, modifier = Modifier.testTag("mobile_add_equipment")) {
                        Icon(Icons.Default.Add, contentDescription = "Cadastrar Equipamento")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (warehouse != null) {
                FloatingActionButton(
                    onClick = onAddEquipment,
                    modifier = Modifier.testTag("mobile_fab_add_equipment"),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Cadastrar Equipamento")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (warehouse != null) {
                // Info Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = warehouse.location, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        if (warehouse.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = warehouse.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Filtering components
            FilterBarSection(viewModel = viewModel)
            Spacer(modifier = Modifier.height(12.dp))

            // Equipment list
            if (equipments.isEmpty()) {
                EmptyInventoryState(
                    queryActive = viewModel.searchQuery.collectAsState().value.isNotBlank(),
                    hasWarehouses = allWarehouses.isNotEmpty()
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(equipments) { equipment ->
                        EquipmentCardItem(
                            equipment = equipment,
                            warehouseName = warehouse?.name ?: "Vários",
                            onEdit = { onEditEquipment(equipment.id) },
                            onDelete = { onDeleteEquipment(equipment) }
                        )
                    }
                }
            }
        }
    }
}

// ======================== MODAL ADD / EDIT FORMS ========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseFormScreen(
    warehouse: Warehouse?,
    onSave: (name: String, location: String, description: String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(warehouse?.name ?: "") }
    var location by remember { mutableStateOf(warehouse?.location ?: "") }
    var description by remember { mutableStateOf(warehouse?.description ?: "") }

    var nameError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (warehouse == null) "Cadastrar Galpão" else "Editar Galpão", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancelar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Field Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("Nome do Galpão *") },
                isError = nameError,
                supportingText = { if (nameError) Text("O nome do galpão é obrigatório *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("warehouse_form_name"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Field Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Setor / Endereço") },
                placeholder = { Text("Ex: Bloco B, Setor Sul") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("warehouse_form_location"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Field Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição adicional") },
                placeholder = { Text("Ex: Destinado a armazenamento refrigerado de insumos elétricos") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("warehouse_form_description"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            nameError = true
                        } else {
                            onSave(name.trim(), location.trim(), description.trim())
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("warehouse_form_submit"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Salvar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentFormScreen(
    equipment: Equipment?,
    warehouses: List<Warehouse>,
    initialWarehouseId: Int,
    onSave: (warehouseId: Int, name: String, tag: String, category: String, status: String, description: String) -> Unit,
    onCancel: () -> Unit
) {
    var selectedWarehouseId by remember { mutableStateOf(equipment?.warehouseId ?: initialWarehouseId) }
    var name by remember { mutableStateOf(equipment?.name ?: "") }
    var tag by remember { mutableStateOf(equipment?.tag ?: "") }
    var category by remember { mutableStateOf(equipment?.category ?: "Ferramenta") }
    var status by remember { mutableStateOf(equipment?.status ?: "Operativo") }
    var description by remember { mutableStateOf(equipment?.description ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var tagError by remember { mutableStateOf(false) }

    val categories = listOf("Maquinário", "Ferramenta", "Elétrico", "Instrumento", "TI / Software", "Outros")
    val statuses = listOf("Operativo", "Em Manutenção", "Inativo")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (equipment == null) "Cadastrar Equipamento" else "Editar Equipamento", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancelar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dropdown Selected Warehouse (Local)
            var warehouseExpanded by remember { mutableStateOf(false) }
            val currentWarehouse = warehouses.find { it.id == selectedWarehouseId }
            Box {
                OutlinedTextField(
                    value = currentWarehouse?.name ?: "Escolha um Galpão...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Galpão Alocado") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = warehouseExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { warehouseExpanded = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedTextColor = if (currentWarehouse != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = warehouseExpanded,
                    onExpandedChange = { warehouseExpanded = it }
                ) {
                    ExposedDropdownMenu(
                        expanded = warehouseExpanded,
                        onDismissRequest = { warehouseExpanded = false },
                        modifier = Modifier.exposedFilter()
                    ) {
                        warehouses.forEach { wh ->
                            DropdownMenuItem(
                                text = { Text(wh.name) },
                                onClick = {
                                    selectedWarehouseId = wh.id
                                    warehouseExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Field Equipment Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("Nome do Equipamento *") },
                isError = nameError,
                supportingText = { if (nameError) Text("O nome do equipamento é obrigatório *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("equipment_form_name"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Field TAG / Patrimonio
            OutlinedTextField(
                value = tag,
                onValueChange = {
                    tag = it
                    if (it.isNotBlank()) tagError = false
                },
                label = { Text("Código TAG / Patrimônio *") },
                placeholder = { Text("Ex: EQ-9844, GER-01") },
                isError = tagError,
                supportingText = { if (tagError) Text("O código TAG/Patrimônio é obrigatório *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("equipment_form_tag"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Category Selection Dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryExpanded = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.exposedFilter()
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Status Selection Dropdown
            var statusExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status Operacional") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { statusExpanded = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it }
                ) {
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false },
                        modifier = Modifier.exposedFilter()
                    ) {
                        statuses.forEach { stat ->
                            DropdownMenuItem(
                                text = { Text(stat) },
                                onClick = {
                                    status = stat
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Description Box
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Especificações / Observações") },
                placeholder = { Text("Ex: Última revisão em Jan/2026. Operando com estabilidade.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("equipment_form_description"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        val isNameBlank = name.isBlank()
                        val isTagBlank = tag.isBlank()

                        if (isNameBlank) nameError = true
                        if (isTagBlank) tagError = true

                        if (!isNameBlank && !isTagBlank) {
                            onSave(selectedWarehouseId, name.trim(), tag.trim(), category, status, description.trim())
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("equipment_form_submit"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Salvar")
                }
            }
        }
    }
}

// Special custom modifier to handle Material Exposed Filter elements compatibility beautifully
private fun Modifier.exposedFilter(): Modifier = this.widthIn(max = 280.dp)

// Utility composable helper to fetch custom heights or modifiers smoothly
@Composable
private fun size(size: androidx.compose.ui.unit.Dp): Modifier = Modifier.size(size)
