<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html>
<body>

<form action="${contextPath}/web/saveAuditEvent" method="post">
Old Object:<br>
<textarea id="oldObjectXML" name="oldObjectXML" rows="10" cols="200"></textarea>

<br>
New Object:<br>
<textarea id="newObjectXML" name="newObjectXML" rows="10" cols="200"></textarea>

<br>
Who:<br>
<input type="text" name="who" id="who"  value="josephr"/>

<input type="submit" value="Submit"/>
</form>

</body>
</html>
