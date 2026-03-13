package service;

import model.Document;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

// ============================================================
//  CLASS : SearchService
//  PACKAGE: service
//
//  PURPOSE:
//    Contains ALL business logic for searching, history,
//    retrieval tracking, and analytics.
//
//  IMPORTANT DESIGN DECISION:
//    SearchService does NOT talk to repositories directly.
//    It only talks to DocumentService.
//    This is correct layered architecture:
//      Main -> Service -> Repository
//    NOT:
//      Main -> Service -> Repository  AND  Service -> Repository (skipping)
//
//  WHAT "BUSINESS LOGIC" MEANS HERE:
//    - Validate search input (blank keyword? reject it)
//    - Decide how to rank results (by score, by access count)
//    - Manage search history with a Stack (max 5, undo support)
//    - Track every retrieval in a Queue (FIFO log)
//    - Use a PriorityQueue to surface top accessed documents
//    - Binary Search for exact title lookup
//
//  DATA STRUCTURES USED:
//    - Stack         : search history, LIFO, newest on top, max 5
//    - Queue         : retrieval log in FIFO order
//    - PriorityQueue : max-heap to rank documents by access count
//    - ArrayList     : used for Binary Search (sorted by title)
//
//  METHODS:
//    searchByTag()              - find docs by tag, rank by access count
//    searchByKeyword()          - scan all docs, rank by relevance score
//    searchByTitle()            - Binary Search on sorted title list
//    getSearchHistory()         - return recent searches from Stack
//    undoLastSearch()           - pop from Stack
//    getRetrievalLog()          - return FIFO Queue contents
//    getTopAccessedDocuments()  - use PriorityQueue max-heap
// ============================================================

public class SearchService {

    // SearchService only depends on DocumentService, NOT on repositories
    private DocumentService docService;

    // Stack: stores the last MAX_HISTORY search queries (newest on top)
    // LIFO: Last In First Out — push() adds, pop() removes most recent
    private Stack<String> searchHistory;

    // Queue: stores doc IDs in the order they were retrieved
    // FIFO: First In First Out — add() appends, iteration shows oldest first
    private Queue<String> retrievalLog;

    private static final int MAX_HISTORY = 5;

    // ----------------------------------------------------------------
    //  Constructor
    //  Only DocumentService is injected — no direct repo access here
    // ----------------------------------------------------------------
    public SearchService(DocumentService docService) {
        this.docService    = docService;
        this.searchHistory = new Stack<String>();
        this.retrievalLog  = new LinkedList<String>();
    }

    // We store the scores from the last keyword search here
    // so Main can display them alongside the results
    private ArrayList<Integer> lastKeywordScores = new ArrayList<Integer>();

    // ================================================================
    //  searchByTag
    //
    //  BUSINESS LOGIC:
    //    1. Validate: tag must not be blank
    //    2. Ask DocumentService for document IDs that have this tag
    //    3. Fetch each Document via DocumentService
    //    4. Rank results by access count (most accessed shown first)
    //    5. Record this search in the Stack history
    //    6. Record each result in the FIFO retrieval Queue
    //    7. Increment access count on each retrieved document
    //
    //  Returns empty list if tag not found or input is blank.
    //
    //  DSA: HashMap O(1) lookup (via DocumentService -> TagRepository)
    //       Selection sort for ranking
    // ================================================================
    public ArrayList<Document> searchByTag(String tag) {

        // Business Rule: tag must not be blank
        if (tag == null || tag.trim().isEmpty()) {
            return new ArrayList<Document>();
        }

        String cleanTag = tag.toLowerCase().trim();

        // Push to search history Stack BEFORE fetching results
        pushToHistory("tag:" + cleanTag);

        // Get doc IDs for this tag via DocumentService
        ArrayList<String> docIds = docService.getDocIdsByTag(cleanTag);

        // Fetch actual Document objects
        ArrayList<Document> results = new ArrayList<Document>();
        for (int i = 0; i < docIds.size(); i++) {
            Document d = docService.getDocumentById(docIds.get(i));
            if (d != null) {
                results.add(d);
            }
        }

        // Business Rule: rank by access count descending (most popular first)
        // Selection sort — straightforward for beginners
        for (int i = 0; i < results.size() - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < results.size(); j++) {
                if (results.get(j).getAccessCount() > results.get(maxIdx).getAccessCount()) {
                    maxIdx = j;
                }
            }
            Document temp = results.get(i);
            results.set(i, results.get(maxIdx));
            results.set(maxIdx, temp);
        }

