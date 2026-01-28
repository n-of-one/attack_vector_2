// URLs with information are encoded to obfuscate the information. This discourages players from chanigng the URL on their own during a game.
// It potentially increases immersion if you can't see the REST-like urls in the browser.

export const decodePath = (base64PathSafe: string) => {
    // Base 64 Encoding with URL and Filename Safe Alphabet -> regular base64
    const base64 = base64PathSafe.replaceAll('_', '/').replaceAll('-', '+')

    const binString = atob(base64);
    const bytes = new Uint8Array(binString.length);
    for (let i = 0; i < binString.length; i++) {
        bytes[i] = binString.charCodeAt(i) ^ (i % 256)
    }
    return String.fromCharCode(...bytes)
}

// xor with index then base64 encode then repalce / with _ to avoid routing problems
export const avEncodedPath = (path: string) => {
    const bytes = new Uint8Array(path.length);

    for (let i = 0; i < path.length; i++) {
        bytes[i] = path.charCodeAt(i) ^ (i % 256)
    }
    const base64Encoded = btoa(String.fromCharCode(...bytes))
    return base64Encoded.replaceAll('/', '_').replaceAll('+', '-') // make URL path safe
}

export const avEncodedUrl = (path: string, requireLogin: boolean = true) => {
    const reference = avEncodedPath(path)
    const rootPath = requireLogin ? 'x' : 'o'
    return `${window.location.origin}/${rootPath}/${reference}`
}
