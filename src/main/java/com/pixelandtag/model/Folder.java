package com.pixelandtag.model;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.pixelandtag.cmp.entities.User;

public class Folder extends ModelBase {

	public static final String INBOX = "Inbox";
    public static final String SENT  = "Sent";
    public static final String REF   = "Reference";
    public static final String TRASH = "Trash";

    public static final String[] DEFAULT_FOLDER_NAMES = {
        INBOX, SENT, REF, TRASH,
    };
    @ManyToOne
    private User user;

    private String name;

    @OneToMany(mappedBy="folder")
    private Set<Message> messages;

    public Folder() {
    }
    public Folder(String name, User user) {
        this.name = name;
        this.user = user;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Set<Message> getMessages() {
        return messages;
    }
    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }
    @Override
    public String toString() {
        return name;
    }
    
}
