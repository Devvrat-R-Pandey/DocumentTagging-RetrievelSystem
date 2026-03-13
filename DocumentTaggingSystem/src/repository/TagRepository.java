package repository;

import java.util.ArrayList;
import java.util.HashMap;

// ============================================================
//  CLASS : TagRepository
//  PACKAGE: repository
//
//  PURPOSE:
//    Maintains the tag index — a mapping from each tag to the
//    list of document IDs that have been assigned that tag.
//
//  RULE:
//    This class ONLY stores and retrieves tag data.
//    It does NOT validate or apply any business rules.
//
//  DATA STRUCTURES USED:
//    - HashMap : tag (String) -> ArrayList<String> of docIds
//                This gives O(1) average lookup for any tag.
//
//  Example of what the tag index looks like:
//    "security" -> ["D101", "D105"]
//    "finance"  -> ["D102"]
//    "policy"   -> ["D101", "D105"]
// ============================================================

public class TagRepository {

    // HashMap: tag -> list of document IDs that have this tag
    private HashMap<String, ArrayList<String>> tagIndex;

    // ----------------------------------------------------------------
    //  Constructor
    // ----------------------------------------------------------------
    public TagRepository() {
        tagIndex = new HashMap<String, ArrayList<String>>();
    }

    // ----------------------------------------------------------------
    //  addDocToTag
    //  Links a document ID to a tag in the index.
    //  Creates a new tag entry if it does not exist yet.
    // ----------------------------------------------------------------
    public void addDocToTag(String tag, String docId) {
        String t = tag.toLowerCase().trim();
        if (!tagIndex.containsKey(t)) {
            tagIndex.put(t, new ArrayList<String>());
        }
        tagIndex.get(t).add(docId);
    }

    // ----------------------------------------------------------------
    //  removeDocFromTag
    //  Unlinks a document ID from a tag.
    //  If no documents remain for that tag, the tag entry is deleted.
    // ----------------------------------------------------------------
    public void removeDocFromTag(String tag, String docId) {
        String t = tag.toLowerCase().trim();
        ArrayList<String> list = tagIndex.get(t);
        if (list != null) {
            list.remove(docId);
            if (list.isEmpty()) {
                tagIndex.remove(t); // clean up tag with zero documents
            }
        }
    }

    // ----------------------------------------------------------------
    //  getDocIdsByTag
    //  Returns all document IDs associated with the given tag.
    //  Returns an empty list (not null) if the tag does not exist.
    // ----------------------------------------------------------------
    public ArrayList<String> getDocIdsByTag(String tag) {
        String t = tag.toLowerCase().trim();
        ArrayList<String> list = tagIndex.get(t);
        if (list == null) {
            return new ArrayList<String>(); // always return a list, never null
        }
        return list;
    }

    // ----------------------------------------------------------------
    //  getAllTags
    //  Returns all unique tag names currently in the index.
    // ----------------------------------------------------------------
    public ArrayList<String> getAllTags() {
        return new ArrayList<String>(tagIndex.keySet());
    }

    // ----------------------------------------------------------------
    //  tagExists
    //  Returns true if the given tag has at least one document linked.
    // ----------------------------------------------------------------
    public boolean tagExists(String tag) {
        return tagIndex.containsKey(tag.toLowerCase().trim());
    }
}
