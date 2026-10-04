package com.astraedus.nudge.ui.screens.groups

import com.astraedus.nudge.R

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    viewModel: GroupViewModel,
    onNavigateBack: () -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(state.selectedGroup?.name ?: strings.getString(R.string.ui_app_groups))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.selectedGroup != null) viewModel.clearSelection()
                        else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.getString(R.string.ui_back))
                    }
                },
                actions = {
                    if (state.selectedGroup != null) {
                        IconButton(onClick = { viewModel.showAddMemberDialog() }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = strings.getString(R.string.ui_add_member))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.selectedGroup == null) {
                FloatingActionButton(onClick = { viewModel.showCreateDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = strings.getString(R.string.ui_add_group))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.selectedGroup == null) {
                GroupList(
                    groups = state.groups,
                    onGroupClick = { viewModel.selectGroup(it.group) }
                )
            } else {
                MemberList(
                    members = state.selectedGroupMembers,
                    installedApps = state.installedApps,
                    onRemove = { viewModel.removeMember(it) }
                )
            }
        }
    }

    if (state.showCreateDialog) {
        CreateGroupDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onCreate = { viewModel.createGroup(it) }
        )
    }

    if (state.showAddMemberDialog) {
        AddMemberDialog(
            installedApps = state.installedApps,
            currentMembers = state.selectedGroupMembers,
            onDismiss = { viewModel.hideAddMemberDialog() },
            onAdd = { viewModel.addMember(it) }
        )
    }
}

@Composable
private fun GroupList(
    groups: List<GroupWithCount>,
    onGroupClick: (GroupWithCount) -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    if (groups.isEmpty()) {
        ListItem(
            headlineContent = { Text(strings.getString(R.string.ui_no_groups_yet)) },
            supportingContent = { Text(strings.getString(R.string.ui_tap_to_create_a_group)) }
        )
    } else {
        LazyColumn {
            items(groups, key = { it.group.id }) { group ->
                ListItem(
                    headlineContent = { Text(group.group.name) },
                    supportingContent = { Text(strings.getString(R.string.ui_apps, group.memberCount)) },
                    leadingContent = {
                        Icon(Icons.Default.Folder, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onGroupClick(group) }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun MemberList(
    members: List<String>,
    installedApps: List<com.astraedus.nudge.data.repository.InstalledAppsRepository.AppInfo>,
    onRemove: (String) -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    val appMap = remember(installedApps) {
        installedApps.associateBy { it.packageName }
    }

    if (members.isEmpty()) {
        ListItem(
            headlineContent = { Text(strings.getString(R.string.ui_no_members)) },
            supportingContent = { Text(strings.getString(R.string.ui_tap_the_add_button_to_add_apps)) }
        )
    } else {
        LazyColumn {
            items(members) { packageName ->
                val app = appMap[packageName]
                ListItem(
                    headlineContent = { Text(app?.appName ?: packageName) },
                    supportingContent = { Text(packageName) },
                    trailingContent = {
                        IconButton(onClick = { onRemove(packageName) }) {
                            Icon(Icons.Default.Close, contentDescription = strings.getString(R.string.ui_remove))
                        }
                    }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.getString(R.string.ui_create_group)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(strings.getString(R.string.ui_group_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onCreate(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text(strings.getString(R.string.ui_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.getString(R.string.ui_cancel)) }
        }
    )
}

@Composable
private fun AddMemberDialog(
    installedApps: List<com.astraedus.nudge.data.repository.InstalledAppsRepository.AppInfo>,
    currentMembers: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    val strings = androidx.compose.ui.platform.LocalContext.current.resources
    val available = installedApps.filter { it.packageName !in currentMembers }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.getString(R.string.ui_add_app_to_group)) },
        text = {
            LazyColumn {
                items(available, key = { it.packageName }) { app ->
                    ListItem(
                        headlineContent = { Text(app.appName) },
                        modifier = Modifier.clickable {
                            onAdd(app.packageName)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.getString(R.string.ui_done)) }
        }
    )
}
