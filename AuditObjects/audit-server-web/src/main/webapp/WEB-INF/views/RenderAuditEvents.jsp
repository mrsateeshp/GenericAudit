<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<!-- Define where the tree should appear -->

<table style="width:100%">
  <tr>
    <th>Operation Type</th>
    <th>Data Tree</th>
    <th>When</th>
    <th>Who</th>
    <th>Id</th>
  </tr>
  <c:forEach items="${resultList}" var="result" varStatus="status">
    <tr>
      <td align="center">${result.operationType}</td>
      <td><div id="jsonTree${status.index}"></div></td>
      <td align="center">${result.when}</td>
      <td align="center">${result.who}</td>
      <td align="center">${result.id}</td>
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
