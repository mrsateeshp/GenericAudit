<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


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
