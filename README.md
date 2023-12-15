# 📜 CTTS: CrypTool Transcriber &amp; Solver 

CrypTool Transcriber &amp; Solver is a ciphertext transcription and cryptanalysis tool initially developed by George Lasry and now maintained by the CrypTool team.
The tool is written in Java; the user interface is built using JavaFx. It was formerly known as OTA (Offline Transcription Application).

The following documentation is based on the "May 10, 2022 Version 3.2" OTA documentation written initially by George Lasry. It was converted from Word format to this document here, to have a single online version of the documentation.

The authors hopes that this document and the associated tutorials will be helpful to the reader, and especially, that CTTS is effective for their own research.

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

# 💾 Installing and Running CTTS

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

# 👍📃 Preparing Images for Transcription 

Images to be transcribed with CTTS should be either .bmp, .jpg, or .png files. There are a few recommendations on preparing images for transcription, possibly using an image processing tool such as Photoshop (usually, a freeware utility like Photo Filtre should suffice):

*	Since CTTS has no built-in image processing features, images must be in the correct orientation. 
*	You may want to apply basic effects like changing the contrast, brightness, or saturation. More advanced operations like sharpening might be useful, although they may alter the original image so that transcription might be more difficult rather than easier.
*	Images with thousands of symbols are more easily processed if they are cut into parts (in our tutorial, we split the original large f42.jpg image into f42a.jpg, f42b.jpg, etc…).
*	The most critical requirement is making sure that the lines of symbols are more or less horizontally aligned. In the case of an image with inconsistent alignment, splitting into small parts and slightly rotating those smaller partial images with a per image optimal angle is the most effective strategy. 

