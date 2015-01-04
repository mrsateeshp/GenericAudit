package com.thoughtstream.audit.web.controller;

import com.thoughtstream.audit.bean.MongoDBInstance;
import com.thoughtstream.audit.process.FancyTreeProcessor;
import com.thoughtstream.audit.service.*;
import com.thoughtstream.audit.web.dto.AuditSaveResponse;
import com.thoughtstream.audit.web.dto.AuditSearchResult;
import com.thoughtstream.audit.web.dto.AuditSearchSuggestions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.util.Date;
import java.util.List;

@Controller
public class DefaultController {

    static final Logger logger = LoggerFactory.getLogger(DefaultController.class);
    private MongoBasedAuditSearchService auditSearchService =
            new MongoBasedAuditSearchService(new MongoDBInstance(new Tuple2<String, Object>("localhost", 27017), "AuditObjects"), "defCollection", "xpaths");

    private MongoAuditMessageStoringService auditSavingService =
            new MongoAuditMessageStoringService(new MongoDBInstance(new Tuple2<String, Object>("localhost", 27017), "AuditObjects"), "defCollection", "xpaths");

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

    @RequestMapping(value = {"/web/searchForAuditEvents"}, method = RequestMethod.GET)
    public ModelAndView searchForAuditEvents(
            @RequestParam(value = "query") String currentQuery,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "yyyy-mm-dd") Date fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "yyyy-mm-dd") Date toDate,
            @RequestParam(value = "noOfRows", required = false) Integer noOfRows,
            @RequestParam(value = "fromRow", required = false) Integer fromRow
    ) {
        currentQuery = StringUtils.trimWhitespace(currentQuery);
        List<AuditSearchResult> results = new ArrayList<AuditSearchResult>();

        Iterable<MongoDBSearchResult> searchResult = auditSearchService.search(currentQuery, fromDate, toDate, noOfRows == null ? 0 : 10, fromRow == null ? 0 : fromRow);
        for(MongoDBSearchResult mongoDBSearchResult : scala.collection.JavaConversions.asJavaCollection(searchResult)){
            results.add(
                    new AuditSearchResult(mongoDBSearchResult.id(), mongoDBSearchResult.metaData(),
                            FancyTreeProcessor.transformToPresentableJsNodes(mongoDBSearchResult.document()).head().toString())
            );
        }

        ModelAndView modelAndView = new ModelAndView("RenderAuditEvents");
        modelAndView.addObject("resultList", results);

        return modelAndView;
    }

    @RequestMapping(value = {"/testAuditMessage"}, method = RequestMethod.GET)
    public String testAuditMessage() {
        return "TestAuditMessage";
    }

    @RequestMapping(value = {"/api/saveAuditEvent"}, method = RequestMethod.POST)
    @ResponseBody
    public AuditSaveResponse saveAuditEventFromApi(
            @RequestParam(value = "newObjectXML", required = false) String newObjectXML,
            @RequestParam(value = "oldObjectXML", required = false) String oldObjectXML,
            @RequestParam(value = "who", required = false) String who,
            @RequestParam(value = "when", required = false) Long when,
            @RequestParam(value = "operationType", required = false) String operationType
    ) {

        try {
            saveAuditEvent(newObjectXML,oldObjectXML,who,when,operationType);

            return new AuditSaveResponse(true,null);
        } catch (Exception e) {
            logger.error("Exception saving audit message."+ e);
            e.printStackTrace();
            return new AuditSaveResponse(false,e.getMessage());
        }
    }

    @RequestMapping(value = {"/web/saveAuditEvent"}, method = RequestMethod.POST)
    public String saveAuditEvent(
            @RequestParam(value = "newObjectXML", required = false) String newObjectXML,
            @RequestParam(value = "oldObjectXML", required = false) String oldObjectXML,
            @RequestParam(value = "who", required = false) String who,
            @RequestParam(value = "when", required = false) Long when,
            @RequestParam(value = "toDate", required = false) String operationType
    ) {

        if(StringUtils.isEmpty(newObjectXML)) {
            newObjectXML = null;
        }

        if(StringUtils.isEmpty(oldObjectXML)) {
            oldObjectXML = null;
        }

        XMLDataSnapshot dataSnapshot = new XMLDataSnapshot(newObjectXML, oldObjectXML);
        AuditSaveRequest<XMLDataSnapshot> saveRequest = new AuditSaveRequest<XMLDataSnapshot>(dataSnapshot,new AuditMetaData(who, when != null ? new Date(when) : null , operationType));
        auditSavingService.save(saveRequest);

        return serach();
    }

    @RequestMapping(value = {"/"}, method = RequestMethod.GET)
    public ModelAndView home() {

        List<AuditSearchResult> results = new ArrayList<AuditSearchResult>();
        scala.collection.Iterable<MongoDBSearchResult> searchResult = auditSearchService.search("/user", 0, 10);
        Iterator<MongoDBSearchResult> iterator = searchResult.iterator();
        while (iterator.hasNext()){
            MongoDBSearchResult mongoDBSearchResult = iterator.next();

            results.add(
                    new AuditSearchResult(mongoDBSearchResult.id(), mongoDBSearchResult.metaData(),
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

