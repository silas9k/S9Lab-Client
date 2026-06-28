param(
    [string]$SourceModel = "$env:USERPROFILE\Downloads\blockbench_dummy_mc_skin.bbmodel",
    [string]$SourceTexture = "$env:USERPROFILE\Downloads\blockbench_dummy_mc_skin_64x64.png",
    [string]$OutputDirectory = (Join-Path $PSScriptRoot "..\blockbench-templates")
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $SourceModel)) {
    throw "Source model not found: $SourceModel"
}
if (-not (Test-Path -LiteralPath $SourceTexture)) {
    throw "Source texture not found: $SourceTexture"
}

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

$model = Get-Content -LiteralPath $SourceModel -Raw | ConvertFrom-Json
$model.meta.model_format = "bedrock"
$model.meta.box_uv = $false
$model.name = "S9Lab Player Emote Template"
$model.model_identifier = "geometry.s9lab_player_emote_template"
$model.visible_box = @(2, 3, 0)

$elementByName = @{}
foreach ($element in $model.elements) {
    $elementByName[$element.name] = $element
    if ($element.PSObject.Properties.Name -contains "export") {
        $element.export = $true
    } else {
        $element | Add-Member -NotePropertyName export -NotePropertyValue $true
    }
}

$bones = @(
    @{ Name = "body"; Origin = @(0, 24, 0); Cubes = @("Body", "Body Overlay"); Color = 0 },
    @{ Name = "head"; Origin = @(0, 24, 0); Cubes = @("Head", "Head Overlay"); Color = 1 },
    @{ Name = "right_arm"; Origin = @(-5, 22, 0); Cubes = @("Right Arm", "Right Arm Overlay"); Color = 2 },
    @{ Name = "left_arm"; Origin = @(5, 22, 0); Cubes = @("Left Arm", "Left Arm Overlay"); Color = 3 },
    @{ Name = "right_leg"; Origin = @(-2, 12, 0); Cubes = @("Right Leg", "Right Leg Overlay"); Color = 4 },
    @{ Name = "left_leg"; Origin = @(2, 12, 0); Cubes = @("Left Leg", "Left Leg Overlay"); Color = 5 }
)

$groups = @()
$outliner = @()
$boneUuid = @{}
foreach ($bone in $bones) {
    $uuid = [guid]::NewGuid().ToString()
    $boneUuid[$bone.Name] = $uuid
    $children = @($bone.Cubes | ForEach-Object { $elementByName[$_].uuid })
    $groups += [pscustomobject]@{
        name = $bone.Name
        uuid = $uuid
        export = $true
        locked = $false
        scope = 0
        selected = $false
        origin = $bone.Origin
        rotation = @(0, 0, 0)
        bedrock_binding = ""
        color = $bone.Color
        children = @()
        reset = $false
        shade = $true
        mirror_uv = $false
        visibility = $true
        autouv = 0
        isOpen = $true
    }
    $outliner += [pscustomobject]@{
        uuid = $uuid
        isOpen = $true
        children = $children
    }
}
$model.groups = $groups
$model.outliner = $outliner

function New-RotationKeyframe([double]$time, [double]$x, [double]$y, [double]$z) {
    [pscustomobject]@{
        channel = "rotation"
        data_points = @([pscustomobject]@{ x = "$x"; y = "$y"; z = "$z" })
        uuid = [guid]::NewGuid().ToString()
        time = $time
        color = -1
        interpolation = "linear"
    }
}

function New-Animator([string]$name, [array]$keyframes) {
    [pscustomobject]@{
        name = $name
        type = "bone"
        rotation_global = $false
        quaternion_interpolation = $false
        keyframes = $keyframes
    }
}

$axisAnimators = [ordered]@{}
$axisAnimators[$boneUuid["head"]] = New-Animator "head" @(
    (New-RotationKeyframe 0 0 0 0),
    (New-RotationKeyframe 1 0 -30 0)
)
$axisAnimators[$boneUuid["right_arm"]] = New-Animator "right_arm" @(
    (New-RotationKeyframe 0 0 0 0),
    (New-RotationKeyframe 1 -70 0 25)
)
$axisAnimators[$boneUuid["left_arm"]] = New-Animator "left_arm" @(
    (New-RotationKeyframe 0 0 0 0),
    (New-RotationKeyframe 1 0 0 -70)
)

$blankAnimators = [ordered]@{}
foreach ($bone in $bones) {
    $blankAnimators[$boneUuid[$bone.Name]] = New-Animator $bone.Name @(
        (New-RotationKeyframe 0 0 0 0)
    )
}

$model.animations = @(
    [pscustomobject]@{
        uuid = [guid]::NewGuid().ToString()
        name = "animation.player.my_emote"
        loop = "loop"
        override = $false
        length = 1.0
        snapping = 20
        selected = $true
        saved = $false
        path = ""
        scope = 0
        anim_time_update = ""
        blend_weight = ""
        start_delay = ""
        loop_delay = ""
        animators = [pscustomobject]$blankAnimators
    },
    [pscustomobject]@{
        uuid = [guid]::NewGuid().ToString()
        name = "animation.player.axis_check"
        loop = "hold"
        override = $false
        length = 1.0
        snapping = 20
        selected = $false
        saved = $false
        path = ""
        scope = 0
        anim_time_update = ""
        blend_weight = ""
        start_delay = ""
        loop_delay = ""
        animators = [pscustomobject]$axisAnimators
    }
)

$textureBytes = [System.IO.File]::ReadAllBytes($SourceTexture)
$textureBase64 = [Convert]::ToBase64String($textureBytes)
$textureName = "s9lab_player_dummy_skin.png"
$texture = $model.textures[0]
$texture.path = $textureName
$texture.name = $textureName
$texture.relative_path = $textureName
$texture.saved = $true
$texture.source = "data:image/png;base64,$textureBase64"

$outputModel = Join-Path $OutputDirectory "S9Lab_Player_Emote_Template.bbmodel"
$outputTexture = Join-Path $OutputDirectory $textureName
$model | ConvertTo-Json -Depth 100 | Set-Content -LiteralPath $outputModel -Encoding UTF8
Copy-Item -LiteralPath $SourceTexture -Destination $outputTexture -Force

Write-Host "Created: $outputModel"
Write-Host "Created: $outputTexture"
