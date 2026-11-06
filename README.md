# Tables Express

Application pour apprendre les tables de multiplication de 0 à 11. L'enfant choisit son pseudo et ses tables, puis répond **à voix haute** en moins de 3 secondes :

| Temps de réponse | Points |
|---|---|
| avant 1,3 seconde | 3 |
| avant 2,2 secondes | 2 |
| avant 3,2 secondes | 1 |
| erreur ou plus de 3,2 secondes | 0 |

Un compteur 0, 1, 2, 3 s'affiche sous le calcul. Dans l'appli Android, le temps compte à partir du moment où l'enfant commence à parler. Le journal du micro, en fin de partie, aide à comprendre une réponse perdue.

Un mode clavier est prévu si le micro n'est pas disponible.

## Installer l'application Android

1. Ouvre l'onglet **Releases** du dépôt (colonne de droite sur la page d'accueil).
2. Télécharge `tables-express.apk` depuis le téléphone.
3. Ouvre le fichier et autorise l'installation d'applications depuis le navigateur si Android le demande.

## Version web

Elle est en ligne sur GitHub Pages : `https://etien278.github.io/tables-express/`. Dans Chrome, le bouton « Installer l'application » l'ajoute à l'écran d'accueil.

## Organisation

- `www/` : l'application (une seule page HTML, plus le manifeste, le service worker et les icônes)
- `android/` : le projet Android (Capacitor) qui emballe `www/` avec la reconnaissance vocale native du téléphone
- `.github/workflows/android.yml` : construit l'APK à chaque modification et le publie dans Releases
- `.github/workflows/pages.yml` : met la version web en ligne
