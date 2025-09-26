# Test script to verify SUPER_ADMIN can see all projects

# Test login
$loginBody = @{
    username = "admin"
    password = "updated"
} | ConvertTo-Json

try {
    Write-Host "Testing login..."
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -ContentType "application/json" -Body $loginBody
    $token = $loginResponse.token
    Write-Host "Login successful! Token: $($token.Substring(0, 20))..."
    
    # Test my-projects endpoint
    Write-Host "Testing /api/projects/my-projects endpoint..."
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $projectsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/projects/my-projects" -Method GET -Headers $headers
    Write-Host "Projects found: $($projectsResponse.Count)"
    foreach ($project in $projectsResponse) {
        Write-Host "  - Project: $($project.name) (ID: $($project.id), Manager: $($project.managerName))"
    }
    
} catch {
    Write-Host "Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response body: $responseBody"
    }
}
