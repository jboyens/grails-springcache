<div id="popularAlbums" class="top10">
	<h2><g:message code="title.popular.albums" default="Most Popular Albums:"/></h2>
	<ol>
		<g:each var="albumInstance" in="${albumInstanceList}" status="i">
			<li>
				<span class="album"><g:link controller="album" action="show" id="${albumInstance.id}"><g:message message="${albumInstance}"/></g:link></span>
				<rateable:ratings bean="${albumInstance}" id="rating_${i}" active="false" />
			</li>
		</g:each>
	</ol>
</div>