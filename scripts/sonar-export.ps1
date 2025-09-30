param(
  [Parameter(Mandatory=$true)] [string]$OrgKey,
  [Parameter(Mandatory=$true)] [string]$ProjectKey,
  [string]$BaseUrl = "https://sonarcloud.io"
)

$ErrorActionPreference = "Stop"

# --- Token ---
$token = $env:SONAR_TOKEN
if ([string]::IsNullOrWhiteSpace($token)) {
  throw "Falta SONAR_TOKEN. Define:  `$env:SONAR_TOKEN = 'TU_TOKEN'"
}

# --- Limpieza de residuos previos ---
Get-ChildItem -Path . -Recurse -File -Include `
  "sonar_*.json","sonar_report.html","sonar_report.pdf","java_pid*.hprof","*.hprof" `
  -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue

# --- Auth header (Basic token:) ---
$basic = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("$($token):"))
$hdrs  = @{ Authorization = "Basic $basic"; Accept = "application/json" }

# --- Métricas a consultar ---
$metricKeys = @(
  'ncloc','files','classes','functions','comment_lines','comment_lines_density',
  'bugs','vulnerabilities','code_smells',
  'security_hotspots','security_hotspots_reviewed','security_review_rating',
  'reliability_rating','security_rating','sqale_rating',
  'duplicated_lines_density','duplicated_blocks',
  'coverage','lines_to_cover','uncovered_lines',
  'tests','test_failures','test_errors','test_success_density'
) -join ','

# --- Llamadas a SonarCloud ---
$measuresUrl = "$BaseUrl/api/measures/component?organization=$OrgKey&component=$ProjectKey&metricKeys=$metricKeys"
$issuesUrl   = "$BaseUrl/api/issues/search?organization=$OrgKey&componentKeys=$ProjectKey&ps=500&p=1"
$qgUrl       = "$BaseUrl/api/qualitygates/project_status?organization=$OrgKey&projectKey=$ProjectKey"

$measures = Invoke-RestMethod -Headers $hdrs -Method GET -Uri $measuresUrl
$issues   = Invoke-RestMethod -Headers $hdrs -Method GET -Uri $issuesUrl
$qg       = Invoke-RestMethod -Headers $hdrs -Method GET -Uri $qgUrl
$qgState  = $qg.projectStatus.status  # OK/WARN/ERROR

# --- Mapear métricas ---
$vals = @{}
$measures.component.measures | ForEach-Object { $vals[$_.metric] = $_.value }

# --- Issues por tipo y severidad ---
$byType = @{ BUG=0; VULNERABILITY=0; CODE_SMELL=0 }
$bySeverity = @{ BLOCKER=0; CRITICAL=0; MAJOR=0; MINOR=0; INFO=0 }
foreach ($i in $issues.issues) {
  if ($byType.ContainsKey($i.type))       { $byType[$i.type]++ }
  if ($bySeverity.ContainsKey($i.severity)){ $bySeverity[$i.severity]++ }
}

# --- Normalización de ratings ---
function ToLetter([string]$raw) {
  if ([string]::IsNullOrWhiteSpace($raw)) { return $null }
  $s = $raw.Trim()
  if ($s -match '^[A-Ea-e]$') { return $s.ToUpper() }  # ya es letra
  $s = $s -replace ',', '.'
  $m = [regex]::Match($s, '([0-9]+(\.[0-9]+)?)')
  if ($m.Success) {
    $num = [double]::Parse($m.Value, [Globalization.CultureInfo]::InvariantCulture)
    $n = [int][math]::Round($num)
    switch ($n) { 1 {'A'} 2 {'B'} 3 {'C'} 4 {'D'} default {'E'} }
  } else { $null }
}

function GetRatingLetter($metricKey, $vals, $qg) {
  # 1) Métricas directas
  $fromMeasures = ToLetter $vals[$metricKey]
  if ($fromMeasures) { return $fromMeasures }

  # 2) Condiciones del Quality Gate (actual)
  $cond = $qg.projectStatus.conditions | Where-Object { $_.metricKey -eq $metricKey } | Select-Object -First 1
  if ($cond) {
    $fromQG = ToLetter $cond.actual
    if ($fromQG) { return $fromQG }
  }

  # 3) Fallback razonable si el QG es OK
  if ($qg.projectStatus.status -eq 'OK') { return 'A' }
  return 'E'
}

function LetterClass([string]$L) {
  switch ($L) {
    'A' { 'ok' }
    'B' { 'ok' }
    'C' { 'warn' }
    'D' { 'err' }
    'E' { 'err' }
    default { 'warn' }
  }
}

$relL = GetRatingLetter 'reliability_rating'     $vals $qg
$secL = GetRatingLetter 'security_rating'        $vals $qg
$mntL = GetRatingLetter 'sqale_rating'           $vals $qg  # maintainability
$srvL = GetRatingLetter 'security_review_rating' $vals $qg

$relC = LetterClass $relL
$secC = LetterClass $secL
$mntC = LetterClass $mntL
$srvC = LetterClass $srvL

