package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class BioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = BioRepository(db)

    // --- State Observables (from Room) ---
    val projects: StateFlow<List<BioProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<AnalysisRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val papers: StateFlow<List<ResearchPaper>> = repository.allPapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NotebookEntry>> = repository.allEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.getMessagesForSession("Default Session")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Selected / Active State ---
    private val _selectedProject = MutableStateFlow<BioProject?>(null)
    val selectedProject = _selectedProject.asStateFlow()

    // --- DNA Sequence Workspace State ---
    val dnaInput = MutableStateFlow("ATGCGATCGATCGATCGATCGATCGATCGATCGATC")
    val dnaSequenceName = MutableStateFlow("TP53 Segment Alpha")

    // --- RNA Sequence Workspace State ---
    val rnaInput = MutableStateFlow("AUGCGAUCGAUCGAUCGAUCGAUCGAUCGAUCGAUC")

    // --- Protein Workspace State ---
    val proteinInput = MutableStateFlow("MALWMRLLPLLALLALWGPDPAAAFVNQHLCGSHLVEALYLVCGERGFFYTPKTR")

    // --- Database Search State ---
    val dbSearchQuery = MutableStateFlow("CRISPR Cas9 vectors")
    val isSearchingDb = MutableStateFlow(false)
    val pubMedResults = MutableStateFlow<List<ResearchPaper>>(emptyList())
    val uniProtResults = MutableStateFlow<List<UniProtEntry>>(emptyList())

    // --- AI Assistant Chat State ---
    val isAiLoading = MutableStateFlow(false)
    val inputMessage = MutableStateFlow("")

    // --- Paper Analysis Selection ---
    private val _selectedPaper = MutableStateFlow<ResearchPaper?>(null)
    val selectedPaper = _selectedPaper.asStateFlow()

    init {
        // Initialize with default template data if Room database is empty!
        viewModelScope.launch(Dispatchers.IO) {
            projects.take(1).collect { list ->
                if (list.isEmpty()) {
                    seedDefaultData()
                } else {
                    _selectedProject.value = list.first()
                }
            }
        }
    }

    private suspend fun seedDefaultData() {
        val defaultProjId = repository.insertProject(
            BioProject(
                name = "Human Epigenome Regulation (SaaS-01)",
                description = "Analyzing chromatin configurations and CRISPR vectors across cancerous epigenetic cell targets."
            )
        )
        
        // Select this project
        _selectedProject.value = BioProject(
            id = defaultProjId.toInt(),
            name = "Human Epigenome Regulation (SaaS-01)",
            description = "Analyzing chromatin configurations and CRISPR vectors across cancerous epigenetic cell targets."
        )

        // Seed some sample DNA run records
        val dnaResults = JSONObject().apply {
            put("length", 36)
            put("gcContent", 50.0)
            put("molecularWeight", 11142.8)
            put("complement", "TACGCTAGCTAGCTAGCTAGCTAGCTAGCTAGCTAG")
            put("protein", "MRSISISISISISISI")
        }

        repository.insertRecord(
            AnalysisRecord(
                projectId = defaultProjId.toInt(),
                type = "DNA",
                sequenceName = "TP53 Segment Alpha",
                sequence = "ATGCGATCGATCGATCGATCGATCGATCGATCGATC",
                resultsJson = dnaResults.toString()
            )
        )

        // Seed default research papers
        repository.insertPaper(
            ResearchPaper(
                title = "Therapeutic Targeting of Epigenetic Regulators in Human Pathways",
                authors = "Dr. Sarah Jenkins, Dr. Alan Turing",
                abstractText = "This seminal paper describes the computational screening of CRISPR-Cas9 base-editing guides against DNA methyltransferase loci. We analyze the targeted structural changes in primary cells, reporting highly reproducible methylation alignment rates across multiple lanes.",
                fullText = "1. OVERVIEW AND STRUCTURAL PRINCIPLES\nWe engineered modern sgRNAs targeting the promoter regions of transcription factors...\n\n2. CRISPR VECTOR DESIGN\nOligonucleotides were designed with 20nt target sequences and synthesized commercially. The PAM site selected was standard NGG.\n\n3. SEQUENCING DETAILS\nFastQ files were mapped against hg38 genome using Bowtie2. Standard differential expression analyses show down-regulation of oncogenes.\n\n4. CONCLUSIONS\nCRISPR editing of these loci yields therapeutic potential with minimal off-target edits.",
                pmid = "34509121",
                journal = "Nature Biotechnology",
                pubYear = "2024",
                notes = "Primary guide design guidelines are documented in Section 2."
            )
        )

        // Seed default notebook entry
        repository.insertEntry(
            NotebookEntry(
                title = "sgRNA CRISPR Design Specifications",
                content = "### Guide RNA Design Notes\n\n1. Target locus: DNMT1 promoter region\n2. Selected PAM: NGG (SpCas9)\n3. Sequence: `5'- CACCGGGCGGTGACGGAGCCGGAC -3'`\n4. Efficiency score prediction: 0.89 (via DeepCRISPR model)\n5. Off-target risk: minimal (0.012 mismatch coefficient across hg38)\n\nThese guides will be transfected into cell line MCF-7 next Tuesday. Check GC content (currently 68%) to ensure optimal hybridization kinetics."
            )
        )

        // Seed default assistant message
        repository.insertMessage(
            ChatMessage(
                role = "model",
                content = "Welcome to the Bioinformatics AI Assistant. I am fully integrated with local research paper indexes and scientific databases (NCBI, UniProt, PubMed). How can I assist you with your molecular biology, sequence alignments, or CRISPR designs today?"
            )
        )
    }

    // --- Action Methods ---

    fun selectProject(project: BioProject) {
        _selectedProject.value = project
    }

    fun selectPaper(paper: ResearchPaper) {
        _selectedPaper.value = paper
    }

    fun createNewProject(name: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = repository.insertProject(BioProject(name = name, description = description))
            val list = projects.value
            _selectedProject.value = list.find { it.id == newId.toInt() } ?: BioProject(id = newId.toInt(), name = name, description = description)
        }
    }

    fun deleteProject(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProject(id)
            if (_selectedProject.value?.id == id) {
                _selectedProject.value = projects.value.firstOrNull { it.id != id }
            }
        }
    }

    fun addNotebookEntry(title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertEntry(NotebookEntry(title = title, content = content))
        }
    }

    fun updateNotebookEntry(id: Int, title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateEntry(NotebookEntry(id = id, title = title, content = content))
        }
    }

    fun deleteNotebookEntry(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEntry(id)
        }
    }

    fun saveAnalysisRecord(type: String, name: String, sequence: String, results: JSONObject) {
        val projId = _selectedProject.value?.id ?: 1
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRecord(
                AnalysisRecord(
                    projectId = projId,
                    type = type,
                    sequenceName = name,
                    sequence = sequence,
                    resultsJson = results.toString()
                )
            )
        }
    }

    fun searchDatabases() {
        val query = dbSearchQuery.value
        if (query.trim().isEmpty()) return
        
        isSearchingDb.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Parallel searches
                val pmList = repository.searchPubMed(query)
                val upList = repository.searchUniProt(query)
                pubMedResults.value = pmList
                uniProtResults.value = upList
            } catch (e: Exception) {
                // Graceful fallback lists if any issue
            } finally {
                isSearchingDb.value = false
            }
        }
    }

    fun sendMessage() {
        val msg = inputMessage.value
        if (msg.trim().isEmpty()) return

        inputMessage.value = ""
        viewModelScope.launch(Dispatchers.IO) {
            // Insert user message
            repository.insertMessage(ChatMessage(role = "user", content = msg))
            
            isAiLoading.value = true
            
            // Build dynamic system prompt context including papers, projects, sequences if they exist
            val currentDna = dnaInput.value
            val contextSnippet = "Current analysis context: Project in workspace: '${_selectedProject.value?.name ?: "None"}'. DNA sequence currently working on: '$currentDna'."
            
            val response = NetworkModule.generateGeminiResponse(
                prompt = msg,
                systemInstruction = "You are a highly experienced Bioinformatics and CRISPR Software Systems Architect. Provide precise molecular details. Include code snippets in Python or R if helpful. Context: $contextSnippet"
            )
            
            // Insert AI response
            repository.insertMessage(ChatMessage(role = "model", content = response))
            isAiLoading.value = false
        }
    }

    fun sendTemplateQuery(template: String) {
        inputMessage.value = template
        sendMessage()
    }

    fun importPaperToWorkspace(paper: ResearchPaper) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPaper(paper)
        }
    }

    fun deletePaperFromWorkspace(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePaper(id)
            if (_selectedPaper.value?.id == id) {
                _selectedPaper.value = null
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearSession("Default Session")
            // Add a welcome message back
            repository.insertMessage(
                ChatMessage(
                    role = "model",
                    content = "Chat workspace cleared. Ask me any sequence alignment, genomic structure, or CRISPR gRNA questions!"
                )
            )
        }
    }
}
