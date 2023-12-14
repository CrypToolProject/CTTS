# CTTS: CrypTool Transcriber &amp; Solver 

CrypTool Transcriber &amp; Solver is a ciphertext transcription and cryptanalysis tool initially developed by George Lasry and now maintained by the CrypTool team.
The tool is written in Java; the user interface is built using JavaFx.

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
