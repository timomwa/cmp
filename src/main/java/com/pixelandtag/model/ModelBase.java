package com.pixelandtag.model;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ModelBase {
    @Id
    @GeneratedValue
    private Integer id;
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    @Override
    public boolean equals(Object obj) {
        try { return id.equals(((ModelBase) obj).getId()); }
        catch (Exception exc) { return false; }
    }
    @Override
    public int hashCode() {
        return 31 + ((id == null) ? 0 : id.hashCode());
    }
}
