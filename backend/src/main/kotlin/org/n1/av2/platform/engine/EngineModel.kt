package org.n1.av2.platform.engine


/**
 * Marker annotation to indicate that a method will be called as a Scheduled task.
 * This emphasized that the game state might have changed since this method was 'called'.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ScheduledTask

// Marker annotation to indicate that a function is called by the system, so the currentUser is not set, and stompService.reply does not work here.
@Target(AnnotationTarget.FUNCTION)
annotation class CalledBySystem
