package org.n1.av2.backend.model.validation

import jakarta.validation.Constraint
import jakarta.validation.ReportAsSingleViolation
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.messaging.handler.annotation.Payload
import java.lang.annotation.ElementType
import kotlin.reflect.KClass


@MustBeDocumented
@Constraint(validatedBy = [])
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Pattern(regexp = "[a-zA-Z0-9_-]+", message = "only use alphanumeric, underscore and hyphen/minus")
@Size(min = 3, max = 10, message = "must be between 3 and 10 characters long")
annotation class UserName(
    val message: String = "",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = []
)