package com.thoughtstream.audit.web.controller;

import com.thoughtstream.audit.bean.MongoDBInstance;
import com.thoughtstream.audit.process.FancyTreeProcessor;
import com.thoughtstream.audit.service.MongoBasedAuditSearchService;
import com.thoughtstream.audit.service.MongoDBSearchResult;
import com.thoughtstream.audit.web.dto.AuditSearchResult;
import com.thoughtstream.audit.web.dto.AuditSearchSuggestions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import scala.Tuple2;
import scala.collection.*;
import scala.collection.Iterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class DefaultController {

    static final Logger logger = LoggerFactory.getLogger(DefaultController.class);
    private MongoBasedAuditSearchService auditSearchService =
            new MongoBasedAuditSearchService(new MongoDBInstance(new Tuple2<String, Object>("localhost", 27017), "AuditObjects"), "defCollection", "xpaths");

    @RequestMapping(value = {"/search"}, method = RequestMethod.GET)
    public String serach() {
        return "AuditSearch";
    }

    @RequestMapping(value = {"/web/getSearchSuggestions"}, method = RequestMethod.GET)
    @ResponseBody
    public String[] getSearchSuggestions(
            @RequestParam(value = "term") String currentQuery
    ) {
        currentQuery = StringUtils.trimWhitespace(currentQuery);
        Iterable<String> stringIterable = auditSearchService.searchQuerySuggestions(currentQuery);


        String[] response = scala.collection.JavaConversions.asJavaCollection(stringIterable).toArray(new String[0]);

        logger.debug("Response for Query[{}] is [{}]", currentQuery, response);

        return response;
    }

    @RequestMapping(value = {"/"}, method = RequestMethod.GET)
    public ModelAndView home() {

        List<AuditSearchResult> results = new ArrayList<AuditSearchResult>();
        scala.collection.Iterable<MongoDBSearchResult> searchResult = auditSearchService.search("/user", 0, 10);
        Iterator<MongoDBSearchResult> iterator = searchResult.iterator();
        while (iterator.hasNext()){
            MongoDBSearchResult mongoDBSearchResult = iterator.next();

            results.add(
                    new AuditSearchResult(mongoDBSearchResult.id(), mongoDBSearchResult.auditInfo().who(),
                    mongoDBSearchResult.auditInfo().when(),
                    FancyTreeProcessor.transformToPresentableJsNodes(mongoDBSearchResult.document()).head().toString())
            );
        }

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("resultList", results);

        return modelAndView;
    }

    @RequestMapping(value = {"/json"}, method = RequestMethod.GET)
    @ResponseBody
    public AuditSearchSuggestions jsonResponse() {
        AuditSearchSuggestions suggestions = new AuditSearchSuggestions();
        suggestions.add("item1");
        suggestions.add("item2");
        suggestions.add("item3");

        return suggestions;
    }
}

