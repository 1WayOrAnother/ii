package org.ayfaar.app.controllers;

import org.ayfaar.app.dao.CommonDao;
import org.ayfaar.app.model.Document;
import org.ayfaar.app.utils.GoogleService;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Optional;

@RestController
@RequestMapping("api/document")
public class DocumentController {
    @Inject CommonDao commonDao;
    @Inject GoogleService googleService;

    @RequestMapping(method = RequestMethod.POST)
    public Document create(@RequestParam String url,
                           @RequestParam(required = false) Optional<String> name,
                           @RequestParam(required = false) String author,
                           @RequestParam(required = false) String annotation) {
        Assert.hasLength(url);
        final String docId = GoogleService.extractDocIdFromUrl(url);
        final GoogleService.DocInfo docInfo = googleService.getDocInfo(docId);
        final Document document = Document.builder()
                .id(docId)
                .name(name.orElse(docInfo.title))
                .annotation(annotation)
                .author(author)
                .thumbnail(docInfo.thumbnailLink)
                .mimeType(docInfo.mimeType)
                .icon(docInfo.iconLink)
                .downloadUrl(docInfo.downloadUrl)
                .build();
        return commonDao.save(document);
    }
}
