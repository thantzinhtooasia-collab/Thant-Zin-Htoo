package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReportPostDialog(
    postId: String,
    onDismiss: () -> Unit,
    onSubmitReport: (reason: String) -> Unit
) {
    val reportReasons = listOf(
        "သတင်းမှား သို့မဟုတ် ငွေကြေးလိမ်လည်မှု (Scam / Fraud)",
        "တရားမဝင် ပွဲစားလုပ်ငန်း သို့မဟုတ် အထောက်အထားအတု (Illegal Broker)",
        "ဆဲဆိုကြိမ်းမောင်းခြင်းနှင့် မသင့်လျော်သော အသုံးအနှုန်း (Hate Speech)",
        "အလုပ်သမား အခွင့်အရေး ချိုးဖောက်မှု (Labor Exploitation)",
        "အခြား မသင့်လျော်သော အကြောင်းအရာ (Other)"
    )

    var selectedReason by remember { mutableStateOf(reportReasons[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Report",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ပို့စ်ကို တိုင်ကြားရန် (Report Post)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "ဤပို့စ်သည် အသိုက်အဝန်း စည်းကမ်းချက်များနှင့် မညီညွတ်ပါက တိုင်ကြားရသည့် အကြောင်းရင်းကို ရွေးချယ်ပေးပါ:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                reportReasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (selectedReason == reason) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (selectedReason == reason) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmitReport(selectedReason)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("submit_report_button")
            ) {
                Text("တိုင်ကြားမည်")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("မလုပ်တော့ပါ")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun BlockUserDialog(
    userName: String,
    onDismiss: () -> Unit,
    onConfirmBlock: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Block",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "အသုံးပြုသူကို ပိတ်ဆို့မည် (Block User)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Text(
                text = "${userName} ကို ပိတ်ဆို့လိုက်ပါက သူ၏ ပို့စ်များနှင့် မက်ဆေ့ခ်ျများကို သင်တွေ့မြင်ရတော့မည် မဟုတ်ပါ။ ဤလုပ်ဆောင်ချက်ကို ပရိုဖိုင်တွင် ပြန်လည်ပြင်ဆင်နိုင်ပါသည်။",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmBlock()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_block_button")
            ) {
                Text("ပိတ်ဆို့မည် (Block)")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("မလုပ်တော့ပါ")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
