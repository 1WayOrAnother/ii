package org.ayfaar.app.controllers;

import org.ayfaar.app.dao.CommonDao;
import org.ayfaar.app.dao.ItemDao;
import org.ayfaar.app.dao.TermDao;
import org.ayfaar.app.model.Item;
import org.ayfaar.app.model.Link;
import org.ayfaar.app.model.Term;
import org.ayfaar.app.spring.Model;
import org.ayfaar.app.utils.AliasesMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ayfaar.app.utils.ValueObjectUtils.getModelMap;
import static org.springframework.util.Assert.notNull;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("item")
public class ItemController {

    @Autowired CommonDao commonDao;
    @Autowired ItemDao itemDao;
    @Autowired TermDao termDao;
    @Autowired TermController termController;
    @Autowired AliasesMap aliasesMap;

    @RequestMapping(value = "{number}", method = POST)
    @Model
    public Item add(@PathVariable String number, @RequestBody String content) {
        Item item = itemDao.getByNumber(number);
        if (item == null) {
            item = new Item(number);
        }
        item.setContent(content);
        itemDao.save(item);
        return item;
    }

    @RequestMapping("{number}")
    @Model
//    @Cacheable("items")
    public ModelMap get(@PathVariable String number) {
        Item item = itemDao.getByNumber(number);
        notNull(item, "Item not found");
        ModelMap modelMap = (ModelMap) getModelMap(item);
//        modelMap.put("linkedTerms", getLinkedTerms(item));
//        modelMap.put("prev", itemDao.getByNumber(getPrev(item.getNumber())));
        modelMap.put("next", getModelMap(itemDao.get(item.getNext())));
        if (modelMap.get("next") != null) {
            ((ModelMap) modelMap.get("next")).remove("content");
        }
        return modelMap;
    }

    public static String getPrev(String number) {
        String[] split = number.split("\\.");
        return split[0] + "." + formatNumber(Integer.valueOf(split[1]) - 1, split[1].length());
    }

    public static String getNext(String number) {
        String[] split = number.split("\\.");
        return split[0] + "." + formatNumber(Integer.valueOf(split[1]) + 1, split[1].length());
    }

    public static String formatNumber(int number, int length) {
        String formattedNumber = String.valueOf(number);
        while (length > formattedNumber.length()) {
            formattedNumber = "0"+formattedNumber;
        }
        return formattedNumber;
    }

    @RequestMapping("{number}/linked-terms")
    @Model
    @ResponseBody
//    @Cacheable("items")
    public Object getLinkedTerms(@PathVariable String number) {
        Item item = itemDao.getByNumber(number);
        notNull(item, "Пункт не найден");

        return getLinkedTerms(item);
    }

    public Set<Term> getLinkedTerms(Item item) {
        Set<Term> contains = new HashSet<Term>();

        for (Map.Entry<String, AliasesMap.Proxy> entry : aliasesMap.entrySet()) {
            Matcher matcher = Pattern.compile("([\\s\\(>«])(" + entry.getKey()
                    + ")([»<\\s,:\\.\\?!\\)])").matcher(item.getContent());
            if (matcher.find()) {
                contains.add(entry.getValue().getPrime());
            }
        }

        return contains;
    }

    @RequestMapping("{number}/{term}")
    @Model
    public Link assignToTerm(@PathVariable String number, @PathVariable String term) {
        return assignToTermWithWeight(number, term, null);
    }

    @RequestMapping("{number}/{term}/{weight}")
    @Model
    public Link assignToTermWithWeight(@PathVariable String number,
                                       @PathVariable String term,
                                       @PathVariable Byte weight) {
        Item item = commonDao.get(Item.class, "number", number);
        if (item == null) {
            item = commonDao.save(new Item(number));
        }

        Term termObj = termDao.getByName(term);
        if (termObj == null) {
            termObj = termController.add(term, null);
        }

        return commonDao.save(new Link(item, termObj, weight));
    }
}
