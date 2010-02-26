<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="main"/>
		<title>Cache Administration</title>
	</head>
	<body>
		<div class="nav">
			<span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
		</div>
		<div class="body">
			<h1>Cache Administration</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div class="list">
				<table>
					<thead>
						<tr>
							<th>Name</th>
							<th>Status</th>
							<th>Size</th>
							<th>Memory</th>
							<th>Disk</th>
							<th>Hits</th>
							<th>Misses</th>
							<th colspan="2">&nbsp;</th>
						</tr>
					</thead>
					<tbody>
						<g:each var="cacheInstance" in="${cacheInstanceList}" status="i">
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
								<td>${cacheInstance.name}</td>
								<td>${cacheInstance.status}</td>
								<td>${cacheInstance.statistics.objectCount}</td>
								<td>${cacheInstance.statistics.memoryStoreObjectCount}</td>
								<td>${cacheInstance.statistics.diskStoreObjectCount}</td>
								<td>${cacheInstance.statistics.cacheHits}</td>
								<td>${cacheInstance.statistics.cacheMisses}</td>
								<td>
									<g:form action="flush">
										<g:hiddenField name="name" value="${cacheInstance.name}"/>
										<g:submitButton name="flush" value="Flush"/>
									</g:form>
								</td>
								<td>
									<g:form action="clear">
										<g:hiddenField name="name" value="${cacheInstance.name}"/>
										<g:submitButton name="clear" value="Clear"/>
									</g:form>
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</div>
	</body>
</html>