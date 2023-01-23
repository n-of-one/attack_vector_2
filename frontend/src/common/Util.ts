import {useState} from "react"

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