package repository;

import model.Document;
import java.util.ArrayList;
import java.util.HashMap;

// ============================================================
//  CLASS : DocumentRepository
//  PACKAGE: repository
//
//  PURPOSE:
//    Stores and retrieves Document objects.
//    Think of this as the "database" for documents.
//
//  RULE:
//    This class ONLY stores data and provides raw access to it.
//    It does NOT validate business rules or make decisions.
//    All business rules (e.g. "ID must not be blank") belong
//    in the service layer.
//
//  DATA STRUCTURES USED:
//    - HashMap   : docId -> Document   for O(1) average lookup
//    - ArrayList : sorted by title A-Z for Binary Search
// ============================================================

public class DocumentRepository {

    // HashMap: key = document ID, value = Document object
    private HashMap<String, Document> docMap;

    // ArrayList kept sorted A-Z by title — used for Binary Search
    private ArrayList<Document> sortedByTitle;

    // ----------------------------------------------------------------
    //  Constructor
    // ----------------------------------------------------------------
    public DocumentRepository() {
        docMap        = new HashMap<String, Document>();
        sortedByTitle = new ArrayList<Document>();
    }

    // ----------------------------------------------------------------
    //  save
    //  Stores a Document in the HashMap and the sorted list.
    // ----------------------------------------------------------------
    public void save(Document doc) {
        docMap.put(doc.getId(), doc);
        sortedByTitle.add(doc);
        insertionSortByTitle(); // keep sorted after every insert
    }

    // ----------------------------------------------------------------
    //  delete
    //  Removes a Document by ID from both storage structures.
    //  Returns the removed Document, or null if not found.
    // ----------------------------------------------------------------
    public Document delete(String id) {
        Document doc = docMap.remove(id);
        if (doc != null) {
            for (int i = 0; i < sortedByTitle.size(); i++) {
                if (sortedByTitle.get(i).getId().equals(id)) {
                    sortedByTitle.remove(i);
                    break;
                }
            }
        }
        return doc;
    }

    // ----------------------------------------------------------------
    //  findById
    //  Returns a Document by ID using HashMap — O(1) average.
    //  Returns null if not found.
    // ----------------------------------------------------------------
    public Document findById(String id) {
        return docMap.get(id);
    }

    // ----------------------------------------------------------------
    //  existsById
    //  Returns true if a document with the given ID is stored.
    // ----------------------------------------------------------------
    public boolean existsById(String id) {
        return docMap.containsKey(id);
    }

    // ----------------------------------------------------------------
    //  findAll
    //  Returns all stored documents as a new ArrayList.
    // ----------------------------------------------------------------
    public ArrayList<Document> findAll() {
        return new ArrayList<Document>(docMap.values());
    }

    // ----------------------------------------------------------------
    //  getSortedByTitle
    //  Returns the list sorted A-Z by title.
    //  Used by Binary Search in the service layer.
    // ----------------------------------------------------------------
    public ArrayList<Document> getSortedByTitle() {
        return sortedByTitle;
    }

    // ----------------------------------------------------------------
    //  count
    //  Returns how many documents are stored.
    // ----------------------------------------------------------------
    public int count() {
        return docMap.size();
    }

    // ----------------------------------------------------------------
    //  insertionSortByTitle  (private helper)
    //  Keeps sortedByTitle in A-Z order after every insert.
    //  Insertion Sort chosen because we only insert one doc at a time,
    //  and insertion sort is most efficient for nearly-sorted lists.
    // ----------------------------------------------------------------
    private void insertionSortByTitle() {
        for (int i = 1; i < sortedByTitle.size(); i++) {
            Document key = sortedByTitle.get(i);
            int j = i - 1;
            while (j >= 0 &&
                   sortedByTitle.get(j).getTitle().compareToIgnoreCase(key.getTitle()) > 0) {
                sortedByTitle.set(j + 1, sortedByTitle.get(j));
                j--;
            }
            sortedByTitle.set(j + 1, key);
        }
    }
}
