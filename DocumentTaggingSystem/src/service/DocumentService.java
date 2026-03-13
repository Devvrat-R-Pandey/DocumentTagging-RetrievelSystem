package service;

import model.Document;
import repository.DocumentRepository;
import repository.TagRepository;
import java.util.ArrayList;

// ============================================================
//  CLASS : DocumentService
//  PACKAGE: service
//
//  PURPOSE:
//    Contains ALL business logic for managing documents and tags.
//
//  WHAT "BUSINESS LOGIC" MEANS HERE:
//    - Validating inputs before saving (blank ID? blank title?)
//    - Enforcing rules (no duplicate IDs, no duplicate tags)
//    - Coordinating between DocumentRepository and TagRepository
//      (e.g. when removing a doc, also clean up its tags)
//    - Sorting results before returning them
//    - Auto-suggesting related tags
//
//  RULE:
//    Main.java only reads input and prints output.
//    All decisions and rules live here.
//    Repositories only store/fetch — they make no decisions.
//
//  METHODS:
//    addDocument()         - validates then saves a new document
//    removeDocument()      - removes doc + cleans up tag index
//    assignTag()           - validates then links tag to doc
//    removeTag()           - validates then unlinks tag from doc
//    getDocumentById()     - fetch one document by ID
//    getAllDocumentsSorted()- fetch all docs sorted by ID
//    getAllTagsSorted()     - fetch all tags sorted A-Z
//    getDocIdsByTag()      - get doc IDs for a tag (used by SearchService)
//    suggestRelatedTags()  - suggest tags based on co-occurrence
// ============================================================

public class DocumentService {

    private DocumentRepository docRepo;
    private TagRepository      tagRepo;

    // ----------------------------------------------------------------
    //  Constructor
    //  Both repositories are injected so this service can coordinate them.
    // ----------------------------------------------------------------
    public DocumentService(DocumentRepository docRepo, TagRepository tagRepo) {
        this.docRepo = docRepo;
        this.tagRepo = tagRepo;
    }

    // ================================================================
    //  addDocument
    //
    //  BUSINESS RULES enforced here:
    //    1. ID must not be blank
    //    2. Title must not be blank
    //    3. No two documents can share the same ID
    //
    //  Returns:
    //     0  -> success
    //    -1  -> ID is blank
    //    -2  -> Title is blank
    //    -3  -> Document with this ID already exists
    // ================================================================
    public int addDocument(String id, String title, String content) {

        // Rule 1: ID must not be blank
        if (id == null || id.trim().isEmpty()) {
            return -1;
        }

        // Rule 2: Title must not be blank
        if (title == null || title.trim().isEmpty()) {
            return -2;
        }

        // Rule 3: ID must be unique
        if (docRepo.existsById(id.trim())) {
            return -3;
        }

        // All rules passed — create and save
        Document doc = new Document(id.trim(), title.trim(),
                                    content == null ? "" : content.trim());
        docRepo.save(doc);
        return 0;
    }

    // ================================================================
    //  removeDocument
    //
    //  BUSINESS RULES enforced here:
    //    1. Document must exist before we can remove it
    //    2. When a document is removed, ALL its tag links in the
    //       tag index must also be cleaned up (data consistency)
    //
    //  Returns:
    //     0  -> success
    //    -1  -> document not found
    // ================================================================
    public int removeDocument(String id) {

        Document doc = docRepo.findById(id);

        // Rule 1: must exist
        if (doc == null) {
            return -1;
        }

        // Rule 2: clean up tag index for every tag this doc had
        ArrayList<String> tags = doc.getTags();
        for (int i = 0; i < tags.size(); i++) {
            tagRepo.removeDocFromTag(tags.get(i), id);
        }

        // Now safe to delete from document storage
        docRepo.delete(id);
        return 0;
    }

    // ================================================================
    //  assignTag
    //
    //  BUSINESS RULES enforced here:
    //    1. Document must exist
    //    2. Tag must not be blank
    //    3. Tag must not already be on this document (no duplicates)
    //    4. After adding to the Document object, the tag index
    //       in TagRepository must also be updated (consistency)
    //
    //  Returns:
    //     0  -> success
    //    -1  -> document not found
    //    -2  -> tag is blank
    //    -3  -> tag already exists on this document
    // ================================================================
    public int assignTag(String docId, String tag) {

        // Rule 1: document must exist
        Document doc = docRepo.findById(docId);
        if (doc == null) {
            return -1;
        }

        // Rule 2: tag must not be blank
        if (tag == null || tag.trim().isEmpty()) {
            return -2;
        }

        // Rule 3: no duplicate tags on same document
        if (doc.hasTag(tag)) {
            return -3;
        }

        // Rule 4: add to document AND update tag index (both must stay in sync)
        doc.addTag(tag);                        // updates the Document object
        tagRepo.addDocToTag(tag, docId);        // updates the tag index
        return 0;
    }

    // ================================================================
    //  removeTag
    //
    //  BUSINESS RULES enforced here:
    //    1. Document must exist
    //    2. Tag must not be blank
    //    3. Tag must actually be on this document
    //    4. Removing tag must update BOTH the Document object
    //       AND the tag index (consistency)
    //
    //  Returns:
    //     0  -> success
    //    -1  -> document not found
    //    -2  -> tag is blank
    //    -3  -> tag not found on this document
    // ================================================================
    public int removeTag(String docId, String tag) {

        // Rule 1: document must exist
        Document doc = docRepo.findById(docId);
        if (doc == null) {
            return -1;
        }

        // Rule 2: tag must not be blank
        if (tag == null || tag.trim().isEmpty()) {
            return -2;
        }

        // Rule 3: tag must exist on this document
        if (!doc.hasTag(tag)) {
            return -3;
        }

        // Rule 4: remove from BOTH the Document object and the tag index
        doc.removeTag(tag);                      // updates the Document object
        tagRepo.removeDocFromTag(tag, docId);    // updates the tag index
        return 0;
    }

