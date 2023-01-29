import {useState} from "react"
import {zeroPad} from "./component/Pad";

export type ActionType = any

export const delay = (toRun: () => void) => {
  setTimeout(toRun, 1)
}

export const useRunOnce = (callBack: () => void) => {
  const [hasBeenCalled, setHasBeenCalled] = useState(false)
  if (hasBeenCalled) return
  callBack()
  setHasBeenCalled(true)
}

export const useRunOnceDelayed = (callBack: () => void) => {
  const [hasBeenCalled, setHasBeenCalled] = useState(false)
  if (hasBeenCalled) return
  delay(callBack)
  setHasBeenCalled(true)
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