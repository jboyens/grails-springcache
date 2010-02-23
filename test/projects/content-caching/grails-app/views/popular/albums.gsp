<div id="popularAlbums" class="top10">
	<h2><g:message code="title.popular.albums" default="Most Popular Albums:"/></h2>
	<ol>
		<g:each var="albumInstance" in="${albumInstanceList}">
			<li>
				<g:link controller="album" action="show" id="${albumInstance.id}"><g:message message="${albumInstance}"/></g:link>
				<rateable:ratings bean="${albumInstance}" />
			</li>
		</g:each>
	</ol>
</div>