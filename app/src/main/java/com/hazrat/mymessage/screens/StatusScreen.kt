package com.hazrat.mymessage.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hazrat.mymessage.CommonDivider
import com.hazrat.mymessage.CommonProgressBar
import com.hazrat.mymessage.CommonRow
import com.hazrat.mymessage.DestinationScreen
import com.hazrat.mymessage.MMViewModel
import com.hazrat.mymessage.TitleText
import com.hazrat.mymessage.navigateTo

@Composable
fun StatusScreen(navController: NavController, viewModel: MMViewModel) {

    val inProcess = viewModel.inProgressStatus.value

    if (inProcess) {
        CommonProgressBar()
    } else {
        val statuses = viewModel.status.value
        val userData = viewModel.userData.value
        val myStatus = statuses.filter {
            it.user.userId == userData?.userId
        }
        val otherStatus = statuses.filter {
            it.user.userId != userData?.userId
        }
        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
                uri->
            uri?.let {
                viewModel.uploadStatus(uri)
            }
        }
        Scaffold(
            floatingActionButton = {
                FAB {
                    launcher.launch("image/*")
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)

                ) {
                    TitleText(text = "Status")
                    if (statuses.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No Status")
                        }

                    } else {
                        if (myStatus.isNotEmpty()) {
                            CommonRow(
                                imageUrl = myStatus[0].user.imageUrl, name = myStatus[0].user.name
                            ) {
                                navigateTo(
                                    navController,
                                    route = DestinationScreen.SingleStatus.createRoute(myStatus[0].user.userId!!)
                                )
                            }
                            CommonDivider()
                            val uniqueUsers = otherStatus.map { it.user }.toSet().toList()
                            LazyColumn(
                                modifier = Modifier.weight(1f)
                            ) {
                                items(uniqueUsers) { user ->
                                    CommonRow(
                                        imageUrl = user.imageUrl, name = user.name
                                    ) {
                                        navigateTo(
                                            navController,
                                            DestinationScreen.SingleStatus.createRoute(user.userId!!)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    BottomNavigationMenu(BottomNavigationItem.STATUSLIST, navController)
                }
            }
        )
    }
}

@Composable
fun FAB(
    onFabClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onFabClick,
        containerColor = Color.Green,
        shape = CircleShape,
        modifier = Modifier.padding(40.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Add Status",
            tint = Color.Black
        )
    }
}