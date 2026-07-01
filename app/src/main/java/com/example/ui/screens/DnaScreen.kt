package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BioViewModel
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnaScreen(viewModel: BioViewModel) {
    val sequence by viewModel.dnaInput.collectAsState()
    val sequenceName by viewModel.dnaSequenceName.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- Dynamic Bioinformatics Algorithms ---
    val cleanSeq = remember(sequence) {
        sequence.uppercase().filter { it in listOf('A', 'T', 'G', 'C', 'N') }
    }

    val seqLength = cleanSeq.length

    val gcContent = remember(cleanSeq) {
        if (cleanSeq.isEmpty()) 0.0
        else {
            val gcCount = cleanSeq.count { it == 'G' || it == 'C' }
            (gcCount.toDouble() / cleanSeq.length) * 100
        }
    }

    val molecularWeight = remember(cleanSeq) {
        // Approximate molecular weight of single stranded DNA: (Length * 303.7) + 79.0 g/mol
        if (cleanSeq.isEmpty()) 0.0
        else (cleanSeq.length * 303.7) + 79.0
    }

    val complement = remember(cleanSeq) {
        cleanSeq.map {
            when (it) {
                'A' -> 'T'
                'T' -> 'A'
                'G' -> 'C'
                'C' -> 'G'
                else -> 'N'
            }
        }.joinToString("")
    }

    val reverseComplement = remember(complement) {
        complement.reversed()
    }

    // Translation mapping
    val codonTable = mapOf(
        "ATG" to "M", "TGG" to "W",
        "TTT" to "F", "TTC" to "F",
        "TTA" to "L", "TTG" to "L", "CTT" to "L", "CTC" to "L", "CTA" to "L", "CTG" to "L",
        "ATT" to "I", "ATC" to "I", "ATA" to "I",
        "GTT" to "V", "GTC" to "V", "GTA" to "V", "GTG" to "V",
        "TCT" to "S", "TCC" to "S", "TCA" to "S", "TCG" to "S", "AGT" to "S", "AGC" to "S",
        "CCT" to "P", "CCC" to "P", "CCA" to "P", "CCG" to "P",
        "ACT" to "T", "ACC" to "T", "ACA" to "T", "ACG" to "T",
        "GCT" to "A", "GCC" to "A", "GCA" to "A", "GCG" to "A",
        "TAT" to "Y", "TAC" to "Y",
        "CAT" to "H", "CAC" to "H",
        "CAA" to "Q", "CAG" to "Q",
        "AAT" to "N", "AAC" to "N",
        "AAG" to "K", "AAA" to "K",
        "GAT" to "D", "GAC" to "D",
        "GAA" to "E", "GAG" to "E",
        "TGT" to "C", "TGC" to "C",
        "CGT" to "R", "CGC" to "R", "CGA" to "R", "CGG" to "R", "AGA" to "R", "AGG" to "R",
        "GGT" to "G", "GGC" to "G", "GGA" to "G", "GGG" to "G",
        "TAA" to "*", "TAG" to "*", "TGA" to "*" // Stops
    )

    val translation = remember(cleanSeq) {
        val sb = StringBuilder()
        for (i in 0 until cleanSeq.length - 2 step 3) {
            val codon = cleanSeq.substring(i, i + 3)
            val aa = codonTable[codon] ?: "X"
            sb.append(aa)
        }
        sb.toString()
    }

    // ORFs Detector (start ATG, end Stop *)
    val orfs = remember(translation) {
        val foundOrfs = mutableListOf<String>()
        var startIdx = -1
        for (i in translation.indices) {
            if (translation[i] == 'M') {
                if (startIdx == -1) startIdx = i
            } else if (translation[i] == '*' && startIdx != -1) {
                foundOrfs.add(translation.substring(startIdx, i))
                startIdx = -1
            }
        }
        if (startIdx != -1) {
            foundOrfs.add(translation.substring(startIdx))
        }
        foundOrfs
    }

    // Restriction sites detection
    val restrictionSites = remember(cleanSeq) {
        val sites = mutableListOf<Pair<String, Int>>()
        val enzymePatterns = mapOf(
            "EcoRI (GAATTC)" to "GAATTC",
            "BamHI (GGATCC)" to "GGATCC",
            "HindIII (AAGCTT)" to "AAGCTT"
        )
        for ((name, pattern) in enzymePatterns) {
            var index = cleanSeq.indexOf(pattern)
            while (index != -1) {
                sites.add(Pair(name, index + 1))
                index = cleanSeq.indexOf(pattern, index + 1)
            }
        }
        sites
    }

    // Codon frequencies
    val codonFrequencies = remember(cleanSeq) {
        val map = mutableMapOf<String, Int>()
        for (i in 0 until cleanSeq.length - 2 step 3) {
            val codon = cleanSeq.substring(i, i + 3)
            if (codon.length == 3) {
                map[codon] = map.getOrDefault(codon, 0) + 1
            }
        }
        map.toList().sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "🧬 DNA SEQUENCE ASSAY WORKSPACE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Perform dynamic transcription, complementary alignments, molecular weights, restrict enzyme searches, and translation.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Target Name & Sequence Inputs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = sequenceName,
                        onValueChange = { viewModel.dnaSequenceName.value = it },
                        label = { Text("Sequence Identifier/Target Name") },
                        modifier = Modifier.fillMaxWidth().testTag("dna_name_input")
                    )

                    OutlinedTextField(
                        value = sequence,
                        onValueChange = { viewModel.dnaInput.value = it },
                        label = { Text("Pasted DNA Sequence (A, T, G, C)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("dna_sequence_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        placeholder = { Text("Enter nucleotides...") }
                    )
                }
            }
        }

        // Statistics Grid Row
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CardValueWidget(
                    modifier = Modifier.weight(1f),
                    title = "Sequence Length",
                    value = "$seqLength bp"
                )
                CardValueWidget(
                    modifier = Modifier.weight(1f),
                    title = "GC Ratio",
                    value = String.format("%.1f %%", gcContent)
                )
                CardValueWidget(
                    modifier = Modifier.weight(1f),
                    title = "Approx. Mass",
                    value = String.format("%.0f Da", molecularWeight)
                )
            }
        }

        // Operations Results Container
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "TRANSCRIPTION & ALIGNMENTS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                    // Complement Display
                    AlignmentDisplayBlock(
                        label = "Complement Structure",
                        content = complement,
                        onCopy = { clipboardManager.setText(AnnotatedString(complement)) }
                    )

                    // Reverse Complement
                    AlignmentDisplayBlock(
                        label = "Reverse Complement Sequence (5' -> 3')",
                        content = reverseComplement,
                        onCopy = { clipboardManager.setText(AnnotatedString(reverseComplement)) }
                    )

                    // Protein Translation
                    AlignmentDisplayBlock(
                        label = "Translated Amino Acid Sequence (F1)",
                        content = translation,
                        onCopy = { clipboardManager.setText(AnnotatedString(translation)) }
                    )
                }
            }
        }

        // ORF and Enzymes Results
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "GENETIC SUB-STRUCTURES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                    // ORF Detection
                    Text(text = "Open Reading Frames (ORFs):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    if (orfs.isEmpty()) {
                        Text(text = "No standard start/stop bounded ORFs found in frame +1.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        orfs.forEachIndexed { idx, orf ->
                            Text(
                                text = "ORF ${idx + 1} (Len: ${orf.length} aa): $orf",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF16A34A),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFECFDF5))
                                    .padding(6.dp)
                            )
                        }
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    // Restriction Enzymes
                    Text(text = "Restriction Sites Detected:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    if (restrictionSites.isEmpty()) {
                        Text(text = "No standard restriction sites found (EcoRI, BamHI, HindIII).", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        restrictionSites.forEach { (enzyme, pos) ->
                            Text(
                                text = "Cut Site for $enzyme found at index position $pos",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                    .padding(6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Codon Usage Block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "CODON USAGE FREQUENCY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    if (codonFrequencies.isEmpty()) {
                        Text(text = "No codon frequencies computed.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            codonFrequencies.take(4).forEach { (codon, count) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color(0xFFF1F5F9))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = codon, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "$count runs", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Save Button Area
        item {
            Button(
                onClick = {
                    val resultJson = JSONObject().apply {
                        put("length", seqLength)
                        put("gcContent", gcContent)
                        put("molecularWeight", molecularWeight)
                        put("complement", complement)
                        put("protein", translation)
                        put("orfsCount", orfs.size)
                    }
                    viewModel.saveAnalysisRecord("DNA", sequenceName, cleanSeq, resultJson)
                    // Trigger simple show in log or snackbar or notify complete
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_dna_assay_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Assay Record to Workspace", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun CardValueWidget(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun AlignmentDisplayBlock(
    label: String,
    content: String,
    onCopy: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Copy",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onCopy() }
            )
        }
        Text(
            text = if (content.isEmpty()) "Empty Sequence" else content,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = if (content.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9))
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                .padding(8.dp)
        )
    }
}
