# LOOP — Reviews & Ratings Component (JavaFX + SQLite)

NC1605 Group Project · Assessment B implementation of the **Reviews & Ratings**
component for the LOOP food-delivery platform. Rebuilt as a runnable JavaFX
(FXML + controllers) Maven project backed by a SQLite database.

---

## 1. Requirements

* **JDK 11 or newer** (the project targets Java 11; JavaFX 13 needs JDK 11+).
* **Maven** (bundled "Embedded" Maven inside Eclipse is fine).
* Eclipse with the **m2e (Maven)** plugin — standard in "Eclipse for Java Developers".

Dependencies (JavaFX 13, SQLite JDBC) are declared in `pom.xml` and are
downloaded automatically by Maven on first build — you do **not** install
JavaFX separately.

---

## 2. Import into Eclipse

1. `File > Import...`
2. Choose **Maven > Existing Maven Projects**, click **Next**.
3. **Root Directory** → **Browse...** → select this `loop-reviews` folder
   (the one containing `pom.xml`).
4. Ensure `/pom.xml` is ticked, click **Finish**.
5. Wait for Eclipse to resolve dependencies (bottom-right progress bar).

---

## 3. Run it

Run through the JavaFX Maven plugin (this sets the JavaFX module path for you):

1. Right-click the project → **Run As > Maven build...** (the one with the `...`).
2. In **Goals**, type:  `clean javafx:run`
3. Click **Run**.

After the first run you can just use **Run As > Maven build** and it will reuse
`clean javafx:run`.

From a terminal the equivalent is:

```
mvn clean javafx:run
```

### Demo logins (seeded on first run)

| Role     | Email             | Password   |
|----------|-------------------|------------|
| Admin    | admin@loop.com    | admin123   |
| Customer | tasmia@loop.com   | password1  |
| Customer | daniel@loop.com   | password2  |
| Customer | priya@loop.com    | password3  |

Customers can browse dishes they've purchased, submit/edit/delete reviews
(within the 5-minute window), vote helpful/unhelpful, and view analytics.
The admin account opens the moderation dashboard.

---

## 4. Inspect the database (DB Browser for SQLite)

On startup the app prints the **absolute path** of the database file to the
console, e.g.:

```
========================================================
 LOOP Reviews - SQLite database file:
   /Users/you/.../loop-reviews/loop.db
========================================================
```

1. Open **DB Browser for SQLite**.
2. **Open Database** → paste/navigate to that `loop.db` path.
3. Use the **Browse Data** tab to view the `users`, `products`, `orders`,
   `reviews`, `helpful_votes` and `admin_moderation_log` tables.
4. Changes made in the app persist — reopen `loop.db` (or hit refresh) to see them.

The file is created in the working directory the app is launched from (the
project root when run via Eclipse/Maven). Delete `loop.db` to reset to seed data.

---

## 5. Project structure

```
loop-reviews/
├─ pom.xml                         JavaFX 13 + sqlite-jdbc, javafx-maven-plugin
└─ src/main/
   ├─ java/loop/reviews/
   │  ├─ App.java                  Application entry, global exception handler
   │  ├─ SceneManager.java         Swaps FXML roots on one Stage
   │  ├─ Session.java              Logged-in user + selected product/review
   │  ├─ model/                    User, Customer, Admin, Product, Order,
   │  │                            Review, HelpfulVote, ModerationLog
   │  ├─ db/                       Database + one DAO per table
   │  ├─ controller/               One controller per FXML screen
   │  └─ util/                     Validation, SentimentAnalyzer, Toast
   └─ resources/
      ├─ fxml/                     login, home, product_reviews, submit_review,
      │                            my_reviews, edit_review, admin_moderation, analytics
      └─ css/style.css             Shared cream-and-green LOOP theme
```

## 6. Functional requirement coverage

FR1 login (role-based) · FR2 browse purchased products · FR3 submit review
(rating + comment + optional image) · FR4/FR5 edit/delete within live countdown
window · FR6 view/sort/filter/keyword-search · FR7 one helpful/unhelpful vote per
customer · FR8 admin flag/edit/remove/restore with audit log · FR9 average rating
(nearest 0.1) + distribution + sentiment · FR10 duplicate prevention + disallowed
character validation.

See `IMPLEMENTATION_NOTES.md` for design assumptions and Assessment-A deltas.
