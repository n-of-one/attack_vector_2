import {zeroPad} from "../component/Pad";
import {TICK_MILLIS} from "./Schedule";

export const delay = (toRun: () => void) => {
  setTimeout(toRun, 1)
}

export const delayTicks = (tickCount: number, toRun: () => void) => {
  setTimeout(toRun, TICK_MILLIS * tickCount)
}


export const formatTimeInterval = (totalSecondsLeft: number | null) => {
  if (!totalSecondsLeft) {
    return "00:00:00"
  }
  const waitHours = Math.floor(totalSecondsLeft / (60 * 60));
  const secondsLeftForMinutes = totalSecondsLeft % (60 * 60);
  const waitMinutes = Math.floor(secondsLeftForMinutes / 60);
  const waitSeconds = secondsLeftForMinutes % 60;

  return zeroPad(waitHours, 2) + ":" + zeroPad(waitMinutes, 2) + ":" + zeroPad(waitSeconds, 2);
}

export function notEmpty<TValue>(value: TValue | null | undefined): value is TValue {
  return value !== null && value !== undefined
}

export function hashCode(str: string): number {
  var h: number = 0;
  for (var i = 0; i < str.length; i++) {
    h = 31 * h + str.charCodeAt(i);
  }
  return h & 0xFFFFFFFF
}

export const decodeAppReference = (base64: string) => {
  const binString = atob(base64);
  const bytes = new Uint8Array(binString.length);
  for (let i = 0; i < binString.length; i++) {
    bytes[i] = binString.charCodeAt(i) ^ (i % 256)
  }
  return String.fromCharCode(...bytes)
}

// xor with index then base64 encode
export const avEncodedPath = (path: string) => {
    const bytes = new Uint8Array(path.length);

    for (let i = 0; i < path.length; i++) {
        bytes[i] = path.charCodeAt(i) ^ (i % 256)
    }
    return btoa(String.fromCharCode(...bytes))
}

export const avEncodedUrl = (path: string) => {
  const reference = avEncodedPath(path)
  return `${window.location.origin}/x/${reference}`
}
