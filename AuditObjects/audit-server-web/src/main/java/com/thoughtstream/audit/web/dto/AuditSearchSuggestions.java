package com.thoughtstream.audit.web.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sateesh
 * @since 01/01/2015
 */
public class AuditSearchSuggestions implements Serializable{
    private Set<String> suggestions = new HashSet<String>();

    public void add(String input) {
        suggestions.add(input);
    }

    public Set<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(Set<String> suggestions) {
        this.suggestions = suggestions;
    }
}
