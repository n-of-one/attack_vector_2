import Cookies from "js-cookie";


export const ROLE_HACKER = "ROLE_HACKER"
export const ROLE_SITE_MANAGER = "ROLE_SITE_MANAGER"
export const ROLE_USER_MANAGER = "ROLE_USER_MANAGER"
export const ROLE_ADMIN = "ROLE_ADMIN"
export const ROLE_GM = "ROLE_GM"


class UserAuthorizations {
    public authenticated: boolean
    public roles: string[]

    constructor() {
        const cookieRoles: string | undefined = Cookies.get("roles")
        this.authenticated = cookieRoles !== undefined
        this.roles = (cookieRoles !== undefined) ? cookieRoles.split("|") : []
    }

    hasRole(role: string): boolean {
        return this.roles.includes(role)
    }
}

const userAuthorizations = new UserAuthorizations()
export default userAuthorizations

