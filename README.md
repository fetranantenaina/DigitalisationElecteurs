# Digitalisation des Électeurs Malagasy

Prototype Android pédagogique réalisé en Kotlin + XML.

## Fonctionnalités

- Création d'une fiche électeur
- Stockage local SQLite
- Liste des électeurs avec ListView + Adapter
- Affichage du détail
- Recherche par identifiant, nom, région, district ou fokontany
- Suppression avec confirmation AlertDialog
- Toast et navigation entre Activity avec Intent
- OptionsMenu : actualiser / à propos
- ContextMenu : afficher / supprimer
- Design personnalisé avec colors.xml et themes.xml

## Lancer le projet

1. Décompresser le ZIP.
2. Ouvrir le dossier `DigitalisationElecteursMalagasy` dans Android Studio.
3. Laisser Gradle synchroniser le projet.
4. Utiliser un émulateur Android ou un téléphone Android.
5. Cliquer sur Run.

Java/JDK recommandé : 17.
Compile SDK : 35.
Minimum Android : 7.0 (API 24).

## Important

Ce projet est un prototype pédagogique. Les données sont personnelles et potentiellement sensibles : pour une utilisation réelle avec des données électorales, il faut prévoir authentification, contrôle d'accès, chiffrement, sauvegardes sécurisées, journalisation, politique de conservation et conformité aux règles applicables.
