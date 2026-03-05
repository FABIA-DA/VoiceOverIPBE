<#
.SYNOPSIS
  External Docker Volume Management

.DESCRIPTION
  Creates or deletes all predefined Docker volumes used by the stack.

.USAGE
  .\docker-volumes.ps1 create
  .\docker-volumes.ps1 delete
#>

param (
    [Parameter(Position=0)]
    [string]$Action
)

# Defined volumes
$Volumes = @(
    "shared-audio",
    "form-db_data",
    "recordings",
    "coqui-be-cache",
    "coqui-models",
    "whisper-cache"
)

function Show-Usage {
    Write-Host ""
    Write-Host "Usage:"
    Write-Host "  .\docker-volumes.ps1 create   -> Create all volumes"
    Write-Host "  .\docker-volumes.ps1 delete   -> Delete all volumes"
    Write-Host ""
}

# Validate input
if (-not $Action) {
    Show-Usage
    exit 1
}

switch ($Action.ToLower()) {

    "create" {
        foreach ($vol in $Volumes) {
            Write-Host "Creating volume: $vol"
            docker volume create $vol | Out-Null
        }
        Write-Host "All volumes created."
    }

    "delete" {
        Write-Host "WARNING: This will delete ALL defined volumes!"
        $confirm = Read-Host "Type YES to continue"

        if ($confirm -ne "YES") {
            Write-Host "Aborted."
            exit
        }

        foreach ($vol in $Volumes) {
            Write-Host "Removing volume: $vol"
            docker volume rm $vol
        }

        Write-Host "All volumes removed."
    }

    default {
        Write-Host "Invalid action: $Action"
        Show-Usage
        exit 1
    }
}