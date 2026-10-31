# DIN NATO: Progress Tracker

**Last updated:** 2026-10-29
**Current step:** Milestone complete (Steps 2 to 7). Next: upload to GitHub as a backup.
**Honest status:** Users, items, and claims are all saved in MySQL (verified: records survive an app restart; claim create, approve, and reject each saved correctly). ISSUE-06 is fixed: Found items can only become Pending Claim or Claimed through the Claims screen (verified). Database errors now show a clear dialog (startup check and a global handler in `Main`). Approve and Reject now ask for confirmation. The core database work (Steps 2 to 6) is done and verified, and Step 7 (testing, cleanup, README) is finished. The deferred features listed in the Issues log are not built.

**Legend:** [x] verified | [~] exists in code, reviewed but not tested by us | [ ] not done

---

## Big picture

```
GUI (done)  ->  Services (in-memory, must change)  ->  JDBC (Step 2)  ->  MySQL (done)
```

The GUI never touches data directly. It only calls the three service classes. So the database work happens **inside the services**, and the GUI files should not need changes.

---

## Completed

### Environment
- [x] MySQL installed and running (MySQL Workbench connects to `Local instance MySQL80`)
- [x] Java JDK installed (`java 26.0.2`, `javac 26.0.2`)
- [x] VS Code Java extensions working (status bar shows "Java: Ready")

### Database
- [x] `din_nato` database created (visible in Workbench SCHEMAS)
- [x] Tables created: `users`, `items`, `claims` (Output panel showed green checks)
- [x] Script saved as `database/din_nato.sql`

### GUI (written by you; behavior not re-tested by us)
- [~] `Main.java`, `LoginFrame`, `DashboardFrame`, `ItemsPanel`, `LostItemsPanel`, `FoundItemsPanel`, `ItemFormDialog`, `ClaimsPanel`, `UsersPanel`
- [~] Navy sidebar, white workspace, summary cards, status colors

### OOP concepts (reviewed in code; demo not yet rehearsed)
- [~] Class, Object, Constructor, Encapsulation (private fields, validating setters)
- [~] Inheritance: `LostItem` and `FoundItem` extend abstract `Item`
- [~] Abstraction: `Item` declares abstract methods (`getItemType`, `describe`, ...)
- [~] Polymorphism: subclasses override `describe()` and `isResolved()`; dashboard loops over `Item` references

---

## Next steps (in order)

| # | Step | Files | Status |
|---|------|-------|--------|
| 2 | Download MySQL Connector/J, put JAR in `lib/` | `lib/mysql-connector-j-9.7.0.jar` | [x] |
| 2 | Connection class + config file + test | `src/database/DatabaseConnection.java`, `src/database/TestConnection.java`, `db.properties` | [x] SUCCESS |
| 3 | Users in the database (login works from MySQL) | `src/services/UserService.java`, `src/gui/LoginFrame.java` | [x] verified |
| 4 | Items in the database (add, edit, search, status) | `src/services/ItemService.java` | [x] verified (tested on a Lost item) |
| 5A | Claims in the database | `src/services/ClaimService.java` | [x] verified |
| 5B | Block manual Claimed / Pending Claim (ISSUE-06) | `src/services/ItemService.java` | [x] verified |
| 6A | Database errors shown clearly (ISSUE-04) | `src/Main.java` | [x] verified |
| 6B | Confirm before Approve / Reject (ISSUE-09) | `src/gui/ClaimsPanel.java` | [x] verified |
| 7A | Test the untested features (Found items, required fields, bad dates, over-long input) | none | [x] verified |
| 7B | Remove test data | SQL run in Workbench | [x] verified |
| 7C | Replace the starter accounts | Users screen + SQL in Workbench | [x] verified |
| 7D | Write the README (`.gitignore` is already done) | `README.md` | [x] written |
| 7E | Walk through the seven OOP concepts for the demo | none | [x] notes given (rehearse once before the demo) |

### What changes where
- **Must change:** `UserService`, `ItemService`, `ClaimService` (replace the in-memory lists with SQL via `PreparedStatement`).
- **New:** `DatabaseConnection`, `TestConnection`, `db.properties`.
- **Should not need changes:** all `gui/*`, all `models/*`, `Main.java`, `UIStyles`.
- **Possible small change:** `Item` / `Claim` already have what we need (`setId`), but `Claim` has no `setId`. We only add it if required.

---

## Decisions made

- `reported_by` and `claimant_name` are stored as **text**, not foreign keys to `users`, because the models and GUI use names (and values like "Security Desk"). Change only if the instructor requires foreign keys.
- Roles are `Admin` and `Staff` (matches `User.setRole`).
- Passwords keep the existing salted SHA-256 hashing in `UserService`.
- The DB password is **not** in source code. It lives in `db.properties`, which is listed in `.gitignore`.
- Starter data (`admin/admin123`, `student/student123` and the sample items) will be inserted **only when the tables are empty**.
- **Account requests (future):** DIN NATO keeps admin-only account creation. A "Request a DIN NATO account" service will be added to the e-community website (which already has services such as document requests and messages). How the request reaches DIN NATO is still open: (1) the admin reads the request on the website and adds the user in the Users screen; (2) the website saves requests in a table that DIN NATO shows to the admin for approval; (3) DIN NATO signs people in against the e-community accounts. Option 3 needs matching password hashing and a limited MySQL user, so it is the biggest change. Not started.

## Issues log

Found by reading the GUI code. None of these were reproduced by running the app yet, so treat each as "needs confirming". Status: **Open** = not started, **Deferred** = decided to do later.

### Deferred features (decided, build later)

