Dim objShell
Set objShell = WScript.CreateObject("WScript.Shell")
objShell.CurrentDirectory = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)
objShell.Run "cmd /c java --module-path ""C:\javafx-sdk\javafx-sdk-21.0.11\lib"" --add-modules javafx.controls,javafx.fxml -jar ""inventy-stamp.jar""", 0, False
Set objShell = Nothing