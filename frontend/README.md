# Frontend

## Entwicklung
Um alle Abhängigkeiten zu installieren, muss vor der Entwicklung folgender Befehl ausgeführt werden:
```bash
npm i
```
Falls `npm` noch nicht installiert ist, dann am besten einen NVM installieren und dort drüber `npm` installieren.

Anschliessend kann das Projekt gestartet werden
```bash
npm start
```
Das Projekt startet auf Port `localhost:3000`. Der Browser wird beim Speichern von Änderungen im Code jeweils refreshed.



## Deployment
Für Deployment mit UNIX, iOS auf das Spring Boot Projekt:
```bash
npm run clean-deploy
```
Für Deployment mit Windows auf das Spring Boot Projekt:
```bash
npm run clean-deploy-windows
```

Danach Spring Boot neu starten.