$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$reports = @()
foreach ($version in @('1.21.1','26.1.2')) {
    $jarPath = Join-Path $project "neoforge-$version/build/libs/thaumic-tinkerer-0.1.0-dev+$version.jar"
    $archive = [IO.Compression.ZipFile]::OpenRead($jarPath)
    try {
        $names = @($archive.Entries | ForEach-Object FullName)
        foreach ($required in @('LICENSE','LICENSE-CC-BY-NC-SA-3.0.txt','META-INF/neoforge.mods.toml','thaumictinkerer.mixins.json','net/lingmu/thaumictinkerer/ThaumicTinkerer.class','net/lingmu/thaumictinkerer/mixin/ExperienceOrbAccessor.class')) {
            if ($required -notin $names) { throw "$version missing $required" }
        }
        if (@($names | Where-Object { $_ -match '^(com/leclowndu93150/|thaumcraft/|appeng/|META-INF/(jarjar|jars)/|assets/thaumaturge/)' }).Count) {
            throw "$version bundles dependency code or assets"
        }
        $reader = [IO.StreamReader]::new($archive.GetEntry('META-INF/neoforge.mods.toml').Open())
        try { $metadata = $reader.ReadToEnd() } finally { $reader.Dispose() }
        if ($metadata.Contains('${') -or !$metadata.Contains("versionRange=`"[$version]`"")) { throw "$version invalid metadata" }
        $classes = @($archive.Entries | Where-Object { $_.FullName.EndsWith('.class') })
        $expectedMajor = if ($version -eq '1.21.1') {65} else {69}
        foreach ($entry in $classes) {
            $stream = $entry.Open()
            try {
                $bytes = [byte[]]::new(8)
                if ($stream.Read($bytes,0,8) -ne 8 -or ($bytes[6]*256+$bytes[7]) -ne $expectedMajor) { throw "$version wrong bytecode in $($entry.FullName)" }
            } finally { $stream.Dispose() }
        }
        $jsonCount = 0
        foreach ($entry in $archive.Entries | Where-Object { $_.FullName.EndsWith('.json') }) {
            $reader = [IO.StreamReader]::new($entry.Open())
            try { $value = $reader.ReadToEnd() | ConvertFrom-Json -AsHashtable } finally { $reader.Dispose() }
            $jsonCount++
            if ($version -eq '26.1.2' -and $entry.FullName -match '^assets/thaumictinkerer/models/item/(.+)\.json$') {
                if ("assets/thaumictinkerer/items/$($Matches[1]).json" -notin $names) { throw "Missing modern item definition: $($entry.FullName)" }
            }
        }
        $reports += [pscustomobject][ordered]@{version=$version;javaMajor=$expectedMajor;classes=$classes.Count;json=$jsonCount;jarSha256=(Get-FileHash -Algorithm SHA256 -LiteralPath $jarPath).Hash.ToLowerInvariant();status='passed'}
    } finally { $archive.Dispose() }
}
$reports | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath (Join-Path $project 'validation/package-report.json') -Encoding utf8
$reports | Format-Table version,javaMajor,classes,json,status
