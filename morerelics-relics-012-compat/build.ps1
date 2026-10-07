param(
    [switch]$Install
)

$ErrorActionPreference = 'Stop'

$projectDir = (Resolve-Path $PSScriptRoot).Path
$meusModsDir = (Resolve-Path (Join-Path $projectDir '..\..')).Path
$instanceDir = 'C:\Users\pichau\curseforge\minecraft\Instances\21'
$installDir = 'C:\Users\pichau\curseforge\minecraft\Install'

$javaHome = 'C:\Program Files\Microsoft\jdk-21.0.12.8-hotspot'
$javac = Join-Path $javaHome 'bin\javac.exe'
$jarTool = Join-Path $javaHome 'bin\jar.exe'

$minecraftJar = Join-Path $installDir 'libraries\net\minecraft\client\1.21.1\client-1.21.1-official.jar'
$mixinJar = Join-Path $installDir 'libraries\net\fabricmc\sponge-mixin\0.15.2+mixin.0.8.7\sponge-mixin-0.15.2+mixin.0.8.7.jar'
$fmlLoaderJar = Join-Path $installDir 'libraries\net\neoforged\fancymodloader\loader\4.0.43\loader-4.0.43.jar'
$neoForgeJar = Join-Path $installDir 'libraries\net\neoforged\neoforge\21.1.248\neoforge-21.1.248-universal.jar'
$asmJar = Join-Path $installDir 'libraries\org\ow2\asm\asm\9.7.1\asm-9.7.1.jar'
$asmTreeJar = Join-Path $installDir 'libraries\org\ow2\asm\asm-tree\9.7.1\asm-tree-9.7.1.jar'
$dataFixerJar = Join-Path $installDir 'libraries\com\mojang\datafixerupper\8.0.16\datafixerupper-8.0.16.jar'
$mergeToolApiJar = Join-Path $installDir 'libraries\net\neoforged\mergetool\2.0.7\mergetool-2.0.7-api.jar'
$moreRelicsActiveJar = Join-Path $instanceDir 'mods\morerelics-1.7.7-1.21.1.jar'
$moreRelicsDisabledJar = "$moreRelicsActiveJar.disabled"
$moreRelicsJar = if (Test-Path -LiteralPath $moreRelicsActiveJar) {
    $moreRelicsActiveJar
} else {
    $moreRelicsDisabledJar
}
$relicsJar = Join-Path $instanceDir 'mods\relics-1.21.1-0.12.8.jar'
$curiosJar = Join-Path $instanceDir 'mods\curios-neoforge-9.5.1+1.21.1.jar'

$requiredFiles = @(
    $javac,
    $jarTool,
    $minecraftJar,
    $mixinJar,
    $fmlLoaderJar,
    $neoForgeJar,
    $asmJar,
    $asmTreeJar,
    $dataFixerJar,
    $mergeToolApiJar,
    $moreRelicsJar,
    $relicsJar,
    $curiosJar
)

foreach ($requiredFile in $requiredFiles) {
    if ([string]::IsNullOrWhiteSpace($requiredFile) -or -not (Test-Path -LiteralPath $requiredFile)) {
        throw "Dependência de compilação não encontrada: $requiredFile"
    }
}

$buildDir = Join-Path $projectDir 'build'
$classesDir = Join-Path $buildDir 'classes'
$stagingDir = Join-Path $buildDir 'staging'
$libsDir = Join-Path $buildDir 'libs'
$metadata = Get-Content -LiteralPath (Join-Path $projectDir 'src\main\resources\META-INF\neoforge.mods.toml') -Raw
$version = [regex]::Match($metadata, '(?m)^version="([^"]+)"').Groups[1].Value
if ($version -notmatch '^\d+\.\d+\.\d+$') { throw "Versão de build inválida: $version" }
$jarName = "morerelics-new-relics-fix-$version.jar"
$jarPath = Join-Path $libsDir $jarName
$archiveDir = Join-Path $meusModsDir 'builds'

$resolvedBuildParent = [IO.Path]::GetFullPath((Split-Path $buildDir -Parent))
if ($resolvedBuildParent -ne $projectDir) {
    throw "Diretório de build fora do projeto: $buildDir"
}

foreach ($directory in @($classesDir, $stagingDir)) {
    if (Test-Path -LiteralPath $directory) {
        $resolved = [IO.Path]::GetFullPath($directory)
        if (-not $resolved.StartsWith($projectDir + [IO.Path]::DirectorySeparatorChar)) {
            throw "Diretório de build fora do projeto: $resolved"
        }
        Remove-Item -LiteralPath $directory -Recurse -Force
    }
}

New-Item -ItemType Directory -Force -Path $classesDir, $stagingDir, $libsDir | Out-Null

$classpath = @(
    $minecraftJar,
    $mixinJar,
    $fmlLoaderJar,
    $neoForgeJar,
    $asmJar,
    $asmTreeJar,
    $dataFixerJar,
    $mergeToolApiJar,
    $moreRelicsJar,
    $relicsJar,
    $curiosJar
) -join [IO.Path]::PathSeparator

