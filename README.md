#Heterogenous Computing: Übung 2

Alle Implementierungen führen den DFT Blockweise mit einem bestimmten Versatz aus. Die Magnituden der Frequenzbereiche 

Standard Parameter:\
BlockSize: 512\
Shift: 64\
Threshold: 0.1

# CPU Lösung
Einfach src/main starten. Führt sowohl sequentiellen als auch cpu-parallelen DFT aus, misst Ausführungszeiten und 
Speedup-Faktor und schreibt die Ergebnisse der Analyse in eine .txt Datei in den Ordner Data (zum Plotten).

# CUDA Lösung
Vielleicht muss CUDA installiert sein. Ich verwende CUDA v.12.5 und eine NVIDIA GeForce GTX 1650.

1. In Konsole nach src navigieren
2. Kompilieren\
nvcc -o cfft FFT.cu\
3. Ausführen:\
cfft.exe *FileName* \
z.B. cfft.exe monotone_f210_10sec.wav
Von mir kompilierte .exe ist auch im Git

# Wav File Creator:
In jutil/WavFileFactory.java ist eine main Methode in der monotone Wav Files erzeugt und in Ordner SoundFiles gespeichert werden.

Zu übergebene Parameter sind Frequenz und und Dauer in Sekunden. 

# Python Plotter
Zusätzlich gibt es ein Python script dass einfach das Ergebnis der Fourier Transofrmation ausgibt.