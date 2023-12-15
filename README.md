# CTTS: CrypTool Transcriber &amp; Solver 

CrypTool Transcriber &amp; Solver is a ciphertext transcription and cryptanalysis tool initially developed by George Lasry and now maintained by the CrypTool team.
The tool is written in Java; the user interface is built using JavaFx.

The following documentation is based on the "May 10, 2022 Version 3.2" documentation written initially by George Lasry. It was converted from Word format to this document here, to have a single online version of the documentation.

## ℹ️ About

CTTS allows for the manual transcription of historical ciphertexts, represented by graphic symbols, letters, or digits, in an efficient manner, via a primarily graphical user interface. It supports the following features:

* **Symbol segmentation:** Marking symbols on images.
* **Symbol classification:** Assigning the symbols on the document image(s) to specific symbol types, including support for bulk operations.
* **Managing symbol types:** Adding transcription values, importing a key file with decryption values, or assigning a reference icon to a symbol type. In addition, viewing all the symbols of the same type together so that mismatches can be easily detected and fixed.
* **Review and iterative editing** of the transcription, of the decryption, and (new in 3.x) of the decryption.
* **Working on multiple documents** with the same types of symbols.
* **Saving** transcription and decryption outputs, as well as graphical snapshots.
* **Cryptanalysis** to recover an initial key for homophonic ciphers. Together with the ability to edit the key directly from CTTS, it is now possible to perform full cycles of transcription-cryptanalysis-decryption and improvement (transcription, key) in one place.

The process is performed offline, with the files stored and managed on a local computer.

The output of the CTTS is intended for:
* Transcribing a ciphertext for computer cryptanalysis.
* Deciphering a ciphertext, with built-in cryptanalysis, or using an original key found in archives, or via external cryptanalysis. The transcription and the key can be iteratively improved, with an end-to-end holistic transcription, decryption, and review process.
* Generating ground-truth data – training and test data - for automated machine learning transcription algorithms.
* Semi-automated transcription, CTTS producing segmented symbols to be classified by machine learning algorithms, then manually editing and finalizing the results with OTA.

This tool started as a proof of concept to understand the requirements for a graphical transcription user interface and initially served to transcribe a small number of ciphertexts with hundreds of symbols each at most. 

It later evolved into a full-fledged application that was required to enable the transcription and decryption of the tens of thousands of graphical symbols contained in a collection in the French National Library. As such, it was significantly redesigned, and modified with the aim of supporting a large-scale transcription and decryption of documents, involving a large number of distinct graphical symbols (150) and total symbols (100,000).

# Installing and Running CTTS

CTTS was developed in Java, using the JavaFX graphical library, JavaFX is an open-source, next-generation client application platform for desktop, mobile, and embedded systems built on Java. It is not intended for web-service development.

To run CTTS, Java Runtime Environment (JRE) must be installed on the computer. It is required to install Java 10 (JRE 18.3) - for convenience, it will be easier to download it from here for Windows, Linux (rpm), Linux (tar), or MacOS. It is not the latest due to JavaFX compatibility issues. To test proper Java installation, open a Command Line window, type java -version. 
The following output should be expected:

![java_version_output.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/java_version_output.png)

The computer should have at least 16GB (32G preferred) of RAM, with a screen supporting at least a resolution of a 2560 x 1600 pixels. First, download the required files (tutorials, and the jar file) from this link. The CTTS is available in Java jar format, named gui.jar (under the Jar Files directory). You may copy this jar file to any directory.

To run CTTS, you need first to create a working directory, containing one or more image files (see next section for requirements on workable image files). Using a command-line session, you should use cd commands, so that this is the current working directory. Note that CTTS will store its working files and backups in that directory and its subdirectories. For example, to start the Tutorial 1 directory:

![java_gui.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/java_gui.png)

**Note to MacOS users:** You may need to copy the file gui.jar into the current directory, then instead, run java -jar gui.jar 

If you have more than one version of Java, you can also specify the full path for the java.exe with the required version, as follows:

