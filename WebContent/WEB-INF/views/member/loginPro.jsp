<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${check==1}">
 <c:redirect url="main.mw"></c:redirect>
</c:if>

<c:if test="${check == 0}">
 <script>
  alert("입력하신 정보가 맞지 않습니다.");
  history.go(-1);
 </script>
</c:if>

