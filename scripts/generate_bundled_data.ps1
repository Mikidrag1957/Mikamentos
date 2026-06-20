param(
    [string]$OutputFile = "..\app\src\main\res\raw\bundled_medications.json"
)

$medications = @(
    @{ names = @("paracetamol", "acetaminophen", "panadol", "tylenol", "apiretal", "gelocatil", "efferalgan", "termalgin") },
    @{ names = @("ibuprofeno", "ibuprofen", "advil", "motrin", "nurofen", "dalsy", "neobrufen") },
    @{ names = @("aspirina", "aspirin", "adiro", "disgren", "tromalyt", "anopyrin") },
    @{ names = @("omeprazol", "omeprazole", "prilosec", "losec", "helicid", "parlet") },
    @{ names = @("amoxicilina", "amoxicillin", "clamoxyl", "amoxil") },
    @{ names = @("metformina", "metformin", "glucophage", "dianben") },
    @{ names = @("atorvastatina", "atorvastatin", "lipitor", "prevencor", "atoris") },
    @{ names = @("amlodipino", "amlodipine", "norvasc", "agen") },
    @{ names = @("enalapril", "enalapril", "renitec") },
    @{ names = @("losartan", "losartan", "cozaar") },
    @{ names = @("simvastatina", "simvastatin", "zocor") },
    @{ names = @("salbutamol", "albuterol", "ventolin") },
    @{ names = @("prednisona", "prednisone", "dacortin") },
    @{ names = @("warfarina", "warfarin", "aldocumar") },
    @{ names = @("heparina", "heparin") },
    @{ names = @("insulina", "insulin", "humalog", "lantus", "novorapid") },
    @{ names = @("diazepam", "valium") },
    @{ names = @("lorazepam", "orfiadal") },
    @{ names = @("alprazolam", "xanax", "trankimazin") },
    @{ names = @("zolpidem", "stilnox") },
    @{ names = @("tramadol", "adolonta") },
    @{ names = @("morfina", "morphine") },
    @{ names = @("codeina", "codeine", "codeisan") },
    @{ names = @("furosemida", "furosemide", "lasix") },
    @{ names = @("ramipril", "acovil") },
    @{ names = @("bisoprolol", "bisoprolol") },
    @{ names = @("metoprolol", "metoprolol", "lopressor", "vasocardin") },
    @{ names = @("atenolol", "atenolol", "tenormin") },
    @{ names = @("cetirizina", "cetirizine", "zyrtec") },
    @{ names = @("loratadina", "loratadine", "claritin") },
    @{ names = @("diclofenaco", "diclofenac", "voltaren") },
    @{ names = @("naproxeno", "naproxen", "naproxyn") },
    @{ names = @("citalopram", "citalopram", "celexa", "citalec") },
    @{ names = @("sertralina", "sertraline", "zoloft") },
    @{ names = @("fluoxetina", "fluoxetine", "prozac") },
    @{ names = @("levotiroxina", "levothyroxine", "eutirox") },
    @{ names = @("tamsulosina", "tamsulosin", "omnic") },
    @{ names = @("finasterida", "finasteride", "proscar") },
    @{ names = @("sildenafilo", "sildenafil", "viagra") },
    @{ names = @("tadalafilo", "tadalafil", "cialis") },
    @{ names = @("metamizol", "dipyrone", "nolotil") },
    @{ names = @("pantoprazol", "pantoprazole", "pantoc") },
    @{ names = @("escitalopram", "escitalopram", "lexapro") },
    @{ names = @("clopidogrel", "clopidogrel", "plavix") },
    @{ names = @("hidroclorotiazida", "hydrochlorothiazide") },
    @{ names = @("candesartan", "candesartan", "atacand") },
    @{ names = @("pregabalina", "pregabalin", "lyrica") },
    @{ names = @("gabapentina", "gabapentin", "neurontin") },
    @{ names = @("quetiapina", "quetiapine", "seroquel", "derin", "kuentiax") },
    @{ names = @("cortisona", "cortisone") },
    @{ names = @("trimetazidina", "trimetazidine", "vastarel") },
    @{ names = @("memantina", "memantine", "memolan") },
    @{ names = @("bromazepam", "lexaurin") },
    @{ names = @("perindopril", "prestarium") },
    @{ names = @("gliclazida", "gliclazide", "diamicron") },
    @{ names = @("glibenclamida", "glyburide", "daonil") },
    @{ names = @("digoxina", "digoxin") },
    @{ names = @("metadona", "methadone") },
    @{ names = @("azitromicina", "azithromycin", "zitromax") },
    @{ names = @("ranitidina", "ranitidine", "zantac") },
    @{ names = @("captopril", "captopril", "tensiomin") },
    @{ names = @("valsartan", "valsartan", "diovan") },
    @{ names = @("acenocumarol", "acenocoumarol", "sintrom") },
    @{ names = @("alopurinol", "allopurinol", "zyloric") },
    @{ names = @("domperidona", "domperidone", "motilium") },
    @{ names = @("metoclopramida", "metoclopramide", "primperan") },
    @{ names = @("betahistina", "betahistine", "serc") },
    @{ names = @("venlafaxina", "venlafaxine", "dobupal") },
    @{ names = @("duloxetina", "duloxetine", "cymbalta") },
    @{ names = @("mirtazapina", "mirtazapine", "remeron") },
    @{ names = @("topiramato", "topiramate", "topamax") },
    @{ names = @("carbamazepina", "carbamazepine", "tegretol") },
    @{ names = @("levodopa", "levodopa", "sinemet") },
    @{ names = @("donepezilo", "donepezil", "aricept") },
    @{ names = @("rivastigmina", "rivastigmine", "exelon") },
    @{ names = @("galantamina", "galantamine", "razadyne") },
    @{ names = @("deflazacort", "deflazacort", "deflazacort") },
    @{ names = @("ezetimiba", "ezetimibe", "ezetrol") },
    @{ names = @("fenofibrato", "fenofibrate", "lipidil") },
    @{ names = @("espironolactona", "spironolactone", "aldactone") },
    @{ names = @("claritromicina", "clarithromycin", "klaricid") },
    @{ names = @("levofloxacino", "levofloxacin", "tavanic") },
    @{ names = @("fluconazol", "fluconazole", "diflucan") },
    @{ names = @("aciclovir", "acyclovir", "zovirax") },
    @{ names = @("montelukast", "montelukast", "singulair") },
    @{ names = @("budesonida", "budesonide", "pulmicort") },
    @{ names = @("nifedipino", "nifedipine", "adalat") },
    @{ names = @("colchicina", "colchicine", "colchicina") },
    @{ names = @("sumatriptan", "sumatriptan", "imigran") },
    @{ names = @("pramipexol", "pramipexole", "sifrol") },
    @{ names = @("ropinirol", "ropinirole", "requip") },
    @{ names = @("carvedilol", "carvedilol", "carvedilol") },
    @{ names = @("doxazosina", "doxazosin", "carduran") },
    @{ names = @("celecoxib", "celecoxib", "celebrex") },
    @{ names = @("betametasona", "betamethasone", "celestoderm") },
    @{ names = @("baclodeno", "baclofen", "lioresal") },
    @{ names = @("minoxidil", "minoxidil", "minoxidil") },
    @{ names = @("lansoprazol", "lansoprazole", "prevas") },
    @{ names = @("ebastina", "ebastine", "ebastel") }
)

