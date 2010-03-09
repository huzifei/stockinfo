<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<% response.setHeader("Pragma","No-cache"); %>
<% response.setHeader("Cache-Control","no-cache"); %>
<% response.setHeader("Expires","0"); %>

<html>
<head>
	<title>信息</title>
</head>

<% String stockTitleStr = (String)request.getAttribute("stockTitleReq"); %>
<% String stockTillDateStr = (String)request.getAttribute("stockTillDateReq"); %>
<% int profitsNum = (Integer)request.getAttribute("profitsNumReq"); %>
<% Double[] profits = (Double[])request.getAttribute("profitsReq"); %>
<% Double[] profitsPercent = (Double[])request.getAttribute("profitsPercentReq"); %>
<% String profitsAverage = (String)request.getAttribute("profitsAverageReq"); %>
<% String profitsShortAverage = (String)request.getAttribute("profitsShortAverageReq"); %>

<% int goperateCashinNum = (Integer)request.getAttribute("goperateCashinNumReq"); %>
<% Double[] goperateCashin = (Double[])request.getAttribute("goperateCashinReq"); %>
<% Double[] goperateCashinPercent = (Double[])request.getAttribute("goperateCashinPercentReq"); %>
<% String goperateCashinAverage = (String)request.getAttribute("goperateCashinAverageReq"); %>
<% String goperateCashinShortAverage = (String)request.getAttribute("goperateCashinShortAverageReq"); %>


<body>
	<%-- 增加页头 --%>
	<table width="100%" height="10%">
		<tr>
			<td>
				<table width=100% border=0>
					<tr>
						<td>
							<font size=2.5px><b>信息提示： </b> </font>
						</td>
						<td><%=stockTitleStr %> --- <%=stockTillDateStr %></td>
					</tr>
				</table>
			</td>
		</tr>
	</table>

<table>
	<tr>
	<td valign="top">
	<table border=1>
		<tr><td colspan="3"><b>profit increase percent</b></td></tr>
		<% for (int i=0; i < profitsNum; i++) { %>
		<tr>
			<td><%=profits[i+1] %></td>
			<td><%=profits[i] %></td>
			<td><%=profitsPercent[i] %></td>
		</tr>
		<%} %>
		<tr><td colspan="2">profits short average is: <%=profitsShortAverage %></td></tr>
		<tr><td colspan="2">profits average is: <%=profitsAverage %></td></tr>
	</table>
	</td>

	<td></td>

	<td valign="top">
	<table border=1>
		<tr><td colspan="3"><b>operateCashin increase percent</b></td></tr>
		<% for (int i=0; i < goperateCashinNum; i++) { %>
		<tr>
			<td><%=goperateCashin[i+1] %></td>
			<td><%=goperateCashin[i] %></td>
			<td><%=goperateCashinPercent[i] %></td>
		</tr>
		<%} %>
		<tr><td colspan="2">operateShortCashin average is: <%=goperateCashinShortAverage %></td></tr>
		<tr><td colspan="2">operateCashin average is: <%=goperateCashinAverage %></td></tr>
	</table>
	</td>

	</tr>
	
</table>
</body>
</html>
