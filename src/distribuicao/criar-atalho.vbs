Set objShell = CreateObject("WScript.Shell")
Set objLink = objShell.CreateShortcut(objShell.SpecialFolders("Desktop") & "\Inventy Stamp.lnk")
objLink.TargetPath = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName) & "\iniciar.vbs"
objLink.IconLocation = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName) & "\logo.ico"
objLink.WorkingDirectory = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)
objLink.Description = "Inventy Stamp"
objLink.Save
Set objLink = Nothing
Set objShell = Nothing
MsgBox "Atalho criado no Desktop com sucesso!"