function Strip-Html {
    param([string]$s)
    $result = $s -replace '<[^>]*>', ''
    $result = $result -replace '&nbsp;|&#xa0;|&amp;|&lt;|&gt;|&quot;|#[0-9]+;', ' '
    return ($result -replace '\s+', ' ').Trim()
}

$results = @()
$total = $medications.Count
$i = 0

Add-Type -AssemblyName System.Web

foreach ($med in $medications) {
    $i++
    $primary = $med.names[0]
    Write-Host "[$i/$total] $primary..."

    try {
        $encoded = [System.Web.HttpUtility]::UrlEncode($primary)
        $url = "https://cima.aemps.es/cima/rest/medicamentos?nombre=$encoded"
        $searchResult = Invoke-RestMethod -Uri $url -TimeoutSec 10

        $medResult = $searchResult.resultados | Select-Object -First 1
        if (-not $medResult) {
            Write-Host "  -> No results in CIMA"
            continue
        }

        $nregistro = $medResult.nregistro
        $nombre = $medResult.nombre
        $pa = if ($medResult.vtm) { $medResult.vtm.nombre } else { "" }
        $dos = if ($medResult.dosis) { "$($medResult.dosis)" } else { "" }

        $paraQueSeUsa = ""
        $comoSeUsa = ""

        # Try JSON endpoint (docSegmentado/contenido/2)
        $prospectoUrl = "https://cima.aemps.es/cima/rest/docSegmentado/contenido/2?nregistro=$nregistro"
        try {
            $raw = Invoke-RestMethod -Uri $prospectoUrl -TimeoutSec 15
            if ($raw -is [array] -and $raw.Count -ge 2) {
                $hasContent = $false
                foreach ($section in $raw) {
                    $seccion = "$($section.seccion)".Trim()
                    $contenido = Strip-Html -s "$($section.contenido)"
                    if ($seccion -eq "1" -and $contenido.Length -gt 0) { $paraQueSeUsa = $contenido; $hasContent = $true }
                    if ($seccion -eq "3" -and $contenido.Length -gt 0) { $comoSeUsa = $contenido; $hasContent = $true }
                }
                if ($hasContent) { Write-Host "  -> JSON sections OK" }
            }
        } catch { }

        # Fallback: Accept text/plain + parse
        if ($paraQueSeUsa.Length -eq 0 -and $comoSeUsa.Length -eq 0) {
            try {
                $headers = @{ "Accept" = "text/plain" }
                $respText = Invoke-RestMethod -Uri $prospectoUrl -Headers $headers -TimeoutSec 15
                if ($respText -is [string] -and $respText.Length -gt 200) {
                    $clean = Strip-Html -s $respText

                    # Find TOC: "1. ... 2. ... 3. ... 4. ... 5. ... 6. ..."
                    # Extract section titles from TOC
                    $titles = @{}
                    for ($n = 1; $n -le 6; $n++) {
                        $pat = "$n. "
                        $idx = $clean.IndexOf($pat)
                        if ($idx -ge 0) {
                            $s = $idx + $pat.Length
                            $nextNum = $n + 1
                            $nextPat = if ($nextNum -le 6) { "$nextNum. " } else { $null }
                            $e = if ($nextPat) { $clean.IndexOf($nextPat, $s) } else { -1 }
                            if ($e -lt 0) { $e = $s + 200 }
                            $titles[$n] = $clean.Substring($s, $e - $s).Trim()
                        }
                    }

                    # Find content after TOC (after "6. ...")
                    $contentStart = 0
                    $pat6 = "6. "
                    $idx6 = $clean.IndexOf($pat6)
                    if ($idx6 -ge 0) {
                        $contentStart = $idx6 + $pat6.Length
                        # Find the next important text after "6. ..."
                        # Skip past the end of the TOC line (after the full "6. Contenido del envase e información adicional")
                    }

                    # Search for unnumbered section titles after the TOC
                    if ($titles.ContainsKey(1) -and $contentStart -gt 0) {
                        $title1 = $titles[1]
                        $idx1 = $clean.IndexOf($title1, $contentStart)
                        if ($idx1 -ge 0) {
                            $after1 = $idx1 + $title1.Length
                            # Find next section's title
                            $nextStart = $clean.Length
                            if ($titles.ContainsKey(2)) {
                                $idx2 = $clean.IndexOf($titles[2], $after1)
                                if ($idx2 -ge 0 -and $idx2 -lt $nextStart) { $nextStart = $idx2 }
                            }
                            $paraQueSeUsa = $clean.Substring($after1, $nextStart - $after1).Trim()
                            $paraQueSeUsa = ($paraQueSeUsa -replace '\s+', ' ').Trim()
                        }
                    }

                    if ($titles.ContainsKey(3) -and $contentStart -gt 0) {
                        $title3 = $titles[3]
                        # Find section 3 title after where we found section 2 content ended (or contentStart)
                        $searchFrom = $contentStart
                        if ($titles.ContainsKey(2)) {
                            $idx2title = $clean.IndexOf($titles[2], $contentStart)
                            if ($idx2title -ge 0) { $searchFrom = $idx2title + $titles[2].Length }
                        }
                        $idx3 = $clean.IndexOf($title3, $searchFrom)
                        if ($idx3 -ge 0) {
                            $after3 = $idx3 + $title3.Length
                            $nextStart = $clean.Length
                            if ($titles.ContainsKey(4)) {
                                $idx4 = $clean.IndexOf($titles[4], $after3)
                                if ($idx4 -ge 0 -and $idx4 -lt $nextStart) { $nextStart = $idx4 }
                            }
                            $comoSeUsa = $clean.Substring($after3, $nextStart - $after3).Trim()
                            $comoSeUsa = ($comoSeUsa -replace '\s+', ' ').Trim()
                        }
                    }

                    if ($paraQueSeUsa.Length -gt 0 -or $comoSeUsa.Length -gt 0) {
                        Write-Host "  -> Plain text sections: para=$($paraQueSeUsa.Length) como=$($comoSeUsa.Length)"
                    }
                }
            } catch { }
        }

        if ($paraQueSeUsa.Length -gt 1000) { $paraQueSeUsa = $paraQueSeUsa.Substring(0, 1000) }
        if ($comoSeUsa.Length -gt 1000) { $comoSeUsa = $comoSeUsa.Substring(0, 1000) }

        $results += @{
            names = $med.names
            principio_activo = $pa
            dosis = $dos
            para_que_se_usa = $paraQueSeUsa
            como_se_usa = $comoSeUsa
        }
        Write-Host "  -> OK: $nombre ($nregistro)"
    }
    catch {
        Write-Host "  -> ERROR: $($_.Exception.Message)"
    }
}

$jsonObj = @{
    version = 1
    generated = (Get-Date -Format "yyyy-MM-dd")
    count = $results.Count
    medications = $results
}

$json = $jsonObj | ConvertTo-Json -Depth 10

$resolvedOutput = [System.IO.Path]::GetFullPath([System.IO.Path]::Combine($PSScriptRoot, $OutputFile))
$json | Set-Content -Path $resolvedOutput -Encoding UTF8
Write-Host "`nDone! Generated $($results.Count) medications -> $resolvedOutput"
