$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$project = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$releaseVersion = ((Get-Content -LiteralPath (Join-Path $project 'neoforge-1.21.1/gradle.properties') | Where-Object { $_ -match '^mod_version=' }) -split '=',2)[1].Trim()
$dist = Join-Path $project "dist/$releaseVersion"
New-Item -ItemType Directory -Force -Path $dist | Out-Null
foreach ($version in @('1.21.1','26.1.2')) {
    foreach ($suffix in @('','-sources')) {
        $name = "thaumic-tinkerer-$releaseVersion+$version$suffix.jar"
        Copy-Item -LiteralPath (Join-Path $project "neoforge-$version/build/libs/$name") -Destination (Join-Path $dist $name)
    }
}
foreach ($name in @('README.md','LICENSE','LICENSE-CC-BY-NC-SA-3.0.txt')) {
    Copy-Item -LiteralPath (Join-Path $project $name) -Destination (Join-Path $dist $name)
}
foreach ($folder in @('docs','catalog')) {
    New-Item -ItemType Directory -Force -Path (Join-Path $dist $folder) | Out-Null
    Get-ChildItem -LiteralPath (Join-Path $project $folder) -File | Copy-Item -Destination (Join-Path $dist $folder)
}
New-Item -ItemType Directory -Force -Path (Join-Path $dist 'validation') | Out-Null
foreach ($name in @('README.md','resource-report.json','package-report.json','client-assets-1.21.1.json','client-assets-26.1.2.json')) {
    Copy-Item -LiteralPath (Join-Path $project "validation/$name") -Destination (Join-Path $dist "validation/$name")
}
$sourceZip = Join-Path $dist "thaumic-tinkerer-$releaseVersion-project.zip"
if (Test-Path -LiteralPath $sourceZip) { Remove-Item -LiteralPath $sourceZip }
$archive = [IO.Compression.ZipFile]::Open($sourceZip,[IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($file in Get-ChildItem -LiteralPath $project -Recurse -File -Force) {
        $relative = [IO.Path]::GetRelativePath($project,$file.FullName).Replace('\','/')
        if ($relative -match '(^|/)(\.git|\.gradle|build|run|dev-libs|dist|test-classes|\.idea)(/|$)' -or $relative.EndsWith('.log')) { continue }
        [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive,$file.FullName,"thaumic_tinkerer/$relative",[IO.Compression.CompressionLevel]::Optimal) | Out-Null
    }
} finally { $archive.Dispose() }
$checksums = foreach ($file in Get-ChildItem -LiteralPath $dist -Recurse -File | Where-Object { $_.Name -ne 'SHA256SUMS.txt' }) {
    '{0}  {1}' -f (Get-FileHash -Algorithm SHA256 -LiteralPath $file.FullName).Hash.ToLowerInvariant(),[IO.Path]::GetRelativePath($dist,$file.FullName).Replace('\','/')
}
$checksums | Set-Content -LiteralPath (Join-Path $dist 'SHA256SUMS.txt') -Encoding utf8
Get-ChildItem -LiteralPath $dist -Filter '*.jar' | Select-Object Name,Length
Write-Output "Source project: $sourceZip"
