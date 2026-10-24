package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CommunityGuidelinesDialog(onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Guidelines",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "အသိုက်အဝန်း စည်းကမ်းချက်များ (Community Guidelines)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "MyanmarConnect သည် ထိုင်းနိုင်ငံရောက် မြန်မာလုပ်သားများ အချင်းချင်း အထောက်အကူဖြစ်စေရန် ဖန်တီးထားခြင်းဖြစ်ပါသည်။ အောက်ပါစည်းကမ်းချက်များကို လိုက်နာကြရန် မေတ္တာရပ်ခံအပ်ပါသည်:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))

                GuidelineItem("၁။ အပြန်အလှန် လေးစားမှုရှိပါ", "လူမျိုးရေး၊ ဘာသာရေး၊ ကျား/မ ခွဲခြားဆက်ဆံခြင်းနှင့် ဆဲဆိုရိုင်းပြမှုများကို လုံးဝခွင့်မပြုပါ။")
                GuidelineItem("၂။ ငွေကြေးနှင့် အထောက်အထား လိမ်လည်မှု တားမြစ်ခြင်း", "တရားမဝင် ပွဲစားကြော်ငြာများ၊ ငွေကြေးအတုအယောင် လဲလှယ်မှုများနှင့် စာရွက်စာတမ်းအတု ရောင်းချခြင်းများကို တားမြစ်ပါသည်။ တွေ့ရှိပါက အကောင့်အပြီးပိတ်သိမ်းပါမည်။")
                GuidelineItem("၃။ တိကျမှန်ကန်သော သတင်းအချက်အလက် မျှဝေပါ", "ဗီဇာ၊ အလုပ်ပါမစ်နှင့် ဥပဒေရေးရာများတွင် သတင်းမှားများ ဖြန့်ဝေခြင်း မပြုရပါ။")
                GuidelineItem("၄။ ကိုယ်ရေးကိုယ်တာ အချက်အလက် ကာကွယ်ပါ", "မိမိ၏ နိုင်ငံကူးလက်မှတ် (Passport)၊ ပန်းရောင်ကတ် သို့မဟုတ် ဘဏ်အကောင့်နံပါတ် အပြည့်အစုံကို လူသိရှင်ကြား ပို့စ်များတွင် မတင်ပါနှင့်။")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("နားလည်ပါသည်")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Policy,
                    contentDescription = "Privacy Policy",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ကိုယ်ရေးအချက်အလက် လုံခြုံမှုမူဝါဒ (Privacy Policy)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "MyanmarConnect Application သည် သုံးစွဲသူများ၏ ကိုယ်ရေးအချက်အလက် လုံခြုံမှုကို အလေးထားပါသည်။",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                PolicySection("၁။ စုဆောင်းရရှိသော အချက်အလက်များ", "သင်စာရင်းသွင်းရာတွင် ထည့်သွင်းသည့် အမည်၊ ဖုန်းနံပါတ် သို့မဟုတ် အီးမေးလ်၊ လက်ရှိနေထိုင်ရာ ထိုင်းမြို့နယ်နှင့် ပရိုဖိုင်ဓာတ်ပုံတို့ကိုသာ အသုံးပြုပါသည်။ စကားဝှက်များကို လုံခြုံသော SHA-256 Hash စနစ်ဖြင့်သာ သိမ်းဆည်းပြီး မည်သူမျှ မမြင်နိုင်ပါ။")
                PolicySection("၂။ တိုက်ရိုက် မက်ဆေ့ခ်ျ (Direct Chat) လုံခြုံမှု", "သင်၏ သီးသန့်မက်ဆေ့ခ်ျများကို စကားပြောဆိုသူ နှစ်ဦးကြားသာ ဖတ်ရှုနိုင်ပြီး အခြားအသုံးပြုသူများထံ မျှဝေခြင်း မပြုပါ။")
                PolicySection("၃။ တတိယပါတီများနှင့် မျှဝေခြင်း မပြုပါ", "ကျွန်ုပ်တို့သည် အသုံးပြုသူများ၏ အချက်အလက်များကို မည်သည့် ကြော်ငြာအေဂျင်စီ သို့မဟုတ် အခြားပြင်ပအဖွဲ့အစည်းထံသို့မျှ ရောင်းချခြင်း မရှိပါ။")
                PolicySection("၄။ အကောင့်နှင့် အချက်အလက် ဖျက်သိမ်းခွင့်", "သင်သည် မည်သည့်အချိန်တွင်မဆို ပရိုဖိုင်ဆက်တင်မှတဆင့် မိမိအကောင့်နှင့် ပို့စ်များကို အပြီးတိုင် ဖျက်သိမ်းနိုင်ပါသည်။")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("လက်ခံပါသည်")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AccountDeletionDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Delete Account",
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "အကောင့်ကို အပြီးတိုင် ဖျက်သိမ်းမည်လား။",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                )
            }
        },
        text = {
            Text(
                text = "သတိပေးချက်: အကောင့်ဖျက်သိမ်းလိုက်ပါက သင်တင်ထားသော ပို့စ်များ၊ မှတ်ချက်များနှင့် မက်ဆေ့ခ်ျမှတ်တမ်းများအားလုံး အပြီးတိုင် ပျက်ပြယ်သွားမည်ဖြစ်ပြီး နောက်နောင် ပြန်လည်ရယူနိုင်မည် မဟုတ်ပါ။ ဆက်လက်လုပ်ဆောင်လိုပါသလား။",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmDelete()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_delete_account_button")
            ) {
                Text("အပြီးတိုင် ဖျက်သိမ်းမည်")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("မဖျက်တော့ပါ")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun GuidelineItem(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

@Composable
private fun PolicySection(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}