**Note:** In addition to the ability to rotate pictures by small angles, Photo Filtre (https://www.photofiltre-studio.com/pf7-en.htm) has very handy distort/deform features, that be applied to a whole image (from the top menu: Effect/Deform/Trapezoid), or to an area (select an area, mouse right-click, then select Deform, and stretch selection as desired). However, with the latest versions of CTTS (version 2.2., March 24, 2022, or later), with improved automated line alignment, those complex operations will in most cases not be necessary.

The images in the tutorial have already been prepared for use by CTTS.

# 📃📃📃 Transcribing Multiple Images 

While is also more convenient to start gui.jar without parameters (java -jar gui.jar, CTTS will search for jpg, bmp, and png files in the current directory, and open them), you can add specific the images in the command line parameters. Using the directory Tutorial 2, we show an example, with 4 images (see the command line to start Tutorial 2):

![multiple_images.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/multiple_images.png)

![multiple_images_gui.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/multiple_images_gui.png)

On the top right-side, [1/4]  indicates that you will be transcribing 4 ciphertext documents with the same types of symbols. Press function F1 to view the next document.

![multiple_images_gui_2.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/multiple_images_gui_2.png)

Press F2 to return to the first document. You are now ready to start transcribing. 

# 📝 Transcription 

At any time, you may zoom out or in either with the F5 and F6 keys, with CTRL+<the + key> and  CTRL+<the - key>, or with CTRL +<mousewheel up/down>. To transcribe a symbol, using the mouse, mark the symbol, by pressing the mouse on one corner, and releasing it when on the opposite corner. In the following example, we have multiple instances of a symbol that looks like a “c”. 

![transcription_gui_explanation.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/transcription_gui_explanation.png)

To delete a symbol, click on it to select it, then press the Delete key (in MacOS, fn and delete). To move a symbol, click on its middle, and drag it to the desired position. To resize or change its boundary, hover over the border you wish to move, then drag the border, select a corner, and drag it to extend or reduce the surrounding box.

After (or before) selecting a few instances of the symbol, we may want to give the symbol type some name, and assign some generic icon to represent it. Click on the white rectangle near the zoomed view of the selected symbol, and type in some name or serial number. In this tutorial, we will assign it to 00. Note that as you type the name, the item changes instantly in the list.

![transcription_gui_explanation2.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/transcription_gui_explanation2.png)

We can also assign a generic icon to this symbol type, by importing an icon file (Import Icon), or saving (Save as Icon) the selected (zoomed) symbol from the document as the icon, after choosing one which looks generic enough. Basic image processing is then applied to it. 

![transcription_gui_explanation3.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/transcription_gui_explanation3.png)

The icons are stored under the icons/ directory. To use an icon from an existing set of icons, use Import Icon. You are now ready to mark instances of a new type of symbol. First, select the desired color from the list (it is recommended to scroll down the list, and select a non-similar color). 

# 🔍 Editing Symbol Assignment 

You have learned how to mark symbols on a document and assign them some names and icons.  We will now show how to change those assignments. Start the tutorial in directory Tutorial 3. 

![symbol_assignment_console.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/symbol_assignment_console.png)

![symbol_assignment_gui.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/symbol_assignment_gui.png)

It is easy to observe that one of the symbols under 02 actually belongs to the 00 symbol type. To fix the error, select it, and simply drag it from the bottom pane (not from the document image! - this will only move the marker), into the correct line on the left list. 

![symbol_assignment_gui_explanation.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/symbol_assignment_gui_explanation.png)

After fixing the problem:

![symbol_assignment_gui_fixed_problem.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/symbol_assignment_gui_fixed_problem.png)

# 🔍 Using the Symbol Types Views

Start the tutorial in the Tutorial 4 directory. It contains 4 image documents, fully transcribed.:

![using_symbol_types_console.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/using_symbol_types_console.png)

![using_symbol_types_gui.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/using_symbol_types_gui.png)

There are multiple errors in the transcription that we will need to find and fix in an efficient manner. For that purpose, we shall be using the Symbol Types views. The Symbol Types views can be reached by pressing F11 or tab (pressing again F11 or tab will move back to Transcription). Note: On MacOS, only tab works, as F11 is reserved. The default view is the Symbol Types - List View, which conveniently shows some of the symbols from the documents currently associated with each type.

We now select the symbol 01 in the list. It is evident that several symbols do not belong here.

![using_symbol_types_correct.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/using_symbol_types_correct.png)

It is possible to select and drag each one into the correct type (05), one by one. But there is a more efficient way to do that.

You can select them, one-by-one, then drag the whole selection together into the correct symbol type, a much more efficient process.

![using_symbol_types_multiple_drag.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/using_symbol_types_multiple_drag.png)

The  Symbol Types - List View is most convenient when there is only a small number of distinct symbols types (a few tens). For larger numbers of types, the  Symbol Types - Grid View might be more convenient, especially for the case in the tutorial, where we have 150 distinct symbol types. You can move to the Symbol Types - Grid View by pressing F12.

![using_symbol_types_grid_view.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/using_symbol_types_grid_view.png)

In the Symbol Types - Grid View you can select, drag & drop, one or more symbols from the bottom part, as in the Symbol Types - List View. You can get back to Symbol Types - List View by pressing F12 again. To get back to Transcription (from any of those views), press F11 or tab (in MacOS, only tab). It is possible to change the order of the symbols in Symbol Types - Grid View (this also affect their order in the left list, shown in various modes). With F1, you can sort by name (as show above). With F2, you can sort by decryption value - single letters are always shown first (see left image). With F3, you can sort by frequency (the more frequent are shown at the beginning - see image at the right, after pressing F12 to get back to Symbol Types - List View. As expected, it can be seen that E, S, and R (encoded with 01, 02, 04, respectively) are the most frequent.

![using_symbol_types_grid_and_list_view.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/using_symbol_types_grid_and_list_view.png)

In addition to those simple sorting methods, it is possible to place a symbol anywhere on the grid view. Click on symbol, drag it, and drop it to the desired place - the ones right after will be shift by one place: For example, we want to move symbol “c.” (27 = ION) near “c” (01 = E), there are two options:
  a)	Insert: Click on 27 = ION with left mouse button, drag to 02 = Y, and drop it. The symbols right after will be shifted by one.
  b)	Swap: Click on 27 = ION with right mouse button, drag to 02 = Y, and drop it. 27 = ION has exchanged their positions (not change in the other symbols).

![using_symbol_types_place_a_symbol.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/using_symbol_types_place_a_symbol.png)

The ability to freely position the symbols is useful, for example, if you want to group together similar looking symbols. 

# 🔍 Reviewing the Transcription

Another method, primarily visual, to review the correctness of the transcription is to use the Transcription Review window. It can be reached from the Transcription view, by pressing F12 (pressing F12 again will get back to Transcription).

![reviewing_transcription_dragging_symbol.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/reviewing_transcription_dragging_symbol.png)

This view is also helpful in case a symbol was wrongly assigned, because it was not properly marked, and some parts were not included, as shown below:

![reviewing_transcription_see_problem.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/reviewing_transcription_see_problem.png)

To fix the problem:
  1)	Select the symbol
  2)	Drag it into the correct symbol type.
  3)	Press F12 to return to Transcription. The relevant symbol will be shown as selected, and you may simply resize it.

![reviewing_transcription_resize_rectangle.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/reviewing_transcription_resize_rectangle.png)

# 💾 Saving Transcription Results

All files are automatically saved upon exiting with ESC. You can also manually save by pressing F10 or CTRL+S from any mode or view. To exit without saving, press the X on the top-right corner. In MacOS, the close button is usually on the right top corner.

