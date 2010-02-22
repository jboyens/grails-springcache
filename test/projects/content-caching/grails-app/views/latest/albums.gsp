<div id="latestAlbums" class="top10">
	<h2><g:message code="title.latest.albums" default="Latest Albums:"/></h2>
	<ol>
		<g:each var="albumInstance" in="${albumInstanceList}">
			<li>
				<g:link controller="album" action="show" id="${albumInstance.id}"><g:message message="${albumInstance}"/></g:link>
			</li>
		</g:each>
	</ol>
</div>