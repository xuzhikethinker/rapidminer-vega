; Java Launcher
;--------------
 
;You want to change the next four lines
Name RapidMiner
Caption "RapidMiner Launcher"
Icon "rapidminer_icon.ico"
OutFile "../release/files/RapidMiner.exe"
 
# Request execution level
RequestExecutionLevel user

SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow
 
Section ""

; This will set the environment variables accordingly
Call SetEnvironment 

; Searching for system memory to use
System::Alloc 32
Pop $1
System::Call "Kernel32::GlobalMemoryStatus(i) v (r1)"
System::Call "*$1(&i4 .r2, &i4 .r3, &i4 .r4, &i4 .r5, \
                  &i4 .r6, &i4.r7, &i4 .r8, &i4 .r9)"
System::Free $1


; for Xmx and Xms
IntOp $R9 $5 / 1024
IntOp $R9 $R9 / 1024
IntOp $R9 $R9 * 90
IntOp $R9 $R9 / 100
IntCmp $R9 64 less64 less64 more64
less64: 
StrCpy $R9 64
Goto mem_more
more64:
Goto mem_more

mem_more:
IntCmp $R9 1200 less1200 less1200 more1200
less1200:
Goto after_mem_more
more1200: 
StrCpy $R9 1200
Goto after_mem_more

after_mem_more:
  Call GetJRE
  Pop $R0
 
  Call GetParameters
  Pop $R1

; testing for number of processors for switching to multi threaded GC
ReadEnvStr $R2 "NUMBER_OF_PROCESSORS"  
  IntFmt $0 "0x%08X" $R2
  IntCmp $0 1 is1 done morethan1
is1:
  StrCpy $R2 ''
  Goto done
morethan1:
  IntOp $0 $0 - 1
  StrCpy $R2 '-XX:+UseParallelGC -XX:+UseParallelOldGC -XX:ParallelGCThreads=$0'
  Goto done
done:
 
  
  ; invoking RapidMiner via launcher.jar  
  StrCpy $0 '"$R0" $R2 -Xmx$R9m -Xms$R9m -classpath "${CLASSPATH}" -Drapidminer.home=. -Drapidminer.operators.additional="${RAPIDMINER_OPERATORS_ADDITIONAL}" -jar lib/launcher.jar $R1'
 
  
  SetOutPath $EXEDIR

  Relaunch:
	  Call PerformUpdate
	  
	  ExecWait $0 $1
	  Call SetEnvironment ;if settings have been adapted inside RapidMiner
  IntCmp $1 2 Relaunch
  
SectionEnd
 
Function PerformUpdate
;
;  Check for Directory RUinstall
;  If found, copy everything from this directory and remove it 
 
  Push $R0
 
  ClearErrors
  StrCpy $R0 "$EXEDIR\RUinstall\*"
  IfFileExists $R0 UpdateFound
  StrCpy $R0 ""
        
  UpdateFound:
    ; Check if update contains new RapidMiner.exe
    StrCpy $R0 "$EXEDIR\RUinstall\RapidMiner.exe"
    IfFileExists UpdateItself UpdateOther
    UpdateItself:
       Rename "$EXEDIR\RUinstall\RapidMiner.exe" "$EXEDIR\RapidMiner.exex"
       Rename /REBOOTOK "$EXEDIR\RapidMiner.exex" "$EXEDIR\RapidMiner.exe"
    
    UpdateOther:  
       CopyFiles /SILENT $EXEDIR\RUinstall\* $EXEDIR
       RmDir /r $EXEDIR\RUinstall
     
FunctionEnd

Function GetJRE
;
;  Find JRE (javaw.exe)
;  1 - in .\jre directory (JRE Installed with application)
;  2 - in JAVA_HOME environment variable
;  3 - in the registry
;  4 - assume javaw.exe in current dir or PATH
 
  Push $R0
  Push $R1
 
  ClearErrors
  StrCpy $R0 "$EXEDIR\jre\bin\javaw.exe"
  IfFileExists $R0 JreFound
  StrCpy $R0 ""
 
  ClearErrors
  ReadEnvStr $R0 "JAVA_HOME"
  StrCpy $R0 "$R0\bin\javaw.exe"
  IfErrors 0 JreFound
 
  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  StrCpy $R0 "$R0\bin\javaw.exe"
 
  IfErrors 0 JreFound
  StrCpy $R0 "javaw.exe"
        
 JreFound:
  Pop $R1
  Exch $R0
FunctionEnd



 ; GetParameters
 ; input, none
 ; output, top of stack (replaces, with e.g. whatever)
 ; modifies no other variables.
Function GetParameters
 
  Push $R0
  Push $R1
  Push $R2
  Push $R3
 
  StrCpy $R2 1
  StrLen $R3 $CMDLINE
 
  ;Check for quote or space
  StrCpy $R0 $CMDLINE $R2
  StrCmp $R0 '"' 0 +3
    StrCpy $R1 '"'
    Goto loop
  StrCpy $R1 " "
 
  loop:
    IntOp $R2 $R2 + 1
    StrCpy $R0 $CMDLINE 1 $R2
    StrCmp $R0 $R1 get
    StrCmp $R2 $R3 get
    Goto loop
 
  get:
    IntOp $R2 $R2 + 1
    StrCpy $R0 $CMDLINE 1 $R2
    StrCmp $R0 " " get
    StrCpy $R0 $CMDLINE "" $R2
 
  Pop $R3
  Pop $R2
  Pop $R1
  Exch $R0
 
FunctionEnd

; This function will read from the ./scripts/config/config.win file
; which environment variables have to be set
Function SetEnvironment
	Push $R0		; R0 will be used as handle
	Push $R1		; R1 is the line string for the environment variable name
	Push $R2		; R2 is the line string for the environment variable value
	Push $R3		; R3 is the current value of the environment variable
	Push $R4		; R4 is the mode: either append or overwrite
	
	ClearErrors
	FileOpen $R0 $EXEDIR\scripts\config\config.win r
	IfErrors nextVariableEnd
	
	nextVariable:
		ClearErrors
		FileRead $R0 $R4
		StrCpy $R4 $R4 -2
		FileRead $R0 $R1
		StrCpy $R1 $R1 -2
		FileRead $R0 $R2
		StrCpy $R2 $R2 -2
		IfErrors nextVariableEnd
		
		; check whether the overwrite mode is on, then only set
		StrCmp $R4 "overwrite" useRawValue
		; if not, check if environment variable already exists. 
		ClearErrors
		ReadEnvStr $R3 $R1
		IfErrors useRawValue 	;If exists, append new value
			StrCpy $R2 "$R3;$R2"
		useRawValue:		;else set to raw value
			System::Call 'Kernel32::SetEnvironmentVariableA(t R1, t R2) .r0'
  	
		Goto nextVariable
	nextVariableEnd:
	
	; close file
	FileClose $R0
	
	; restore old registers
	clearErrors
    Pop $R4
	Pop $R3
	Pop $R2
	Pop $R1
	Pop $R0
FunctionEnd