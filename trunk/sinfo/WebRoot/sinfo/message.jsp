<%@ page contentType="text/html; charset=UTF-8"%>

<head>
	<title>信息</title>

</head>

<body class="detailbody">
	<%-- 增加页头 --%>
	<table width="100%" height="100%">
		<tr>
			<td>
				<table width=100% border=0>
					<tr>
						<td rowspan="5" width=10% valign="top">
						</td>
					</tr>
					<tr>
						<td>
							<br>
							<font size=2.5px><b>信息提示： </b> <%= request.getAttribute("retResult") %>
								<br> </font>
							<br>
							<br>
						</td>
					</tr>


				</table>
			</td>
		</tr>

		
	</table>

</body>
</html:html>
