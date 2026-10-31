# DIN NATO: Loss and Found Management System

A Java desktop application for managing lost and found items. People report lost items, staff register found items, and owners can claim found items. Data is stored in a MySQL database.

This is an academic project that demonstrates Java Object-Oriented Programming.

## Features

- **Login** with accounts stored in MySQL (passwords are salted and hashed)
- **Dashboard** with summary cards (lost, found, claimed or returned) and recent items
- **Lost Items and Found Items:** add, edit, view, update status, search, and filter by category and status
- **Claims:** submit a claim on a found item, then approve or reject it (with a confirmation prompt)
- **Users (Admin only):** add new accounts as Admin or Staff
- **Saved data:** everything is kept in MySQL, so it is still there after the app is closed
- **Clear error messages** when the database cannot be reached

### Rules the app enforces

- Required fields, date format, no future dates, and maximum text lengths are checked before saving.
- A found item can only become **Pending Claim** or **Claimed** through the Claims screen, not by editing it by hand.
- Only Admin accounts can see the Users screen and create accounts.

## Technologies

| Tool | Purpose |
|---|---|
| Java (JDK) | Main language (developed with JDK 26) |
| Java Swing | Desktop interface |
| MySQL 8.0 | Database |
| JDBC with MySQL Connector/J 9.7.0 | Connects Java to MySQL |
| Visual Studio Code | Code editor (with the Extension Pack for Java) |
| MySQL Workbench | Creating and checking the database |

## How the program is organized

```
GUI (Swing)  ->  Services  ->  DatabaseConnection / JDBC  ->  MySQL
```

- **GUI** shows screens and forms and calls the services. It contains no SQL.
- **Services** check the input and run the SQL (`PreparedStatement` is used for every query).
- **Models** are the objects that represent items, users, and claims.
- **DatabaseConnection** opens the MySQL connection using the settings in `db.properties`.

```
DIIN_NATO/
├── src/
│   ├── Main.java
│   ├── database/    DatabaseConnection.java, TestConnection.java
│   ├── models/      Item.java, LostItem.java, FoundItem.java, User.java, Claim.java
│   ├── services/    ItemService.java, UserService.java, ClaimService.java
│   ├── gui/         LoginFrame, DashboardFrame, ItemsPanel, LostItemsPanel,
│   │                FoundItemsPanel, ItemFormDialog, ClaimsPanel, UsersPanel
│   └── utils/       UIStyles.java
├── lib/             mysql-connector-j-9.7.0.jar
├── database/        din_nato.sql
├── db.properties    (your own database login, not uploaded to Git)
└── README.md
```

## Setup

### 1. Install the tools

- Java JDK (check with `java -version` and `javac -version`)
- MySQL Server and MySQL Workbench
- Visual Studio Code with the **Extension Pack for Java**

### 2. Create the database

1. Open MySQL Workbench and connect to your local MySQL server.
2. Choose **File > Open SQL Script** and open `database/din_nato.sql`.
3. Run the script with the lightning bolt button.
4. Refresh the schemas list. You should see a database called `din_nato` with the tables `users`, `items`, and `claims`.

### 3. Add the MySQL driver

The file `lib/mysql-connector-j-9.7.0.jar` must be in the `lib` folder. If it is missing, download MySQL Connector/J 9.7.0 (Platform Independent) and copy the `.jar` file into `lib`. VS Code lists it under **Referenced Libraries**.

### 4. Create `db.properties`

Create a file named `db.properties` in the project folder (next to `src`) with these three lines, using your own MySQL password:

```
db.url=jdbc:mysql://localhost:3306/din_nato?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_MYSQL_PASSWORD
```

This file holds a password, so it is listed in `.gitignore` and must **never** be uploaded to GitHub.

### 5. Check the connection (optional)

Open `src/database/TestConnection.java` in VS Code and click **Run**. A working setup prints `SUCCESS: connected to din_nato...`.

### 6. Run the application

Open the project folder in VS Code, open `src/Main.java`, and click **Run**. Run it from the project folder, because the program looks for `db.properties` there.

## First login

When the `users` table is completely empty, the app creates two starter accounts and six sample items:

- `admin` / `admin123` (Admin)
- `student` / `student123` (Staff)

These passwords are written in the source code, so replace them right away:

1. Log in as `admin` and add your own Admin account under **Users > + Add User**.
2. Close the app, then delete the starter accounts in MySQL Workbench:

```sql
DELETE FROM din_nato.users WHERE username IN ('admin', 'student') AND user_id > 0;
```

## The seven OOP concepts

| Concept | Where to find it |
|---|---|
| **Class** | `Item`, `LostItem`, `FoundItem`, `User`, `Claim`, and the service classes |
| **Object** | An item report is a `LostItem` or `FoundItem` object; `ItemService.build(...)` creates them, and each row loaded from MySQL becomes an object |
| **Constructor** | `Item(...)` sets up every attribute; `LostItem` and `FoundItem` call it with `super(...)`; `User` and `Claim` have their own constructors |
| **Encapsulation** | All fields in the models are `private`. Getters read them and setters check them (for example, `Item.setName` rejects an empty name and `Item.setDateReported` rejects a future date) |
| **Inheritance** | `LostItem` and `FoundItem` extend the abstract class `Item` and reuse its fields and methods |
| **Polymorphism** | `describe()`, `getStatusOptions()`, `isResolved()`, and `getItemType()` behave differently for lost and found items. The screens work with `Item` references and call these methods without knowing the exact subclass |
| **Abstraction** | `Item` is abstract. It defines what every item can do (the abstract methods) and leaves the details to the subclasses. The service layer also hides all SQL from the GUI |

## Known limitations

These are known and planned for later:

- A claim does not yet require proof, and Staff accounts can approve any claim.
- Users can be added but not edited or removed, and there is no password reset or password change.
- There is no limit on failed login attempts.
- Items cannot be deleted from the app.
- Dates are typed in as `yyyy-MM-dd`.
- Passwords use a salted SHA-256 hash. A slower method (such as PBKDF2) would be stronger.
- Idea for later: a "Request a DIN NATO account" service on the e-community website, so new users can ask for an account.

## Troubleshooting

| Message | What to check |
|---|---|
| "Cannot connect to the database" at startup | MySQL is running, and `db.properties` has the right URL, user, and password |
| "Could not read db.properties" | The file is in the project folder, and the program is started from that folder |
| `ClassNotFoundException` or "No suitable driver" | The Connector/J `.jar` is in `lib` and shows under Referenced Libraries |
| Login says "Invalid username or password" for a new database | Use the starter accounts listed above |
