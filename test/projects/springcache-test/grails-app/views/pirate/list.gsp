<html>
	<head>
		<title>Pirate List</title>
		<meta name="layout" content="main"/>
	</head>
	<body>
		<h2>Pirate List</h2>
		<ul>
			<g:each var="name" in="${pirateNames}">
				<li>${name}</li>
			</g:each>
		</ul>
		<g:form action="add">
			<fieldset>
				<legend>New Pirate</legend>
				<g:textField name="name"/>
				<g:submitButton name="add" value="Add"/>
			</fieldset>
		</g:form>
	</body>
</html>