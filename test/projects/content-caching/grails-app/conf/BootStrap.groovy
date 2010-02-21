import musicstore.auth.Role
import musicstore.auth.User

class BootStrap {

	def init = {servletContext ->
		def userRole = Role.findByAuthority("ROLE_USER")
		if (!userRole) {
			userRole = new Role(authority: "ROLE_USER", description: "An application user")
			userRole.save(failOnError: true)
		}
	}

	def destroy = {
	}
} 