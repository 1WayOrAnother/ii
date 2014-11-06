package org.ayfaar.app.utils;


import lombok.Data;
import lombok.Getter;
import org.ayfaar.app.dao.CommonDao;
import org.ayfaar.app.dao.LinkDao;
import org.ayfaar.app.dao.TermDao;
import org.ayfaar.app.model.Link;
import org.ayfaar.app.model.Term;
import org.ayfaar.app.model.TermMorph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

import static org.ayfaar.app.utils.UriGenerator.getValueFromUri;

@Component
public class NewAliasesMap implements TermsMap {
    @Autowired
    private CommonDao commonDao;
    @Autowired
    private TermDao termDao;
    @Autowired
    private LinkDao linkDao;

    private Map<String, LinkInfo> links;
    private Map<String, TermProvider> aliasesMap;

    @PostConstruct
    private void load() {
        aliasesMap = new HashMap<String, TermProvider>();

        List<TermMorph> allTermMorphs = commonDao.getAll(TermMorph.class);
        List<TermDao.TermInfo> termsInfo = termDao.getAllTermInfo();
        List<Link> allSynonyms = linkDao.getAllSynonyms();

        links = new HashMap<String, LinkInfo>();
        for(Link link : allSynonyms) {
            links.put(link.getUid2().getUri(), new LinkInfo(link.getType(), (Term)link.getUid1()));
        }

        for(TermDao.TermInfo info : termsInfo) {
            String uri = UriGenerator.generate(Term.class, info.getName());
            String mainTermUri = null;

            if(links.containsKey(uri)) {
                mainTermUri = links.get(uri).getMainTerm().getUri();
            }
            aliasesMap.put(info.getName(), new TermProviderImpl(uri, mainTermUri, info.isHasShortDescription()));
        }

        for(TermMorph morph : allTermMorphs) {
            String mainTermUri = null;
            boolean hasShortDescription = false;
            if(links.containsKey(morph.getTermUri())) {
                final Term mainTerm = links.get(morph.getTermUri()).getMainTerm();
                mainTermUri = mainTerm.getUri();
                hasShortDescription = mainTerm.getShortDescription() != null;
            }
            aliasesMap.put(morph.getName(), new TermProviderImpl(morph.getTermUri(), mainTermUri, hasShortDescription));
        }
    }

    @Data
    private class LinkInfo {
        private byte type;
        private Term mainTerm;

        private LinkInfo(byte type, Term term) {
            this.type = type;
            this.mainTerm = term;
        }
    }

    public class TermProviderImpl implements TermProvider {
        @Getter
        private String uri;
        private String mainTermUri;
        private boolean hasShortDescription;

        public TermProviderImpl(String uri, String mainTermUri, boolean hasShortDescription) {
            this.uri = uri;
            this.mainTermUri = mainTermUri;
            this.hasShortDescription = hasShortDescription;
        }

        public String getName() {
            return getValueFromUri(Term.class, uri);
        }

        public boolean hasShortDescription() {
            return hasShortDescription;
        }

        public Term getTerm() {
            return termDao.get(uri);
        }

        public List<TermProvider> getAliases() {
            return getListProviders(Link.ALIAS, getName());
        }

        public List<TermProvider> getAbbreviations() {
            return getListProviders(Link.ABBREVIATION, getName());
        }

        public TermProvider getCode() {
            List<TermProvider> codes = getListProviders(Link.CODE, getName());
            return codes.size() > 0 ? codes.get(0) : null;
        }

        public TermProvider getMainTermProvider() {
            return hasMainTerm() ? aliasesMap.get(getValueFromUri(Term.class, mainTermUri)) : null;
        }

        public Byte getType() {
            return links.get(uri) != null ? links.get(uri).getType() : null;
        }

        public boolean hasMainTerm() {
            return mainTermUri != null;
        }

        public boolean isAbbreviation() {
            return Link.ABBREVIATION.equals(getType());
        }

        public boolean isAlias() {
            return Link.ALIAS.equals(getType());
        }

        public boolean isCode() {
            return Link.CODE.equals(getType());
        }
    }

    @Override
    public TermProvider getTermProvider(String name) {
        return aliasesMap.get(name);
    }

    @Override
    public Set<Map.Entry<String, TermProvider>> getAll() {
        return aliasesMap.entrySet();
    }

    @Override
    public Term getTerm(String name) {
        TermProvider termProvider = aliasesMap.get(name);
        return termProvider != null ? termProvider.getTerm() : null;
    }

    private List<TermProvider> getListProviders(byte type, String name) {
        List<TermProvider> providers = new ArrayList<TermProvider>();

        for(Map.Entry<String, LinkInfo> link : links.entrySet()) {
            if(link.getValue().getType() == type && link.getValue().getMainTerm().getName().equals(name)) {
                providers.add(aliasesMap.get(getValueFromUri(Term.class, link.getKey())));
            }
        }
        return providers;
    }
}