The results are saved in the working directory, and in subdirectories:

The icons\ directory contains the icon files, before image processing.

![saving_transcription_results_icons.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/saving_transcription_results_icons.png)

The positions\ directory contains a text file per document (e.g., f42a_positions.txt), with the position and dimensions (x,y, w, h) of the rectangles surrounding the marked symbols, as well as a number indicating the symbol type they belong to (those are not the same as the numbers used for naming the symbol types - rather, those numbers are fixed, and won’t change if the name of a symbol type is changed). Those files are intended to serve as machine learning ground truths for training and validation.

![saving_transcription_results_positions.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/saving_transcription_results_positions.png)

The snapshots\ directory contains the various snapshots manually or automatically generated. More on snapshots in another section. By default, a snapshot of the symbol types- similar to the Symbol Types - Grid View is automatically generated (key.png) when saving.

![saving_transcription_results_snapshot_grid.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/saving_transcription_results_snapshot_grid.png)

The transcription\ directory contains text files with the transcriptions of each document (e.g., f42a.txt), and another all.txt with all the transcriptions together.

![saving_transcription_results_transcription_text.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/saving_transcription_results_transcription_text.png)

The names of the symbols are separated with ;, and therefore, ; may not be a symbol name or part of a symbol type name. Those files are intended to serve as input for computerized cryptanalysis or decryption.

The working directory includes multiples backup copies for the symbols, in the form of binary files (*_positions, *_positions_SECOND_COPY), as well as for the symbol types (color* - yes, a bad name for symbol types :-)). Also, a negative version of each image is pre-generated, to save processing time. 

# 🔑 Using a Decryption Key

Following cryptanalysis (externally, using the transcription output files, or with built-in cryptanalysis), or using a key obtained from original historical documents, it is possible to further review and improve the transcription of the documents. This is also a critical step, in order to obtain a clean-enough transcription, and an accurate-enough key, necessary to decipher a historical text so that it is readable and usable for historical research. The key file must follow the guidelines set by DECRYPT (https://cl.lingfil.uu.se/~bea/publ/transcription-guidelines-v2.pdf). For example (metadata lines starting with # are ignored):

![using_a_decryption_key_example.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/using_a_decryption_key_example.png)

To use a key file, add -k key.txt to the command line - for example (assuming the file gui.jar is in the current directory):
java -jar gui.jar f42a.jpg f42b.jpg f42c.jpg f42d.jpg -k key.txt 
It is also possible to start CTTS without any parameters (java -jar gui.jar), in which case, CTTS will look for a key.txt file in the current directory, in addition to image files. 

New in version 3.0 and above: It is now possible to modify the key and edit the decryption value of symbol types. 
The decryption values from the key file are reflected in all the views.

![using_a_decryption_key_gui_description.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/using_a_decryption_key_gui_description.png)

The most important use of information from a key file is in the Transcription Review window. With all the elements being available in one place - the original image segments, the selected symbol, the symbol type icon, and the decryption value, as well as tentative decryption, it is possible to significantly improve the accuracy of the transcription, and validate/improve the key, with the end goal of producing a readable decryption.

![using_a_decryption_key_gui_description2.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/using_a_decryption_key_gui_description2.png)

# 📊 New in Version 3.0: Built-in Cryptanalysis

To run this example, copy the file key.txt to keyKeep.txt, then delete key.txt, and restart CTTS without parameters. Following transcription, it is possible to recover an initial key, using the built-in Cryptanalysis function, which can be started with F8 from any mode. First, enter the parameters as described below. 

![built_in_cryptanalysis_gui_description.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/built_in_cryptanalysis_gui_description.png)

When the parameters are ready, press Start Cryptanalysis. In this case, the number of remaining distinct ciphertext symbols to be assigned (76) is higher than the number of homophones that may be assigned (68). 

![built_in_cryptanalysis_too_many_ciphertext_symbols.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/built_in_cryptanalysis_too_many_ciphertext_symbols.png)

To fix the problem, we set a higher count threshold for a ciphertext symbol type to be considered (10). Still, 93% of the transcribed symbols will be processed. 

![built_in_cryptanalysis_higher_threshold.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/built_in_cryptanalysis_higher_threshold.png)

We press again Start Cryptanalysis.

![built_in_cryptanalysis_running_analysis.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/built_in_cryptanalysis_running_analysis.png)

To retain the recovered key, press Save Key. This will provide with a initial key, which does not cover the nomenclature, and some homophones are likely to be wrong, mostly because we don’t know yet which symbols types are nomenclature/nulls. If we know them, we can give them some transcription value starting with _, e.g., _100, so that they are ignored in cryptanalysis. Typically, the next step after cryptanalysis and saving the key is to go the Transcription Review mode, and improve/edit the key iteratively. Initially, decryption values from cryptanalysis are in lower case. 

When some words can be read clearly, the relevant decrypted values of the homophone symbols (symbols representing a single letter) can be changed to capital letters, and in the example below, the letters that form the word COVRIR = COURIR - to run, or by selecting Locked. It is possible to run cryptanalysis again, and the algorithm will not try to change the decrypted value of locked homophones. After confirming/fixing the decryption value of more homophones, it becomes possible to identify nomenclature symbols that represent short word/suffixes/prefixes. In addition, any symbol with a name starting with _ (e.g., _, or _122, _clear_), or a decrypted value starting with _, will be ignored and treated as a null/word separator.
After a few iterations, it is possible to read additional parts of the deciphered text, and try to deduce how the nomenclature looks like, and assign some nomenclature entries (e.g. short words, places, names) to some of the code values.

![built_in_cryptanalysis_lock_homophones.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/built_in_cryptanalysis_lock_homophones.png)

# 📷 Snapshots

A snapshot of the current mode or view can be generated, by pressing F7. Those snapshots can be used to share results with collaborators that do not have access to CTTS. These include:
  1)	A snapshot of the Symbol Types  including the decryption values
  2)	A snapshot of the  symbols marked on the image - Transcription mode
  3)	A snapshot of the Transcription Review window.

