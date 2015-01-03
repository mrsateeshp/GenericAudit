<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="en">
<head>
    <meta charset="uat-8">
    <title>Generic Audit Search</title>
    <link rel="stylesheet" href="//code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.css">
    <script src="//code.jquery.com/jquery-1.10.2.js"></script>
    <script src="//code.jquery.com/ui/1.11.2/jquery-ui.js"></script>
    <link rel="stylesheet" href="/resources/demos/style.css">

    <script>
        function acceptSuggestion(currentStr, suggestedStr){
            var result = "";
            var index = currentStr.lastIndexOf("/");
            if(index != -1) {
                result = currentStr.substring(0, index+1) + suggestedStr;
            } else {
                result = currentStr;
            }
            return result;
        }

        $(function(){
            function selectEventHandler(event, ui){
                var suggestedQuery = acceptSuggestion($(this).val(), ui.item.value);
                $(this).val(suggestedQuery);
                event.preventDefault();
            }

            $("#searchQuery").autocomplete({
                source: "${contextPath}/web/getSearchSuggestions",
                select: selectEventHandler,
                focus: selectEventHandler
            });

            //event handler for enter.
            $("#searchQuery").keypress(function(e){
                if(e.keyCode === 13){
                    performAuditSearch();
                    $(this).focus();
                    e.preventDefault();
                }
            });
        });

        function performAuditSearch() {
            var currentQuery = $("#searchQuery").val();
            var fromDate = $("#fromDatepicker").val();
            var toDate = $("#toDatepicker").val();
            $("#searchResults").load("$(contextPath}/web/searchForAuditEvents?query=" +
                encodeURIComponent(currentQuery) + "&&fromDate=" + fromDate + "&&toDate=" + toDate);
            return;
        }

    </script>
</head>

<body>
<div class="ui-widget">
    <label for="tags">Search Query: </label></br>
    <textarea id="searchQuery" rows="4" cols="90"></textarea>
    <button onclick="performAuditSearch()">Search</button></br>

    From Date(yyyy/mm/dd): <input id="fromDatepicker" type="text"> To
    <input id="toDatepicker" type="text">

    <script>
        $(function() {
            $("#fromDatepicker").datepicker({dateFormat: "yy-mm-dd"});
            $("#toDatepicker").datepicker({dateFormat: "yy-mm-dd"});
        });
    </script>
</div>
</br>
<h2>Results</h2>
<div id="searchResults"></div>
</body>
<html>