package com.junkitout.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: Sender,
    val text: String,
    val showEmailButton: Boolean = false,
    val queryForEmail: String = "",
)

enum class Sender { USER, BOT }

data class FaqRule(
    val keywords: List<String>,
    val answer: String,
)

private val FAQ_RULES = listOf(
    FaqRule(
        keywords = listOf("accept", "item", "waste", "type", "plastic", "paper", "e-waste", "ewaste", "metal", "dry", "cardboard", "glass"),
        answer = "♻️ Accepted Items & Categories:\n\n1. E-Waste Disposal: Mobiles, laptops, cables, appliances, TVs, circuit boards.\n2. Household Dry Waste: Metals, cans, plastic containers, rubber.\n3. Paper & Cardboard: Newspapers, books, cartons, office paper.\n4. Plastic Waste: Plastic bottles, packaging, bags, PVC items.\n\n❌ Not Accepted: Wet/kitchen waste, hazardous chemicals, or medical waste."
    ),
    FaqRule(
        keywords = listOf("price", "cost", "rate", "charge", "amount", "rupee", "rs", "kg", "value", "minimum"),
        answer = "💰 Rates & Pricing:\n\n• E-Waste Disposal: ₹12 / kg\n• Paper & Cardboard: ₹8 / kg\n• Household Dry Waste: ₹7 / kg\n• Plastic Waste: ₹6 / kg\n\n📌 Note: Minimum order value is ₹49 per pickup. Our partner verifies final weight at your doorstep."
    ),
    FaqRule(
        keywords = listOf("slot", "time", "schedule", "when", "date", "today", "tomorrow", "hour", "timing"),
        answer = "⏰ Pickup Schedules & Timings:\n\nWe operate 7 days a week with 3 daily slots:\n• 9 AM – 12 PM\n• 12 PM – 3 PM\n• 3 PM – 6 PM\n\nYou can schedule pickups up to 7 days in advance through the Book tab."
    ),
    FaqRule(
        keywords = listOf("pay", "payment", "upi", "card", "cash", "gpay", "phonepe", "paytm", "refund", "razorpay"),
        answer = "💳 Payment Methods:\n\n• Online: UPI (GPay, PhonePe, Paytm, BHIM), Credit/Debit Cards, Netbanking, Wallets via Razorpay.\n• Pay at Pickup: Cash or UPI directly to our pickup agent after weighing."
    ),
    FaqRule(
        keywords = listOf("area", "pincode", "location", "city", "bengaluru", "place", "serviceable", "where"),
        answer = "📍 Serviced Areas:\n\nWe currently cover major areas in Bengaluru including HSR Layout, Koramangala, Indiranagar, BTM Layout, Jayanagar, Whitefield, and surrounding pincodes.\n\nYou can test your exact area/pincode on the Location screen!"
    ),
    FaqRule(
        keywords = listOf("cancel", "reschedule", "change", "modify", "order", "status", "track"),
        answer = "📋 Bookings & Cancellation:\n\nYou can view all your scheduled bookings under the Orders tab.\nIf you need to cancel or reschedule, tap 'Email Support Team' or email us at support@junkitout.in."
    ),
    FaqRule(
        keywords = listOf("hello", "hi", "hey", "start", "help", "assistant"),
        answer = "👋 Hello! How can I help you today? You can choose a quick topic below or type your question."
    )
)

@Composable
fun SupportChatScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = Sender.BOT,
                text = "👋 Hi there! I'm Junk-It-Out Support Assistant.\n\nHow can I help you today? Select a quick topic below or type your question."
            )
        )
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun sendEmail(userQuery: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@junkitout.in")).apply {
            putExtra(Intent.EXTRA_SUBJECT, "Junk-It-Out Customer Inquiry")
            putExtra(Intent.EXTRA_TEXT, "Hello Support Team,\n\nI have a question regarding:\n\"$userQuery\"\n\nThank you!")
        }
        runCatching { ctx.startActivity(intent) }
    }

    fun handleUserMessage(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return

        messages.add(ChatMessage(sender = Sender.USER, text = q))
        inputText = ""

        val normalized = q.lowercase()
        val matchedRule = FAQ_RULES.firstOrNull { rule ->
            rule.keywords.any { kw -> normalized.contains(kw) }
        }

        if (matchedRule != null) {
            messages.add(ChatMessage(sender = Sender.BOT, text = matchedRule.answer))
        } else {
            messages.add(
                ChatMessage(
                    sender = Sender.BOT,
                    text = "I don't have an automated answer for:\n\"$q\"\n\nWould you like to send this question directly to our Support Team via email?",
                    showEmailButton = true,
                    queryForEmail = q
                )
            )
        }

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ScreenRoot(dark = false, background = solid(Color(0xFFF6FAF6))) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Surface(
                color = JColors.ForestGreen,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth().systemBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackButton(onBack)
                    Spacer(Modifier.width(12.dp))
                    Box(
                        Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.SmartToy, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Junk-It-Out Assistant", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Text("● Online • Instant FAQ Answers", color = Color(0xFFA2E8B5), fontSize = 11.sp)
                    }
                    IconButton(onClick = { sendEmail("General Inquiry") }) {
                        Icon(Icons.Filled.Email, "Email Support", tint = Color.White)
                    }
                }
            }

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(msg, onSendEmail = { sendEmail(it) })
                }
            }

            // Quick Topic Chips
            Column(Modifier.fillMaxWidth().background(Color.White).padding(vertical = 8.dp)) {
                Text(
                    "Frequent Topics:",
                    fontSize = 11.sp,
                    color = JColors.Subtle,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val chips = listOf(
                        "Accepted Items",
                        "Rates & Pricing",
                        "Pickup Slots",
                        "Payment Methods"
                    )
                    chips.forEach { chip ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = JColors.Light,
                            border = BorderStroke(1.dp, JColors.Border),
                            modifier = Modifier.clickable { handleUserMessage(chip) }
                        ) {
                            Text(
                                chip,
                                fontSize = 11.sp,
                                color = JColors.Primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Text Input Bar
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type your question...", fontSize = 14.sp, color = JColors.Subtle) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JColors.Primary,
                            unfocusedBorderColor = JColors.Border,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { handleUserMessage(inputText) })
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier.size(46.dp).clip(CircleShape).background(JColors.Primary)
                            .clickable { handleUserMessage(inputText) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, onSendEmail: (String) -> Unit) {
    val isBot = message.sender == Sender.BOT
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isBot) Arrangement.Start else Arrangement.End
    ) {
        if (isBot) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(JColors.Mint),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.SmartToy, null, tint = JColors.Primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(Modifier.widthIn(max = 280.dp)) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (isBot) 4.dp else 16.dp,
                    bottomEnd = if (isBot) 16.dp else 4.dp
                ),
                color = if (isBot) Color.White else JColors.Primary,
                shadowElevation = 1.dp
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = if (isBot) JColors.Text else Color.White,
                        fontSize = 14.sp,
                        lineHeight = 19.sp
                    )

                    if (message.showEmailButton) {
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { onSendEmail(message.queryForEmail) },
                            colors = ButtonDefaults.buttonColors(containerColor = JColors.Primary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Email, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Email Support Team", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
