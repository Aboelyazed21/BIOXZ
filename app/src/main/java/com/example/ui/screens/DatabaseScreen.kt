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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BioViewModel
import com.example.data.ResearchPaper
import com.example.data.UniProtEntry

@Composable
fun DatabaseScreen(viewModel: BioViewModel) {
    val query by viewModel.dbSearchQuery.collectAsState()
    val isSearching by viewModel.isSearchingDb.collectAsState()
    val pubMedResults by viewModel.pubMedResults.collectAsState()
    val uniProtResults by viewModel.uniProtResults.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = PubMed, 1 = UniProt (NCBI)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "ENTREZ SCIENTIFIC DATABASES CLIENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.dbSearchQuery.value = it },
                        placeholder = { Text("Search PubMed/UniProt for INS_HUMAN, CRISPR, etc...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("db_search_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { viewModel.searchDatabases() }
                            .testTag("db_search_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                        }
                    }
                }
            }
        }

        Divider(color = Color(0xFFE2E8F0))

        // Tab Row
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("PubMed Literature (${pubMedResults.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("UniProt Proteins (${uniProtResults.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        // Results lists
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Querying live scientific endpoints...", fontSize = 13.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (activeTab == 0) {
                    // PubMed
                    if (pubMedResults.isEmpty()) {
                        item {
                            EmptyResultsState(msg = "Enter a search query to search PubMed literature index.")
                        }
                    } else {
                        items(pubMedResults) { paper ->
                            PubMedPaperCard(paper = paper, onImport = {
                                viewModel.importPaperToWorkspace(paper)
                            })
                        }
                    }
                } else {
                    // UniProt
                    if (uniProtResults.isEmpty()) {
                        item {
                            EmptyResultsState(msg = "Enter a search query to pull verified amino-acid structures from UniProt.")
                        }
                    } else {
                        items(uniProtResults) { protein ->
                            UniProtEntryCard(protein = protein, onLoadDna = {
                                viewModel.dnaInput.value = protein.sequence?.value ?: ""
                            }, onLoadProtein = {
                                viewModel.proteinInput.value = protein.sequence?.value ?: ""
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyResultsState(msg: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🔍", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun PubMedPaperCard(paper: ResearchPaper, onImport: () -> Unit) {
    var imported by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "PubMed PMID: ${paper.pmid}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${paper.journal} • ${paper.pubYear}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = paper.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = "Authors: ${paper.authors}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Text(
                text = paper.abstractText,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    onImport()
                    imported = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (imported) Color(0xFF16A34A) else MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (imported) "Imported to Workspace" else "Import Paper for AI Summaries", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun UniProtEntryCard(
    protein: UniProtEntry,
    onLoadDna: () -> Unit,
    onLoadProtein: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Accession: ${protein.primaryAccession}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "ID: ${protein.uniProtkbId}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )
            }

            Text(
                text = protein.proteinDescription?.recommendedName?.fullName?.value ?: "Verified PolyPeptide Vector",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = "Organism: ${protein.organism?.scientificName ?: "Homo sapiens"}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            protein.sequence?.let { seq ->
                Text(
                    text = "Sequence (${seq.length} aa, Mw: ${seq.molWeight} Da):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (seq.value.length > 60) seq.value.take(60) + "..." else seq.value,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLoadProtein,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Load Amino Acids", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onLoadDna,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Load DNA Vector", fontSize = 11.sp)
                }
            }
        }
    }
}
