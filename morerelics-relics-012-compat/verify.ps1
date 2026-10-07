param(
    [string]$CompatJar
)

$ErrorActionPreference = 'Stop'

$projectDir = (Resolve-Path $PSScriptRoot).Path
$instanceDir = 'C:\Users\pichau\curseforge\minecraft\Instances\21'
$installDir = 'C:\Users\pichau\curseforge\minecraft\Install'
$javaHome = 'C:\Program Files\Microsoft\jdk-21.0.12.8-hotspot'

$javac = Join-Path $javaHome 'bin\javac.exe'
$java = Join-Path $javaHome 'bin\java.exe'
$mixinJar = Join-Path $installDir 'libraries\net\fabricmc\sponge-mixin\0.15.2+mixin.0.8.7\sponge-mixin-0.15.2+mixin.0.8.7.jar'
$asmJar = Join-Path $installDir 'libraries\org\ow2\asm\asm\9.7.1\asm-9.7.1.jar'
$asmTreeJar = Join-Path $installDir 'libraries\org\ow2\asm\asm-tree\9.7.1\asm-tree-9.7.1.jar'
$metadata = Get-Content -LiteralPath (Join-Path $projectDir 'src\main\resources\META-INF\neoforge.mods.toml') -Raw
$version = [regex]::Match($metadata, '(?m)^version="([^"]+)"').Groups[1].Value
if (-not $CompatJar) {
    $CompatJar = Join-Path $projectDir "build\libs\morerelics-new-relics-fix-$version.jar"
}
$activeMoreRelics = Join-Path $instanceDir 'mods\morerelics-1.7.7-1.21.1.jar'
$disabledMoreRelics = "$activeMoreRelics.disabled"
$moreRelicsJar = if (Test-Path -LiteralPath $activeMoreRelics) {
    $activeMoreRelics
} else {
    $disabledMoreRelics
}
$testClasses = Join-Path $projectDir 'build\test-classes'
$testSource = Join-Path $projectDir 'src\test\java\dev\codex\morerelicscompat\BytecodeRemapVerifier.java'
$activationTestSource = Join-Path $projectDir 'src\test\java\dev\codex\morerelicscompat\AbilityActivationVerifier.java'
$minecraftJar = Join-Path $installDir 'libraries\net\minecraft\client\1.21.1\client-1.21.1-official.jar'
$relicsJar = Join-Path $instanceDir 'mods\relics-1.21.1-0.12.8.jar'
$curiosJar = Join-Path $instanceDir 'mods\curios-neoforge-9.5.1+1.21.1.jar'
$guavaJar = Join-Path $installDir 'libraries\com\google\guava\guava\32.1.2-jre\guava-32.1.2-jre.jar'
$fastutilJar = Join-Path $installDir 'libraries\it\unimi\dsi\fastutil\8.5.12\fastutil-8.5.12.jar'
$dataFixerJar = Join-Path $installDir 'libraries\com\mojang\datafixerupper\8.0.16\datafixerupper-8.0.16.jar'
$nettyBufferJar = Join-Path $installDir 'libraries\io\netty\netty-buffer\4.1.97.Final\netty-buffer-4.1.97.Final.jar'
$nettyCommonJar = Join-Path $installDir 'libraries\io\netty\netty-common\4.1.97.Final\netty-common-4.1.97.Final.jar'
$eventBusJar = Join-Path $installDir 'libraries\net\neoforged\bus\8.0.5\bus-8.0.5.jar'
$neoForgeJar = Join-Path $installDir 'libraries\net\neoforged\neoforge\21.1.248\neoforge-21.1.248-universal.jar'

