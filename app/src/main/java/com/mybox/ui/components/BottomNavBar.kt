package com.mybox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.LockPerson
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BottomNavItem(
    val label: String,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector
)

@Composable
fun BottomNavBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            label = "密码管理",
            outlinedIcon = Icons.Outlined.LockPerson,
            filledIcon = Icons.Filled.LockPerson
        ),
        BottomNavItem(
            label = "事件管理",
            outlinedIcon = Icons.Outlined.Event,
            filledIcon = Icons.Filled.Event
        )
    )

    val backgroundColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
    val activeColor = MaterialTheme.colorScheme.onPrimary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(backgroundColor),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onItemSelected(index) }
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Icon(
                    imageVector = if (selectedItem == index) item.filledIcon else item.outlinedIcon,
                    contentDescription = item.label,
                    tint = if (selectedItem == index) activeColor else inactiveColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = item.label,
                    fontWeight = if (selectedItem == index) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (selectedItem == index) activeColor else inactiveColor
                )
            }
        }
    }
}
