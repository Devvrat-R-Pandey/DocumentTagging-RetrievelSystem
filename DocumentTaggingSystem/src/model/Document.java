package model;

import java.util.ArrayList;

// ============================================================
//  CLASS : Document
//  PACKAGE: model
//
//  PURPOSE:
//    Represents one document in the system.
//    Stores the document's data: ID, title, content, tags,
//    and an access counter.
//
//  RULE:
//    This class only holds data and provides basic operations
//    on its own fields (addTag, removeTag, display).
//    It does NOT talk to any repository or service.
// ============================================================

public class Document {

    private String            id;
    private String            title;
    private String            content;
    private ArrayList<String> tags;         // all tags assigned to this document
    private int               accessCount;  // how many times this doc was retrieved

    // ----------------------------------------------------------------
    //  Constructor
    // ----------------------------------------------------------------
    public Document(String id, String title, String content) {
        this.id          = id;
        this.title       = title;
        this.content     = content;
        this.tags        = new ArrayList<String>();
        this.accessCount = 0;
    }

    // ----------------------------------------------------------------
    //  Getters
    // ----------------------------------------------------------------
    public String            getId()          { return id;          }
    public String            getTitle()       { return title;       }
    public String            getContent()     { return content;     }
    public int               getAccessCount() { return accessCount; }
    public ArrayList<String> getTags()        { return tags;        }

    // ----------------------------------------------------------------
    //  incrementAccess
    //  Called every time this document is retrieved via search.
    // ----------------------------------------------------------------
    public void incrementAccess() {
        accessCount++;
    }

    // ----------------------------------------------------------------
    //  addTag
    //  Adds a tag to this document (lowercase, no duplicates).
    //  Returns true if added, false if tag already existed.
    // ----------------------------------------------------------------
    public boolean addTag(String tag) {
        String t = tag.toLowerCase().trim();
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).equals(t)) {
                return false; // duplicate, reject
            }
        }
        tags.add(t);
        return true;
    }

    // ----------------------------------------------------------------
    //  removeTag
    //  Removes a tag from this document.
    //  Returns true if removed, false if tag was not found.
    // ----------------------------------------------------------------
    public boolean removeTag(String tag) {
        String t = tag.toLowerCase().trim();
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).equals(t)) {
                tags.remove(i);
                return true;
            }
        }
        return false;
    }

    // ----------------------------------------------------------------
    //  hasTag
    //  Returns true if this document already has the given tag.
    // ----------------------------------------------------------------
    public boolean hasTag(String tag) {
        String t = tag.toLowerCase().trim();
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).equals(t)) {
                return true;
            }
        }
        return false;
    }

    // ----------------------------------------------------------------
    //  display
    //  Prints this document's details to the console.
    // ----------------------------------------------------------------
    public void display() {
        System.out.println("  ID      : " + id);
        System.out.println("  Title   : " + title);
        System.out.println("  Content : " + content);
        System.out.print  ("  Tags    : [");
        for (int i = 0; i < tags.size(); i++) {
            System.out.print(tags.get(i));
            if (i < tags.size() - 1) System.out.print(", ");
        }
        System.out.println("]");
        System.out.println("  Accesses: " + accessCount);
    }
}
