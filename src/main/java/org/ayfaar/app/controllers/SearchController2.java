package org.ayfaar.app.controllers;

import org.ayfaar.app.services.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

import static java.lang.Math.min;
import static java.util.Arrays.asList;

@Controller
@RequestMapping("v2/search")
public class SearchController2 {
    @Autowired SearchService searchService;

    public static final int MAX_SUGGESTIONS = 7;

    public List<String> suggestions(String q) {
        Queue<String> queriesQueue = new LinkedList<String>(asList(
                "^"+q,
                "[\\s\\-]" + q,
                q
        ));

        List<String> suggestions = new ArrayList<String>();

        while (suggestions.size() < MAX_SUGGESTIONS && queriesQueue.peek() != null) {
            List<String> founded = searchService.getTerms(queriesQueue.poll(), suggestions);
            suggestions.addAll(founded.subList(0, min(MAX_SUGGESTIONS - suggestions.size(), founded.size())));
        }
        return suggestions;
    }
}
