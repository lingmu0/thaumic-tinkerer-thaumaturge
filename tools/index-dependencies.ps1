param(
    [Parameter(Mandatory=$true)][ValidateSet('1.21.1','26.1.2')][string]$Version,
    [Parameter(Mandatory=$true)][string]$JarPath
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$resolvedJar = (Resolve-Path -LiteralPath $JarPath).Path
$archive = [IO.Compression.ZipFile]::OpenRead($resolvedJar)
$index = [ordered]@{}
try {
    foreach ($entry in $archive.Entries) {
        if ($entry.FullName -match '^data/.+\.json$') {
            $reader = [IO.StreamReader]::new($entry.Open())
            try { $index[$entry.FullName] = $reader.ReadToEnd() | ConvertFrom-Json -AsHashtable }
            finally { $reader.Dispose() }
        } elseif ($entry.FullName -match '^assets/thaumaturge/(models/item|items)/.+\.json$') {
            $index[$entry.FullName] = $true
        }
    }
} finally { $archive.Dispose() }
$index['_jarSha256'] = (Get-FileHash -Algorithm SHA256 -LiteralPath $resolvedJar).Hash.ToLowerInvariant()
$destination = Join-Path $PSScriptRoot "../dev-libs/thaumaturge-index-$Version.json"
$index | ConvertTo-Json -Depth 100 -Compress | Set-Content -LiteralPath $destination -Encoding utf8
Write-Output "$Version indexed $($index.Count) dependency resources."