    // ================================================================
    //  getDocumentById
    //  Returns a Document by ID, or null if not found.
    //  Used by Main and SearchService to fetch a document.
    // ================================================================
    public Document getDocumentById(String id) {
        return docRepo.findById(id);
    }

    // ================================================================
    //  documentExists
    //  Returns true if a document with the given ID exists.
    // ================================================================
    public boolean documentExists(String id) {
        return docRepo.existsById(id);
    }

    // ================================================================
    //  getAllDocumentsSorted
    //  Returns all documents sorted by ID ascending (A -> Z / D101 -> D105).
    //  Sorting logic lives here in the service, not in the repository.
    //
    //  DSA: Selection Sort — simple, easy to understand for beginners
    // ================================================================
    public ArrayList<Document> getAllDocumentsSorted() {
        ArrayList<Document> all = docRepo.findAll();

        // Selection sort by document ID ascending
        for (int i = 0; i < all.size() - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < all.size(); j++) {
                if (all.get(j).getId().compareTo(all.get(minIdx).getId()) < 0) {
                    minIdx = j;
                }
            }
            // Swap
            Document temp = all.get(i);
            all.set(i, all.get(minIdx));
            all.set(minIdx, temp);
        }

        return all;
    }

    // ================================================================
    //  getAllTagsSorted
    //  Returns all unique tags sorted alphabetically A-Z.
    //  Sorting logic lives here in the service, not in the repository.
    //
    //  DSA: Bubble Sort — straightforward for small tag lists
    // ================================================================
    public ArrayList<String> getAllTagsSorted() {
        ArrayList<String> tags = tagRepo.getAllTags();

        // Bubble sort A-Z
        for (int i = 0; i < tags.size() - 1; i++) {
            for (int j = 0; j < tags.size() - i - 1; j++) {
                if (tags.get(j).compareTo(tags.get(j + 1)) > 0) {
                    String temp = tags.get(j);
                    tags.set(j, tags.get(j + 1));
                    tags.set(j + 1, temp);
                }
            }
        }
        return tags;
    }

    // ================================================================
    //  getDocIdsByTag
    //  Returns the list of document IDs that have a given tag.
    //  Used by SearchService when searching by tag.
    // ================================================================
    public ArrayList<String> getDocIdsByTag(String tag) {
        return tagRepo.getDocIdsByTag(tag);
    }

    // ================================================================
    //  getAllDocumentsRaw
    //  Returns all documents without sorting.
    //  Used by SearchService for keyword scanning.
    // ================================================================
    public ArrayList<Document> getAllDocumentsRaw() {
        return docRepo.findAll();
    }

    // ================================================================
    //  getSortedByTitle
    //  Returns the title-sorted list from the repository.
    //  Used by SearchService for Binary Search.
    // ================================================================
    public ArrayList<Document> getSortedByTitle() {
        return docRepo.getSortedByTitle();
    }

    // ================================================================
    //  suggestRelatedTags
    //
    //  BUSINESS LOGIC:
    //    When a tag is assigned to a document, look at all other
    //    documents that share that same tag. Collect their other tags,
    //    count how often each appears, and suggest the most common one
    //    — as long as the current document does not already have it.
    //
    //  Example:
    //    "security" is on D101 [policy, security] and D105 [security, audit, policy].
    //    If we add "security" to a new doc, "policy" appears 2 times
    //    across siblings so it gets suggested.
    //
    //  Returns a list of suggested tag names (currently top 1).
    // ================================================================
    public ArrayList<String> suggestRelatedTags(String newTag, String currentDocId) {
        ArrayList<String> suggestions = new ArrayList<String>();

        // Get all document IDs that already have this tag
        ArrayList<String> siblingIds = tagRepo.getDocIdsByTag(newTag);
        if (siblingIds.isEmpty()) {
            return suggestions; // no siblings, nothing to suggest
        }

        // Count frequency of each co-occurring tag across all siblings
        ArrayList<String>  freqKeys   = new ArrayList<String>();
        ArrayList<Integer> freqCounts = new ArrayList<Integer>();

        for (int i = 0; i < siblingIds.size(); i++) {
            String sibId = siblingIds.get(i);

            // Skip the document we are currently assigning to
            if (sibId.equals(currentDocId)) continue;

            Document sibling = docRepo.findById(sibId);
            if (sibling == null) continue;

            ArrayList<String> siblingTags = sibling.getTags();

            for (int j = 0; j < siblingTags.size(); j++) {
                String candidate = siblingTags.get(j);

                // Skip the tag we just assigned (not helpful to suggest it back)
                if (candidate.equals(newTag.toLowerCase().trim())) continue;

                // Skip tags the current document already has
                Document currentDoc = docRepo.findById(currentDocId);
                if (currentDoc != null && currentDoc.hasTag(candidate)) continue;

                // Add or increment in frequency list
                boolean found = false;
                for (int k = 0; k < freqKeys.size(); k++) {
                    if (freqKeys.get(k).equals(candidate)) {
                        freqCounts.set(k, freqCounts.get(k) + 1);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    freqKeys.add(candidate);
                    freqCounts.add(1);
                }
            }
        }

        if (freqKeys.isEmpty()) {
            return suggestions;
        }

        // Find the candidate with the highest frequency
        String bestTag   = "";
        int    bestCount = 0;
        for (int i = 0; i < freqKeys.size(); i++) {
            if (freqCounts.get(i) > bestCount) {
                bestCount = freqCounts.get(i);
                bestTag   = freqKeys.get(i);
            }
        }

        suggestions.add(bestTag);
        return suggestions;
    }
}
