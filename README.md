# Programmieraufgabe - Repository Vorlage
Im Verzeichnis src/ sind alle Quelltextdateien und Pakete abzulegen.
Elemente außerhalb des src/ Verzeichnisses werden nicht kompiliert und folglich nicht berücksichtigt.

## Tokenization-Befehl
Der Sequenzabgleich unterstützt nun den Befehl `tokenization <id> <strategy>`, um den in der
Anwendung gespeicherten Text unter der Kennung `<id>` zu zerlegen. Das Ergebnis wird als
`~`-separierte Liste von Token ausgegeben.

Folgende Strategien stehen zur Auswahl:

* `CHAR`: jeder Unicode-Codepunkt wird als einzelnes Token zurückgegeben.
* `WORD`: trennt anhand von Leerraum. Satzzeichen bleiben an den Wörtern haften.
* `SMART`: trennt anhand von Leerraum und gibt Satzzeichen (mit Ausnahme von Apostroph und Bindestrich
  zwischen alphanumerischen Zeichen) als eigene Token zurück.

## Analyze-Befehl
Mit dem Befehl `analyze <strategy> <minMatchLength>` werden alle aktuell geladenen Texte mit der
gewählten Tokenisierungsstrategie verglichen. Für jedes Textpaar werden dabei nur zusammenhängende
Übereinstimmungen berücksichtigt, deren Länge mindestens `minMatchLength` Token beträgt. Nach
Abschluss der Analyse gibt die Anwendung die benötigte Zeit im Format `Analysis took <dur> ms`
aus und stellt das Ergebnis für weitere Befehle bereit.