# --- CSV ---
$csvObj = [PSCustomObject]@{
  organization                     = $OrgKey
  project_key                      = $ProjectKey
  analysis_date                    = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")

  ncloc                            = $vals['ncloc']
  files                            = $vals['files']
  classes                          = $vals['classes']
  functions                        = $vals['functions']
  comment_lines                    = $vals['comment_lines']
  comment_lines_density_pct        = $vals['comment_lines_density']

  bugs                             = $vals['bugs']
  vulnerabilities                  = $vals['vulnerabilities']
  code_smells                      = $vals['code_smells']

  security_hotspots                = $vals['security_hotspots']
  security_hotspots_reviewed_pct   = $vals['security_hotspots_reviewed']

  reliability_rating               = $relL
  security_rating                  = $secL
  maintainability_rating           = $mntL
  security_review_rating           = $srvL

  duplicated_lines_density_pct     = $vals['duplicated_lines_density']
  duplicated_blocks                = $vals['duplicated_blocks']

  coverage_pct                     = $vals['coverage']
  lines_to_cover                   = $vals['lines_to_cover']
  uncovered_lines                  = $vals['uncovered_lines']

  tests                            = $vals['tests']
  test_failures                    = $vals['test_failures']
  test_errors                      = $vals['test_errors']
  test_success_density_pct         = $vals['test_success_density']

  issues_total                     = $issues.total
  issues_bug                       = $byType['BUG']
  issues_vulnerability             = $byType['VULNERABILITY']
  issues_code_smell                = $byType['CODE_SMELL']
  issues_blocker                   = $bySeverity['BLOCKER']
  issues_critical                  = $bySeverity['CRITICAL']
  issues_major                     = $bySeverity['MAJOR']
  issues_minor                     = $bySeverity['MINOR']
  issues_info                      = $bySeverity['INFO']

  quality_gate_status              = $qgState
}
$csvPath = Join-Path (Get-Location) "sonar_metrics.csv"
$csvObj | Export-Csv -Path $csvPath -NoTypeInformation -Encoding UTF8

# --- HTML -> PDF ---
$style = @"
<style>
 body { font-family: Arial, Helvetica, sans-serif; margin: 20px; color:#222; }
 h1 { margin: 0 0 8px 0; }
 .meta { color:#555; margin-bottom:12px; }
 table { border-collapse: collapse; width: 100%; margin-top:10px; }
 th, td { border: 1px solid #ddd; padding: 6px 8px; font-size: 13px; }
 th { background: #f2f2f2; text-align: left; }
 .ok   { color: #2e7d32; font-weight: bold; }
 .warn { color: #e65100; font-weight: bold; }
 .err  { color: #c62828; font-weight: bold; }
</style>
"@

$gateClass = if ($qgState -eq 'OK') {'ok'} elseif ($qgState -eq 'WARN') {'warn'} else {'err'}

$ratingsTable = @"
<tr><th>Reliability</th><td class='$relC'>$relL</td></tr>
<tr><th>Security</th><td class='$secC'>$secL</td></tr>
<tr><th>Maintainability</th><td class='$mntC'>$mntL</td></tr>
<tr><th>Security Review</th><td class='$srvC'>$srvL</td></tr>
"@

$html = @"
<html><head><meta charset='utf-8'>$style<title>Sonar Report - $ProjectKey</title></head>
<body>
  <h1>SonarCloud Report</h1>
  <div class='meta'>
    <div><b>Organization:</b> $OrgKey</div>
    <div><b>Project:</b> $ProjectKey</div>
    <div><b>Generated:</b> $(Get-Date)</div>
    <div><b>Quality Gate:</b> <span class='$gateClass'>$qgState</span></div>
  </div>

  <h3>Overview</h3>
  <table>
    <tr><th>ncloc</th><td>$($vals['ncloc'])</td><th>files</th><td>$($vals['files'])</td><th>classes</th><td>$($vals['classes'])</td></tr>
    <tr><th>bugs</th><td>$($vals['bugs'])</td><th>vulnerabilities</th><td>$($vals['vulnerabilities'])</td><th>code smells</th><td>$($vals['code_smells'])</td></tr>
    <tr><th>duplication %</th><td>$($vals['duplicated_lines_density'])</td><th>coverage %</th><td>$($vals['coverage'])</td><th>tests</th><td>$($vals['tests'])</td></tr>
  </table>

  <h3>Ratings</h3>
  <table>$ratingsTable</table>

  <h3>Coverage</h3>
  <table>
    <tr><th>Lines to cover</th><td>$($vals['lines_to_cover'])</td><th>Uncovered lines</th><td>$($vals['uncovered_lines'])</td><th>Coverage %</th><td>$($vals['coverage'])</td></tr>
  </table>

  <h3>Issues</h3>
  <table>
    <tr><th>Total</th><td>$($issues.total)</td><th>BUG</th><td>$($byType['BUG'])</td><th>VULN</th><td>$($byType['VULNERABILITY'])</td><th>CODE_SMELL</th><td>$($byType['CODE_SMELL'])</td></tr>
    <tr><th>BLOCKER</th><td>$($bySeverity['BLOCKER'])</td><th>CRITICAL</th><td>$($bySeverity['CRITICAL'])</td><th>MAJOR</th><td>$($bySeverity['MAJOR'])</td><th>MINOR</th><td>$($bySeverity['MINOR'])</td></tr>
  </table>
</body></html>
"@

$htmlPath = Join-Path (Get-Location) "sonar_report.html"
$pdfPath  = Join-Path (Get-Location) "sonar_report.pdf"
Set-Content -Path $htmlPath -Value $html -Encoding UTF8
if (Test-Path $pdfPath) { Remove-Item $pdfPath -Force -ErrorAction SilentlyContinue }

$chrome = "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe"
$edge   = "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe"
if (Test-Path $chrome) {
  & $chrome --headless=new --disable-gpu --print-to-pdf="$pdfPath" "$htmlPath" | Out-Null
} elseif (Test-Path $edge) {
  & $edge   --headless=new --disable-gpu --print-to-pdf="$pdfPath" "$htmlPath" | Out-Null
} else {
  Write-Warning "No se encontró Chrome/Edge; deja abierto sonar_report.html"
}

if (Test-Path $pdfPath) { Remove-Item $htmlPath -Force -ErrorAction SilentlyContinue }

Write-Host "`nOK -> sonar_metrics.csv  y  sonar_report.pdf" -ForegroundColor Green
