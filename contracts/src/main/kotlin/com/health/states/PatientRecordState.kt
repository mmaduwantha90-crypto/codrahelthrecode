package com.health.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.time.Instant
import com.health.contracts.PatientContract

/**
 * Represents a single medical record associated with a patient.  It stores a
 * descriptive [title] along with an arbitrary [value] (which could be JSON or
 * other encoded data) and links back to the owning patient via the [patientId].
 */
@BelongsToContract(PatientContract::class)
data class PatientRecordState(
    val recordId: UniqueIdentifier,
    val patientId: UniqueIdentifier,
    val title: String,
    val value: String,
    val createdBy: Party,
    val createdAt: Instant,
    override val participants: List<AbstractParty> = listOf(createdBy)
) : LinearState {
    override val linearId: UniqueIdentifier get() = recordId
}