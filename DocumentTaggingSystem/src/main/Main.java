package main;

import model.Document;
import repository.DocumentRepository;
import repository.TagRepository;
import service.DocumentService;
import service.SearchService;
import java.util.ArrayList;
import java.util.Scanner;

// ============================================================
//  CLASS : Main
//  PACKAGE: main
//
//  PURPOSE:
//    Entry point. Shows a 3-option main menu.
//    Each option opens a sub-menu. Press 0 in any sub-menu
//    to go back to the main menu. Press 0 in the main menu
//    to exit the application.
//
//  MAIN MENU:
//    1. Document Management
//    2. Search
//    3. History & Analytics
//    0. Exit
//
//  RULE:
//    Main only reads input and prints output.
//    All logic lives in DocumentService and SearchService.
// ============================================================

public class Main {

    static Scanner sc = new Scanner(System.in);

    static DocumentRepository docRepo = new DocumentRepository();
    static TagRepository      tagRepo = new TagRepository();

    static DocumentService docService    = new DocumentService(docRepo, tagRepo);
    static SearchService   searchService = new SearchService(docService);

    // ================================================================
    //  MAIN
    // ================================================================
    public static void main(String[] args) {
        loadSampleData();

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("Enter your choice: ");
            String choice = sc.nextLine().trim();

            if      (choice.equals("1")) { openDocumentMenu();  }
            else if (choice.equals("2")) { openSearchMenu();    }
            else if (choice.equals("3")) { openHistoryMenu();   }
            else if (choice.equals("0")) { running = false; System.out.println("\nGoodbye!"); }
            else                         { System.out.println("  [!] Invalid choice. Try again."); }
        }
    }

    // ================================================================
    //  MAIN MENU
    // ================================================================
    static void printMainMenu() {
        System.out.println("\n+--------------------------------------+");
        System.out.println("|  Document Tagging & Retrieval System |");
        System.out.println("+--------------------------------------+");
        System.out.println("|  1.  Document Management             |");
        System.out.println("|  2.  Search                          |");
        System.out.println("|  3.  History & Analytics             |");
        System.out.println("|  0.  Exit                            |");
        System.out.println("+--------------------------------------+");
    }

    // ================================================================
    //  SUB-MENU 1 — Document Management
    //  Press 0 to go back to the main menu
    // ================================================================
    static void openDocumentMenu() {
        boolean inMenu = true;
        while (inMenu) {
            printDocumentMenu();
            System.out.print("Enter your choice: ");
            String choice = sc.nextLine().trim();

            if      (choice.equals("1")) { addDocument();      }
            else if (choice.equals("2")) { removeDocument();   }
            else if (choice.equals("3")) { assignTag();        }
            else if (choice.equals("4")) { removeTag();        }
            else if (choice.equals("5")) { listAllDocuments(); }
            else if (choice.equals("6")) { listAllTags();      }
            else if (choice.equals("0")) { inMenu = false; System.out.println("  [Back to Main Menu]"); }
            else                         { System.out.println("  [!] Invalid choice. Try again."); }
        }
    }

    static void printDocumentMenu() {
        System.out.println("\n+--------------------------------------+");
        System.out.println("|       1. Document Management         |");
        System.out.println("+--------------------------------------+");
        System.out.println("|  1.  Add a Document                  |");
        System.out.println("|  2.  Remove a Document               |");
        System.out.println("|  3.  Assign Tag to Document          |");
        System.out.println("|  4.  Remove Tag from Document        |");
        System.out.println("|  5.  List All Documents              |");
        System.out.println("|  6.  List All Tags                   |");
        System.out.println("|  0.  Back to Main Menu               |");
        System.out.println("+--------------------------------------+");
    }

    // ================================================================
    //  SUB-MENU 2 — Search
    //  Press 0 to go back to the main menu
    // ================================================================
    static void openSearchMenu() {
        boolean inMenu = true;
        while (inMenu) {
            printSearchMenu();
            System.out.print("Enter your choice: ");
            String choice = sc.nextLine().trim();

            if      (choice.equals("1")) { searchByTag();     }
            else if (choice.equals("2")) { searchByKeyword(); }
            else if (choice.equals("3")) { searchByTitle();   }
            else if (choice.equals("0")) { inMenu = false; System.out.println("  [Back to Main Menu]"); }
            else                         { System.out.println("  [!] Invalid choice. Try again."); }
        }
    }

    static void printSearchMenu() {
        System.out.println("\n+--------------------------------------+");
        System.out.println("|             2. Search                |");
        System.out.println("+--------------------------------------+");
        System.out.println("|  1.  Search by Tag                   |");
        System.out.println("|  2.  Search by Keyword               |");
        System.out.println("|  3.  Search by Title [Binary Search] |");
        System.out.println("|  0.  Back to Main Menu               |");
        System.out.println("+--------------------------------------+");
    }

    // ================================================================
    //  SUB-MENU 3 — History & Analytics
    //  Press 0 to go back to the main menu
    // ================================================================
    static void openHistoryMenu() {
        boolean inMenu = true;
        while (inMenu) {
            printHistoryMenu();
            System.out.print("Enter your choice: ");
            String choice = sc.nextLine().trim();

            if      (choice.equals("1")) { showSearchHistory();        }
            else if (choice.equals("2")) { undoLastSearch();           }
            else if (choice.equals("3")) { showRetrievalLog();         }
            else if (choice.equals("4")) { showTopAccessedDocuments(); }
            else if (choice.equals("0")) { inMenu = false; System.out.println("  [Back to Main Menu]"); }
            else                         { System.out.println("  [!] Invalid choice. Try again."); }
        }
    }

    static void printHistoryMenu() {
        System.out.println("\n+--------------------------------------+");
        System.out.println("|       3. History & Analytics         |");
        System.out.println("+--------------------------------------+");
        System.out.println("|  1.  Recent Search History [Stack]   |");
        System.out.println("|  2.  Undo Last Search                |");
        System.out.println("|  3.  Retrieval Log  [Queue FIFO]     |");
        System.out.println("|  4.  Top Accessed   [PriorityQueue]  |");
        System.out.println("|  0.  Back to Main Menu               |");
        System.out.println("+--------------------------------------+");
    }


    // ================================================================
    //  DOCUMENT MANAGEMENT — feature methods
    // ================================================================

    static void addDocument() {
        System.out.println("\n--- Add New Document ---");
        System.out.print("Enter Document ID (e.g. D106): ");
        String id = sc.nextLine().trim();

        System.out.print("Enter Title  : ");
        String title = sc.nextLine().trim();

        System.out.print("Enter Content: ");
        String content = sc.nextLine().trim();

        int result = docService.addDocument(id, title, content);

        if      (result ==  0) System.out.println("  [OK] Document '" + id + "' added successfully.");
        else if (result == -1) System.out.println("  [!] Document ID cannot be blank.");
        else if (result == -2) System.out.println("  [!] Title cannot be blank.");
        else if (result == -3) System.out.println("  [!] A document with ID '" + id + "' already exists.");
    }

    static void removeDocument() {
        System.out.println("\n--- Remove Document ---");
        System.out.print("Enter Document ID to remove: ");
        String id = sc.nextLine().trim();

        int result = docService.removeDocument(id);

        if      (result ==  0) System.out.println("  [OK] Document '" + id + "' removed.");
        else if (result == -1) System.out.println("  [!] Document '" + id + "' not found.");
    }

    static void assignTag() {
        System.out.println("\n--- Assign Tag to Document ---");
        System.out.print("Enter Document ID: ");
        String id = sc.nextLine().trim();

        System.out.print("Enter tag to add : ");
        String tag = sc.nextLine().trim();

        int result = docService.assignTag(id, tag);

        if (result == 0) {
            System.out.println("  [OK] Tag '" + tag.toLowerCase().trim() + "' assigned to '" + id + "'.");
            ArrayList<String> suggestions = docService.suggestRelatedTags(tag, id);
            if (!suggestions.isEmpty()) {
                System.out.println("  [Suggestion] You might also add: '" + suggestions.get(0) + "'");
            }
        } else if (result == -1) {
            System.out.println("  [!] Document '" + id + "' not found.");
        } else if (result == -2) {
            System.out.println("  [!] Tag cannot be blank.");
        } else if (result == -3) {
            System.out.println("  [!] Tag '" + tag.toLowerCase().trim() + "' is already on this document.");
        }
    }

    static void removeTag() {
        System.out.println("\n--- Remove Tag from Document ---");
        System.out.print("Enter Document ID: ");
        String id = sc.nextLine().trim();

        Document doc = docService.getDocumentById(id);
        if (doc == null) {
            System.out.println("  [!] Document '" + id + "' not found.");
            return;
        }

        System.out.print("  Current tags: ");
        ArrayList<String> tags = doc.getTags();
        for (int i = 0; i < tags.size(); i++) {
            System.out.print(tags.get(i));
            if (i < tags.size() - 1) System.out.print(", ");
        }
        System.out.println();

        System.out.print("Enter tag to remove: ");
        String tag = sc.nextLine().trim();

        int result = docService.removeTag(id, tag);

        if      (result ==  0) System.out.println("  [OK] Tag '" + tag.toLowerCase().trim() + "' removed from '" + id + "'.");
        else if (result == -2) System.out.println("  [!] Tag cannot be blank.");
        else if (result == -3) System.out.println("  [!] Tag '" + tag.toLowerCase().trim() + "' was not on this document.");
    }

    static void listAllDocuments() {
        System.out.println("\n--- All Documents ---");
        ArrayList<Document> all = docService.getAllDocumentsSorted();

        if (all.isEmpty()) {
            System.out.println("  No documents found.");
            return;
        }

        System.out.println("  Total: " + all.size() + " document(s)");
        System.out.println("  -----------------------------------------");
        for (int i = 0; i < all.size(); i++) {
            all.get(i).display();
            System.out.println("  -----------------------------------------");
        }
    }

    static void listAllTags() {
        System.out.println("\n--- All Tags ---");
        ArrayList<String> tags = docService.getAllTagsSorted();

        if (tags.isEmpty()) {
            System.out.println("  No tags found.");
            return;
        }

        System.out.println("  Total unique tags: " + tags.size());
        System.out.println("  -----------------------------------------");
        for (int i = 0; i < tags.size(); i++) {
            String tag    = tags.get(i);
            ArrayList<String> docIds = docService.getDocIdsByTag(tag);
            System.out.println("  Tag: " + tag + "  ->  Documents: " + docIds);
        }
    }


    // ================================================================
    //  SEARCH — feature methods
    // ================================================================

    static void searchByTag() {
        System.out.println("\n--- Search by Tag ---");
        System.out.print("Enter tag: ");
        String tag = sc.nextLine().trim();

        ArrayList<Document> results = searchService.searchByTag(tag);

        if (results.isEmpty()) {
            System.out.println("  [X] No documents found with tag '" + tag + "'.");
        } else {
            System.out.println("  [OK] " + results.size() + " document(s) found:");
            System.out.println("  -----------------------------------------");
            for (int i = 0; i < results.size(); i++) {
                results.get(i).display();
                System.out.println("  -----------------------------------------");
            }
        }
    }

    static void searchByKeyword() {
        System.out.println("\n--- Search by Keyword ---");
        System.out.print("Enter keyword: ");
        String keyword = sc.nextLine().trim();

        ArrayList<Document> results = searchService.searchByKeyword(keyword);
        ArrayList<Integer>  scores  = searchService.getLastKeywordScores();

        if (results.isEmpty()) {
            System.out.println("  [X] No documents found for keyword '" + keyword + "'.");
        } else {
            System.out.println("  [OK] " + results.size() + " document(s) found:");
            System.out.println("  -----------------------------------------");
            for (int i = 0; i < results.size(); i++) {
                System.out.println("  [Relevance Score: " + scores.get(i) + "]");
                results.get(i).display();
                System.out.println("  -----------------------------------------");
            }
        }
    }

    static void searchByTitle() {
        System.out.println("\n--- Search by Title [Binary Search O(log n)] ---");
        System.out.print("Enter exact title: ");
        String title = sc.nextLine().trim();

        Document found = searchService.searchByTitle(title);

        if (found == null) {
            System.out.println("  [X] No document found with title '" + title + "'.");
        } else {
            System.out.println("  [OK] Document found!");
            System.out.println("  -----------------------------------------");
            found.display();
        }
    }


    // ================================================================
    //  HISTORY & ANALYTICS — feature methods
    // ================================================================

    static void showSearchHistory() {
        System.out.println("\n--- Recent Search History (Stack - newest first) ---");
        ArrayList<String> history = searchService.getSearchHistory();

        if (history.isEmpty()) {
            System.out.println("  No searches yet.");
            return;
        }

        System.out.println("  Total saved: " + history.size() + " (max 5)");
        for (int i = 0; i < history.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + history.get(i));
        }
    }

    static void undoLastSearch() {
        System.out.println("\n--- Undo Last Search ---");
        String undone = searchService.undoLastSearch();

        if (undone == null) {
            System.out.println("  [!] No search history to undo.");
        } else {
            System.out.println("  [OK] Undid search: '" + undone + "'");
        }
    }

    static void showRetrievalLog() {
        System.out.println("\n--- Document Retrieval Log (Queue - FIFO order) ---");
        ArrayList<String> log = searchService.getRetrievalLog();

        if (log.isEmpty()) {
            System.out.println("  No documents retrieved yet.");
            return;
        }

        System.out.println("  Order: first accessed -> latest accessed");
        System.out.println("  -----------------------------------------");
        for (int i = 0; i < log.size(); i++) {
            String docId = log.get(i);
            Document doc = docService.getDocumentById(docId);
            String title = (doc != null) ? doc.getTitle() : "[document removed]";
            System.out.println("  " + (i + 1) + ". " + docId + "  -  " + title);
        }
    }

    static void showTopAccessedDocuments() {
        System.out.println("\n--- Top Accessed Documents (PriorityQueue Max-Heap) ---");
        ArrayList<Document> top = searchService.getTopAccessedDocuments(5);

        if (top.isEmpty()) {
            System.out.println("  No documents available.");
            return;
        }

        System.out.println("  Rank  | ID     | Title                          | Accesses");
        System.out.println("  -------------------------------------------------------");
        for (int i = 0; i < top.size(); i++) {
            Document doc = top.get(i);
            System.out.printf("  %-5d | %-6s | %-30s | %d%n",
                (i + 1), doc.getId(), doc.getTitle(), doc.getAccessCount());
        }
    }


    // ================================================================
    //  SAMPLE DATA
    // ================================================================
    static void loadSampleData() {
        docService.addDocument("D101", "Cyber Security Policy",
            "Policy document for cyber security guidelines and rules.");
        docService.addDocument("D102", "Finance Report Q1",
            "Quarterly finance report for Q1 including budget and expenses.");
        docService.addDocument("D103", "Team Meeting Notes",
            "Notes from the weekly team meeting on internal projects.");
        docService.addDocument("D104", "AI Whitepaper",
            "Whitepaper discussing AI systems, machine learning, and future technology.");
        docService.addDocument("D105", "Annual Security Audit",
            "Annual audit report covering security vulnerabilities and policy compliance.");

        docService.assignTag("D101", "policy");
        docService.assignTag("D101", "security");
        docService.assignTag("D102", "finance");
        docService.assignTag("D102", "report");
        docService.assignTag("D103", "meeting");
        docService.assignTag("D103", "internal");
        docService.assignTag("D104", "ai");
        docService.assignTag("D104", "technology");
        docService.assignTag("D105", "security");
        docService.assignTag("D105", "audit");
        docService.assignTag("D105", "policy");

    }
}
