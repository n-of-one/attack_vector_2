
export class Query {

    params = new Map<string, string>()

    constructor(query: string) {
        if (!query) return

        const queryParts = query.split("&")
        queryParts.forEach(queryPart => {
            const [key, value] = queryPart.split("=")
            this.params.set(key, value)
        })
    }

    value(key: string): string | null {
        return this.params.get(key) ?? null
    }

}