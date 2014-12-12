package org.ayfaar.app.utils;

import org.ayfaar.app.dao.ItemDao;
import org.ayfaar.app.dao.TermMorphDao;
import org.ayfaar.app.model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class ItemsUpdater {
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private TermsMarker termsMarker;
    @Autowired
    private TermMorphDao termMorphDao;

    public void updateContent(String name) {
        List<String> terms = termMorphDao.getAllMorphs(name);
        List<Item> items = itemDao.findInContent(terms);

        for (Item item : items) {
            item.setContent(termsMarker.mark(item.getContent()));
            itemDao.save(item);
        }
    }
}
