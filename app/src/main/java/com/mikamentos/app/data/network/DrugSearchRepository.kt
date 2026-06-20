package com.mikamentos.app.data.network

import com.mikamentos.app.data.repository.MedicationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrugSearchRepository @Inject constructor(
    private val fdaApi: FdaApiService,
    private val cimaApi: CimaApiService,
    private val translationApi: TranslationService,
    private val repository: MedicationRepository,
    private val bundledData: BundledDrugData
) {

    private val cache = mutableMapOf<String, String>()
    private val translationCache = mutableMapOf<String, String>()

    private var lastTranslationTime = 0L
    private var translateFailing = false
    private var translateFailTimestamp = 0L

    fun clearTranslationCache() {
        translationCache.clear()
        repository.clearTranslationCache()
    }

    companion object {
        private const val RATE_LIMIT_MS = 1000L
        private const val RETRY_COOLDOWN_MS = 120_000L

        private val medicationNameMap = mapOf(
            "paracetamol" to "acetaminophen",
            "ibuprofeno" to "ibuprofen",
            "aspirina" to "aspirin",
            "omeprazol" to "omeprazole",
            "amoxicilina" to "amoxicillin",
            "metformina" to "metformin",
            "atorvastatina" to "atorvastatin",
            "amlodipino" to "amlodipine",
            "naproxeno" to "naproxen",
            "diclofenaco" to "diclofenac",
            "ciprofloxacino" to "ciprofloxacin",
            "azitromicina" to "azithromycin",
            "digitoxina" to "digitoxin",
            "digoxina" to "digoxin",
            "warfarina" to "warfarin",
            "heparina" to "heparin",
            "insulina" to "insulin",
            "prednisona" to "prednisone",
            "cortisona" to "cortisone",
            "loratadina" to "loratadine",
            "cetirizina" to "cetirizine",
            "tramadol" to "tramadol",
            "codeina" to "codeine",
            "morphina" to "morphine",
            "metadona" to "methadone",
            "glibenclamida" to "glyburide",
            "gliclazida" to "gliclazide",
            "levothroxina" to "levothyroxine",
            "tiroxina" to "thyroxine",
            "tamsulosina" to "tamsulosin",
            "finasterida" to "finasteride",
            "sildenafilo" to "sildenafil",
            "ibuprofen" to "ibuprofen",
            "aspirin" to "aspirin",
            "omeprazole" to "omeprazole",
            "amoxicillin" to "amoxicillin",
            "metformin" to "metformin",
            "atorvastatin" to "atorvastatin",
            "losartan" to "losartan",
            "amlodipine" to "amlodipine",
            "naproxen" to "naproxen",
            "diclofenac" to "diclofenac",
            "ciprofloxacin" to "ciprofloxacin",
            "azithromycin" to "azithromycin",
            "metoprolol" to "metoprolol",
            "bisoprolol" to "bisoprolol",
            "atenolol" to "atenolol",
            "propranolol" to "propranolol",
            "enalapril" to "enalapril",
            "ramipril" to "ramipril",
            "captopril" to "captopril",
            "digoxin" to "digoxin",
            "warfarin" to "warfarin",
            "heparin" to "heparin",
            "insulin" to "insulin",
            "prednisone" to "prednisone",
            "cortisone" to "cortisone",
            "loratadine" to "loratadine",
            "cetirizine" to "cetirizine",
            "diazepam" to "diazepam",
            "alprazolam" to "alprazolam",
            "lorazepam" to "lorazepam",
            "zolpidem" to "zolpidem",
            "codeine" to "codeine",
            "morphine" to "morphine",
            "methadone" to "methadone",
            "glyburide" to "glyburide",
            "gliclazide" to "gliclazide",
            "levothyroxine" to "levothyroxine",
            "thyroxine" to "thyroxine",
            "tamsulosin" to "tamsulosin",
            "finasteride" to "finasteride",
            "sildenafil" to "sildenafil",
            "tadalafil" to "tadalafil",
            "viagra" to "sildenafil",
            "cialis" to "tadalafil",
            "xanax" to "alprazolam",
            "valium" to "diazepam",
            "ambien" to "zolpidem",
            "panadol" to "acetaminophen",
            "tylenol" to "acetaminophen",
            "advil" to "ibuprofen",
            "motrin" to "ibuprofen",
            "nurofen" to "ibuprofen",
            "voltaren" to "diclofenac",
            "cataflam" to "diclofenac",
            "stilnox" to "zolpidem",
            "lexaurin" to "bromazepam",
            "atuvartation" to "atorvastatin",
            "trimetazidin" to "trimetazidine",
            "memolan" to "memantine",
            "anopyrin" to "aspirin",
            "agen" to "amlodipine",
            "citalec" to "citalopram",
            "vasocardin" to "metoprolol",
            "atoris" to "atorvastatin",
            "prestarium" to "perindopril",
            "derin" to "quetiapine",
            "kuentiax" to "quetiapine",
            "tebokan" to "ginkgo biloba",
            "helicid" to "omeprazole",
            "tensiomin" to "captopril",
            "vigantol" to "cholecalciferol",
            "bromazepam" to "bromazepam",
            "trimetazidine" to "trimetazidine",
            "memantine" to "memantine",
            "citalopram" to "citalopram",
            "perindopril" to "perindopril",
            "quetiapine" to "quetiapine",
            "ginkgo biloba" to "ginkgo biloba",
            "cholecalciferol" to "cholecalciferol"
        )
    }

    suspend fun searchDrugDescription(medicationName: String, targetLanguage: String): Result<String> {
        val cleanName = medicationName.trim()

        val bundled = bundledData.search(cleanName)
        if (bundled != null) {
            val text = buildBundledDescription(bundled)
            val translated = translateIfNeeded(text, targetLanguage)
            if (translated.isSuccess) return Result.success(translated.getOrThrow())
            return Result.success(text)
        }

        val searchName = medicationNameMap[cleanName.lowercase()] ?: cleanName

        var cimaResult: String? = null
        try {
            cimaResult = searchCima(cleanName)
        } catch (_: Exception) {}
        if (cimaResult == null && searchName != cleanName.lowercase()) {
            try {
                cimaResult = searchCima(searchName)
            } catch (_: Exception) {}
        }

        try {
            val fdaResult = searchFda(searchName)
            if (fdaResult.isSuccess) {
                val fdaText = fdaResult.getOrThrow()
                val translated = translateIfNeeded(fdaText, targetLanguage)
                if (translated.isSuccess) {
                    val best = pickBetter(cimaResult, translated.getOrThrow(), targetLanguage)
                    return Result.success(best)
                }
            }
        } catch (_: Exception) {}

        if (searchName != cleanName.lowercase()) {
            try {
                val fdaResult = searchFda(searchName)
                if (fdaResult.isSuccess) {
                    val fdaText = fdaResult.getOrThrow()
                    val translated = translateIfNeeded(fdaText, targetLanguage)
                    if (translated.isSuccess) {
                        val best = pickBetter(cimaResult, translated.getOrThrow(), targetLanguage)
                        return Result.success(best)
                    }
                }
            } catch (_: Exception) {}
        }

        if (cimaResult != null) return Result.success(cimaResult)

        return Result.failure(Exception("Medicamento no encontrado: $cleanName"))
    }

    private fun buildBundledDescription(med: BundledMedication): String {
        val parts = mutableListOf<String>()
        parts.add(med.names.first().replaceFirstChar { it.uppercase() })
        if (med.principioActivo.isNotBlank()) parts.add("Principio activo: ${med.principioActivo}")
        if (med.dosis.isNotBlank()) parts.add("Dosis: ${med.dosis}")
        if (med.paraQueSeUsa.isNotBlank()) parts.add("Para qué se usa: ${med.paraQueSeUsa}")
        if (med.comoSeUsa.isNotBlank()) parts.add("Cómo se usa: ${med.comoSeUsa}")
        return parts.joinToString("\n\n")
    }

    private fun pickBetter(cimaText: String?, otherText: String, targetLanguage: String): String {
        if (cimaText == null) return otherText
        if (detectLanguage(otherText) != targetLanguage) return cimaText
        if (targetLanguage == "es") return cimaText
        return if (otherText.length > cimaText.length * 2) otherText else cimaText
    }

    private suspend fun translateIfNeeded(text: String, targetLanguage: String): Result<String> {
        val detected = detectLanguage(text)
        if (detected == targetLanguage) return Result.success(text)
        val translated = translateDescription(text, targetLanguage)
        if (translated.equals(text, ignoreCase = true)) {
            return Result.failure(Exception("No se pudo traducir al idioma seleccionado"))
        }
        return Result.success(translated)
    }

    private suspend fun searchCima(name: String): String? {
        val response = cimaApi.searchMedicamentos(nombre = name)
        val med = response.resultados?.firstOrNull() ?: return null

        val parts = mutableListOf<String>()
        med.nombre?.let { parts.add(it) }
        med.principioActivo?.let { parts.add("Principio activo: $it") }
        med.dosis?.let { parts.add("Dosis: $it") }
        med.formaFarmaceutica?.nombre?.let { parts.add("Forma: $it") }
        med.vtm?.nombre?.let { if (it != med.principioActivo) parts.add("VTM: $it") }
        med.labtitular?.let { parts.add("Laboratorio: $it") }
        med.labcomercializador?.let { if (it != med.labtitular) parts.add("Comercializador: $it") }
        med.estado?.nombre?.let { parts.add("Estado: $it") }
        val basicInfo = parts.joinToString("\n")

        val prospecto = try {
            val body = cimaApi.getProspecto(med.nregistro ?: return basicInfo.ifEmpty { null })
            body.use { it.string() }
        } catch (_: Exception) { null }

        if (prospecto != null) {
            val cleaned = stripHtml(prospecto)
            val sections = extractSections(cleaned)
            return basicInfo + "\n\n" + sections
        }

        return basicInfo.ifEmpty { null }
    }

    private fun stripHtml(html: String): String {
        return html
            .replace(Regex("<[^>]*>"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun extractSections(text: String): String {
        val wanted = mapOf(
            "QUÉ ES" to "Para qué se usa",
            "CÓMO TOMAR" to "Cómo se usa"
        )

        val blocks = text.split(Regex("\\n\\s*\\n")).map { it.trim() }.filter { it.isNotBlank() }

        val result = mutableListOf<String>()
        var currentHeader: String? = null
        var currentContent = StringBuilder()
        var matchedKey: String? = null

        for (block in blocks) {
            val upper = block.uppercase()
            if (upper.contains("CONTENIDO DEL PROSPECTO")) continue

            val key = wanted.keys.firstOrNull { upper.contains(it) }
            if (key != null) {
                if (currentHeader != null && matchedKey != null && currentContent.isNotBlank()) {
                    val body = currentContent.toString().trim()
                    if (body.isNotBlank()) result.add("${wanted[matchedKey]}: $body")
                }
                currentHeader = block
                matchedKey = key
                currentContent = StringBuilder()
            } else if (currentHeader != null) {
                if (currentContent.isNotBlank()) currentContent.append(" ")
                currentContent.append(block)
            }
        }
        if (currentHeader != null && matchedKey != null && currentContent.isNotBlank()) {
            val body = currentContent.toString().trim()
            if (body.isNotBlank()) result.add("${wanted[matchedKey]}: $body")
        }

        if (result.isNotEmpty()) return result.joinToString("\n\n")

        val numbered = mapOf(
            "1." to "Para qué se usa",
            "3." to "Cómo se usa"
        )
        var foundNum: String? = null
        var numContent = StringBuilder()
        for (numKey in listOf("1.", "2.", "3.", "4.", "5.", "6.")) {
            val idx = text.indexOf(numKey)
            if (idx < 0) continue
            val titleKey = numbered[numKey]
            if (titleKey != null) {
                if (foundNum != null && numContent.isNotBlank()) {
                    val body = numContent.toString().trim()
                    if (body.isNotBlank()) result.add("${numbered[foundNum]}: $body")
                }
                foundNum = numKey
                numContent = StringBuilder()
            } else if (foundNum != null) {
                numContent.append(text.substring(idx))
            }
        }
        if (foundNum != null && numContent.isNotBlank()) {
            val body = numContent.toString().trim()
            if (body.isNotBlank()) result.add("${numbered[foundNum]}: $body")
        }

        if (result.isNotEmpty()) return result.joinToString("\n\n")

        return text
    }

    private suspend fun searchFda(name: String): Result<String> {
        val response = fdaApi.searchDrug("openfda.brand_name:${name}")

        if (response.results.isNullOrEmpty()) {
            val genericResponse = fdaApi.searchDrug("openfda.generic_name:${name}")
            if (genericResponse.results.isNullOrEmpty()) {
                val allResponse = fdaApi.searchDrug(name)
                if (allResponse.results.isNullOrEmpty()) {
                    return Result.failure(Exception("Not found"))
                }
                return Result.success(buildDescription(allResponse.results.first()))
            }
            return Result.success(buildDescription(genericResponse.results.first()))
        }
        return Result.success(buildDescription(response.results.first()))
    }

    private fun buildDescription(result: FdaResult): String {
        val parts = mutableListOf<String>()

        result.purpose?.firstOrNull()?.let { parts.add(it) }
        result.indications_and_usage?.firstOrNull()?.let { parts.add(it) }
        result.description?.firstOrNull()?.let { parts.add(it) }

        return parts.joinToString("\n\n")
    }

    fun resetTranslateState() {
        translateFailing = false
        translateFailTimestamp = 0L
    }

    fun getTranslationFromCache(text: String, targetLang: String): String? {
        val key = "${text.hashCode()}_$targetLang"
        return translationCache[key] ?: repository.getTranslationFromCache(key)
    }

    fun isTranslationCached(text: String, targetLang: String): Boolean {
        val key = "${text.hashCode()}_$targetLang"
        return translationCache.containsKey(key) || repository.getTranslationFromCache(key) != null
    }

    suspend fun translateDescription(text: String, targetLang: String): String {
        if (text.isBlank()) return text

        if (translateFailing) {
            val elapsed = System.currentTimeMillis() - translateFailTimestamp
            if (elapsed < RETRY_COOLDOWN_MS) return text
            translateFailing = false
        }

        val cacheKey = "${text.hashCode()}_$targetLang"
        translationCache[cacheKey]?.let { return it }
        repository.getTranslationFromCache(cacheKey)?.let {
            translationCache[cacheKey] = it
            return it
        }

        val sourceLang = detectLanguage(text)
        if (sourceLang == targetLang) return text

        val now = System.currentTimeMillis()
        val sinceLast = now - lastTranslationTime
        if (sinceLast < RATE_LIMIT_MS) {
            kotlinx.coroutines.delay(RATE_LIMIT_MS - sinceLast)
        }

        val result = tryTranslationChain(text, sourceLang, targetLang)

        if (result != null) {
            translationCache[cacheKey] = result
            repository.saveTranslationToCache(cacheKey, result)
            translateFailing = false
            return result
        }

        translateFailing = true
        translateFailTimestamp = System.currentTimeMillis()
        return text
    }

    private suspend fun tryTranslationChain(text: String, sourceLang: String, targetLang: String): String? {
        val mymemoryResult = tryMyMemory(text, sourceLang, targetLang)
        if (mymemoryResult != null) return mymemoryResult

        val googleResult = tryGoogleTranslate(text, sourceLang, targetLang)
        if (googleResult != null) return googleResult

        return null
    }

    private suspend fun tryMyMemory(text: String, sourceLang: String, targetLang: String): String? {
        return try {
            val langPair = "$sourceLang|$targetLang"
            val email = repository.getMyMemoryEmail()
            val response = if (email != null) {
                translationApi.translate(text, langPair, email)
            } else {
                translationApi.translate(text, langPair)
            }
            val result = response.responseData.translatedText
            if (result.isNotBlank()
                && !result.equals(text, ignoreCase = true)
                && !result.contains("MYMEMORY", ignoreCase = true)
            ) {
                lastTranslationTime = System.currentTimeMillis()
                android.util.Log.d("DrugSearch", "MyMemory OK: $sourceLang->$targetLang")
                result
            } else {
                android.util.Log.w("DrugSearch", "MyMemory rejected result='$result' text='$text'")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("DrugSearch", "MyMemory failed: ${e.message}")
            null
        }
    }

    private suspend fun tryGoogleTranslate(text: String, sourceLang: String, targetLang: String): String? {
        return try {
            val googleResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val encoded = java.net.URLEncoder.encode(text, "UTF-8")
                val url = java.net.URL("https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sourceLang&tl=$targetLang&dt=t&q=$encoded")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                conn.connectTimeout = 7000
                conn.readTimeout = 7000
                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val response = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    parseGoogleTranslateResponse(response)
                } else {
                    android.util.Log.w("DrugSearch", "Google Translate returned HTTP $responseCode")
                    conn.disconnect()
                    null
                }
            }
            if (googleResult != null && googleResult.isNotBlank() && !googleResult.equals(text, ignoreCase = true)) {
                lastTranslationTime = System.currentTimeMillis()
                android.util.Log.d("DrugSearch", "Google OK: $sourceLang->$targetLang")
                googleResult
            } else null
        } catch (e: Exception) {
            android.util.Log.e("DrugSearch", "Google failed: ${e.message}")
            null
        }
    }

    fun detectLanguage(text: String): String {
        val lower = text.lowercase()

        data class LangDetect(val lang: String, val patterns: List<String>, val weight: Int = 1)
        val detectors = listOf(
            LangDetect("es", listOf(
                "principio activo", "laboratorio", "forma farmacéutica", "comprimidos", "tabletas",
                "tratamiento", "indicaciones", "posología", "contraindicaciones", "advertencias",
                "uso en", "pacientes", "dosis", "administración", "reacciones adversas",
                "interacciones", "sobredosificación", "propiedades farmacocinéticas"
            ), 3),
            LangDetect("es", listOf(
                "el ", "la ", "los ", "las ", "un ", "una ", "para ", "con ", "por ",
                "del ", "al ", "que ", "se ", "no ", "su ", "como ", "más ", "pero "
            ), 1),
            LangDetect("sk", listOf(
                "liek", "tablety", "užíva", "indikácie", "dávkovanie", "vedľajšie",
                "kontraindikácie", "interakcie", "predávkovanie", "farmakokinetické",
                "terapeutické", "účinná látka", "spôsob", "uchovávania"
            ), 3),
            LangDetect("sk", listOf(
                "je ", "sa ", "na ", "do ", "pre ", "z ", "k ", "od ", "alebo",
                "podľa", "ktoré", "ktorý", "ktorá", "ale", "ako", "nie "
            ), 1),
            LangDetect("fr", listOf(
                "traitement", "indication", "comprimés", "utilisation", "posologie",
                "contre-indications", "effets indésirables", "interactions",
                "substance active", "voie d'administration", "précautions"
            ), 3),
            LangDetect("fr", listOf(
                "le ", "la ", "les ", "des ", "pour ", "avec ", "dans ", "sur ",
                "une ", "est ", "sont ", "pas ", "par "
            ), 1),
            LangDetect("it", listOf(
                "trattamento", "indicazioni", "compresse", "uso", "posologia",
                "controindicazioni", "effetti indesiderati", "interazioni",
                "principio attivo", "farmacocinetiche", "somministrazione"
            ), 3),
            LangDetect("it", listOf(
                "il ", "la ", "i ", "le ", "di ", "per ", "con ", "dal ", "che ",
                "non ", "una ", "degli ", "delle "
            ), 1),
            LangDetect("ca", listOf(
                "tractament", "indicacions", "comprimits", "ús", "posologia",
                "contraindicacions", "efectes adversos", "interaccions",
                "principi actiu", "administració", "precaució"
            ), 3),
            LangDetect("ca", listOf(
                "el ", "la ", "els ", "les ", "un ", "una ", "per ", "amb ",
                "del ", "que ", "no ", "se "
            ), 1)
        )

        val scores = mutableMapOf<String, Int>()
        for (d in detectors) {
            var count = 0
            for (p in d.patterns) {
                if (lower.contains(p)) count++
            }
            scores[d.lang] = (scores[d.lang] ?: 0) + count * d.weight
        }

        val best = scores.maxByOrNull { it.value }
        return if (best != null && best.value >= 2) best.key else "en"
    }

    private fun parseGoogleTranslateResponse(response: String): String? {
        return try {
            val jsonArray = org.json.JSONArray(response)
            val sentences = jsonArray.getJSONArray(0)
            val sb = StringBuilder()
            for (i in 0 until sentences.length()) {
                val sentence = sentences.getJSONArray(i)
                sb.append(sentence.getString(0))
            }
            sb.toString()
        } catch (e: Exception) {
            null
        }
    }
}