![snapshots_examples.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/snapshots_examples.png)

  4)	A snapshot of the decryption key (if available)

![snapshots_decryption_key.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/snapshots_decryption_key.png)

  5) A snapshot of the decryption on top of the image (first, press F9 twice while in the Transcription window)

![snapshot_decryption_image.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/snapshot_decryption_image.png)

# 🌞 Tips

Transcribing documents with a large number of symbols

Using CTTS, there are multiple possible strategies to transcribe a set of documents with a large number of symbols, e.g. more than 500 symbols (for simplicity, we assume that there is already a list of symbol types, with names and icons):

1)	Symbol-by-symbol segmentation and classification: For each symbol on the image, select the relevant symbol type (classification), then mark the symbol on the document (segmentation). 
2)	Segmentation and classification by symbol type: Select a symbol type, then mark all the matching symbols on the document.
3)	Segmentation, then classification: Using the Transcription mode, select a default (e.g., new 1000) symbol type, then mark all the symbols (of any type) on the document. When a few hundred of such unclassified symbols have been marked, using one of the

Symbol Types views, for each symbol type, select all the relevant symbols (currently assigned to 1000, then perform a bulk drag-and-drop operation, to correctly (re)classify them.

Option (1) is only applicable to documents with a small number of symbols, as for each symbol, you need to (a) click on the relevant symbol type from the list (b) mark the symbol on the document. With options (2) and (3), you will still need to mark the symbols individually, but the classification is done only once per symbol type. 

The advantage of (2) over (3) is that with (3), you will need an additional click on each symbol, to select it, before the bulk drag-and-drop operation. On the other hand, segmenting all the symbols sequentially, rather than marking only specific ones, can be done much more quickly, without the risk (in option (2)) of forgetting some symbols of a certain type, while (2) also requires multiple visual inspections of the same document, per symbol type). 

The author has experimented with all methods and found that working according to (3) and each time segmenting about 10-15 lines (or 500 symbols), then classifying them with bulk operations, is often the most efficient way to make quick progress. A fourth option that worked well is to divide the symbol types into a small number of categories (e.g. symbols with a dot vs. symbols without a dot), first segmenting all the symbols of one category, then continuing with the next category, with a different color, and so on.

# 📐 Transcribing documents with inconsistent or poor line alignment

CTTS has an internal line segmentation algorithm, to generate the transcription text files, with the symbols in the right order (line after line, from left to right), and to render the Transcription Review window, which presents line-by-line comprehensive information. For both purposes, CTTS needs to figure out the order of the symbols, and how they should be divided into lines. Sometimes, symbols are so badly unaligned in the image that CTTS may produce inaccurate results (with the March 24, 2022 version 2.2 of CTTS, this is less likely to happen, however). In order to verify the correct alignment of the symbols, press F9 from the Transcription mode, as shown below. To fix any alignment issue, you may need to move or stretch the boxes around those outlier symbols, closer to other symbols in the desired line,  after press F9 again to return to normal Transcription mode.

![line_segmentation_visualization.png](https://github.com/CrypToolProject/CTTS/blob/main/documentation/images/line_segmentation_visualization.png)
