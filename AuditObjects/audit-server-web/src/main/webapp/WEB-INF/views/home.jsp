<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<head>

  <!-- Include jQuery and jQuery UI -->

  <script src="http://code.jquery.com/jquery-1.11.1.min.js" type="text/javascript"></script>

  <script src="http://code.jquery.com/ui/1.11.2/jquery-ui.min.js" type="text/javascript"></script>

  <!-- Include Fancytree skin and library -->

  <link href="http://cdn.jsdelivr.net/jquery.fancytree/2.4.1/skin-win8/ui.fancytree.min.css" rel="stylesheet" type="text/css">

  <script src="http://cdn.jsdelivr.net/jquery.fancytree/2.4.1/jquery.fancytree-all.min.js" type="text/javascript"></script>

  <!-- Initialize the tree when page is loaded -->

 

</head>

<body>

  <!-- Define where the tree should appear -->

<table style="width:100%">
  <c:forEach items="${resultList}" var="result" varStatus="status">
    <tr>
      <td><div id="jsonTree${status.index}"></div></td>
      <td>${result.when}</td>
      <td>${result.who}</td>
      <td>${result.id}</td>
    </tr>
  </c:forEach>
</table>

  <script type="text/javascript">
    $(function(){

      <c:forEach items="${resultList}" var="result" varStatus="status">
        $("#jsonTree${status.index}").fancytree({
          source: [${result.jsonString}]
        });
      </c:forEach>

    });
  </script>

</body>

</html>