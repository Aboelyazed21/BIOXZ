package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BioViewModel
import com.example.data.ChatMessage
import kotlinx.coroutines.launch

@Composable
fun AiChatScreen(viewModel: BioViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val inputMessage by viewModel.inputMessage.collectAsState()

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val templates = listOf(
        "Generate a CRISPR sgRNA targeting Human DNMT1 with high PAM efficiency",
        "Write a robust Python function to compute codon usage frequency for a FASTA sequence",
        "Explain RNASeq normalization. Difference between TPM, FPKM, and DESeq2's median-of-ratios",
        "Simulate transition/transversion mutations in standard mitochondrial DNA vectors",
        "Suggest target reference sequences for validating Huntington's disease CAG repeats"
    )

    // Scroll to bottom when message size changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Header with actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Scientific AI Assistant",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Integrated with Deep RAG & Molecular Models",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.testTag("clear_chat_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear Chat",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Divider(color = Color(0xFFE2E8F0))

        // Messages Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message = message)
            }

            if (isAiLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "AI is aligning parameters and analyzing...",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Templates Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(top = 8.dp)
        ) {
            Text(
                text = "SUGGESTED ANALYTICAL INSTRUCTIONS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates) { template ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .clickable { viewModel.sendTemplateQuery(template) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (template.length > 45) template.take(45) + "..." else template,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Input Box Row
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { viewModel.inputMessage.value = it },
                    placeholder = { Text("Ask about DNA sequences, papers, or bioinformatics formulas...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { viewModel.sendMessage() }
                        .testTag("send_msg_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9) // Slate 100 for assistant bubble
    val textColor = if (isUser) Color.White else Color(0xFF0F172A) // Slate 900 for assistant text
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp) // Generous width for official report formats
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .border(
                    width = 1.dp,
                    color = if (isUser) Color.Transparent else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .padding(14.dp)
        ) {
            SelectionContainer {
                FormattedScienceText(text = message.content, textColor = textColor)
            }
        }
    }
}

@Composable
fun FormattedScienceText(text: String, textColor: Color) {
    val lines = text.split("\n")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        var isCodeMode = false
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("```")) {
                isCodeMode = !isCodeMode
                return@forEach
            }

            if (isCodeMode) {
                // Code block with clean padding & monospace
                Text(
                    text = line,
                    color = if (textColor == Color.White) Color(0xFFEFF6FF) else Color(0xFF1E293B),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (textColor == Color.White) Color.White.copy(alpha = 0.15f) else Color(0xFFF8FAFC))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            } else {
                when {
                    trimmed.startsWith("####") || trimmed.startsWith("###") || trimmed.startsWith("##") || trimmed.startsWith("#") -> {
                        val headerText = trimmed.replace(Regex("^#+\\s*"), "")
                        Text(
                            text = headerText,
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                        )
                    }
                    trimmed.startsWith("-") || trimmed.startsWith("*") -> {
                        val bulletText = trimmed.substring(1).trim()
                        val annotatedText = parseInlineFormatting(bulletText, textColor)
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(text = "• ", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = annotatedText,
                                color = textColor,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    else -> {
                        val annotatedText = parseInlineFormatting(line, textColor)
                        Text(
                            text = annotatedText,
                            color = textColor,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

fun parseInlineFormatting(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val boldStart = text.indexOf("**", cursor)
            if (boldStart != -1) {
                val boldEnd = text.indexOf("**", boldStart + 2)
                if (boldEnd != -1) {
                    if (boldStart > cursor) {
                        append(text.substring(cursor, boldStart))
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                        append(text.substring(boldStart + 2, boldEnd))
                    }
                    cursor = boldEnd + 2
                } else {
                    append(text.substring(cursor))
                    cursor = text.length
                }
            } else {
                val tickStart = text.indexOf("`", cursor)
                if (tickStart != -1) {
                    val tickEnd = text.indexOf("`", tickStart + 1)
                    if (tickEnd != -1) {
                        if (tickStart > cursor) {
                            append(text.substring(cursor, tickStart))
                        }
                        withStyle(style = SpanStyle(fontFamily = FontFamily.Monospace, background = defaultColor.copy(alpha = 0.08f), fontSize = 11.sp)) {
                            append(text.substring(tickStart + 1, tickEnd))
                        }
                        cursor = tickEnd + 1
                    } else {
                        append(text.substring(cursor))
                        cursor = text.length
                    }
                } else {
                    append(text.substring(cursor))
                    cursor = text.length
                }
            }
        }
    }
}