| ID | Issue | Needs | Status |
|----|-------|-------|--------|
| ISSUE-01 | **Self sign-up. Decision: not built inside this app; account requests will come from the e-community website later (see Decisions).** Original idea: "Create account" link on the login screen. New users must never become Admin. Open question: new **Resident** role (limited access) or **Staff**. | `LoginFrame`, `UserService`, new sign-up dialog, role list in DB if Resident is chosen | Deferred |
| ISSUE-02 | **Proof for claims.** A claim needs a proof type (dropdown) and written proof details, both required. Staff see the proof when approving. Photo upload stays a later feature. | `claims` table gets `proof_type` and `proof_details`; `Claim`, `ClaimService`, `ClaimsPanel` | Deferred |

### High: fix while connecting the database

| ID | Issue | Where | Fix in |
|----|-------|-------|--------|
| ISSUE-03 | The login screen shows the demo accounts (`admin / admin123`, `student / student123`). Anyone can read the passwords. | `LoginFrame` (hint label) | **Fixed** in Step 3B |
| ISSUE-04 | Database failures are not handled. Screens only catch `IllegalArgumentException`, so MySQL being stopped or a wrong password would not show a clear message. `Main` also never checks the connection at startup. | all panels, `Main` | **Fixed** in Step 6A (startup check verified with a wrong password) |
| ISSUE-05 | Length limits are not checked. The tables limit item name to 100, category 50, location 150, username 50, full name 100, but the models accept any length, so a long entry would cause a "Data too long" SQL error. | `Item`, `User` setters | **Implemented** in Steps 3 and 4 (not yet tested with over-long input) |
| ISSUE-06 | Status can be changed by hand to **Pending Claim** or **Claimed** (Edit form and Update Status button) without any claim record. This bypasses the claims screen and, later, the proof requirement. | `ItemService` (rule), shown by the existing dialogs | **Fixed** in Step 5B |
| ISSUE-07 | Creating, approving, or rejecting a claim changes several rows (the claim, the item, other claims). With a database this must be one transaction, so a failure cannot leave half the changes saved. | `ClaimService` | **Fixed** in Step 5A |

### Medium

| ID | Issue | Where |
|----|-------|-------|
| ISSUE-08 | No permissions. Every logged-in user can approve or reject claims and edit any item. Only the Users tab is hidden, and the services do no role checks. Ties in with ISSUE-01. | `DashboardFrame`, services |
| ISSUE-09 | **Fixed in Step 6B (verified).** Approve and Reject used to act immediately with no "Are you sure?". | `ClaimsPanel` |
| ISSUE-10 | The Users screen can only add users. No edit, deactivate/delete, or password reset. | `UsersPanel`, `UserService` |
| ISSUE-11 | Search runs on every keystroke. Fine with sample data, but with MySQL it sends a query per letter. A short delay timer would fix it. | `ItemsPanel` |
| ISSUE-12 | Dates are typed by hand as yyyy-MM-dd, which is easy to get wrong. A date picker would be friendlier. | `ItemFormDialog` |
| ISSUE-17 | No limit on failed login attempts, so passwords can be guessed repeatedly. Accepted for this milestone. | `LoginFrame`, `UserService` |

### Low / polish

| ID | Issue | Where |
|----|-------|-------|
| ISSUE-13 | Empty tables show nothing. Add a "No records found" message. | all panels |
| ISSUE-14 | Items from the master design not built yet (optional): top-bar search field and sidebar icons. | `DashboardFrame` |
| ISSUE-15 | Passwords use salted SHA-256, which is fast to guess against. PBKDF2 (built into Java, no library) is stronger. Optional upgrade. | `UserService` |
| ISSUE-16 | Watch item: the item and claim tables map a clicked row straight to the list position. If table sorting is ever turned on, this must use `convertRowIndexToModel`. (`UIStyles` not reviewed yet.) | `ItemsPanel`, `ClaimsPanel` |

### Minor notes
- GitHub backup: private repository. Upload `src`, `lib`, `README.md`, `PROGRESS.md`, and `database/din_nato.sql`. Never upload `db.properties` (password) or `.DS_Store` files. Web upload ignores `.gitignore`, so leave them out by hand.
- The starter accounts `admin` and `student` were deleted from the database. Current users: `HanGO` (Admin) and `staff1` (Staff). The first-run code in `UserService` only recreates the starters when the `users` table is completely empty (documented in the README).
- Project lives inside OneDrive. One new file briefly disappeared during setup. If files vanish again, move the project outside OneDrive.
- Mac leftover files (`._*`, `.DS_Store`) were deleted and are now in `.gitignore`.
- The test ran under VS Code's own Java 25 runtime while the terminal has Java 26. Use the VS Code Run button for now. If we ever compile in the terminal, use the same JDK to avoid version errors.
- `LoginFrame.java` shows 1 warning (likely an unused import). Harmless, clean up later.
- `ClaimsPanel.refresh()` looks up the item once per claim row. Fine for a student project, optimize only if slow.

---

## Testing checklist (mark only after actually testing)

- [x] Java JDK installed and configured
- [x] VS Code Java support configured
- [x] MySQL installed and running
- [x] Database and tables created
- [x] JDBC connection succeeds (`SUCCESS: connected to din_nato. Rows in items table: 0`)
- [x] Main application compiles
- [x] Swing window launches
- [x] Navigation works
- [x] Lost-item records can be created and displayed
- [x] Found-item records can be created and displayed
- [x] Search works
- [x] Updates are saved to MySQL
- [x] Data is still there after restarting the app (users, items, claims)
- [x] Validation prevents invalid records (required fields, future and badly formatted dates, over-long names)
- [x] Database errors are handled clearly (startup test verified)
- [ ] All seven OOP concepts are demonstrable
- [ ] Design is consistent across screens
- [x] README documents setup and run instructions

## Later (not in this milestone)
Advanced claim verification, role-based permissions, image uploads, notifications, reports.
