# Implementation Notes & Assumptions

Reasonable assumptions made where the Assessment A brief was ambiguous. Each is
noted so it can be defended in the Assessment B report.

1. **Base package name.** The brief used a placeholder `<Reviews & Ratings>`,
   which is not a legal Java package. Implemented as `loop.reviews`
   (`loop.reviews.controller` / `.model` / `.db`, plus a `.util` package).

2. **Database name.** The brief mentioned `velmora.db` "or equivalent" from a
   different project template. This component is LOOP, so the file is `loop.db`.

3. **Disallowed characters.** FR10 lists `@,.#/\?"'~`$`. The commas and the full
   stop in that list are treated as list separators, not blocked characters —
   blocking `.` and `,` would make ordinary review sentences impossible. The
   enforced disallowed set is: `@ # / \ ? " ' ~ \` $`. See `util/Validation.java`.

4. **EditTimeWindow entity.** The class diagram's `EditTimeWindow` (createdAt +
   durationSeconds) is folded into the `reviews` table as `created_at` +
   `edit_duration_seconds` (default 300s / 5 min). `Review.isEditWindowOpen()` and
   `remainingSeconds()` provide the behaviour; a live JavaFX `Timeline` drives the
   on-screen countdown in My Reviews and Edit Review.

5. **RatingDistribution / sentiment.** Computed on demand from the `reviews` table
   (`ReviewDao.ratingDistribution`, `util/SentimentAnalyzer`) rather than stored as
   a separate table, keeping the average always consistent (FR9).

6. **User inheritance.** The `User → Customer/Admin` generalisation is represented
   by a `role` column (`CUSTOMER`/`ADMIN`); thin `Customer`/`Admin` subclasses are
   included for design fidelity.

7. **Screens vs. modals.** The original prototype's modals (Submit, Edit) are
   implemented as full screens because navigation uses a single `Stage` with
   `SceneManager` swapping FXML roots, mirroring the prototype's screen switching.

8. **Currency symbol.** Prices are displayed with `£` and built in Java (not FXML
   literals) to avoid the FXMLLoader `$` expression pitfall. Any literal `$` in
   FXML would be escaped as `$$`; the automated check confirms none are present.

9. **Image upload.** Stored as a file path (`image_url`) chosen via `FileChooser`;
   the image is referenced, not copied into the database.

## Changes vs. Assessment A design

* Password hashing was **not** implemented — passwords are stored/compared in
  plain text for this coursework prototype (noted as a limitation).
* Sentiment analysis is a lightweight keyword lexicon rather than an ML model
  (it was an optional enhancement in the design).
* Moderation "notify customer" is represented by the logged audit entry rather
  than a separate notification channel.

## Verification performed

* All 8 FXML screens validated as well-formed XML.
* Automated cross-check: every `fx:id` ↔ `@FXML` field, every `onAction` ↔
  controller method, every `SceneManager.switchTo(...)` target ↔ an existing
  FXML, and no unescaped `$` in FXML.
* All 29 Java sources parsed with a Java parser (no syntax errors).
* NOTE: a full `mvn compile` could not be run in the build sandbox because it has
  no access to Maven Central to download JavaFX/SQLite jars. Compilation and
  `javafx:run` should be performed in Eclipse, where Maven resolves the
  dependencies declared in `pom.xml`.
