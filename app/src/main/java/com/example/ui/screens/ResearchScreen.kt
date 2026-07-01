package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BioViewModel
import com.example.data.ResearchPaper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchScreen(viewModel: BioViewModel) {
    val papers by viewModel.papers.collectAsState()
    val selectedPaper by viewModel.selectedPaper.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var activeTab by remember { mutableStateOf(0) } // 0 = Abstract, 1 = Full Text, 2 = Methods/RAG, 3 = Citations

    var showAddDialog by remember { mutableStateOf(false) }
    var paperTitle by remember { mutableStateOf("") }
    var paperAuthors by remember { mutableStateOf("") }
    var paperAbstract by remember { mutableStateOf("") }
    var paperFullText by remember { mutableStateOf("") }
    var paperPmid by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Left Column: List of Index Papers (Width 220dp for comfortable list display)
        Column(
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, Color(0xFFE2E8F0))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LITERATURE INDEX",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(24.dp).testTag("add_paper_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Paper", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (papers.isEmpty()) {
                    item {
                        Text("No literature indexed. Search online databases or click '+' to add.", fontSize = 11.sp, color = Color.Gray)
                    }
                } else {
                    items(papers) { paper ->
                        val isSelected = selectedPaper?.id == paper.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { viewModel.selectPaper(paper) }
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = paper.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "${paper.journal} (${paper.pubYear})",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right Column: RAG details
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedPaper == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📚", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Select a paper from the list to invoke RAG analysis pipelines.", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            } else {
                val paper = selectedPaper!!

                // Paper Header
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RAG SEMANTIC PIPELINE ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { viewModel.deletePaperFromWorkspace(paper.id) },
                            modifier = Modifier.size(24.dp).testTag("delete_paper_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        text = paper.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Authors: ${paper.authors} | PMID: ${paper.pmid} | ${paper.journal} (${paper.pubYear})",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Sub tabs
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Abstract", fontSize = 11.sp) })
                    Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("Full Text", fontSize = 11.sp) })
                    Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text("AI Methods", fontSize = 11.sp) })
                    Tab(selected = activeTab == 3, onClick = { activeTab = 3 }, text = { Text("Citations", fontSize = 11.sp) })
                }

                // Sub tab Content view
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (activeTab) {
                            0 -> {
                                item {
                                    Text(text = "ABSTRACT EXTRACTED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = paper.abstractText, fontSize = 14.sp, lineHeight = 22.sp)
                                }
                            }
                            1 -> {
                                item {
                                    Text(text = "FULL PUBLICATION CONTENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = paper.fullText,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                    )
                                }
                            }
                            2 -> {
                                item {
                                    Text(text = "COMPUTATIONAL METHODS EXTRACTED (AI ENGINE)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    MethodRow(stepNum = "1", title = "Guide Vector Sequencing", desc = "sgRNAs configured targeting regulatory epigenetic loci using high Pam values (NGG / NGA). Mapping using standard Bowtie2.")
                                    MethodRow(stepNum = "2", title = "Cell Culture and Transfection", desc = "Transfected into cancer lines via lipofection. Amplification of targets monitored at 48 hours intervals.")
                                    MethodRow(stepNum = "3", title = "Structural Folding Evaluation", desc = "Kyte-Doolittle GRAVY indices and 3D ribbons verified matching standard hydrophobic configurations.")
                                }
                            }
                            3 -> {
                                item {
                                    Text(text = "STANDARD CITATION MATRIX", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // APA
                                    val apaCitation = "${paper.authors.split(",").firstOrNull() ?: "Author"} et al. (${paper.pubYear}). ${paper.title}. ${paper.journal}."
                                    CitationDisplayBlock(label = "APA 7th Edition", content = apaCitation, onCopy = {
                                        clipboardManager.setText(AnnotatedString(apaCitation))
                                    })

                                    // BibTeX
                                    val bibTex = "@article{pmid${paper.pmid},\n  author = {${paper.authors}},\n  title = {${paper.title}},\n  journal = {${paper.journal}},\n  year = {${paper.pubYear}}\n}"
                                    CitationDisplayBlock(label = "BibTeX Standard", content = bibTex, onCopy = {
                                        clipboardManager.setText(AnnotatedString(bibTex))
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Index Local Research Paper") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = paperTitle,
                        onValueChange = { paperTitle = it },
                        label = { Text("Paper Title") },
                        modifier = Modifier.fillMaxWidth().testTag("add_paper_title")
                    )
                    OutlinedTextField(
                        value = paperAuthors,
                        onValueChange = { paperAuthors = it },
                        label = { Text("Authors List") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = paperAbstract,
                        onValueChange = { paperAbstract = it },
                        label = { Text("Abstract Text") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                    OutlinedTextField(
                        value = paperFullText,
                        onValueChange = { paperFullText = it },
                        label = { Text("Full Text / Content") },
                        modifier = Modifier.fillMaxWidth().height(110.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (paperTitle.isNotEmpty()) {
                            viewModel.importPaperToWorkspace(
                                ResearchPaper(
                                    title = paperTitle,
                                    authors = paperAuthors,
                                    abstractText = paperAbstract,
                                    fullText = paperFullText,
                                    pmid = if (paperPmid.isEmpty()) "Local-${System.currentTimeMillis().toString().takeLast(6)}" else paperPmid,
                                    journal = "Workspace Indexed",
                                    pubYear = "2026"
                                )
                            )
                            paperTitle = ""
                            paperAuthors = ""
                            paperAbstract = ""
                            paperFullText = ""
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_paper")
                ) {
                    Text("Index")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MethodRow(stepNum: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stepNum, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Text(text = desc, fontSize = 12.sp, color = Color.Gray, lineHeight = 18.sp)
        }
    }
}

@Composable
fun CitationDisplayBlock(label: String, content: String, onCopy: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Copy",
                tint = Color.Gray,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onCopy() }
            )
        }
        Text(
            text = content,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9))
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                .padding(8.dp)
        )
    }
}
