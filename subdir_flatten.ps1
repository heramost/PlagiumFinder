$rootfolders = Dir .\* -Directory

ForEach($folder in $rootfolders)
{
	$innerFolders = Dir $folder.fullname -Directory
	ForEach($innerFolder in $innerFolders)
	{
		Dir $innerFolder.fullname -Recurse -File | Copy-Item -Destination $folder.fullname
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