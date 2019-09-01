
export const delay = (wait) => new Promise( executor => setTimeout(executor, 50 * wait));
