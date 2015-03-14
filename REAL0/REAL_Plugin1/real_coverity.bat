rem ----------------------------------------------
rem -----------------Clean------------------------
rem ----------------------------------------------
call ant -f build_real.xml -verbose clean
rem ----------------------------------------------
rem -------------Coverity Build-------------------
rem ----------------------------------------------
call cov-build --dir C:/real_cov cmd.exe /c "ant -f build_real.xml -verbose build" 
rem ----------------------------------------------
rem -------------Coverity Analyze-----------------
rem ----------------------------------------------
call cov-analyze-java --dir C:/real_cov
rem ----------------------------------------------
rem -------------Coverity Results-----------------
rem ----------------------------------------------
call cov-format-errors --dir C:/real_cov  --filesort --title REAL