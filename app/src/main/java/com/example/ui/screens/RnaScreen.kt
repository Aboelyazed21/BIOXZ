package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BioViewModel
import org.json.JSONObject

@Composable
fun RnaScreen(viewModel: BioViewModel) {
    val rnaInput by viewModel.rnaInput.collectAsState()

    val cleanRna = remember(rnaInput) {
        rnaInput.uppercase().filter { it in listOf('A', 'U', 'G', 'C', 'N') }
    }

    // Secondary Structure Base-pairing algorithm
    // We predict base pairing (Watson-Crick AU, GC and wobble GU)
    val pairings = remember(cleanRna) {
        val pairs = mutableListOf<Pair<Int, Int>>()
        val stack = mutableListOf<Int>()
        for (i in cleanRna.indices) {
            val char = cleanRna[i]
            if (char == 'G' || char == 'A') {
                stack.add(i)
            } else if (char == 'C' || char == 'U') {
                if (stack.isNotEmpty()) {
                    val matchingIndex = stack.removeAt(stack.size - 1)
                    val matchChar = cleanRna[matchingIndex]
                    // Validate base pair
                    if ((matchChar == 'A' && char == 'U') ||
                        (matchChar == 'G' && char == 'C') ||
                        (matchChar == 'G' && char == 'U')
                    ) {
                        pairs.add(Pair(matchingIndex, i))
                    }
                }
            }
        }
        pairs.sortedBy { it.first }
    }

    // --- Mock Volcano Plot Data Coordinates ---
    // A volcano plot displays statistical significance (p-value) vs magnitude of change (log2 Fold Change)
    // Up-regulated in RED, Down-regulated in GREEN, Unregulated in GRAY.
    val volcanoPoints = remember {
        listOf(
            VolcanoPoint(log2FC = -2.5f, pValue = 0.0001f, gene = "DNMT1", status = "Down"),
            VolcanoPoint(log2FC = -1.8f, pValue = 0.001f, gene = "EZH2", status = "Down"),
            VolcanoPoint(log2FC = 3.2f, pValue = 0.00001f, gene = "MYC", status = "Up"),
            VolcanoPoint(log2FC = 2.1f, pValue = 0.0005f, gene = "SOX2", status = "Up"),
            VolcanoPoint(log2FC = -0.3f, pValue = 0.5f, gene = "GAPDH", status = "Neutral"),
            VolcanoPoint(log2FC = 0.5f, pValue = 0.3f, gene = "ACTB", status = "Neutral"),
            VolcanoPoint(log2FC = 1.2f, pValue = 0.08f, gene = "TP53", status = "Neutral"),
            VolcanoPoint(log2FC = -1.1f, pValue = 0.04f, gene = "DNMT3A", status = "Down"),
            VolcanoPoint(log2FC = 2.8f, pValue = 0.00004f, gene = "OCT4", status = "Up")
        )
    }

    // --- Heatmap Expression Data ---
    // Columns are samples: Control-1, Control-2, Treated-1, Treated-2
    val heatmapGenes = listOf("MYC", "SOX2", "DNMT1", "EZH2")
    val heatmapData = remember {
        listOf(
            floatArrayOf(1.2f, 1.0f, 4.5f, 4.2f),   // MYC (up-regulated in treated)
            floatArrayOf(0.8f, 0.9f, 3.1f, 3.4f),   // SOX2 (up-regulated in treated)
            floatArrayOf(5.1f, 4.8f, 1.5f, 1.2f),   // DNMT1 (down-regulated in treated)
            floatArrayOf(3.9f, 4.2f, 0.9f, 0.8f)    // EZH2 (down-regulated in treated)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Panel
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "🧪 RNA TRANSCRIPT ANALYSIS WORKSPACE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Compute wobble base foldings, evaluate differential transcripts, and inspect volcano expression charts and heatmaps.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Sequence Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "RNA Sequence Workspace", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = rnaInput,
                        onValueChange = { viewModel.rnaInput.value = it },
                        label = { Text("RNA Transcript Nucleotides (A, U, G, C)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("rna_input_field")
                    )
                }
            }
        }

        // Foldings & Structures Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "SECONDARY STRUCTURAL PAIRINGS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                    Text(text = "Wobble base pairings predicted in context (AU / GC / GU):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    if (pairings.isEmpty()) {
                        Text(text = "No folded hairpins or stems predicted on current sequence length.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        pairings.forEach { (first, second) ->
                            Text(
                                text = "Stem Loop Connection: Base [${cleanRna[first]} at $first] pairs with Base [${cleanRna[second]} at $second]",
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
                }
            }
        }

        // Volcano Plot Visualizer
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "DIFFERENTIAL EXPRESSION (VOLCANO PLOT)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(text = "MCF-7 Treated vs Ctrl", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            // Draw axes
                            drawLine(
                                color = Color.Gray,
                                start = Offset(40f, canvasHeight - 40f),
                                end = Offset(canvasWidth - 20f, canvasHeight - 40f),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = Color.Gray,
                                start = Offset(40f, 20f),
                                end = Offset(40f, canvasHeight - 40f),
                                strokeWidth = 2f
                            )

                            // Thresholds (Significance at -log10 p-value = 1.3 / p=0.05, and Fold Change +/- 1.5)
                            // We mock these positions for visualization
                            val fcZeroX = 40f + (canvasWidth - 60f) / 2
                            drawLine(
                                color = Color.LightGray,
                                start = Offset(fcZeroX, 20f),
                                end = Offset(fcZeroX, canvasHeight - 40f),
                                strokeWidth = 1f
                            )

                            // Draw Volcano Points
                            volcanoPoints.forEach { point ->
                                // Map points log2FC to X axis (range -4 to +4)
                                val xRange = canvasWidth - 80f
                                val x = 40f + xRange / 2 + (point.log2FC / 4f) * (xRange / 2)

                                // Map -log10 pValue to Y axis
                                val negLog10P = -kotlin.math.log10(point.pValue)
                                val yRange = canvasHeight - 80f
                                val y = (canvasHeight - 40f) - (negLog10P / 5f) * yRange

                                val pointColor = when (point.status) {
                                    "Up" -> Color(0xFFDC2626) // Red
                                    "Down" -> Color(0xFF16A34A) // Green
                                    else -> Color(0xFF94A3B8) // Gray
                                }

                                drawCircle(
                                    color = pointColor,
                                    radius = 8f,
                                    center = Offset(x, y)
                                )

                                // Text label for gene
                                drawContext.canvas.nativeCanvas.drawText(
                                    point.gene,
                                    x + 10f,
                                    y + 5f,
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.DKGRAY
                                        textSize = 24f
                                    }
                                )
                            }
                        }
                    }

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        LegendItem(color = Color(0xFFDC2626), label = "Up-Regulated")
                        LegendItem(color = Color(0xFF16A34A), label = "Down-Regulated")
                        LegendItem(color = Color(0xFF94A3B8), label = "Neutral")
                    }
                }
            }
        }

        // Heatmap Expression Visualizer
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "TRANSCRIPT EXPRESSION HEATMAP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                    // Heatmap Grid
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Samples Header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.width(60.dp))
                            listOf("Ctrl1", "Ctrl2", "Trt1", "Trt2").forEach { sample ->
                                Text(
                                    text = sample,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    color = Color.Gray
                                )
                            }
                        }

                        // Gene rows
                        heatmapGenes.forEachIndexed { geneIdx, gene ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = gene,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(60.dp),
                                    fontFamily = FontFamily.Monospace
                                )

                                val rowData = heatmapData[geneIdx]
                                rowData.forEach { value ->
                                    // Scale heat colors (Red for high, blue for low, white for mid)
                                    val cellColor = if (value > 3.0f) {
                                        Color(0xFFDC2626).copy(alpha = (value / 5.1f).coerceIn(0.2f, 1f))
                                    } else {
                                        Color(0xFF2563EB).copy(alpha = ((5.1f - value) / 5.1f).coerceIn(0.2f, 1f))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(32.dp)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(cellColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format("%.1f", value),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Save Assay Button
        item {
            Button(
                onClick = {
                    val resultJson = JSONObject().apply {
                        put("secondaryStructurePairings", pairings.size)
                        put("regulationRatio", "UpDownScaled")
                    }
                    viewModel.saveAnalysisRecord("RNA", "transcript_assay_01", cleanRna, resultJson)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_rna_assay_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save RNA Analysis Run", fontSize = 14.sp)
            }
        }
    }
}

data class VolcanoPoint(val log2FC: Float, val pValue: Float, val gene: String, val status: String)

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
    }
}
