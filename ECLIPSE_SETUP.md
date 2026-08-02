# Editing the complete LOOP project in Eclipse

## Requirements

- Eclipse IDE for Java Developers, with Maven integration (m2e).
- JDK 21 installed and selected in Eclipse.
- Git installed, or Eclipse EGit.

## Import the complete workspace

1. Clone or pull the repository:

   ```powershell
   git clone https://github.com/kyawminkhant/loop-gp-A.git
   cd loop-gp-A
   ```

2. Open Eclipse and choose a new Eclipse workspace outside the repository. Do not select the repository itself as the Eclipse workspace folder.
3. Select **File > Import**.
4. Select **Maven > Existing Maven Projects**, then click **Next**.
5. For **Root Directory**, select the cloned `loop-gp-A` folder.
6. Eclipse should find the root `pom.xml` and all seven module `pom.xml` files. Keep all eight projects selected and click **Finish**.
7. Wait for Maven dependency downloads and background builds to finish.
8. Select all imported projects, right-click, and choose **Maven > Update Project**. Enable **Force Update of Snapshots/Releases**, then click **OK**.

The Package Explorer will show the aggregator project `loop-workspace` plus the Customer, Orders, Delivery, Inventory, Reviews, Finance, and Product projects.

## Configure Java 21

1. Open **Window > Preferences > Java > Installed JREs**.
2. Add your JDK 21 directory if it is not listed.
3. Tick JDK 21 to make it the default.
4. Open **Java > Compiler** and select compiler compliance level **21**.
5. Run **Maven > Update Project** again if Eclipse previously showed JRE errors.

Some modules compile to Java 11 or 17 bytecode through their Maven settings. JDK 21 can compile all of them.

## Run the integrated Team Hub in Eclipse

### Recommended: Maven launch

1. In Package Explorer, expand `loop-workspace > eclipse`.
2. Right-click `LOOP Team Hub.launch`.
3. Select **Run As > LOOP Team Hub**. If Eclipse instead shows **Run Configurations**, open it and click **Run**.

Alternative:

1. Right-click the `loop-workspace` project and select **Run As > Maven build...**.
2. Enter this in **Goals**:

   ```text
   -f modules/product/pom.xml javafx:run
   ```

3. Set **Base directory** to the repository root, then click **Run**.

The Team Hub opens first. Its buttons replace the current page with the selected module instead of opening another window. Maven workspace resolution links the independently editable projects, and every module uses the shared `database/loop.db`.

## Run an individual module

Right-click its `pom.xml`, select **Run As > Maven build...**, and enter `javafx:run` as the goal. Default starts are:

- Customer: Login
- Orders: Orders
- Delivery: Admin workspace
- Inventory: Dashboard
- Reviews: Customer view
- Finance: Finance Dashboard
- Product: Team Hub

For a role-specific start, add a Maven property before the goal. Examples:

```text
-Dloop.start=driver javafx:run
-Dloop.start=admin javafx:run
-Dloop.start=customer javafx:run
```

## Editing rules

- Edit Java under each module's `src/main/java` folder.
- Edit JavaFX layouts and styles under `src/main/resources`.
- Customer uses its original `src` layout, which its Maven configuration already supports.
- Keep database changes in the root `database/loop.db` and synchronize `database/loop.sql` before committing.
- Do not create a second database inside a module folder.
- Do not commit Eclipse-generated `.project`, `.classpath`, `.settings`, `bin`, or `target` files; `.gitignore` excludes them.

## Commit and push from Eclipse

1. Open **Window > Show View > Other > Git > Git Staging**.
2. Review **Unstaged Changes** carefully.
3. Stage only your module files.
4. Enter a clear commit message and click **Commit and Push**.
5. Push to your assigned `module/...` branch and open a pull request into `main`.

## Fix common Eclipse problems

### Red errors after import

Use **Maven > Update Project**, then **Project > Clean**. Confirm JDK 21 is selected.

### JavaFX classes cannot be resolved

Do not add JavaFX JARs manually. Update the Maven project so Eclipse reloads the dependencies from `pom.xml`.

### Duplicate project name

Remove the old project from the Eclipse workspace without deleting files, then import again using **Maven > Existing Maven Projects**.

### Hub opens but a module does not

Select all imported projects and run **Maven > Update Project** with **Force Update of Snapshots/Releases** enabled. Confirm all eight Maven projects are open, **Resolve Workspace artifacts** is enabled in the Team Hub launch configuration, dependency downloads have completed, and the root `database/loop.db` exists.
