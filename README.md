# 📄 Document Tagging & Retrieval System

A **Java-based console application** that simulates how large organizations manage, tag, and retrieve digital documents efficiently. Built using core **Data Structures and Algorithms** concepts as part of a DSA course project.

---

---

## 🗂️ Project Structure

```
DocumentTaggingSystem/
└── src/
    ├── model/
    │   └── Document.java               # Data class — fields, addTag, removeTag
    ├── repository/
    │   ├── DocumentRepository.java     # Stores documents (HashMap + sorted ArrayList)
    │   └── TagRepository.java          # Tag index (HashMap: tag → list of doc IDs)
    ├── service/
    │   ├── DocumentService.java        # All business logic for documents and tags
    │   └── SearchService.java          # All search logic, history, and analytics
    └── main/
        └── Main.java                   # Menu system — input/output only
```

---

## 🏗️ Architecture — Layered Design

```
User Input
    │
  Main              ← reads input, prints output only
    │
  DocumentService   ← all rules and logic for documents & tags
  SearchService     ← all rules and logic for searching & history
    │
  DocumentRepository  ← raw storage, no logic
  TagRepository       ← tag index storage, no logic
    │
  Document            ← data shape only
```

---

## 🧠 Data Structures Used

| Data Structure       | Location             | Purpose                                             |
| -------------------- | -------------------- | --------------------------------------------------- |
| `HashMap`            | `DocumentRepository` | Store documents by ID — O(1) lookup                 |
| `HashMap`            | `TagRepository`      | Tag index: tag → list of doc IDs — O(1) lookup      |
| `ArrayList`          | `DocumentRepository` | Sorted by title A-Z for Binary Search               |
| `Stack`              | `SearchService`      | Search history — LIFO, max 5 entries, supports undo |
| `Queue` (LinkedList) | `SearchService`      | Retrieval log in FIFO order                         |
| `PriorityQueue`      | `SearchService`      | Max-heap to surface top accessed documents          |

---

## ⚙️ Algorithms Used

| Algorithm      | Location                             | Purpose                                  | Complexity        |
| -------------- | ------------------------------------ | ---------------------------------------- | ----------------- |
| Binary Search  | `SearchService.searchByTitle()`      | Find document by exact title             | O(log n)          |
| Insertion Sort | `DocumentRepository`                 | Keep title list sorted after each insert | O(n²) / O(n) best |
| Selection Sort | `DocumentService`, `SearchService`   | Sort docs by ID, rank results by score   | O(n²)             |
| Bubble Sort    | `DocumentService.getAllTagsSorted()` | Sort tag list alphabetically             | O(n²)             |
| Linear Scan    | `SearchService.searchByKeyword()`    | Scan all docs for keyword matches        | O(n)              |

---

## ✨ Features

### 1. Document Management

- ➕ Add a document (ID, title, content)
- ❌ Remove a document (also cleans up tag index)
- 🏷️ Assign a tag to a document
- 🗑️ Remove a tag from a document
- 📋 List all documents (sorted by ID)
- 🔖 List all tags (sorted A-Z, shows linked documents)

### 2. Search

- 🔍 **Search by Tag** — HashMap O(1) lookup, results ranked by access count
- 🔎 **Search by Keyword** — scans title, content, tags; ranked by relevance score
  - Title match = 2 points
  - Content match = 1 point
  - Tag match = 1 point
- 📖 **Search by Title** — Binary Search on sorted list, O(log n)

### 3. History & Analytics

- 🕐 **Recent Search History** — Stack (LIFO), stores last 5 searches
- ↩️ **Undo Last Search** — Stack `pop()` removes most recent entry
- 📜 **Retrieval Log** — Queue (FIFO), shows documents in the order they were accessed
- 🏆 **Top Accessed Documents** — PriorityQueue max-heap, shows top 5 by access count

---

## 🖥️ Menu Flow

```
Main Menu
├── 1. Document Management  →  6 options  →  press 0 to go back
├── 2. Search               →  3 options  →  press 0 to go back
├── 3. History & Analytics  →  4 options  →  press 0 to go back
└── 0. Exit
```

---

## 🔍 Sample Run

```
+--------------------------------------+
|  Document Tagging & Retrieval System |
+--------------------------------------+
|  1.  Document Management             |
|  2.  Search                          |
|  3.  History & Analytics             |
|  0.  Exit                            |
+--------------------------------------+
Enter your choice: 2

+--------------------------------------+
|             2. Search                |
+--------------------------------------+
|  1.  Search by Tag                   |
|  2.  Search by Keyword               |
|  3.  Search by Title [Binary Search] |
|  0.  Back to Main Menu               |
+--------------------------------------+
Enter your choice: 1

```

---

## 📚 Concepts Covered

- Object-Oriented Programming (OOP) — Classes, Encapsulation, Constructors
- Layered Architecture — Model / Repository / Service / Main
- HashMap — insertion, lookup, deletion
- ArrayList — dynamic arrays, manual sorting
- Stack — LIFO operations, push/pop, search history
- Queue — FIFO operations, retrieval log
- PriorityQueue — max-heap with custom Comparator
- Binary Search — O(log n) title lookup
- Sorting Algorithms — Insertion Sort, Selection Sort, Bubble Sort
- Input Validation — return codes, error handling

---

## 🛠️ Tech Stack

- **Language:** Java (JDK 25)
- **Build Tool:** None — compile directly with `javac`
- **Libraries:** Java standard library only (`java.util.*`)
- **IDE used:** IntelliJ IDEA (any IDE works)
