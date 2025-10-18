package com.health.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.ProgressTracker
import com.health.states.PatientRecordState

/**
 * A simple RPC flow to fetch all records for a given patient by linearId.
 * This flow does not interact with other parties.
 */
@StartableByRPC
class GetRecordsByPatientFlow(
    private val patientId: String
) : FlowLogic<List<PatientRecordState>>() {

    companion object {
        object QUERYING : ProgressTracker.Step("Querying vault for patient records.")
        fun tracker() = ProgressTracker(QUERYING)
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): List<PatientRecordState> {
        progressTracker.currentStep = QUERYING
        val linearId = UniqueIdentifier.fromString(patientId)
        // Query all records and filter by patient ID locally
        val allRecords = serviceHub.vaultService.queryBy<PatientRecordState>().states.map { it.state.data }
        return allRecords.filter { it.patientId == linearId }
    }
}