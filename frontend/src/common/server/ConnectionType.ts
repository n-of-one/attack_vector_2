
// export type ConnectionType = "WS_UNRESTRICTED" | "WS_HACKER_MAIN" | "NETWORKED_APP_ENDPOINT"

export interface ConnectionType {
    endpoint: string
}

export const WS_UNRESTRICTED: ConnectionType = {endpoint: "/ws_unrestricted"}
export const WS_HACKER_MAIN: ConnectionType = {endpoint: "/ws_hacker"}
export const NETWORKED_APP_ENDPOINT: ConnectionType = {endpoint: "/ws_networked_app"}