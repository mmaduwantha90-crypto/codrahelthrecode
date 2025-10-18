package com.health.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import com.health.states.PatientState
import com.health.states.PatientRecordState

/**
 * A simple contract governing the creation of [PatientState]s and [PatientRecordState]s.
 * The contract enforces a handful of basic rules such as one output for creation
 * commands and ensures that mandatory fields are not left blank.
 */
class PatientContract : Contract {
    companion object {
        /** The contract's canonical class name. */
        const val ID = "com.health.contracts.PatientContract"
    }

    /** Commands supported by this contract. */
    interface Commands : CommandData {
        class CreatePatient : TypeOnlyCommandData(), Commands
        class AddRecord : TypeOnlyCommandData(), Commands
    }

    /**
     * The verify function is called for each transaction using this contract.
     * It ensures that the transaction obeys simple structural rules.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.CreatePatient -> requireThat {
                "No inputs should be consumed when creating a patient." using (tx.inputs.isEmpty())
                "Only one output state should be created when creating a patient." using (tx.outputs.size == 1)
                val output = tx.outputsOfType<PatientState>().single()
                "The patient's name must not be blank." using output.name.isNotBlank()
                "The patient's address must not be blank." using output.address.isNotBlank()
            }
            is Commands.AddRecord -> requireThat {
                "No inputs should be consumed when adding a record." using (tx.inputs.isEmpty())
                "Only one output state should be created when adding a record." using (tx.outputs.size == 1)
                val output = tx.outputsOfType<PatientRecordState>().single()
                "The record title must not be blank." using output.title.isNotBlank()
                "The record value must not be blank." using output.value.isNotBlank()
            }
            else -> throw IllegalArgumentException("Unrecognised command.")
        }
    }
}