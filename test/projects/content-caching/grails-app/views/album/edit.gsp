
<%@ page import="musicstore.Album" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'album.label', default: 'Album')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${albumInstance}">
            <div class="errors">
                <g:renderErrors bean="${albumInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${albumInstance?.id}" />
                <g:hiddenField name="version" value="${albumInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
							<tr class="prop">
								<td valign="top" class="name">
								  <label><g:message code="album.artist.label" default="Artist" /></label>
								</td>
								<td valign="top" class="value">
									<g:link controller="artist" action="show" id="${albumInstance?.artist?.id}">${albumInstance?.artist?.encodeAsHTML()}</g:link>
								</td>
							</tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="album.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: albumInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${albumInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="year"><g:message code="album.year.label" default="Year" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: albumInstance, field: 'year', 'errors')}">
                                    <g:textField name="year" value="${fieldValue(bean: albumInstance, field: 'year')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="tracks"><g:message code="album.tracks.label" default="Tracks" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: albumInstance, field: 'tracks', 'errors')}">
                                    <g:select name="tracks" from="${musicstore.Song.list()}" multiple="yes" optionKey="id" size="5" value="${albumInstance?.tracks}" />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