![java_full_path.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/java_full_path.png)

You may ignore the “File not found…” and “Could not restore …” messages which indicate that this is a blank project. You will see this (you may press ESC to quit):

![first_start.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/first_start.png)

**General note to MacOS users**: The function keys (e.g., F10) in MacOS are usually assigned to predefined functions like changing volume, luminosity, etc… To apply a function key in CTTS, you must press fn and the function key at the same time, e.g. fn F10. Same applies to the delete key used to delete a symbol, which requires fn delete on MacOS. It is also possible to change the Keyboard settings so that pressing fn is not necessary, but the default is that pressing fn is necessary. In MacOS, F11 is always used - with or without pressing fn - to view the desktop (F11 again returns from the desktop to the last application), so it cannot be used for CTTS. Therefore, CTTS allows tab to be used instead of F11 for MacOS users.

# Preparing Images for Transcription 

Images to be transcribed with CTTS should be either .bmp, .jpg, or .png files. There are a few recommendations on preparing images for transcription, possibly using an image processing tool such as Photoshop (usually, a freeware utility like Photo Filtre should suffice):

*	Since CTTS has no built-in image processing features, images must be in the correct orientation. 
*	You may want to apply basic effects like changing the contrast, brightness, or saturation. More advanced operations like sharpening might be useful, although they may alter the original image so that transcription might be more difficult rather than easier.
*	Images with thousands of symbols are more easily processed if they are cut into parts (in our tutorial, we split the original large f42.jpg image into f42a.jpg, f42b.jpg, etc…).
*	The most critical requirement is making sure that the lines of symbols are more or less horizontally aligned. In the case of an image with inconsistent alignment, splitting into small parts and slightly rotating those smaller partial images with a per image optimal angle is the most effective strategy. 

**Note:** In addition to the ability to rotate pictures by small angles, Photo Filtre (https://www.photofiltre-studio.com/pf7-en.htm) has very handy distort/deform features, that be applied to a whole image (from the top menu: Effect/Deform/Trapezoid), or to an area (select an area, mouse right-click, then select Deform, and stretch selection as desired). However, with the latest versions of CTTS (version 2.2., March 24, 2022, or later), with improved automated line alignment, those complex operations will in most cases not be necessary.

The images in the tutorial have already been prepared for use by CTTS.

# Transcribing Multiple Images 

While is also more convenient to start gui.jar without parameters (java -jar gui.jar, CTTS will search for jpg, bmp, and png files in the current directory, and open them), you can add specific the images in the command line parameters. Using the directory Tutorial 2, we show an example, with 4 images (see the command line to start Tutorial 2):

![multiple_images.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/multiple_images.png)

![multiple_images_gui.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/multiple_images_gui.png)

On the top right-side, [1/4]  indicates that you will be transcribing 4 ciphertext documents with the same types of symbols. Press function F1 to view the next document.

![multiple_images_gui_2.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/multiple_images_gui_2.png)

Press F2 to return to the first document. You are now ready to start transcribing. 

# Transcription 

At any time, you may zoom out or in either with the F5 and F6 keys, with CTRL+<the + key> and  CTRL+<the - key>, or with CTRL +<mousewheel up/down>. To transcribe a symbol, using the mouse, mark the symbol, by pressing the mouse on one corner, and releasing it when on the opposite corner. In the following example, we have multiple instances of a symbol that looks like a “c”. 

![transcription_gui_explanation.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/transcription_gui_explanation.png)

To delete a symbol, click on it to select it, then press the Delete key (in MacOS, fn and delete). To move a symbol, click on its middle, and drag it to the desired position. To resize or change its boundary, hover over the border you wish to move, then drag the border, select a corner, and drag it to extend or reduce the surrounding box.

After (or before) selecting a few instances of the symbol, we may want to give the symbol type some name, and assign some generic icon to represent it. Click on the white rectangle near the zoomed view of the selected symbol, and type in some name or serial number. In this tutorial, we will assign it to 00. Note that as you type the name, the item changes instantly in the list.

