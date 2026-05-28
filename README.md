# 🌙 CRT Megrine — Système de Gestion
**Croissant-Rouge Tunisien — Délégation de Megrine**

Plateforme web de gestion des bénévoles, dons et événements humanitaires.

---

## 🚀 Lancement rapide

### Prérequis
- **Java 17** ou supérieur ([télécharger](https://adoptium.net/))
- **Maven 3.8+** OU utiliser le wrapper inclus (`mvnw`)

### Vérifier Java
```bash
java -version
```
Doit afficher : `java version "17.x.x"` ou supérieur.

---

## ▶️ Démarrer l'application

### Option 1 — Windows (PowerShell ou CMD)
```cmd
cd megrine
mvnw.cmd spring-boot:run
```

### Option 2 — Linux / macOS (Terminal)
```bash
cd megrine
chmod +x mvnw
./mvnw spring-boot:run
```

### Option 3 — Avec Maven installé globalement
```bash
mvn spring-boot:run
```

---

## 🌐 Accéder à l'application

Ouvrez votre navigateur à l'adresse :
```
http://localhost:8080
```

### Comptes de démonstration
| Utilisateur | Mot de passe | Rôle |
|-------------|-------------|------|
| `admin`     | `admin123`  | Administrateur |
| `user`      | `user123`   | Utilisateur |

---

## 📋 Fonctionnalités

- **Tableau de bord** — Statistiques en temps réel
- **Bénévoles** — Gestion complète (ajout, modification, suppression, recherche)
- **Dons & Aides** — Suivi des donations monétaires et en nature
- **Événements** — Planification des activités humanitaires

---

## 🛠️ Technologie

| Composant | Technologie |
|-----------|-------------|
| Backend | Spring Boot 3.2.5 |
| Sécurité | Spring Security |
| Base de données | H2 (en mémoire, redémarre à chaque lancement) |
| Templates | Thymeleaf |
| Build | Maven |

> ⚠️ Les données sont **réinitialisées** à chaque redémarrage (base H2 en mémoire).
> Pour persister les données, configurez MySQL dans `application.properties`.

---

## 🗄️ Console H2 (Base de données)

Accessible à : `http://localhost:8080/h2-console`
- JDBC URL : `jdbc:h2:mem:megrine`
- Username : `sa`
- Password : *(vide)*

