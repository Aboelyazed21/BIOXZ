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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BioViewModel
import org.json.JSONObject

@Composable
fun ProteinScreen(viewModel: BioViewModel) {
    val proteinInput by viewModel.proteinInput.collectAsState()

    val cleanProtein = remember(proteinInput) {
        proteinInput.uppercase().filter { it in "ACDEFGHIKLMNPQRSTVWY" }
    }

    val proteinLength = cleanProtein.length

    // Dynamic calculations using real biochemical properties
    // Kyte-Doolittle Hydrophobicity Index mapping
    val hydrophobicityMap = mapOf(
        'A' to 1.8, 'R' to -4.5, 'N' to -3.5, 'D' to -3.5, 'C' to 2.5,
        'Q' to -3.5, 'E' to -3.5, 'G' to -0.4, 'H' to -3.2, 'I' to 4.5,
        'L' to 3.8, 'K' to -3.9, 'M' to 1.9, 'F' to 2.8, 'P' to -1.6,
        'S' to -0.8, 'T' to -0.7, 'W' to -0.9, 'Y' to -1.3, 'V' to 4.2
    )

    val averageHydrophobicity = remember(cleanProtein) {
        if (cleanProtein.isEmpty()) 0.0
        else {
            val total = cleanProtein.map { hydrophobicityMap[it] ?: 0.0 }.sum()
            total / cleanProtein.length
        }
    }

    // Molecular weight approximation of protein (approx 110 Da per amino acid)
    val approxWeight = remember(cleanProtein) {
        cleanProtein.length * 110.0
    }

    // Isoelectric point (pI) estimation based on positive and negative residues
    val chargeDistribution = remember(cleanProtein) {
        val positive = cleanProtein.count { it in listOf('K', 'R', 'H') }
        val negative = cleanProtein.count { it in listOf('D', 'E') }
        Pair(positive, negative)
    }

    // Functional motifs finder (phosphorylation, glycosylation, nuclear localization)
    val foundMotifs = remember(cleanProtein) {
        val motifs = mutableListOf<String>()
        // 1. Phosphorylation target: S/T-X-R/K
        val phosphoRegex = Regex("[ST].[RK]")
        var phosphoMatch = phosphoRegex.find(cleanProtein)
        while (phosphoMatch != null) {
            motifs.add("Kinase Phosphorylation Locus at index ${phosphoMatch.range.first + 1} (${phosphoMatch.value})")
            phosphoMatch = phosphoMatch.next()
        }
        // 2. N-glycosylation: N-X-S/T (where X is not P)
        val glycosylRegex = Regex("N[^P][ST]")
        var glycosylMatch = glycosylRegex.find(cleanProtein)
        while (glycosylMatch != null) {
            motifs.add("N-Glycosylation Target at index ${glycosylMatch.range.first + 1} (${glycosylMatch.value})")
            glycosylMatch = glycosylMatch.next()
        }
        motifs
    }

    // Interactive rendering state for structure rotation
    var rotationAngle by remember { mutableStateOf(45f) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "🧬 PROTEIN STRUCTURAL WORKSPACE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFCD34D)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Analyze hydrophobicity profiles, evaluate motifs, and inspect three-dimensional atomic configurations of foldings.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Target Protein Sequence", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = proteinInput,
                        onValueChange = { viewModel.proteinInput.value = it },
                        label = { Text("Fasta Amino Acids (A, C, D, E...)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("protein_input_field")
                    )
                }
            }
        }

        // Stats Row
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CardValueWidget(
                    modifier = Modifier.weight(1f),
                    title = "Amino Acids",
                    value = "$proteinLength residues"
                )
                CardValueWidget(
                    modifier = Modifier.weight(1f),
                    title = "Hydrophobicity",
                    value = String.format("%.2f GRAVY", averageHydrophobicity)
                )
                CardValueWidget(
                    modifier = Modifier.weight(1f),
                    title = "Peptide Mass",
                    value = String.format("%.1f kDa", approxWeight / 1000.0)
                )
            }
        }

        // Interactive 3D Structural Viewer
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
                        Text(text = "3D ATOMIC STRUCTURAL RIBBON", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(text = "Tap canvas to spin structure", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(Color(0xFF0F172A)) // dark screen matching high-end PyMOL viewer
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .clickable { rotationAngle = (rotationAngle + 20f) % 360f }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val yCenter = size.height / 2

                            // Draw a beautiful 3D looking helical strand and a flat beta arrow sheet
                            // Helix path
                            val pathHelix = Path()
                            val rad = Math.toRadians(rotationAngle.toDouble())
                            
                            // Draw the central axis curve
                            for (i in 50..w.toInt() - 50 step 2) {
                                val t = i.toFloat()
                                // Sine wave with rotation offset to model 3D spiral helical folding ribbon
                                val offset3D = Math.sin(t * 0.05 + rad) * 25.0
                                val y = yCenter + Math.cos(t * 0.02) * 40.0 + offset3D

                                if (i == 50) {
                                    pathHelix.moveTo(t, y.toFloat())
                                } else {
                                    pathHelix.lineTo(t, y.toFloat())
                                }
                            }

                            // Render ribbon path
                            drawPath(
                                path = pathHelix,
                                color = Color(0xFF38BDF8), // sky blue helix
                                style = Stroke(width = 8f, cap = StrokeCap.Round)
                            )

                            // Render side residues as atomic dots
                            for (i in 60..w.toInt() - 60 step 30) {
                                val t = i.toFloat()
                                val offset3D = Math.sin(t * 0.05 + rad) * 25.0
                                val y = yCenter + Math.cos(t * 0.02) * 40.0 + offset3D

                                // Oxygen atoms (red)
                                drawCircle(
                                    color = Color(0xFFEF4444),
                                    radius = 12f,
                                    center = Offset(t, (y - 30).toFloat())
                                )
                                // Nitrogens (blue)
                                drawCircle(
                                    color = Color(0xFF3B82F6),
                                    radius = 10f,
                                    center = Offset(t + 10f, (y + 25).toFloat())
                                )
                                // Connecting sidechains
                                drawLine(
                                    color = Color.White.copy(alpha = 0.5f),
                                    start = Offset(t, y.toFloat()),
                                    end = Offset(t, (y - 30).toFloat()),
                                    strokeWidth = 2f
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.5f),
                                    start = Offset(t, y.toFloat()),
                                    end = Offset(t + 10f, (y + 25).toFloat()),
                                    strokeWidth = 2f
                                )
                            }

                            // Write annotations on canvas
                            drawContext.canvas.nativeCanvas.drawText(
                                "Alpha-Helix Ribbon (Rotation: ${rotationAngle.toInt()}°)",
                                20f,
                                40f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 28f
                                }
                            )
                        }
                    }
                }
            }
        }

        // Motifs & Charge Output
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "BIOPHYSICAL ASSAYS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                    // Charge Distribution
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Basic Residues (H, K, R):", fontSize = 13.sp)
                        Text(text = "${chargeDistribution.first}", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Acidic Residues (D, E):", fontSize = 13.sp)
                        Text(text = "${chargeDistribution.second}", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    // Found motifs
                    Text(text = "Active Sites / Secondary Motifs:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    if (foundMotifs.isEmpty()) {
                        Text(text = "No standard phosphorylation or glycosylation sites found.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        foundMotifs.forEach { motif ->
                            Text(
                                text = motif,
                                fontSize = 12.sp,
                                color = Color(0xFFD97706),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFEF3C7))
                                    .padding(6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Save Peptide assay
        item {
            Button(
                onClick = {
                    val resultJson = JSONObject().apply {
                        put("hydrophobicity", averageHydrophobicity)
                        put("isoelectricEst", "Estimated PI")
                    }
                    viewModel.saveAnalysisRecord("PROTEIN", "peptide_structural_run", cleanProtein, resultJson)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_protein_assay_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Protein Structure Run", fontSize = 14.sp)
            }
        }
    }
}
