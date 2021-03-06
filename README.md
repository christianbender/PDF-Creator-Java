## Simple PDF-Creator  

This project is a simple command-line PDF-Creator for textfiles.  
The project is written in Java. You will find a JAR file, called ```PDFCreator.jar``` as executable. 

The application creates PDF files in DIN A4 format and font size 12.  

### Use  

Under Unixes you must make the JAR-file executable. 

```bash
$ chmod +x PDFCreator.jar
```
For running: In the command line type in:  

```bash
$ ./PDFCreator.jar myFile.txt 
```

You can also put multiple files. 

```bash
$ ./PDFCreator.jar myFile1.txt myFile2.txt ... myFileN.txt
```

### Features  

* You can declare headings with ```/header``` at the begin of the line.  
For example ```/header Welcome```.  
![How the feature look like](screenshots/headers.png "Heading feature")


### Examples 

In the repo you will find some example text-files and PDF-files in the directory [examples](examples)    

### Contributing 

You can contribute to this project.  
