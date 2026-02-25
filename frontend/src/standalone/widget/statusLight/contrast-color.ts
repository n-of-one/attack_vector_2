type RGB = [number, number, number];


function resolveColor(input: string): RGB {
    const el = document.createElement("div");
    el.style.color = input;
    document.body.appendChild(el);

    const computed = getComputedStyle(el).color;
    document.body.removeChild(el);

    const match = computed.match(/\d+/g);
    if (!match) throw new Error("Invalid color");

    return [
        parseInt(match[0], 10),
        parseInt(match[1], 10),
        parseInt(match[2], 10),
    ];
}

export function getReadableTextColor(input: string): "#000000" | "#ffffff" {
    const [r, g, b] = resolveColor(input);

    // YIQ brightness formula
    const brightness = (r * 299 + g * 587 + b * 114) / 1000;

    return brightness > 128 ? "#000000" : "#ffffff";
}