        // Record retrievals: increment access count + add to FIFO Queue
        for (int i = 0; i < results.size(); i++) {
            results.get(i).incrementAccess();
            retrievalLog.add(results.get(i).getId());
        }

        return results;
    }

    // ================================================================
    //  searchByKeyword
    //
    //  BUSINESS LOGIC:
    //    1. Validate: keyword must not be blank
    //    2. Scan ALL documents (linear scan) via DocumentService
    //    3. Score each document:
    //         Title match   = 2 points  (title is most important)
    //         Content match = 1 point
    //         Tag match     = 1 point
    //    4. Discard documents with score = 0 (no match)
    //    5. Sort results by score descending (most relevant first)
    //    6. Record this search in the Stack history
    //    7. Record each result in the FIFO retrieval Queue
    //    8. Increment access count on each retrieved document
    //
    //  Scores are saved in lastKeywordScores so Main can display them.
    //
    //  Returns empty list if no matches or input is blank.
    //
    //  DSA: Linear scan O(n), selection sort for ranking
    // ================================================================
    public ArrayList<Document> searchByKeyword(String keyword) {

        // Business Rule: keyword must not be blank
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<Document>();
        }

        String kw = keyword.toLowerCase().trim();

        // Push to search history Stack BEFORE scanning
        pushToHistory("keyword:" + kw);

        ArrayList<Document> results = new ArrayList<Document>();
        lastKeywordScores = new ArrayList<Integer>();

        // Scan all documents via DocumentService (not repository directly)
        ArrayList<Document> allDocs = docService.getAllDocumentsRaw();

        for (int i = 0; i < allDocs.size(); i++) {
            Document doc = allDocs.get(i);
            int score = 0;

            // Title match scores highest (most relevant)
            if (doc.getTitle().toLowerCase().contains(kw))   score += 2;

            // Content match
            if (doc.getContent().toLowerCase().contains(kw)) score += 1;

            // Tag match — stop as soon as one tag matches
            ArrayList<String> tags = doc.getTags();
            for (int j = 0; j < tags.size(); j++) {
                if (tags.get(j).contains(kw)) {
                    score += 1;
                    break;
                }
            }

            // Only include documents that matched at least something
            if (score > 0) {
                results.add(doc);
                lastKeywordScores.add(score);
            }
        }

        // Business Rule: sort by score descending (highest relevance first)
        // Selection sort on BOTH lists in parallel (results + scores must stay aligned)
        for (int i = 0; i < results.size() - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < results.size(); j++) {
                if (lastKeywordScores.get(j) > lastKeywordScores.get(maxIdx)) {
                    maxIdx = j;
                }
            }
            // Swap results
            Document tempDoc = results.get(i);
            results.set(i, results.get(maxIdx));
            results.set(maxIdx, tempDoc);
            // Swap scores (must mirror the results swap)
            int tempScore = lastKeywordScores.get(i);
            lastKeywordScores.set(i, lastKeywordScores.get(maxIdx));
            lastKeywordScores.set(maxIdx, tempScore);
        }

        // Record retrievals: increment access count + add to FIFO Queue
        for (int i = 0; i < results.size(); i++) {
            results.get(i).incrementAccess();
            retrievalLog.add(results.get(i).getId());
        }

        return results;
    }

    // Returns the relevance scores from the last keyword search
    public ArrayList<Integer> getLastKeywordScores() {
        return lastKeywordScores;
    }

    // ================================================================
    //  searchByTitle  (Binary Search)
    //
    //  BUSINESS LOGIC:
    //    1. Validate: title must not be blank
    //    2. Ask DocumentService for the title-sorted list
    //    3. Run Binary Search on that sorted list
    //    4. If found: record in history Stack, retrieval Queue,
    //       and increment access count
    //
    //  Returns null if not found or input is blank.
    //
    //  DSA: Binary Search on sorted ArrayList — O(log n)
    //
    //  How Binary Search works:
    //    low = 0, high = last index
    //    Check middle element
    //    If match         -> return it
    //    If target > mid  -> search right half  (low = mid + 1)
    //    If target < mid  -> search left half   (high = mid - 1)
    //    Repeat until found or range is empty
    // ================================================================
    public Document searchByTitle(String title) {

        // Business Rule: title must not be blank
        if (title == null || title.trim().isEmpty()) {
            return null;
        }

        pushToHistory("title:" + title.trim());

        // Get the sorted list via DocumentService
        ArrayList<Document> sortedList = docService.getSortedByTitle();

        // Binary Search
        int low  = 0;
        int high = sortedList.size() - 1;

        while (low <= high) {
            int mid    = (low + high) / 2;
            Document midDoc = sortedList.get(mid);
            int cmp = midDoc.getTitle().compareToIgnoreCase(title.trim());

            if (cmp == 0) {
                // Found — record retrieval and increment access
                midDoc.incrementAccess();
                retrievalLog.add(midDoc.getId());
                return midDoc;
            } else if (cmp < 0) {
                low = mid + 1;  // target is alphabetically after mid
            } else {
                high = mid - 1; // target is alphabetically before mid
            }
        }

        return null; // not found
    }

    // ================================================================
    //  SEARCH HISTORY — Stack operations
    //  Stack is LIFO: the most recent search is always on top.
    //  We keep a max of MAX_HISTORY (5) entries.
    // ================================================================

    // Returns history as a list, newest first (top of stack first)
    public ArrayList<String> getSearchHistory() {
        ArrayList<String> history = new ArrayList<String>();
        // Stack index: size-1 = top (newest), 0 = bottom (oldest)
        for (int i = searchHistory.size() - 1; i >= 0; i--) {
            history.add(searchHistory.get(i));
        }
        return history;
    }

    // Removes and returns the most recent search (Stack pop)
    // Returns null if history is empty
    public String undoLastSearch() {
        if (searchHistory.isEmpty()) {
            return null;
        }
        return searchHistory.pop();
    }

    public boolean hasSearchHistory() {
        return !searchHistory.isEmpty();
    }

    // ================================================================
    //  RETRIEVAL LOG — Queue operations
    //  Queue is FIFO: first document retrieved is shown first in the log.
    // ================================================================

    // Returns all retrieved doc IDs in FIFO order
    public ArrayList<String> getRetrievalLog() {
        ArrayList<String> log = new ArrayList<String>();
        Object[] arr = retrievalLog.toArray();
        for (int i = 0; i < arr.length; i++) {
            log.add((String) arr[i]);
        }
        return log;
    }

    public boolean hasRetrievalLog() {
        return !retrievalLog.isEmpty();
    }

    // ================================================================
    //  getTopAccessedDocuments  — PriorityQueue (max-heap)
    //
    //  BUSINESS LOGIC:
    //    Build a max-heap of all documents using their access count.
    //    poll() always removes and returns the document with the
    //    highest access count — exactly what we need for a top-N list.
    //
    //  DSA: PriorityQueue with Comparator — O(n log n) to build heap
    //       each poll() is O(log n)
    // ================================================================
    public ArrayList<Document> getTopAccessedDocuments(int topN) {

        // Business Rule: topN must be at least 1
        if (topN < 1) topN = 1;

        ArrayList<Document> allDocs = docService.getAllDocumentsRaw();
        ArrayList<Document> topList = new ArrayList<Document>();

        if (allDocs.isEmpty()) {
            return topList;
        }

        // Build max-heap: document with highest accessCount comes out first
        // Using an anonymous inner class for the Comparator (basic OOP, no lambdas)
        PriorityQueue<Document> maxHeap = new PriorityQueue<Document>(
            allDocs.size(),
            new java.util.Comparator<Document>() {
                public int compare(Document a, Document b) {
                    // Descending: b - a means higher count has higher priority
                    return b.getAccessCount() - a.getAccessCount();
                }
            }
        );

        // Add all documents into the heap
        for (int i = 0; i < allDocs.size(); i++) {
            maxHeap.add(allDocs.get(i));
        }

        // Poll top N documents — each poll() gives the current highest
        int count = 0;
        while (!maxHeap.isEmpty() && count < topN) {
            topList.add(maxHeap.poll());
            count++;
        }

        return topList;
    }

    // ================================================================
    //  PRIVATE HELPER — pushToHistory
    //  Pushes a search query onto the Stack.
    //  Removes the oldest entry (bottom of stack) if over the limit.
    // ================================================================
    private void pushToHistory(String query) {
        searchHistory.push(query);
        // Keep max MAX_HISTORY entries; remove oldest (index 0 = bottom)
        if (searchHistory.size() > MAX_HISTORY) {
            searchHistory.remove(0);
        }
    }
}
