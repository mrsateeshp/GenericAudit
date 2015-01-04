<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html>
<body>

<form action="${contextPath}/web/saveAuditEvent" method="post">

New Object:<br>
<textarea id="newObjectXML" name="newObjectXML" rows="20" cols="200">
  <entity name="user">
    <valueObject name="job">
      <valueObject name="companyDetails">
        <primitive name="established" value="2010" numeric="true"/>
        <primitive name="name" value="Reactive Solutions"/>
      </valueObject>
      <primitive name="designation" value="Principal Engineer"/>
    </valueObject>
    <primitive name="eId" value="johnf"/>
    <primitive name="eType" value="user"/>
    <primitive name="uid" value="123" numeric="true"/>
  </entity>
</textarea>

<br>

Old Object:<br>
<textarea id="oldObjectXML" name="oldObjectXML" rows="20" cols="200"></textarea>

<br>
Who:<br>
<input type="text" name="who" id="who"  value="josephr"/>

<input type="submit" value="Submit"/>
</form>

</body>
</html>