if (Test-Path -LiteralPath $testClasses) {
    $resolved = [IO.Path]::GetFullPath($testClasses)
    if (-not $resolved.StartsWith($projectDir + [IO.Path]::DirectorySeparatorChar)) {
        throw "Diretório de teste fora do projeto: $resolved"
    }
    Remove-Item -LiteralPath $testClasses -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $testClasses | Out-Null

$classpath = @(
    $CompatJar, $mixinJar, $asmJar, $asmTreeJar, $minecraftJar, $relicsJar, $curiosJar,
    $guavaJar, $fastutilJar, $dataFixerJar, $nettyBufferJar, $nettyCommonJar, $eventBusJar, $neoForgeJar
) -join [IO.Path]::PathSeparator

& $javac --release 21 -encoding UTF-8 -proc:none -classpath $classpath -d $testClasses $testSource $activationTestSource
if ($LASTEXITCODE -ne 0) {
    throw "A compilação do verificador falhou com o código $LASTEXITCODE."
}

$runtimeClasspath = $testClasses + [IO.Path]::PathSeparator + $classpath
& $java -classpath $runtimeClasspath dev.codex.morerelicscompat.AbilityActivationVerifier
if ($LASTEXITCODE -ne 0) {
    throw "A validação da ativação falhou com o código $LASTEXITCODE."
}
& $java -classpath $runtimeClasspath dev.codex.morerelicscompat.BytecodeRemapVerifier $moreRelicsJar $CompatJar
if ($LASTEXITCODE -ne 0) {
    throw "A validação de bytecode falhou com o código $LASTEXITCODE."
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$sourceArchive = [IO.Compression.ZipFile]::OpenRead($moreRelicsJar)
$compatArchive = [IO.Compression.ZipFile]::OpenRead($compatJar)
try {
    $verifiedAliases = 0
    $languageEntries = $sourceArchive.Entries | Where-Object {
        $_.FullName -match '^assets/morerelics/lang/([^/]+)\.json$'
    }

    foreach ($sourceEntry in $languageEntries) {
        $language = [IO.Path]::GetFileNameWithoutExtension($sourceEntry.Name)
        $compatEntry = $compatArchive.GetEntry("assets/morerelics_relics_012_compat/lang/$language.json")
        if ($null -eq $compatEntry) {
            throw "Overlay de tradução ausente para $language."
        }

        $sourceReader = [IO.StreamReader]::new($sourceEntry.Open())
        $compatReader = [IO.StreamReader]::new($compatEntry.Open())
        try {
            $sourceTranslations = $sourceReader.ReadToEnd() | ConvertFrom-Json -AsHashtable
            $compatTranslations = $compatReader.ReadToEnd() | ConvertFrom-Json -AsHashtable
        } finally {
            $sourceReader.Dispose()
            $compatReader.Dispose()
        }

        foreach ($key in $sourceTranslations.Keys) {
            if (-not $key.StartsWith('tooltip.relics.')) {
                continue
            }

            $alias = $key -replace '^tooltip\.relics\.', 'relics.description.'
            if (-not $compatTranslations.ContainsKey($alias) -or $compatTranslations[$alias] -ne $sourceTranslations[$key]) {
                throw "Alias de tradução inválido: $alias"
            }
            $verifiedAliases++
        }
    }

    foreach ($requiredAlias in @(
        'relics.description.mass_gauntlet.description',
        'relics.description.mass_gauntlet.ability.heavy_hitter',
        'relics.description.mass_gauntlet.ability.heavy_hitter.description'
    )) {
        $englishEntry = $compatArchive.GetEntry('assets/morerelics_relics_012_compat/lang/en_us.json')
        $englishReader = [IO.StreamReader]::new($englishEntry.Open())
        try {
            $englishTranslations = $englishReader.ReadToEnd() | ConvertFrom-Json -AsHashtable
        } finally {
            $englishReader.Dispose()
        }
        if (-not $englishTranslations.ContainsKey($requiredAlias)) {
            throw "Tradução obrigatória ausente: $requiredAlias"
        }
    }

    Write-Output "Verified $verifiedAliases description translation aliases."
} finally {
    $sourceArchive.Dispose()
    $compatArchive.Dispose()
}
