#  Matonique

Matonique est une application de lecture de musique !!

## Tests

Comande pour  lancer les tests avec un rapport de couverture :

`./gradlew testDebugUnitTest jacocoTestReport`
On uttilise le plugin Jacoco pour generer ce rapport

Le rapport  generé se trouve dans le dossier `/app/build/reports/jacoco`

importer les assets dans votre emulateur
`adb push ./app/src/androidTest/assets/* /sdcard/Music/`