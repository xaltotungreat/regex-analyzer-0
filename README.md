# regex-analyzer-0
This project aims to create an IDE for logs analysis. It is based on the Eclipse framework. It is in fact an Eclipse RCP and can be exported as a product. The idea is to write down the regular expressions in some form with parameters and use them to automate search operations in logs. The project uses Spring to load the configuiration. The configuration consists of the regular expressions configuration, the GUI configuration, the log types configuration and some less important stuff. You can read more about this work in my blog posts http://java-xlt.blogspot.com/. Look for label "Log Analysis". 
Some examples are available in the config folder. 
Steps to create a new configuration:
1. Check out the examples
2. Create a new folder in config for example "newconfig".
3. Create a new beans file main_beans.xml in "newconfig/spring". The file name is HARDCODED.
4. Create the logtypes beans file. This file should be created before all others. This file MUST be included in main_beans.xml.
5. Create the necessary regex beans maybe in separate files. The program will load main_beans.xml. It is preferrable to keep all beans files in "newconfig/spring"
6. Create a new GUI configuration gui_beans.xml in "newconfig/spring". The file name is not hardcoded but MUST be included in main_beans.xml.
7. Add the parameter to the VM parameters in the form "-Dreal.config.product=newconfig". The name of the product MUST be the same as the folder. 
8. Start the program, add a folder with logs (by pressing the big plus "+"). Try your new configuration.