$sources = Get-ChildItem -LiteralPath (Join-Path $projectDir 'src\main\java') -Recurse -Filter '*.java' |
    Select-Object -ExpandProperty FullName

if (-not $sources) {
    throw 'Nenhum arquivo Java encontrado.'
}

& $javac --release 21 -encoding UTF-8 -proc:none -classpath $classpath -d $classesDir $sources
if ($LASTEXITCODE -ne 0) {
    throw "A compilação falhou com o código $LASTEXITCODE."
}

$resourcesDir = Join-Path $projectDir 'src\main\resources'
Copy-Item -Path (Join-Path $resourcesDir '*') -Destination $stagingDir -Recurse
Copy-Item -Path (Join-Path $classesDir '*') -Destination $stagingDir -Recurse

# Relics 0.12 looks up descriptions through relics.description.*, while More
# Relics 1.7.x ships the same strings under tooltip.relics.*. Generate a small
# language overlay for every language bundled by More Relics so the distributed
# compatibility JAR remains self-contained.
Add-Type -AssemblyName System.IO.Compression.FileSystem
$languageDir = Join-Path $stagingDir 'assets\morerelics_relics_012_compat\lang'
New-Item -ItemType Directory -Force -Path $languageDir | Out-Null
$moreRelicsArchive = [IO.Compression.ZipFile]::OpenRead($moreRelicsJar)
try {
    $languageEntries = $moreRelicsArchive.Entries | Where-Object {
        $_.FullName -match '^assets/morerelics/lang/([^/]+)\.json$'
    }

    foreach ($entry in $languageEntries) {
        $language = [IO.Path]::GetFileNameWithoutExtension($entry.Name)
        $reader = [IO.StreamReader]::new($entry.Open())
        try {
            $sourceTranslations = $reader.ReadToEnd() | ConvertFrom-Json -AsHashtable
        } finally {
            $reader.Dispose()
        }

        $aliases = [ordered]@{}
        foreach ($key in $sourceTranslations.Keys) {
            if ($key.StartsWith('tooltip.relics.')) {
                $alias = $key -replace '^tooltip\.relics\.', 'relics.description.'
                $aliases[$alias] = $sourceTranslations[$key]
            }
        }

        $languagePath = Join-Path $languageDir "$language.json"
        $aliases | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $languagePath -Encoding UTF8
        Write-Output "Aliases de tradução gerados ($language): $($aliases.Count)"
    }
} finally {
    $moreRelicsArchive.Dispose()
}

if (Test-Path -LiteralPath $jarPath) {
    Remove-Item -LiteralPath $jarPath -Force
}

& $jarTool --create --file $jarPath --no-manifest -C $stagingDir .
if ($LASTEXITCODE -ne 0) {
    throw "A criação do JAR falhou com o código $LASTEXITCODE."
}

New-Item -ItemType Directory -Force -Path $archiveDir | Out-Null
Copy-Item -LiteralPath $jarPath -Destination (Join-Path $archiveDir $jarName) -Force
Write-Output "Build arquivada: $(Join-Path $archiveDir $jarName)"

if ($Install) {
    & (Join-Path $projectDir 'verify.ps1') -CompatJar $jarPath

    $modsDir = (Resolve-Path (Join-Path $instanceDir 'mods')).Path
    $expectedModsDir = 'C:\Users\pichau\curseforge\minecraft\Instances\21\mods'
    if ($modsDir -ne $expectedModsDir) { throw "Destino de instalação inesperado: $modsDir" }
    $previousBuilds = Get-ChildItem -LiteralPath $modsDir -File | Where-Object {
        $_.Name -match '^morerelics-(relics-beta-fix|new-relics-fix)-[0-9]+\.[0-9]+\.[0-9]+\.jar$'
    }
    if ($previousBuilds) {
        $backupDir = Join-Path $buildDir ('installed-backups\' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff'))
        New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
        foreach ($previousBuild in $previousBuilds) {
            if ([IO.Path]::GetFullPath($previousBuild.DirectoryName) -ne $expectedModsDir) {
                throw "Build anterior fora da instância 21: $($previousBuild.FullName)"
            }
            Copy-Item -LiteralPath $previousBuild.FullName -Destination $backupDir
        }
        foreach ($previousBuild in $previousBuilds) {
            Remove-Item -LiteralPath $previousBuild.FullName -Force
        }
        Write-Output "Backup da versão anterior: $backupDir"
    }
    if (-not (Test-Path -LiteralPath $moreRelicsActiveJar) -and (Test-Path -LiteralPath $moreRelicsDisabledJar)) {
        Move-Item -LiteralPath $moreRelicsDisabledJar -Destination $moreRelicsActiveJar
        Write-Output "More Relics reativado: $moreRelicsActiveJar"
    }

    $installedJar = Join-Path $instanceDir "mods\$jarName"
    Copy-Item -LiteralPath $jarPath -Destination $installedJar -Force
    Write-Output "Instalado: $installedJar"
}

Write-Output "Gerado: $jarPath"
