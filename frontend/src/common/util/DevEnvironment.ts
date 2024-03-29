
const isDevelopmentEnvironment = () => {
    if (window.location.port === "3000") return true
    const host = window.location.host.split(":")[0]
    return host === "localhost" || host === "carcosa"
}

export const developmentServer = isDevelopmentEnvironment()

export const toServerUrl = (path: string) => {
    if (developmentServer) return `http://localhost${path}`
    return path
}