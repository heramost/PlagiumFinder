$rootfolders = Dir .\* -Directory

ForEach($folder in $rootfolders)
{
	$innerFolders = Dir $folder.fullname -Directory
	ForEach($innerFolder in $innerFolders)
	{
		$filesToMove = Dir $innerFolder.fullname -Recurse -File;
		ForEach ($file in $filesToMove) {
			$i = ""
			$destination = $folder.fullname + "\" + $file.BaseName + $i + $file.Extension
			While (Test-Path $destination) {
				$i += "_another"
				$destination = $folder.fullname + "\" + $file.BaseName + $i + $file.Extension
				Write-Output $destination
			}
			Copy-Item -Path $file.FullName -Destination $destination -Force
		}
	}
	Dir $folder.fullname -Recurse -Directory | Remove-Item -Force -Recurse

    $files  = Dir $folder.fullname -File
    ForEach($file in $files)
    	{
    	    $fullname = $file.fullname;
    	    Get-Content $fullname | Set-Content -Encoding ascii "$($fullname)_ascii";
    	    Get-Content  "$($fullname)_ascii" | Set-Content -Encoding ascii $fullname;
    	    Remove-Item "$($fullname)_ascii";
    	}
}
Read-Host "Hit enter"